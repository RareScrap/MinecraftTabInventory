package ru.rarescrap.tabinventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Slot;
import ru.rarescrap.tabinventory.network.syns.TabInventorySync;

import java.util.Map;

public abstract class SupportTabs { // TODO: Поменять название на ISupportTabs
    /**
     * Реализуйте этот интерфейс для вашего {@link net.minecraft.inventory.Container}, чтобы обеспечить
     * синхронизацию для {@link TabInventory}'рей для вашего контейнера.
     */
    public interface Container {
        /**
         * @param invName Имя требуемого инвентаря
         * @return {@link TabInventory} с указанным именени. Если такой инвентарь не найден - null.
         */
        // Этот метод существует, т.к. в одном контейнере могут быть несколько TabInventory
        TabInventory getTabInventory(String invName);

        /**
         * @return Все {@link TabInventory}'и контейнера в формате ИМЯ_ИНВЕНТАРЯ->ИНВЕНТАРЬ.
         * ВНИМАНИЕ: ключ и {@link TabInventory#getInventoryName()} должны совпадать для одной записи.
         */
        Map<String, TabInventory> getTabInventories();

        /**
         * @return Объект-синхронизации для вкладочных инвентарей
         */
        /* Прежде всего нужно, чтобы Gui мог удобнным способом получить объект синхронизации */
        TabInventorySync getSync();
    }

    /**
     * Реализуйте этот интерфейс для вашего {@link net.minecraft.client.gui.inventory.GuiContainer}, чтобы
     * {@link ru.rarescrap.tabinventory.utils.Utils#handleMouseClick(Slot, int, int, int, GuiContainer)}
     * мог использовать ваш контейнер, обеспечив тем самым корректную обработку кликов по инвентарю
     * со вкладками.
     */
    public interface Gui {
        /* Хотя GuiContainer (самый предполагаемый класс, реализующий данный интерфейс) может получить
         * TabInventorySync через каст контейнера - ((<? extends TabContainer>) inventorySlots), этот метод
         * подскажет ему, что этот каст необходимо вынести в его реализацию. А задно подскажет, что при
         * реализации GUI для инвентаря со вкладками, объект TabInventorySync ему еще пригодится. */
        TabInventorySync getSync();
    }
}
