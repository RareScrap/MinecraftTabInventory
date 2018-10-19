package ru.rarescrap.example;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.ChatComponentText;
import ru.rarescrap.tabinventory.network.SetTabSlotMessage;
import ru.rarescrap.tabinventory.network.TabInventoryItemsMessage;

public class EventHandler {

    @SubscribeEvent
    public void onSlotChanged(SetTabSlotMessage.Event e) {
        e.entityPlayer.addChatComponentMessage(
                new ChatComponentText(
                        "В инвентаре " + e.change.inventoryName + " во вкладке " + e.change.tabName + " в слоте №" + e.change.slotIndex + " поменялся предмет с " + e.change.currentItemStack + " на " + e.change.actualItemStack));
    }

    @SubscribeEvent
    public void onItemsGet(TabInventoryItemsMessage.Event e) {
        String str = "Пришли итемы для вкладок: ";
        for (String s : e.newItems.keySet()) {
            str += s + " ";
        }
        e.entityPlayer.addChatComponentMessage(new ChatComponentText(str));
    }

}
