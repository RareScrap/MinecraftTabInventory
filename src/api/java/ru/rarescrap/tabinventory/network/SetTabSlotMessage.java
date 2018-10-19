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
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.syns.Change;

/**
 * Сообщение, пересылающее на клиент изменение инвентаря, произошедшее в одном слоте.
 * Является аналогом {@link S2FPacketSetSlot}, но для инвентарей типа {@link TabInventory}.
 */
public class SetTabSlotMessage implements IMessage {
    private int windowId;
    private Change change;

    // for reflection newInstance
    public SetTabSlotMessage() {}

    public SetTabSlotMessage(int windowId, Change change) {
        this.windowId = windowId;
        this.change = change;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        windowId = ByteBufUtils.readVarShort(buf);
        String inventoryName = ByteBufUtils.readUTF8String(buf);
        String tabName = ByteBufUtils.readUTF8String(buf);
        int slotIndex = ByteBufUtils.readVarShort(buf);
        ItemStack clientItemStack = ByteBufUtils.readItemStack(buf);
        ItemStack serverItemStack = ByteBufUtils.readItemStack(buf);
        change = new Change(inventoryName, tabName, slotIndex, clientItemStack, serverItemStack);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, windowId);
        ByteBufUtils.writeUTF8String(buf, change.inventoryName);
        ByteBufUtils.writeUTF8String(buf, change.tabName);
        ByteBufUtils.writeVarShort(buf, change.slotIndex);

        /* Хотя прошлый стак и можно узнать на клиенте, но вдруг клиент "забудет"
         * свой прошлый стак? Я высылаю его для удобства тех, кто будет использовать
         * данную библиотеку.*/
        ByteBufUtils.writeItemStack(buf, change.currentItemStack);

        ByteBufUtils.writeItemStack(buf, change.actualItemStack);
    }

    public static class MessageHandler implements IMessageHandler<SetTabSlotMessage, IMessage> {
        @Override
        public IMessage onMessage(SetTabSlotMessage message, MessageContext ctx) {

            EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
            if (player.openContainer.windowId == message.windowId) {
                SupportTabs.Container container = (SupportTabs.Container) player.openContainer; // Намеренно не делаю проверку на каст дабы посмотреть возможен ли он вообще
                Change change = message.change;

                // Отсылаем ивент о хандле сообщения
                MinecraftForge.EVENT_BUS.post(new Event(player, change));

                // Устанавливает серверный стак в слот на клиентском контейнере
                TabInventory tabInventory = container.getTabInventory(change.inventoryName);
                tabInventory.getTab(change.tabName).setSlotContent(change.slotIndex, change.actualItemStack);
            }

            return null;
        }
    }

    /**
     * Клиентский евент, срабатывающий, когда на клиент приходит {@link SetTabSlotMessage}
     * @see MessageHandler#onMessage(SetTabSlotMessage, MessageContext)
     */
    public static class Event extends PlayerEvent {
        public Change change;

        public Event(EntityPlayer player, Change change) {
            super(player);
            this.change = change;
        }
    }
}
