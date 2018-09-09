package ru.rarescrap.tabinventory;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

// TODO: Класс сделать абстрактным, а реализацию onMessage() поручить наследникам
public class TabMessageHandler implements IMessageHandler<TabHostInventory.SetCurrentTabPacket, IMessage> {

    public TabMessageHandler() {
    }

    @Override
    public IMessage onMessage(TabHostInventory.SetCurrentTabPacket message, MessageContext ctx) {
        //ExtendedPlayer extendedPlayer = ExtendedPlayer.get(ctx.getServerHandler().playerEntity);

        //extendedPlayer.otherTabsInventory.setCurrentTab(message.newCurrentTabName);
        // Необходимо принудительно проинформировать клиента об изменении
        if (ctx.getServerHandler().playerEntity.openContainer != null)
            ctx.getServerHandler().playerEntity.openContainer.detectAndSendChanges();

        return null;
    }
}
