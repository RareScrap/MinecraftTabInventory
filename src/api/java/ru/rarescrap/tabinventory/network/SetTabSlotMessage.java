package ru.rarescrap.tabinventory.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabInventory;
import ru.rarescrap.tabinventory.network.syns.Change;

/**
 * Сообщение, пересылающее на клиент изменения инвентаря на сервере.
 * Является аналогом {@link S2FPacketSetSlot}, но для инвентарей типа {@link TabInventory}.
 */
public class SetTabSlotMessage implements IMessage {
    private int windowId;
    private Change change;

    // for newInstance
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
        ItemStack serverItemStack = ByteBufUtils.readItemStack(buf); // TODO: Слать только клиентский стак? В смысле, поменять местами с нижним?
        ItemStack clientItemStack = null; // А он нам не нужен
        change = new Change(inventoryName, tabName, slotIndex, clientItemStack, serverItemStack);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarShort(buf, windowId);
        ByteBufUtils.writeUTF8String(buf, change.inventoryName);
        ByteBufUtils.writeUTF8String(buf, change.tabName);
        ByteBufUtils.writeVarShort(buf, change.slotIndex);
        ByteBufUtils.writeItemStack(buf, change.actualItemStack);
    }

    public static class MessageHandler implements IMessageHandler<SetTabSlotMessage, IMessage> {
        @Override
        public IMessage onMessage(SetTabSlotMessage message, MessageContext ctx) {

            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
             if (player.currentWindowId == message.windowId) {
                 SupportTabs container = (SupportTabs) player.openContainer; // Намеренно не делаю проверку на каст дабы посмотреть возможен ли он вообще
                 Change change = message.change;

                 TabInventory tabInventory = container.getTabInventory(change.inventoryName);
                 tabInventory.getTab(change.tabName).setSlotContent(change.slotIndex, change.actualItemStack);
             }

            return null;
        }
    }
}
