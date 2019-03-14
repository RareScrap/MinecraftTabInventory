package ru.rarescrap.example;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;
import ru.rarescrap.tabinventory.network.SetTabSlotMessage;
import ru.rarescrap.tabinventory.network.TabInventoryItemsMessage;

public class EventHandler {

    @SubscribeEvent
    public void onStackAddToTab(StackAddToTabEvent.Pre e) {
        // Евент хандлится на клиенте и на сервере, но в логах будет виден только ClientThread.
        // Это из-за того, что ChatComponentText показывается только на клиенте!
        Side side = e.entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER;

        ((EntityPlayer) e.entity).addChatComponentMessage(
                new ChatComponentText(
                        side.toString() + ": " +
                                "В инвентаре " + e.change.inventoryName +
                                " во вкладке " + e.change.tabName +
                                " в слоте №" + e.change.slotIndex +
                                " поменялся предмет с " + e.change.currentItemStack +
                                " на " + e.change.actualItemStack));
    }

    @SubscribeEvent
    public void onSlotChanged(SetTabSlotMessage.Event e) {
        e.entityPlayer.addChatComponentMessage(
                new ChatComponentText(
                        "На клиенте принят пакет о содержимом в слоте №" + e.change.slotIndex + " инвентаря " + e.change.inventoryName + " для вкладки " + e.change.tabName + ". Теперь там лежит " + e.change.actualItemStack));
    }

    @SubscribeEvent
    public void onItemsGet(TabInventoryItemsMessage.Event e) {
        String str = "На клиент пришли итемы для вкладок: ";
        for (String s : e.newItems.keySet()) {
            str += s + " ";
        }
        e.entityPlayer.addChatComponentMessage(new ChatComponentText(str));
    }

}
