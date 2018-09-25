package ru.rarescrap.example.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

public class BagContainer extends Container {
    private EntityPlayer player;
    private ItemStack heldBag;

    public TabHostInventory tabHostInventory;
    public TabInventory tabInventory;

    // TODO: ???
    public BagContainer() {
        tabInventory = new TabInventory("tab_inv", 36, player);
        tabHostInventory = new TabHostInventory("tabs", 4, tabInventory);
        //initTabs();
    }

    public BagContainer(EntityPlayer player, ItemStack heldItem) {
        this.player = player;
        this.heldBag = heldItem;

        tabInventory = new TabInventory("tab_inv", 36, player);
        tabHostInventory = new TabHostInventory("tabs", 4, tabInventory);

        if (heldItem.getTagCompound() == null) {
            initTabs();
        } else {
            tabHostInventory.readFromNBT(heldItem.getTagCompound());
            tabInventory.readFromNBT(heldItem.getTagCompound());
        }

        addSlots();
    }

    public void initTabs() {
        if (tabHostInventory.isEmpty()) {
            tabHostInventory.setInventorySlotContents(0, new ItemStack(Items.record_11, 1));
            tabHostInventory.setInventorySlotContents(1, new ItemStack(Items.wooden_pickaxe, 1));
            tabHostInventory.setInventorySlotContents(2, new ItemStack(Items.beef, 1));
            tabHostInventory.setInventorySlotContents(3, new ItemStack(Items.boat, 1));
        }
    }

    private void addSlots() {
        // Расставляем слоты на панели вкладок
        for (int i = 0, slotIndex = 0; i < tabHostInventory.getSizeInventory(); ++i, slotIndex++) {
            this.addSlotToContainer(new Slot(tabHostInventory, i, (i*18 +8) +0, 7) {
                @Override
                public boolean isItemValid(ItemStack p_75214_1_) {
                    return tabHostInventory.isUseableByPlayer(player);
                }
            });

        }

        // Расставляем слоты, которе будут хранить содержимое вкладок
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 9; ++x) {
                this.addSlotToContainer(new Slot(tabInventory, x + y * 9 /*+ 9*/, (x*18 +8) +0, (y * 18) + 25) {
                    @Override
                    public boolean isItemValid(ItemStack p_75214_1_) {
                        return true;
                    }
                });
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer p_75145_1_) {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, int clickTypeIn, EntityPlayer player) {
        if (slotId >= 0 // slotId may be -999
                && ((Slot) inventorySlots.get(slotId)).inventory == tabHostInventory
                && !tabHostInventory.isUseableByPlayer(player)) {
            return null;
        } else {
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        }
    }

    /* Хотя этот метод и выполняется на стороне клиента, сохранять данные в nbt тут можно, т.к. итем
     * будет синхронизирован автоматически */
    @Override
    public void onContainerClosed(EntityPlayer p_75134_1_) {
        heldBag.setTagCompound(new NBTTagCompound());
        tabHostInventory.writeToNBT(heldBag.getTagCompound());
        tabInventory.writeToNBT(heldBag.getTagCompound());
        super.onContainerClosed(p_75134_1_);
    }
}
