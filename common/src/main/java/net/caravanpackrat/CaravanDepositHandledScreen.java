package net.caravanpackrat;

// based on: https://fabricmc.net/wiki/tutorial:screenhandler and https://docs.minecraftforge.net/en/1.20.x/gui/screens/

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.entity.player.PlayerInventory;

import static net.caravanpackrat.CaravanPackRatMod.MOD_ID;

public class CaravanDepositHandledScreen extends HandledScreen<CaravanDepositScreenHandler> {

    // appropriated from the "Unity Dark" texture pack, from its "Storage Drawers" integration
    private static final Identifier BACKGROUND_TEXTURE = new Identifier(MOD_ID, "textures/gui/container/caravan_deposit.png");

    public CaravanDepositHandledScreen(CaravanDepositScreenHandler menu, PlayerInventory inventory, Text title) {
        super(menu, inventory, makePlainText(title));
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float partialTick) {
        //renderBackground(drawContext);
        super.render(drawContext, mouseX, mouseY, partialTick);
        drawMouseoverTooltip(drawContext, mouseX, mouseY);
    }

    private static Text makePlainText(Text text){
        Style style = text.getStyle().withColor(0xFFFFFF);
        return text.getWithStyle(style).get(0);
    }

    @Override
    protected void init() {
        super.init();
        titleX = (backgroundWidth - textRenderer.getWidth(title)) / 2;
        titleY = titleY - 1;
        playerInventoryTitleX = Integer.MAX_VALUE; // don't show it

    }

    @Override
    protected void drawBackground(DrawContext drawContext, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BACKGROUND_TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        drawContext.drawTexture(BACKGROUND_TEXTURE,x,y,0,0,backgroundWidth,backgroundHeight);
    }

}
