package ru.rarescrap.tabinventory.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabInventory;

/**
 * Сообщение, посылающееся на cервер при клике по {@link TabInventory}.
 * Является аналогом {@link C0EPacketClickWindow}, но для инвентарей типа {@link TabInventory}.
 */
public class TabClickWindowMessage implements IMessage {
    /** The id of the window which was clicked. 0 for player inventory. */
    public int windowId;
    /** Id of the clicked slot */
    public int slotId;
    /** Button used */
    public int packedClickData;
    /** A unique number for the action, used for transaction handling */
    public short actionNumber;
    /** The item stack present in the slot */
    public ItemStack clickedItem;
    /** Inventory operation mode */
    public int mode;
    /** Имя вкладки, для которой был сделан клик */
    String tabName;

    // for reflection newInstance
    public TabClickWindowMessage() {}

    @SideOnly(Side.CLIENT)
    public TabClickWindowMessage(int windowIdIn, int slotIdIn, int usedButtonIn, int mode, ItemStack clickedItemIn, short actionNumberIn, String tabname) {
        this.windowId = windowIdIn;
        this.slotId = slotIdIn;
        this.packedClickData = usedButtonIn;
        if (clickedItemIn != null)
            this.clickedItem = clickedItemIn.copy(); // TODO: Зачем?
        this.actionNumber = actionNumberIn;
        this.mode = mode;
        this.tabName = tabname;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
       windowId = ByteBufUtils.readVarInt(buf, 4);
       slotId = ByteBufUtils.readVarInt(buf, 4);
       packedClickData = ByteBufUtils.readVarInt(buf, 4);
       clickedItem = ByteBufUtils.readItemStack(buf);
       actionNumber = (short) ByteBufUtils.readVarShort(buf);
       mode = ByteBufUtils.readVarInt(buf, 4);
       tabName = ByteBufUtils.readUTF8String(buf); // TODO: Может мне не нужно посылать имя инвенаря? Его же ведь можно достать из слота по индексу и прокастить
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeVarInt(buf, windowId, 4);
        ByteBufUtils.writeVarInt(buf, slotId, 4);
        ByteBufUtils.writeVarInt(buf, packedClickData, 4);
        ByteBufUtils.writeItemStack(buf, clickedItem);
        ByteBufUtils.writeVarShort(buf, actionNumber);
        ByteBufUtils.writeVarInt(buf, mode, 4);
        ByteBufUtils.writeUTF8String(buf, tabName);
    }

    public static class MessageHandler implements IMessageHandler<TabClickWindowMessage, IMessage> {
        @Override
        public IMessage onMessage(TabClickWindowMessage message, MessageContext ctx) {

            EntityPlayerMP entityPlayerMP = ctx.getServerHandler().playerEntity;
            if (message.windowId == entityPlayerMP.openContainer.windowId) {
                SupportTabs.Container container = (SupportTabs.Container) ctx.getServerHandler().playerEntity.openContainer; // TODO: намеренно не делаю проверку т.к. мне интеренсо возможен ли тут краш?
                String invName = ((Container) container).getSlot(message.slotId).inventory.getInventoryName();
                container.getTabInventory(invName).setCurrentTab(message.tabName); // TODO: Это нормально вообще? Или юзать TabInventory#setInventorySlotContents(int, ItemStack, String)?

                C0EPacketClickWindow packet = new C0EPacketClickWindow(
                        entityPlayerMP.openContainer.windowId,
                        message.slotId,
                        message.packedClickData,
                        message.mode,
                        message.clickedItem,
                        message.actionNumber
                );
                ctx.getServerHandler().processClickWindow(packet);
            }

            return null;
        }
    }
}
