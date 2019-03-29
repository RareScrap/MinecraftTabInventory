package ru.rarescrap.example.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ru.rarescrap.tabinventory.TabContainer;
import ru.rarescrap.tabinventory.TabHostInventory;
import ru.rarescrap.tabinventory.TabInventory;

public class BagContainer extends TabContainer {
    private EntityPlayer player;
    private ItemStack heldBag;

    public TabHostInventory tabHostInventory;
    public TabInventory tabInventory;

    public BagContainer(EntityPlayer player, ItemStack heldItem) {
        this.player = player;
        this.heldBag = heldItem;

        // Инициализируем хост и вкладочный инвентарь к этому хосту
        tabHostInventory = new TabHostInventory("tabs", 4);
        tabInventory = new TabInventory("tab_inv", 36, player, tabHostInventory).connect();

        // Добавляем инвентарь со вкладками к синхронизации
        tabInventories.put(tabInventory.getInventoryName(), tabInventory);

        if (heldItem.getTagCompound() == null) {
            initTabs(); // Инициализируем контент, если при открытии nbt итема пусто
        } else {
            // В противном случае - читаем уже существующий nbt
            tabHostInventory.readFromNBT(heldItem.getTagCompound());// TODO: Предмет может быть переименован на наковальние и в этом случае он будет содержать nbt, которое не сможет обработаться в этой строке
            tabInventory.readFromNBT(heldItem.getTagCompound());
        }

        // Если на серверной стороне ...
        if (!player.worldObj.isRemote) {
            getSync().addSync(tabInventory); // то включим для инвентаря синхронизацию
        }

        addSlots();
    }

    public void initTabs() {
        if (tabHostInventory.isEmpty()) {
            tabHostInventory.setInventorySlotContents(0, new ItemStack(Items.record_11, 1));
            tabHostInventory.setInventorySlotContents(1, new ItemStack(Items.wooden_pickaxe, 1));
            tabHostInventory.setInventorySlotContents(2, new ItemStack(Items.beef, 1));
            tabHostInventory.setInventorySlotContents(3, new ItemStack(Items.boat, 1));

            // debug
            // Базовое заполнение инвентаря для наглядности
            TabInventory.Tab tab = tabInventory.getTab(Items.record_11.getUnlocalizedName());
            for (int i = 0; i < tab.getSizeInventory(); i++) {
                tab.setInventorySlotContents(i, new ItemStack(Items.iron_ingot, 1));
            }

            tab = tabInventory.getTab(Items.wooden_pickaxe.getUnlocalizedName());
            for (int i = 0; i < tab.getSizeInventory(); i++) {
                tab.setInventorySlotContents(i, new ItemStack(Items.gold_ingot, 1));
            }

            tab = tabInventory.getTab(Items.beef.getUnlocalizedName());
            for (int i = 0; i < tab.getSizeInventory(); i++) {
                tab.setInventorySlotContents(i, new ItemStack(Items.carrot, 1));
            }

            tab = tabInventory.getTab(Items.boat.getUnlocalizedName());
            for (int i = 0; i < tab.getSizeInventory(); i++) {
                tab.setInventorySlotContents(i, new ItemStack(Items.diamond_hoe, 1));
            }
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

    @Override
    public void onContainerClosed(EntityPlayer entityPlayer) {
        // Сохраняем инвентари в NBT итема при закрытии контейнера
        if (!entityPlayer.worldObj.isRemote) {
            heldBag.setTagCompound(new NBTTagCompound()); // TODO: Предмет может быть переименован на наковальние и в этом случае потеряет свое имя после закрытия контейнера
            tabHostInventory.writeToNBT(heldBag.getTagCompound());
            tabInventory.writeToNBT(heldBag.getTagCompound());
        }
        super.onContainerClosed(entityPlayer);
    }
}
