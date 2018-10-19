package ru.rarescrap.tabinventory.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S30PacketWindowItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Сообщение, отправляющее на клиент содержимое инвентаря {@link TabInventory}. Зачастую используется
 * в момент, когда игрок открывает {@link net.minecraft.inventory.Container} с TabInventory.
 * Является аналогом {@link S30PacketWindowItems}, но для инвентарей типа {@link TabInventory}.
 */
public class TabInventoryItemsMessage implements IMessage {
    /** Имя целевого инвентаря */
    public String inventoryName;
    /** Предметы инвентаря в формате ИМЯ_ВКЛАДКИ->СОДЕРЖИМОЕ_ВКЛАДКИ */
    public Map<String, ItemStack[]> items = new HashMap<String, ItemStack[]>();

    public int tabSize; // TODO: Поддержка вкладок с разнымой вместимостью
    public int windowId;

    // for newInstance // TODO: Объявить как Depricated чтобы избежать юзание в других мода и посмотреть будет ли работать.
    public TabInventoryItemsMessage() {}

    public TabInventoryItemsMessage(TabInventory tabInventory, int windowId) {
        this.windowId = windowId;
        inventoryName = tabInventory.getInventoryName();
        tabSize = tabInventory.getSizeInventory();
        for (Map.Entry<String, TabInventory.Tab> entry : tabInventory.items.entrySet()) {
            items.put(entry.getKey(), entry.getValue().stacks);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = ByteBufUtils.readVarShort(buf);
        inventoryName = ByteBufUtils.readUTF8String(buf);
        tabSize = ByteBufUtils.readVarInt(buf, 4);
        int tabCount = ByteBufUtils.readVarShort(buf);
        for (int i = 0; i < tabCount; i++) {

            String tabName = ByteBufUtils.readUTF8String(buf);
            int tabSize = ByteBufUtils.readVarInt(buf, 4);
            ItemStack[] stacks = new ItemStack[tabSize];

            for (int j = 0; j < tabSize; j++) {
                stacks[j] = ByteBufUtils.readItemStack(buf);
            }

            items.put(tabName, stacks);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, windowId);
        ByteBufUtils.writeUTF8String(buf, inventoryName);
        ByteBufUtils.writeVarInt(buf, tabSize, 4);
        ByteBufUtils.writeVarShort(buf, items.size());
        for (Map.Entry<String, ItemStack[]> entry : items.entrySet()) {

            String tabName = entry.getKey();
            ItemStack[] stacks = entry.getValue();

            ByteBufUtils.writeUTF8String(buf, tabName);
            ByteBufUtils.writeVarInt(buf, stacks.length, 4); // Думаю, такого буфера хватит

            for (ItemStack stack : stacks) {
                ByteBufUtils.writeItemStack(buf, stack);
            }
        }
    }

    public static class MessageHandler implements IMessageHandler<TabInventoryItemsMessage, IMessage> {
        @Override
        public IMessage onMessage(TabInventoryItemsMessage message, MessageContext ctx) {

            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            if (player.openContainer.windowId == message.windowId) {
                SupportTabs.Container tabContainer = (SupportTabs.Container) player.openContainer; // Намеренно не делаю проверку на каст дабы посмотреть возможен ли он вообще

                TabInventory tabInventory = tabContainer.getTabInventory(message.inventoryName);
                if (tabInventory != null) {

                    // Отсылаем ивент о хандле сообщения
                    MinecraftForge.EVENT_BUS.post(new Event(
                            player,
                            tabInventory.getInventoryName(),
                            tabInventory.items,
                            message.items));

                    // Копируем итемы с сервера в клиентский контейнер
                    for (Map.Entry<String, ItemStack[]> entry : message.items.entrySet()) {
                        tabInventory.getTab(entry.getKey()).stacks = message.items.get(entry.getKey());
                    }
                } else {
                    System.err.println("В контейнере " + tabContainer.toString() + " не найдет TabInventory с именем " + message.inventoryName);
                }
            }

            return null;
        }
    }

    /**
     * Клиентский евент, срабатывающий, когда на клиент приходит {@link TabInventoryItemsMessage}
     * @see MessageHandler#onMessage(TabInventoryItemsMessage, MessageContext)
     */
    public static class Event extends PlayerEvent {
        public Map<String, TabInventory.Tab> oldTabs;
        public Map<String, ItemStack[]> newItems;
        public String inventoryName;

        public Event(EntityPlayer player, String inventoryName, Map<String, TabInventory.Tab> oldTabs, Map<String, ItemStack[]> newItems) {
            super(player);
            this.inventoryName = inventoryName;
            this.oldTabs = oldTabs;
            this.newItems = newItems;
        }
    }
}
