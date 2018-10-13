package ru.rarescrap.tabinventory.network.syns;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.SetTabSlotMessage;
import ru.rarescrap.tabinventory.network.TabClickWindowMessage;
import ru.rarescrap.tabinventory.network.TabInventoryItemsMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс, занимающийся синхронизацией контейнеров с {@link TabInventory}'ями
 * с клиента на сервер и наоборот
 */
public class TabInventorySync<T extends Container & SupportTabs.Container> {
    /** Сетевая обертка для пересылки пакетов с сервера на клиент */
    private final SimpleNetworkWrapper networkWrapper; // TODO: Оставить поле или получить из NetworkUtils#getNetworkWrapper()

    /** Мапа объектов синхронизации в формате "СЕРВЕРНЫЙ ИНВЕНТАРЬ->КЛИЕНТСКИЙ ИНВЕНТАРЬ" */
    private Map<TabInventory, TabInventory> syncState = new HashMap<TabInventory, TabInventory>(); // TODO: Что делать, если игрок закроет контейнер и попытается открыть его заново? Где-то же должно на клиенте хранится уже высланное содержимое! (нет, не должен. Сам майн так не делает)

    /* Хранить только windowsId не вариант, т.к. контейнер может его обновить. И чтобы не уродовать методы
     * еще одним int полем, мы будем хранить ссылку на контейнер, который будет синхронизироваться чере
     * данный TabInventorySync. */
    /** Контейнер инвентарей со вкладками, которые использует данный TabInventorySync для синхронизации */
    private T tabContainer;


    public TabInventorySync(SimpleNetworkWrapper networkWrapper, T tabContainer) {
        this.networkWrapper = networkWrapper;
        this.tabContainer = tabContainer;
    }

    /**
     * Добавляет инвентарь к механизму синхронизации с клиентом
     * @param serverInv Инвентарь, который нужно синхронизировать
     */
    public void addSync(TabInventory serverInv) {
        syncState.put(serverInv, createClientInv(serverInv)); // TODO: Что будет, если добавть уже инвентарь который уже есть? Или один инвентарь по двум разным ключам?
    }

    /**
     * Создает пустой инвентарь, идентичный входному инвентарю
     */
    private TabInventory createClientInv(TabInventory serverInv) {
        // Создает парный серверному клиентский инвентарь, но без стаков
        TabInventory clientInv = new TabInventory(
                serverInv.getInventoryName(),
                serverInv.getSizeInventory(),
                null, // TODO: Почему бы и не создать владельца?
                serverInv.host); // Владелец нам не нужен
        for (Map.Entry<String, TabInventory.Tab> entry : serverInv.items.entrySet()) {
            String tabName = entry.getKey();
            clientInv.addTab(tabName);
        }
        return clientInv;
    }

    /**
     * Формирует список изменений для всех инвентарей из {@link #syncState}
     * @return Список изменений
     */
    public List<Change> detectChanges() {
        List<Change> changes = new ArrayList<Change>();

        for (Map.Entry<TabInventory, TabInventory> serverClientEntry : syncState.entrySet()) {
            TabInventory serverInv = serverClientEntry.getKey();
            TabInventory clientInv = serverClientEntry.getValue();

            changes.addAll(detectChanges(clientInv, serverInv));
        }

        return changes;
    }

    /**
     * Находит изменения между инвентарями. ВНИМАНИЕ: инвентари должны быть идентичны за исключением контента.
     */
    public static List<Change> detectChanges(TabInventory clientInv, TabInventory serverInv) { // TODO: Юнит тесты!
        List<Change> changes = new ArrayList<Change>();
        String clientInvName = serverInv.getInventoryName(); // TODO: Вроде же из clientInv его надо получать, не?

        // Проходимся по всем вкладкам из серверного инвентаря
        for (Map.Entry<String, TabInventory.Tab> serverTabEntry : serverInv.items.entrySet()) {

            TabInventory.Tab serverTab = serverTabEntry.getValue();
            TabInventory.Tab clientTab = clientInv.items.get(serverTab.name);

            changes.addAll(detectChanges(clientTab, serverTab));
        }

        return changes;
    }

    /**
     * Находит изменения между предметами вкладок
     * @param clientTab Вкладка на строне клиента. ДОЛЖНА быть идентична serverTab за исключением контента.
     * @param serverTab Вкладка на строне сервера. ДОЛЖНА быть идентична clientTab за исключением контента.
     * @return
     */
    public static List<Change> detectChanges(TabInventory.Tab clientTab, TabInventory.Tab serverTab) { // TODO: Юнит тесты!
        List<Change> changes = new ArrayList<Change>();

        // Предполагается, что вместимость обоих вкладок одинакова
        for (int i = 0; i < serverTab.stacks.length; i++) {
            ItemStack serverStack = serverTab.stacks[i];
            ItemStack clientStack = clientTab.stacks[i];

            boolean result = ItemStack.areItemStacksEqual(clientStack, serverStack);

            if (!result) {
                changes.add(new Change(
                        serverTab.getInventoryName(),
                        serverTab.name, // TODO: Слишком неявно. Не хватает защиты от дурака
                        i,
                        clientStack,
                        serverStack));
            }
        }

        return changes;
    }

    public void detectAndSendChanges(List players) {
        List<Change> changes = detectChanges();

        EntityPlayerMP player; // Не хочу создавать новую ссылку за каждую итераци. Лучше вынесу ее.
        /* Object, т.к. вероятнее всего игроки будут получены из Container#crafters. А "крафтеры" не всегда
         * могут быть игроками. См Container#crafters и ICrafting. */
        for (Object potencialPlayer : players) {
            if (potencialPlayer instanceof EntityPlayerMP) {
                player = (EntityPlayerMP) potencialPlayer;
                sendChanges(player, changes);
            }
        }

        applyChanges(changes);
    }

    /**
     * Отсылает изменения на клиент. Работает как и Minecraft - отсылает изменения в слотах по однмоу пакету.
     * @see EntityPlayerMP#sendContainerAndContentsToPlayer(Container, List)
     * @see Container#detectAndSendChanges()
     */
    private void sendChanges(EntityPlayerMP player, List<Change> changes) {
        for (Change change : changes) { // TODO: Майн отсылает изменения в слотах по однмоу пакету. Стоит ли мне делать так же?
            networkWrapper.sendTo(new SetTabSlotMessage(tabContainer.windowId, change), player);
        }
    }

    /* Я решил, что лучше применять изменения, чем копировать один инвентарь в другой
     * (слишком много мороки с защитой от дураков) */
    private void applyChanges(List<Change> changes) {
        // Формируем мапу, чтомы иметь возможность получать серверные инвентари по имени без многочисленного перебора
        Map<String, TabInventory> temp = new HashMap<String, TabInventory>();
        for (Map.Entry<TabInventory, TabInventory> serverClientEntry : syncState.entrySet()) {
            TabInventory serverInv = serverClientEntry.getKey();
            temp.put(serverInv.getInventoryName(), serverInv);
        }

        // Применяем изменения
        for (Change change : changes) {
            TabInventory serverInv = temp.get(change.inventoryName);
            TabInventory suitableClientInv = this.syncState.get(serverInv);
            TabInventory.Tab suitableTab = suitableClientInv.getTab(change.tabName);
            suitableTab.setSlotContent(change.slotIndex, change.actualItemStack);
        }
    }

    public void sendContainerAndContentsToPlayer(Container tabContainer, EntityPlayerMP player) { // TODO: Как обозначить, что container должен реализовывать SupportTabs?
        for (Map.Entry<TabInventory, TabInventory> entry : syncState.entrySet()) {
            // Отослать нужно клиентский инвентарь, т.к. именно он есть у других игроков в данный момент времени
            TabInventoryItemsMessage message = new TabInventoryItemsMessage(entry.getValue(), tabContainer.windowId); // TODO: у меня же есть уже windowId. Следует ли мне запрашивать контейнер? Хотя по большому счету я старался сделать этот метод похожим на ICrafting#sendContainerAndContentsToPlayer для более удобного способа вызова из Container#addCraftingToCrafters
            networkWrapper.sendTo(message, player);
        }
    }

    public void sendSlotClick(Slot slotIn, int slotId, int mouseButton, int type, ItemStack clickedItemIn, short actionNumberIn, String tabName) {
        networkWrapper.sendToServer(new TabClickWindowMessage(tabContainer.windowId, slotId, mouseButton, type, clickedItemIn, actionNumberIn, tabName));
    }
}
