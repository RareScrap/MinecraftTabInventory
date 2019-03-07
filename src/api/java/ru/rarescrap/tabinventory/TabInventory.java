package ru.rarescrap.tabinventory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants;
import ru.rarescrap.tabinventory.events.StackAddToTabEvent;
import ru.rarescrap.tabinventory.network.syns.Change;
import ru.rarescrap.tabinventory.utils.Utils;

import java.util.HashMap;
import java.util.Random;

/**
 * Инвентарь, хранящий свое содержимое в нескольких раздельных массивах одинакового размера по принципу вкладок
 */
public class TabInventory implements IInventory {
    /** Имя инвентаря, используемое для сохранения содержимого в NBT */
    private final String inventoryName;

    /** Вместимость каждой вкладки */
    private int tabSlotsCount;
    /** Имя текущей откртой вкладки */
    private String currentTabKey; // TODO: А что с TabHostInvetory#currentTab? Зачем два поля под текущую вкладку? Нужно с этим разобраться.
    /** Хранилище вкладок с предметами */
    public HashMap<String, Tab> items = new HashMap<String, Tab>();
    /** Сущность, к которой привязан инвентарь */
    private Entity inventoryOwnerEntity; // TODO: Добавить такое же поле к TabHostInventory?
    /** Хост для данного инвентаря */ // TODO: Зачем?
    public final TabHostInventory host;

    /**
     * Конструктор, создающий инвентарь с указанными параметрами. После взова конструктора, вам
     * следует добавить в инвентарь вкладки вызовом метода {@link #addTab(String)}.
     * @param inventoryName Имя инвентарая, которое будет использовано при сохранении его содержимого в NBT
     * @param tabSlotsCount Вместимость каждой вкладки
     * @param inventoryOwnerEntity Сущность, к которой привязан инвентарь
     * @param host Инвентарь, хранящий вкладки, которые и занимаются переключением контента
     *
     */
    public TabInventory(String inventoryName, int tabSlotsCount, Entity inventoryOwnerEntity, TabHostInventory host) {
        this.inventoryName = inventoryName;
        this.tabSlotsCount = tabSlotsCount;
        this.inventoryOwnerEntity = inventoryOwnerEntity;
        this.host = host;
    }

    @Override
    public int getSizeInventory() {
        return tabSlotsCount;
    }

    @Override
    public ItemStack getStackInSlot(int slotIndex) {
        if (items.isEmpty()) {
            throw new RuntimeException("TabInventory hasn't any tab");
        }

        if (slotIndex >= 0 && slotIndex < getSizeInventory()) {
            if (this.items.get(currentTabKey) != null) {
                return this.items.get(currentTabKey).stacks[slotIndex];
            }
        }

        return null;
    }

    /**
     * Уменьшаем размер стака до определенного количества элементов и возвращаем получившийся стак.
     * @param slotIndex Номер слота в инвенторе, где лежит предмет, стак которого нужно уменьшить
     * @param amount До скольки нужно уменьший стак
     * @return Предмет с уменьшенным стаком
     */
    @Override
    public ItemStack decrStackSize(int slotIndex, int amount) {
        ItemStack stack = getStackInSlot(slotIndex);
        if (stack != null) {
            if (stack.stackSize > amount) {
                stack = stack.splitStack(amount);
                markDirty(); // Аналог onInventoryChanged()
            } else {
                setInventorySlotContents(slotIndex, null);
            }
        }
        return stack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex) {
        return null; // Отключаем выбрасывание при закрытии GUI
    }

    /**
     * Устанавливает содержимое для текущей вкладки
     * @param slotIndex номер слота
     * @param itemStack предмет, который нужно поместить в инвентарь
     * @see #currentTabKey
     */
    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack) {
        // Проверка на превышения лимита размера стака
        if (itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }

        // Шлём эвент об изменение инвентаря
        ItemStack previousStack = items.get(currentTabKey).stacks[slotIndex];
        if (previousStack != null) {
            previousStack = items.get(currentTabKey).stacks[slotIndex].copy();
        }
        Change change = new Change(inventoryName, currentTabKey, slotIndex, previousStack, itemStack);
        MinecraftForge.EVENT_BUS.post(new StackAddToTabEvent((EntityPlayer) inventoryOwnerEntity, change)); // TODO: Юзать ли эвенты или использовать интерфейс IInvBasic? В других реализациях инвентаря (InventoryBasic, например) объекты этого интерфейса используются чтобы информировать об изменении инвентаря
        // TODO: Выше костыль с кастом

        // Добавляем стак в хранилище
        items.get(currentTabKey).stacks[slotIndex] = itemStack;

        //  Уведомляем об изменении инвентаря
        this.markDirty();
    }

    public void setInventorySlotContents(int slotIndex, ItemStack itemStack, String tabName) { // TODO: Рефакторить это и аналог из IInventory
        String saveTabame = currentTabKey;
        currentTabKey = tabName; // TODO: ЭТО ВООБЩЕ НОРМАЛЬНО? Вот так переключать вкладки?
        setInventorySlotContents(slotIndex, itemStack);
        currentTabKey = saveTabame;
    }

    @Override
    public String getInventoryName() {
        return inventoryName;
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false; // TODO: Не понимаю что этот метод делает. Если он вернет true - то откуда он возьмем этот самй CustomInventoryName?
    }

    @Override
    public int getInventoryStackLimit() {
        return items.get(currentTabKey).stackLimit;
    }

    // http://www.minecraftforge.net/forum/topic/44810-111-how-does-notifyblockupdate-amp-markdirty-work/#comment-242622
    // http://www.minecraftforge.net/forum/topic/47811-what-is-the-function-of-markdirty/
    @Override
    public void markDirty() {
        // Чистим стаки с пустыми предметами
        for (Tab tab : items.values()) {
            ItemStack[] stacks = tab.stacks;

            for (int i = 0; i < stacks.length; i++) {
                if (stacks[i] != null && stacks[i].stackSize == 0) { // TODO: Это вообще возможно?
                    //throw new RuntimeException("ДА! ЭТО ВОЗМОЖНО!"); // TODO: НИ В КОЕМ СЛУЧАЕ НЕ ТОЛКАТЬ В ПРОД!
                    tab.stacks[i] = null;
                }
            }
        }

        // TODO: У других реализация markDirty не прописывает сохранение на диск. Эта строка тут точно нужна?
        // TODO: Проверить при помощи NBTEdit как мод ведет себя без этой строки
        // Сохраняем изменившиеся данные
        writeToNBT(inventoryOwnerEntity.getEntityData());
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
        return entityPlayer.capabilities.isCreativeMode; // Инвентарь можно изменять, только если юзер в креативе
    }

    @Override
    public void openInventory() {
        // TODO: Вызывать это из контейнера, как это делает игра с ванильным контейнерами
    }

    @Override
    public void closeInventory() {
        // TODO: Вызывать это из контейнера, как это делает игра с ванильным контейнерами
    }

    @Override
    public boolean isItemValidForSlot(int slotIndex, ItemStack itemStack) {
        return true;
        //return items.get(currentTabKey).isItemValidForSlotInTab(slotIndex, itemStack);
    }









    /**
     * Записывает состояние инвентаря в NBT
     * @param compound TODO
     */
    public void writeToNBT(NBTTagCompound compound) {
        NBTTagList tabsTagList = new NBTTagList();

        for (Tab tab : items.values()) {
            NBTTagCompound tabCompound = new NBTTagCompound();
            tabCompound.setString("tab_name", Utils.getKeyByValue(items, tab)); // Инфа стока null никогда не отхватим

            NBTTagList tabItemsList = new NBTTagList();
            for (int i = 0; i < tab.stacks.length; i++) {
                if (tab.stacks[i] != null) {
                    NBTTagCompound item = new NBTTagCompound();
                    item.setByte("Slot", (byte) i);
                    tab.stacks[i].writeToNBT(item);
                    tabItemsList.appendTag(item);
                }
            }
            tabCompound.setTag("tab_itemstacks", tabItemsList);

            tabsTagList.appendTag(tabCompound);
        }

        // We're storing our items in a custom tag list using our 'NBT_TAG' from above
        // to prevent potential conflicts
        compound.setTag(inventoryName, tabsTagList);
    }

    /**
     * Читает данные из NBT, восстанавливая состояние инвентаря
     * @param compound TODO
     */
    public void readFromNBT(NBTTagCompound compound) {
        NBTTagList items = compound.getTagList(inventoryName, Constants.NBT.TAG_COMPOUND);

        /* Если инвентарь статов пустой или не содержвится в пришедшем compound'е (а он скорее всего содержится, см init())
         * - добавляем стандартный набор статов */
        /*if (items.tagCount() == 0) {
            initItems();
            return;
        }*/

        // Штатное чтение из NBT
        NBTTagList tabsTagList = compound.getTagList(inventoryName, Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < tabsTagList.tagCount(); i++) {
            NBTTagCompound tabCompound = tabsTagList.getCompoundTagAt(i);
            String tabName = tabCompound.getString("tab_name");

            NBTTagList tabItemsList = tabCompound.getTagList("tab_itemstacks", Constants.NBT.TAG_COMPOUND);
            ItemStack[] itemStacks = new ItemStack[tabSlotsCount];
            for (int i1 = 0; i1 < tabItemsList.tagCount(); i1++) {
                NBTTagCompound item = tabItemsList.getCompoundTagAt(i1);
                byte slot = item.getByte("Slot");
                if (slot >= 0 && slot < getSizeInventory()) {
                    itemStacks[slot] = ItemStack.loadItemStackFromNBT(item); // TODO: Пофиксить баг со "сползающим" инвентарем
                }
            }
            addTab(tabName, itemStacks);
        }
    }

    public void setCurrentTab(String newCurrentTabName) {
        // TODO: защита от дурака
        this.currentTabKey = newCurrentTabName;

    }

    /**
     * Добавляет новую вкладку к инвентарю. Новая вкладка будет иметь размер, равный {@link #tabSlotsCount}.
     * @param tabName Название новой вкладки
     */
    public void addTab(String tabName) {
        Tab t = new Tab(tabName);
        items.put(tabName, t);
        if (currentTabKey == null) {
            currentTabKey = tabName;
        }
    }

    public boolean hasTab(String tabName) {
        return items.get(tabName) != null;
    }

    public Tab getTab(String tabName) {
        return items.get(tabName);
    }

    // debug
    public static Item getRandomItem() {
        Item i = null;
        Object[] objects = Item.itemRegistry.getKeys().toArray();
        Random r = new Random();

        do {
            //Object select = objects[r.nextInt(objects.length)];
            i = Item.getItemById(r.nextInt(objects.length));
        } while (i == null);

        return i;
    }

    /**
     * Добавляет новую вкладку к инвентарю. Новая вкладка будет иметь размер, равный {@link #tabSlotsCount}.
     * @param tabName Название новой вкладки
     * @param stackLimit Лимит предметов в стаках вкладки
     */
    public void addTab(String tabName, int stackLimit) {
        items.put(tabName, new Tab(tabName, stackLimit));
    }

    private void addTab(String tabName, ItemStack[] content) {
        Tab t = new Tab(tabName, content);
        items.put(tabName, t);
    }



    /**
     * Удаляет вкладку с указаннм названием, вместе со всем ее содержимым.
     * @param tabName Название вкладки
     */
    public void removeTab(String tabName) {
        items.remove(tabName);
        if (items.isEmpty()) {
            currentTabKey = null;
        }
    }

    public String getCurrentTab() {
        return currentTabKey;
    }

    /**
     * Создает двунаправленное соединение межуду хостом вкладок и вкладочным инвентарем.
     * Т.е. хост, определеный при создании вызывающего объекта, будет указывать на этот
     * объект как на свой инвентарь.
     */
    public TabInventory connect() {
        host.setTabInv(this);
        return this;
    }








    // TODO: Javadoc, хотя вроде и так все понятно
    public class Tab {
        public String name;
        public ItemStack[] stacks;
        public final int stackLimit;

        /**
         * Конструктор для создания дефолтной вкладки
         */
        public Tab(String name) {
            this(name, null,64);
        }

        public Tab(String tabName, int stackLimit) {
            this(tabName, null, stackLimit);
        }

        public Tab(String name, ItemStack[] content) {
            this(name, content,64);
        }

        /**
         * Конструктор, для создания вкладки с заданным лимитом для стаков
         * @param stackLimit
         */
        public Tab(String name, ItemStack[] content, int stackLimit) {
            this.name = name;
            this.stackLimit = stackLimit;

            if (content == null) {
                this.stacks = new ItemStack[tabSlotsCount]; // Если content - null, то создаем массив с пустым заполнением
            } else {
                /* Я решил не делать умное разрешение неточности во избежания ошибок.
                 * Думаю, что ситуации когда добавляется вкладка, не соответствующая по размерам
                 * полю TabInventory#tabSlotsCount - невозможна. */
                if (content.length != TabInventory.this.tabSlotsCount)
                    throw new RuntimeException("Размер массива content(сейчас " + content.length + ") должен совпадать с TabInventory#tabSlotsCount(сейчас " + TabInventory.this.tabSlotsCount + ")");
                else
                    this.stacks = content;
            }
        }

        public boolean isItemValidForSlotInTab(int slotIndex, ItemStack itemStack) {
            return true; // TODO
        }

        public void setSlotContent(int slotIndex, ItemStack itemStack) {
            stacks[slotIndex] = itemStack;
        }

        public String getInventoryName() {
            return TabInventory.this.inventoryName;
        }
    }
}


