package ru.rarescrap.example.network;

import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import ru.rarescrap.example.gui.BagContainer;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.network.TabMessageHandler;

public class TabHandler extends TabMessageHandler {
    @Override
    public void processMessage(TabHostInventory.SetCurrentTabPacket message, MessageContext ctx) {
        ((BagContainer) ctx.getServerHandler().playerEntity.openContainer).tabInventory.setCurrentTab(message.newCurrentTabName);
    }
}
