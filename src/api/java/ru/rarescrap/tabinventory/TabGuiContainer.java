package ru.rarescrap.tabinventory;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import ru.rarescrap.tabinventory.network.syns.TabInventorySync;
import ru.rarescrap.tabinventory.utils.Utils;

/**
 * Базовая реализация Gui для {@link TabContainer}. Используйте его как суперклас вашего {@link GuiContainer}'а
 * для быстрой и удобной поддержки инвентарей со вкладкам.
 *
 * Если же вы не хотите наследовать от этого класса, то вот что вам вам нужно сделать, чтобы добавить подддержку
 * инвентарей со вкладкими в ваш GuiContainer:
 * <ol>
 *  <li>Реализуйте интерфейс {@link SupportTabs.Gui}</li>
 *  <li>Оверрайдите метод {@link GuiContainer#handleMouseInput()} по аналогии с
 *  {@link TabGuiContainer#handleMouseInput()}. Это необходимо чтобы gui могло
 *  отслеживать переключения вкладок.</li>
 *  <li>Оверрайдите {@link GuiContainer#handleMouseClick(Slot, int, int, int)} по аналогии с
 *  {@link TabGuiContainer#handleMouseClick(Slot, int, int, int)}. Это необходимо, чтобы
 *  пересылать на сервер корректную информацию о кликах.<li>
 * </ol>
 *
 * Вот и все! Как видите, либа старается следовать такому же подходу, как и ванильный код.
 */
public abstract class TabGuiContainer extends GuiContainer
    implements SupportTabs.Gui {

    public <T extends Container & SupportTabs.Container> TabGuiContainer(T tabContainer) {
        super(tabContainer);
    }

    @Override
    public void handleMouseInput() {
        // Отслеживаем переключение вкладок для TabHostInventory
        Utils.handleMouseInput(this, this.guiLeft, this.guiTop);
        // Штатная работа метода
        super.handleMouseInput();
    }

    @Override
    protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, int type) {
        if (! Utils.handleMouseClick(slotIn, slotId, mouseButton, type, this)) {
            // Если Utils.handleMouseClick() вернет false, значит нужно задействовать стандартную обработку кликов
            super.handleMouseClick(slotIn, slotId, mouseButton, type);
        }
    }

    @Override
    public TabInventorySync getSync() {
        return ((SupportTabs.Container) this.inventorySlots).getSync();
    }
}
