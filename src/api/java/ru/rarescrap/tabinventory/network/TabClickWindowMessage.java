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
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import ru.rarescrap.tabinventory.SupportTabs;
import ru.rarescrap.tabinventory.TabInventory;

import java.io.IOException;

/**
 * Сообщение, посылающееся на cервер при клике по {@link TabInventory}.
 * Является оберткой над {@link C0EPacketClickWindow}, но для инвентарей типа {@link TabInventory}.
 */
public class TabClickWindowMessage implements IMessage {
    /** Ванильный пакет, содержащий всю стандартную информацию о клике */
    private C0EPacketClickWindow packetClickWindow;
    /** Имя вкладки, для которой был сделан клик */
    private String tabName;

    // for reflection newInstance
    public TabClickWindowMessage() {}

    // TODO: Javadoc к параметрам
    @SideOnly(Side.CLIENT)
    public TabClickWindowMessage(int windowId, int slotId, int usedButton, int mode, ItemStack clickedItem, short actionNumber, String tabName) {
        this.packetClickWindow =
                new C0EPacketClickWindow(windowId, slotId, usedButton, mode, clickedItem, actionNumber);

        this.tabName = tabName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
       tabName = ByteBufUtils.readUTF8String(buf); // TODO: Может мне не нужно посылать имя инвенаря? Его же ведь можно достать из слота по индексу и прокастить

        // Лучше пересылать весь пакет сразу, чем его поля. Для этого есть очень удобные методы.
        packetClickWindow = new C0EPacketClickWindow();
        try {
            packetClickWindow.readPacketData(new PacketBuffer(buf.copy()));
        } catch (IOException e) {
            // Не представляю ситуации, когда это может случится.
            Exception cause = new Exception("Can't read packet data. Check client constructor arguments", e);
            e.initCause(cause).printStackTrace();
            // TODO: Делать ли "throw e;"? Нужно ли крашить игру?
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, tabName); // Записываем название вкладки, для которой совершается клик

        try {
            packetClickWindow.writePacketData(new PacketBuffer(buf)); // И сам пакет со сведениями о клике
        } catch (IOException e) {
            // Не представляю ситуации, когда это может случится.
            Exception cause = new Exception("Can't write packet data. Check client constructor arguments", e);
            e.initCause(cause).printStackTrace();
            // TODO: Делать ли "throw e;"? Нужно ли крашить игру?
        }
    }

    public static class MessageHandler implements IMessageHandler<TabClickWindowMessage, IMessage> {
        @Override
        public IMessage onMessage(TabClickWindowMessage m, MessageContext ctx) {
            EntityPlayerMP entityPlayerMP = ctx.getServerHandler().playerEntity;

            if (m.packetClickWindow.func_149548_c() == entityPlayerMP.openContainer.windowId) {
                // Получаем контейнер с поддежкой вкладок
                SupportTabs.Container container = (SupportTabs.Container) ctx.getServerHandler().playerEntity.openContainer; // TODO: намеренно не делаю проверку т.к. мне интеренсо возможен ли тут краш?
                // Получаем из контейнера слот по ID, чтобы потом узнать имя связанного с ним инвентаря
                String invName = ((Container) container).getSlot(m.packetClickWindow.func_149544_d()).inventory.getInventoryName();
                // И по этому имени достаем нужный TabInventory и устанавливаем ему
                // вкладку, в которой был сделан клик
                container.getTabInventory(invName).setCurrentTab(m.tabName); // TODO: Это нормально вообще? Или юзать TabInventory#setInventorySlotContents(int, ItemStack, String)?

                // А дальше ванильный пакет все сделает за нас
                ctx.getServerHandler().processClickWindow(m.packetClickWindow);
            }

            return null;
        }
    }
}
