package ru.rarescrap.tabinventory;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.rarescrap.tabinventory.network.NetworkUtils;
import ru.rarescrap.tabinventory.network.syns.TabInventorySync;
import ru.rarescrap.tabinventory.utils.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Базовая реализация контейнера с {@link TabInventory}'ями. Используйте его как суперклас для вашего контейнера
 * для быстрой и удобной поддержки инвентарей со вкладкам.
 *
 * Если же вы не хотите наследовать от этого класса, то вот что вам вам нужно сделать, чтобы добавить пооддержку
 * инвентарей со вкладкими в ваш контейнер:
 * <ol>
 *  <li>Реализуйте интерфейс {@link SupportTabs.Container}</li>
 *  <li>В классе вашего контейнера создайте поле для объекта {@link TabInventorySync}. Этот объект будет заниматься
 *  синхронизацией с контейнером на клиенте.</li>
 *  <li>Оверрайдите {@link #addCraftingToCrafters(ICrafting)} и вызовите внутри него
 *  {@link Utils#addCraftingToCrafters(Container,List, TabInventorySync, ICrafting)}. Это нужно для того, чтобы
 *  изменить стандартную логику синхронизации контейнеров. Дело в том, что по дефолту Minecraft жестко привязывает
 *  содержимое инвенторя к слотам. И если итемстаки в них не равны, на клиент отправляется пакет. Но так как
 *  {@link TabInventory} не привязывает "жестко" свой контент к слотам, то и механизм синхронизации ему нужен другой.
 *  Оверрайд и вызов метода {@link Utils#addCraftingToCrafters(Container,List, TabInventorySync, ICrafting)}
 *  сделает так, чтобы как только к контейнеру подключался игрок, к нему будет отправлено состояние всех инвентарей
 *  со вкладками, которые есть в контейнере. Такое поведение совпадает с дефолтным поведением игры: при добавлении
 *  нового игрока - слать ему весь контент контейнера, который есть и на других клиентах.</li>
 *  <li>Оверрайдите {@link #detectAndSendChanges()} и вызовите внутри него
 *  {@link Utils#detectAndSendChanges(Container, List, TabInventorySync)}. Это заставит ваш контейнер корректно
 *  высылать изменения на клиентские инвентари {@link TabInventory}: по одному пакету на каждое изменение. Именно так
 *  делает сам Minecraft.</li>
 * </ol>
 *
 * Вот и все! Как видите, либа старается следовать такому же подходу, как и ванильный код.
 *
 */
public abstract class TabContainer extends Container
    implements SupportTabs.Container {

    public Map<String, TabInventory> tabInventories = new HashMap<String, TabInventory>();
    protected TabInventorySync<TabContainer> sync;

    public TabContainer() {
        this.sync = new TabInventorySync<TabContainer>(NetworkUtils.getNetworkWrapper(), this);
    }

    @Override
    public void addCraftingToCrafters(ICrafting p_75132_1_) {
        Utils.addCraftingToCrafters(this, this.crafters, sync, p_75132_1_);
    }

    @Override
    public void detectAndSendChanges() {
        Utils.detectAndSendChanges(this, this.crafters, sync);
    }

    @Override
    public TabInventory getTabInventory(String invName) {
        return tabInventories.get(invName);
    }

    @Override
    public Map<String, TabInventory> getTabInventories() {
        return tabInventories;
    }

    @Override
    public TabInventorySync getSync() {
        return sync;
    }

    /**
     * Располагает стаки по слотам в порядке их добавления при помощи вызова
     * {@link #addSlotToContainer(Slot)}. Алогичен реализации из супер-класса, за исключением
     * того, что не оказывает воздействия на слоты, подсоединенные к {@link TabInventory}.
     * Для помещения стаков в TabInventory см. {@link TabInventory#setInventorySlotContents(int, ItemStack, String)}.
     *
     * @see <a href="https://github.com/RareScrap/MinecraftTabInventory/issues/12">Зачем так сделано</a>
     */
    @Override
    public void putStacksInSlots(ItemStack[] p_75131_1_) {
        for (int i = 0; i < p_75131_1_.length; ++i) {
            Slot slot = this.getSlot(i);
            if ( !(slot.inventory instanceof TabInventory) )
                this.getSlot(i).putStack(p_75131_1_[i]);
        }
    }
}
