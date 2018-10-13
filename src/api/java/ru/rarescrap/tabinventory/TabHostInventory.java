package ru.rarescrap.tabinventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants;

import java.util.Map;

/**
 * Инвентарь, хранящий вкладки. Каждый предмет в инвентаре является ключом, по корому устанавливается содержимое
 * в связанном {@link TabInventory}.
 */
public class TabHostInventory extends InventoryBasic {
    private static final String NBT_TAG = "player_data_tabs"; // TODO: Отказаться в пользу inventoryName

    /** Имя последней выбранной вкладки */
    private String currentTab; // TODO: Я не знаю, нужно ли мне это поле, ведь я могу брать его из tabInventory. Но если он null?
    /** Инвентарь, которые устанавливает свое наполнение в зависимости от {@link #currentTab} */
    private TabInventory tabInventory;

    /**
     * Конструктор
     * @param inventoryName Имя инвентаря-хоста вкладок
     * @param inventorySize Размер инвентаря-хоста
     */
    public TabHostInventory(String inventoryName, int inventorySize) {
        super(NBT_TAG, false, inventorySize); // TODO: Выходит что строка ниже сводит половину из настройки консткруктора на нет - надо это исправить
        this.func_110133_a(inventoryName); // Устанавливаем имя инвентаря // TODO: Это нормально? Может быть это поле предназначено для локализованных имен?
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        if (tabInventory != null && itemStack != null && !tabInventory.hasTab(itemStack.getUnlocalizedName()))
            tabInventory.addTab(itemStack.getUnlocalizedName());

        super.setInventorySlotContents(slotIndex, itemStack);
    }

    /* Вызывается, когда стак "забирается из слота". Вопреки заблуждению "забирание" стака
     * делается через этот метод, а не setInventorySlotContents(?, null), Хотя вызов
     * setInventorySlotContents с null-параметром так же возможен, но не для этих целей. */
    // Оставлен справки ради
    @Override
    public ItemStack decrStackSize(int p_70298_1_, int p_70298_2_) {
        return super.decrStackSize(p_70298_1_, p_70298_2_);
    }

    /* Этот метод не вызывается автоматически. Мы сами должны вызывать его где нам нужно.
     * Например, из кода контейнера или другого инвентаря. */
    // Оставлен для справки и в целях обучения
    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return entityPlayer.capabilities.isCreativeMode;
    }

    /* Этот метод не вызывается автоматически. Мы сами должны вызывать его где нам нужно.
     * Например, из кода контейнера или другого инвентаря. */
    // Оставлен для справки и в целях обучения
    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        return super.isItemValidForSlot(slotIndex, itemStack);
    }

    // TODO: нужно ли переопределять makeDirty()?

    public boolean isEmpty() {
        for (int i = 0; i < getSizeInventory(); i++) {
            if (getStackInSlot(i) != null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Записывает состояние инвентаря в NBT
     * @param compound TODO
     */
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList items = new NBTTagList();

        for (int i = 0; i < getSizeInventory(); ++i) {
            if (getStackInSlot(i) != null) {
                NBTTagCompound item = new NBTTagCompound();
                item.setByte("Slot", (byte) i);
                getStackInSlot(i).writeToNBT(item);
                items.appendTag(item);
            }
        }

        // We're storing our items in a custom tag list using our 'NBT_TAG' from above
        // to prevent potential conflicts
        compound.setTag(NBT_TAG, items);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(NBT_TAG, Constants.NBT.TAG_COMPOUND);

        // Штатное чтение из NBT
        for (int i = 0; i < items.tagCount(); ++i) {
            NBTTagCompound item = items.getCompoundTagAt(i);
            byte slot = item.getByte("Slot");
            if (slot >= 0 && slot < getSizeInventory()) {
                setInventorySlotContents(slot, ItemStack.loadItemStackFromNBT(item));
            }
        }
    }

    /**
     * Устанавливает текущую вкладку
     * @param tabName Имя вкладки
     */
    public void setTab(String tabName) {
        if (tabInventory.items.get(tabName) != null) {
            tabInventory.setCurrentTab(tabName);
            currentTab = tabName;
        } else {
            System.err.println("Инвентарь TabInventory::" + getInventoryName() + " не иммет вкладки с названием " + tabName);
        }
    }

    /**
     * Связывает {@link TabInventory} с хостом вкладок
     * @param tabInventory вкладочный инвентарь, который будет связан с этим хостом
     */
    public void setTabInv(TabInventory tabInventory) {
        this.tabInventory = tabInventory;

        /* Проверяем, имеются ли в tabInventory вкладка для каждого итема из хоста
         * Это гарантирует, что tabInventory всегда будет иметь вкладку для каждого итема хоста
         * даже если tabInventory был связан с хостом не сразу. */
        for (Map.Entry<String, TabInventory.Tab> entry : this.tabInventory.items.entrySet()) {
            String tabName = entry.getKey();
            if (!this.tabInventory.hasTab(tabName)) // Если не имеется...
                this.tabInventory.addTab(tabName); // То добавляем новую пустую вкладку
        }
    }
}
