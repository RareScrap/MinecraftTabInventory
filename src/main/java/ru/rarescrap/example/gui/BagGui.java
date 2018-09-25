package ru.rarescrap.example.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import ru.rarescrap.tabinventory.TabHostInventory;

import static ru.rarescrap.example.TabInventoryExample.MODID;

public class BagGui extends GuiContainer {
    private static final ResourceLocation background =
            new ResourceLocation(MODID,"textures/gui/bag_gui.png");

    public BagGui(Container p_i1072_1_) {
        super(p_i1072_1_);

        // Высталяем размеры контейнера. Соответствует размерам GUI на текстуре.
        this.xSize = 176;
        this.ySize = 102;
        // Выставляем края контейнера (верхний и левый)
        this.guiLeft = this.width/2 - xSize/2;
        this.guiTop = this.height/2 - ySize/2;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(background);

        // Отрисовываем текстуру GUI
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void handleMouseInput() {
        // GUI уже имеет дступ к этому полю. Не смсла взвать его так, когда можно взвать напрямую
        //MainContainer container = (MainContainer) player.getEntityPlayer().openContainer;

        // Mouse.getEventX() и Mouse.getEventY() возвращают сырой ввод мыши, так что нам нужно обработать его
        ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
        int width = scaledresolution.getScaledWidth();
        int height = scaledresolution.getScaledHeight();
        int mouseX = Mouse.getX() * width / Minecraft.getMinecraft().displayWidth;
        int mouseZ = height - Mouse.getY() * height / Minecraft.getMinecraft().displayHeight - 1;

        // Проходим по всем слотам в поисках того, на который мы навели курсок
        for (Object inventorySlot : inventorySlots.inventorySlots) {
            Slot slot = (Slot) inventorySlot;

            if (isMouseOverSlot(slot, mouseX, mouseZ)) {

                // Действия, если курсор наведен на прочие вкладки
                if (slot.inventory instanceof TabHostInventory) {
                    try {
                        Item item = slot.getStack().getItem();
                        ((TabHostInventory) slot.inventory).setTab(item.getUnlocalizedName());

                    } catch (NullPointerException e) {
                        System.err.println("Не удалось определить запрос вкладки.");
                    }

                }
            }
        }

        super.handleMouseInput(); // Обрабатываем нажатие на GUI-родителе
    }

    /**
     * копия их родителя (из-за private)
     */
    private boolean isMouseOverSlot(Slot slot, int mouseX, int mouseY) {
        return this.func_146978_c(slot.xDisplayPosition, slot.yDisplayPosition, 16, 16, mouseX, mouseY);
    }
}
