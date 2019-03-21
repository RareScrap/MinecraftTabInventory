package ru.rarescrap.tabinventory.utils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.input.Mouse;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.syns.TabInventorySync;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {
    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (value.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Находит и возвращает первый попавшийся стак с указанным итемом.
     * Поиск производится по всем вкладкам.
     * @param tabInventory Инвентарь, в котором проводится поиск
     * @param item Предмет, стак с которым нужно найти
     * @return Подходящий стак. Если ничего не найдено - null
     */
    public static ItemStack findIn(TabInventory tabInventory, Item item) {
        for (TabInventory.Tab tab : tabInventory.items.values()) {
            ItemStack result = findIn(tabInventory, item, tab.name);
            if (result != null) return result;
        }
        return null;
    }

    /**
     * Находит и возвращает первый попавшийся стак с указанным итемом
     * @param tabInventory Инвентарь, в котором проводится поиск
     * @param item Предмет, стак с которым нужно найти
     * @param tabName Имя вкладки, в которой производится поиск
     * @return Подходящий стак. Если ничего не найдено - null
     */
    public static ItemStack findIn(TabInventory tabInventory, Item item, String tabName) {
        TabInventory.Tab tab = tabInventory.items.get(tabName);
        if (tab == null) return null;

        for (ItemStack stack : tab.stacks) {
            if (stack == null) continue;
            if (stack.getItem() == item) return stack;
        }
        return null;
    }

    /**
     * Идентичныцй {@link Container#getInventory()} метод. За исключение того, что не включает в вывод
     * стаки из {@link TabInventory}. Вместо них включается null.
     */
    public static List getInventoryExcludeTabInventory(Container container) {
        ArrayList arraylist = new ArrayList();

        for (int i = 0; i < container.inventorySlots.size(); ++i) {
            Slot slot = ((Slot) container.inventorySlots.get(i));
            if (slot.inventory instanceof TabInventory) {
                arraylist.add(null);
            } else {
                arraylist.add(((Slot) container.inventorySlots.get(i)).getStack());
            }
        }

        return arraylist;
    }

    // TODO: Нужна инструкция и Javadoc
    public static void addCraftingToCrafters(Container container, List crafters, TabInventorySync syncEngine, ICrafting p_75132_1_) {
        // copy-paste from Container#addCraftingToCrafters(ICrafting)
        if (crafters.contains(p_75132_1_))
        {
            throw new IllegalArgumentException("Listener already listening");
        }
        else
        {
            crafters.add(p_75132_1_);

            /* ================================== MinecraftTabInventory START ================================== */
            if (p_75132_1_ instanceof EntityPlayerMP) {
                // Отправляем содержимое слотов, за исключением слотов для инвентарей TabInventory
                p_75132_1_.sendContainerAndContentsToPlayer(container, Utils.getInventoryExcludeTabInventory(container)); // TODO: Может лучше все таки слать слоты для TabInventory?
                // Отправлеяем все вкладки TabInventory
                syncEngine.sendContainerAndContentsToPlayer(container, (EntityPlayerMP) p_75132_1_);
            } else {
                p_75132_1_.sendContainerAndContentsToPlayer(container, container.getInventory());
            }
            /* =================================== MinecraftTabInventory END =================================== */

            container.detectAndSendChanges();
        }
    }

    // TODO: Нужна инструкция и Javadoc
    public static void detectAndSendChanges(Container container, List crafters, TabInventorySync syncEngine) {
        // copy-paste from Container#detectAndSendChanges()
        for (int i = 0; i < container.inventorySlots.size(); ++i)
        {
            /* ================================== MinecraftTabInventory START ================================== */
            // Пропускаем синхронизацию слотов из TabInventory. Этой задачей займется другой объект.
            if ( ((Slot)container.inventorySlots.get(i)).inventory instanceof TabInventory ) {
                continue;
            }
            /* =================================== MinecraftTabInventory END =================================== */

            ItemStack itemstack = ((Slot)container.inventorySlots.get(i)).getStack();
            ItemStack itemstack1 = (ItemStack)container.inventoryItemStacks.get(i);

            if (!ItemStack.areItemStacksEqual(itemstack1, itemstack))
            {
                itemstack1 = itemstack == null ? null : itemstack.copy();
                container.inventoryItemStacks.set(i, itemstack1);

                for (int j = 0; j < crafters.size(); ++j)
                {
                    ((ICrafting)crafters.get(j)).sendSlotContents(container, i, itemstack1);
                }
            }
        }

        syncEngine.detectAndSendChanges(crafters); // Синхронизируем TabInventory'и
    }

    /**
     * Утильный метод, которые необходимо вызвать в {@link GuiContainer#handleMouseClick(Slot, int, int, int)}
     * вашего GUI, чтобы тот поддерживал клики по {@link TabInventory}'ям.
     * @param slotIn Слот, по которому делается клик
     * @param slotId id слота
     * @param mouseButton Кнопка мыши, которой был сделан клик
     * @param type TODO
     * @param tabGuiContainer GuiContainer, в котором переобпределяется handleMouseClick() для поддержки кликов
     *                     по инвентарю со вкладками. ВНИМАНИЕ: аргумент должен реализовывать {@link SupportTabs}!
     * @return Результат операции. True - если клик был обработан и не нужно вызывать super.handleMouseClick()
     * в вашем GuiContainer. Иначе - false и вам нужно вызвать super.handleMouseClick().
     */
    @SideOnly(Side.CLIENT)
    public static <T extends GuiContainer & SupportTabs.Gui> boolean handleMouseClick(Slot slotIn, int slotId, int mouseButton, int type, T tabGuiContainer) {
        if (slotIn != null && slotIn.inventory instanceof TabInventory) {
            if (slotIn != null)
                slotId = slotIn.slotNumber;

            ItemStack clickedStack = tabGuiContainer.inventorySlots.slotClick(slotId, mouseButton, type, tabGuiContainer.mc.thePlayer);
            short short1 = tabGuiContainer.mc.thePlayer.openContainer.getNextTransactionID(tabGuiContainer.mc.thePlayer.inventory);

            String tabName = ((TabInventory) slotIn.inventory).getCurrentTab();

            // TODO: Проверка на каст
            tabGuiContainer.getSync().sendSlotClick(slotIn, slotId, mouseButton, type, clickedStack, short1, tabName/*, tabGuiContainer.inventorySlots.windowId*/); // TODO: Установился на пересылке клика на сервер

            return true;
        } else {
            return false;
        }
    }

    /**
     * Обрабатывает переключение вкладок при наведении мыши на {@link ru.rarescrap.tabinventory.TabHostInventory}
     */
    @SideOnly(Side.CLIENT)
    public static <T extends GuiContainer & SupportTabs.Gui> void handleMouseInput(T guiContainer, int containerGuiLeft, int containerGuiTop) { // TODO: Нужна инструкция и Javadoc
        // Mouse.getEventX() и Mouse.getEventY() возвращают сырой ввод мыши, так что нам нужно обработать его
        ScaledResolution scaledresolution = new ScaledResolution(guiContainer.mc, guiContainer.mc.displayWidth, guiContainer.mc.displayHeight);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseZ = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

        // Проходим по всем слотам в поисках того, на который мы навели курсор
        for (Object inventorySlot : guiContainer.inventorySlots.inventorySlots) {
            Slot slot = (Slot) inventorySlot;

            if (isMouseOverSlot(slot, mouseX, mouseZ, containerGuiLeft, containerGuiTop)) {

                // Действия, если курсор наведен на прочие вкладки
                if (slot.inventory instanceof TabHostInventory) {
                    try {
                        Item item = slot.getStack().getItem();
                        ((TabHostInventory) slot.inventory).setTab(item.getUnlocalizedName());

                    } catch (NullPointerException e) {
                        System.err.println("Не удалось определить запрос вкладки.");
                    }

                }
            }
        }
    }

    // Аналог GuiContainer#isMouseOverSlot(...)
    @SideOnly(Side.CLIENT)
    public static boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY, int containerGuiLeft, int containerGuiTop) {
        return func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY, containerGuiLeft, containerGuiTop);
    }

    // Аналог GuiContainer#func_146978_c()
    @SideOnly(Side.CLIENT)
    public static boolean func_146978_c(int rectX, int rectY, int rectWidth, int rectHeight, int pointX, int pointY, int containerGuiLeft, int containerGuiTop)
    {
        int k1 = containerGuiLeft;
        int l1 = containerGuiTop;
        pointX -= k1;
        pointY -= l1;
        return pointX >= rectX - 1 && pointX < rectX + rectWidth + 1 && pointY >= rectY - 1 && pointY < rectY + rectHeight + 1;
    }
}
