package ru.rarescrap.tabinventory.network.syns;

import net.minecraft.item.ItemStack;

public class Change {
    // TODO: Хранить ли инфу какому игроку принадлежит это изменение?
    /** Имя инвентаря */
    public final String inventoryName;
    /** Имя вкладки */
    public final String tabName;
    /** Индекс слота во вкладке {@link #tabName} */
    public final int slotIndex;
    /** Стак, существующий на клиенте */
    public final ItemStack currentItemStack;
    /** Актуальный стак, находящийся на сервере */
    public final ItemStack actualItemStack;

    public Change(String inventoryName, String tabName, int slotIndex, ItemStack currentItemStack, ItemStack actualItemStack) {
        this.inventoryName = inventoryName;
        this.tabName = tabName;
        this.slotIndex = slotIndex;
        this.currentItemStack = currentItemStack;
        this.actualItemStack = actualItemStack;
    }
}
