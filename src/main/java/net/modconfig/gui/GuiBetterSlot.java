package net.modconfig.gui;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class GuiBetterSlot
{
    public final Minecraft mc;
    public int width;
    public int height;
    public int top;
    public int bottom;
    public int right;
    public int left;
    public final int slotHeight;
    public int scrollUpButtonID;
    public int scrollDownButtonID;
    public int mouseX;
    public int mouseY;
    public float initialClickY = -2.0F;
    public float scrollMultiplier;
    public float amountScrolled;
    public int selectedElement = -1;
    public long lastClicked = 0L;
    public boolean showSelectionBox = true;
    public boolean field_77243_s;
    public int field_77242_t;

    public ResourceLocation resBG = new ResourceLocation("textures/gui/background.png");

    public GuiBetterSlot(Minecraft mc, int width, int height, int top, int bottom, int slotHeight)
    {
        this.mc = mc;
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.slotHeight = slotHeight;
        this.left = 0;
        this.right = width;
    }

    public void func_77207_a(int width, int height, int top, int bottom)
    {
        this.width = width;
        this.height = height;
        this.top = top;
        this.bottom = bottom;
        this.left = 0;
        this.right = width;
    }

    public void setShowSelectionBox(boolean show)
    {
        this.showSelectionBox = show;
    }

    protected void func_77223_a(boolean flag, int value)
    {
        this.field_77243_s = flag;
        this.field_77242_t = value;
        if (!flag) this.field_77242_t = 0;
    }

    protected abstract int getSize();
    protected abstract void elementClicked(int index, boolean doubleClick);
    protected abstract boolean isSelected(int index);

    protected int getContentHeight()
    {
        return this.getSize() * this.slotHeight + this.field_77242_t;
    }



    protected abstract void drawBackground(MatrixStack matrixStack);
    protected abstract void drawSlot(MatrixStack matrixStack, int index, int x, int y, int height, Tessellator tessellator);

    protected void func_77222_a(MatrixStack matrixStack, int x, int y, Tessellator tessellator) {}
    protected void func_77224_a(MatrixStack matrixStack, int x, int y) {}
    protected void func_77215_b(MatrixStack matrixStack, int mouseX, int mouseY) {}

    public int func_77210_c(int mouseX, int mouseY)
    {
        int left      = this.width / 2 - 110;
        int right     = this.width / 2 + 110;
        int relativeY = mouseY - this.top - this.field_77242_t + (int)this.amountScrolled - 4;
        int slotIndex = relativeY / this.slotHeight;
        return mouseX >= left && mouseX <= right && slotIndex >= 0 && relativeY >= 0 && slotIndex < this.getSize() ? slotIndex : -1;
    }

    public void registerScrollButtons(List buttons, int upId, int downId)
    {
        this.scrollUpButtonID = upId;
        this.scrollDownButtonID = downId;
    }

    private void bindAmountScrolled()
    {
        int maxScroll = this.func_77209_d();
        if (maxScroll < 0) maxScroll /= 2;
        if (this.amountScrolled < 0.0F) this.amountScrolled = 0.0F;
        if (this.amountScrolled > (float)maxScroll) this.amountScrolled = (float)maxScroll;
    }

    public int func_77209_d()
    {
        return this.getContentHeight() - (this.bottom - this.top - 4);
    }

    public void func_77208_b(int amount)
    {
        this.amountScrolled += (float)amount;
        this.bindAmountScrolled();
        this.initialClickY = -2.0F;
    }

    
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (delta != 0)
        {



            this.amountScrolled += (float)(-delta * this.slotHeight / 2);
            this.bindAmountScrolled();
        }
        return true;
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.drawBackground(matrixStack);
        int size = this.getSize();

        int scrollBarX    = this.getScrollBarX();
        int scrollBarXEnd = scrollBarX + 6;





        if (org.lwjgl.glfw.GLFW.glfwGetMouseButton(
                this.mc.getWindow().getWindow(),
                org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS)
        {
            if (this.initialClickY == -1.0F)
            {
                boolean validClick = true;

                if (mouseY >= this.top && mouseY <= this.bottom)
                {
                    int left      = this.width / 2 - 110;
                    int right     = this.width / 2 + 110;
                    int relativeY = mouseY - this.top - this.field_77242_t + (int)this.amountScrolled - 4;
                    int slotIndex = relativeY / this.slotHeight;

                    if (mouseX >= left && mouseX <= right && slotIndex >= 0 && relativeY >= 0 && slotIndex < size)
                    {

                        boolean doubleClick = slotIndex == this.selectedElement
                                && net.minecraft.util.Util.getMillis() - this.lastClicked < 250L;
                        this.elementClicked(slotIndex, doubleClick);
                        this.selectedElement = slotIndex;
                        this.lastClicked     = net.minecraft.util.Util.getMillis();
                    }
                    else if (mouseX >= left && mouseX <= right && relativeY < 0)
                    {
                        this.func_77224_a(matrixStack, mouseX - left, mouseY - this.top + (int)this.amountScrolled - 4);
                        validClick = false;
                    }

                    if (mouseX >= scrollBarX && mouseX <= scrollBarXEnd)
                    {
                        this.scrollMultiplier = -1.0F;
                        int maxScroll = this.func_77209_d();
                        if (maxScroll < 1) maxScroll = 1;

                        int sbh = (int)((float)((this.bottom - this.top) * (this.bottom - this.top))
                                / (float)this.getContentHeight());
                        if (sbh < 32)                       sbh = 32;
                        if (sbh > this.bottom - this.top - 8) sbh = this.bottom - this.top - 8;

                        this.scrollMultiplier /= (float)(this.bottom - this.top - sbh) / (float)maxScroll;
                    }
                    else
                    {
                        this.scrollMultiplier = 1.0F;
                    }

                    this.initialClickY = validClick ? (float)mouseY : -2.0F;
                }
                else
                {
                    this.initialClickY = -2.0F;
                }
            }
            else if (this.initialClickY >= 0.0F)
            {
                this.amountScrolled -= ((float)mouseY - this.initialClickY) * this.scrollMultiplier;
                this.initialClickY   = (float)mouseY;
            }
        }
        else
        {


            this.initialClickY = -1.0F;
        }

        this.bindAmountScrolled();
        RenderSystem.disableLighting();
        RenderSystem.disableFog();

        Tessellator  tessellator = Tessellator.getInstance();
        BufferBuilder buffer     = tessellator.getBuilder();

        drawContainerBackground(matrixStack, tessellator);

        int xPos = this.width / 2 - 16;
        int yPos = this.top + 4 - (int)this.amountScrolled;

        if (this.field_77243_s)
        {
            this.func_77222_a(matrixStack, xPos, yPos, tessellator);
        }

        for (int i = 0; i < size; ++i)
        {
            int slotY = yPos + i * this.slotHeight + this.field_77242_t;
            int slotH = this.slotHeight - 4;

            if (slotY <= this.bottom && slotY + slotH >= this.top)
            {
                if (this.showSelectionBox && this.isSelected(i))
                {
                    int selLeft  = this.width / 2 - 110;
                    int selRight = this.width / 2 + 110;

                    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableTexture();





                    buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    buffer.vertex((double)selLeft,      (double)(slotY + slotH + 2), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0,   0).endVertex();
                    buffer.vertex((double)selRight,     (double)(slotY + slotH + 2), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0,   0).endVertex();
                    buffer.vertex((double)selRight,     (double)(slotY - 2),          0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                    buffer.vertex((double)selLeft,      (double)(slotY - 2),          0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
                    tessellator.end();





                    buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
                    buffer.vertex((double)(selLeft  + 1), (double)(slotY + slotH + 1), 0.0D).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                    buffer.vertex((double)(selRight - 1), (double)(slotY + slotH + 1), 0.0D).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
                    buffer.vertex((double)(selRight - 1), (double)(slotY - 1),           0.0D).uv(1.0F, 0.0F).color(0, 0, 0,   0).endVertex();
                    buffer.vertex((double)(selLeft  + 1), (double)(slotY - 1),           0.0D).uv(0.0F, 0.0F).color(0, 0, 0,   0).endVertex();
                    tessellator.end();

                    RenderSystem.enableTexture();
                }

                this.drawSlot(matrixStack, i, xPos, slotY, slotH, tessellator);
            }
        }

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableAlphaTest();
        RenderSystem.shadeModel(7425);
        RenderSystem.disableTexture();

        int maxScroll = this.func_77209_d();

        if (maxScroll > 0)
        {
            int scrollBarHeight = (this.bottom - this.top) * (this.bottom - this.top) / this.getContentHeight();
            if (scrollBarHeight < 32)                       scrollBarHeight = 32;
            if (scrollBarHeight > this.bottom - this.top - 8) scrollBarHeight = this.bottom - this.top - 8;

            int scrollBarY = (int)this.amountScrolled * (this.bottom - this.top - scrollBarHeight) / maxScroll + this.top;
            if (scrollBarY < this.top) scrollBarY = this.top;




            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.vertex((double)scrollBarX,    (double)this.bottom, 0.0D).uv(0.0F, 1.0F).color(0, 0, 0,   0).endVertex();
            buffer.vertex((double)scrollBarXEnd, (double)this.bottom, 0.0D).uv(1.0F, 1.0F).color(0, 0, 0,   0).endVertex();
            buffer.vertex((double)scrollBarXEnd, (double)this.top,    0.0D).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            buffer.vertex((double)scrollBarX,    (double)this.top,    0.0D).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
            tessellator.end();





            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.vertex((double)scrollBarX,    (double)(scrollBarY + scrollBarHeight), 0.0D).uv(0.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            buffer.vertex((double)scrollBarXEnd, (double)(scrollBarY + scrollBarHeight), 0.0D).uv(1.0F, 1.0F).color(128, 128, 128, 255).endVertex();
            buffer.vertex((double)scrollBarXEnd, (double)scrollBarY,                     0.0D).uv(1.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            buffer.vertex((double)scrollBarX,    (double)scrollBarY,                     0.0D).uv(0.0F, 0.0F).color(128, 128, 128, 255).endVertex();
            tessellator.end();




            buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            buffer.vertex((double)scrollBarX,         (double)(scrollBarY + scrollBarHeight - 1), 0.0D).uv(0.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            buffer.vertex((double)(scrollBarXEnd - 1), (double)(scrollBarY + scrollBarHeight - 1), 0.0D).uv(1.0F, 1.0F).color(192, 192, 192, 255).endVertex();
            buffer.vertex((double)(scrollBarXEnd - 1), (double)scrollBarY,                          0.0D).uv(1.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            buffer.vertex((double)scrollBarX,         (double)scrollBarY,                          0.0D).uv(0.0F, 0.0F).color(192, 192, 192, 255).endVertex();
            tessellator.end();
        }

        this.func_77215_b(matrixStack, mouseX, mouseY);
        RenderSystem.enableTexture();
        RenderSystem.shadeModel(7424);
        RenderSystem.enableAlphaTest();
        RenderSystem.disableBlend();
    }

    protected int getScrollBarX()
    {
        return this.width / 2 + 124;
    }

    
    protected void overlayBackground(MatrixStack matrixStack, int top, int bottom, int alphaTop, int alphaBottom)
    {
        Tessellator   tessellator = Tessellator.getInstance();
        BufferBuilder buffer      = tessellator.getBuilder();
        this.mc.getTextureManager().bind(resBG);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.vertex(0.0D,               (double)bottom, 0.0D).uv(0.0F,                  (float)bottom / f).color(64, 64, 64, alphaBottom).endVertex();
        buffer.vertex((double)this.width, (double)bottom, 0.0D).uv((float)this.width / f, (float)bottom / f).color(64, 64, 64, alphaBottom).endVertex();
        buffer.vertex((double)this.width, (double)top,    0.0D).uv((float)this.width / f, (float)top    / f).color(64, 64, 64, alphaTop   ).endVertex();
        buffer.vertex(0.0D,               (double)top,    0.0D).uv(0.0F,                  (float)top    / f).color(64, 64, 64, alphaTop   ).endVertex();
        tessellator.end();
    }

    protected void drawContainerBackground(MatrixStack matrixStack, Tessellator tessellator)
    {
        BufferBuilder buffer = tessellator.getBuilder();
        this.mc.getTextureManager().bind(resBG);
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        float f = 32.0F;

        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        buffer.vertex((double)left,  (double)bottom, 0.0D).uv((float)left  / f, (float)(bottom + (int)amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        buffer.vertex((double)right, (double)bottom, 0.0D).uv((float)right / f, (float)(bottom + (int)amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        buffer.vertex((double)right, (double)top,    0.0D).uv((float)right / f, (float)(top    + (int)amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        buffer.vertex((double)left,  (double)top,    0.0D).uv((float)left  / f, (float)(top    + (int)amountScrolled) / f).color(32, 32, 32, 255).endVertex();
        tessellator.end();
    }
}