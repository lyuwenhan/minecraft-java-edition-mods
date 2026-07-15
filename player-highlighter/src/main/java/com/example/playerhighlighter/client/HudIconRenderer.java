package com.example.playerhighlighter.client;

import com.example.playerhighlighter.GetDirections;
import com.example.playerhighlighter.PlayerHighlighterMod;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class HudIconRenderer {
    private static final Identifier ICON =
            Identifier.fromNamespaceAndPath(PlayerHighlighterMod.MOD_ID, "textures/gui/target.png");

    private static final Identifier HUD_ELEMENT_ID =
            Identifier.fromNamespaceAndPath(PlayerHighlighterMod.MOD_ID, "target_icons");

    private HudIconRenderer() {}

    public static void register() {
        HudElementRegistry.attachElementBefore(
                VanillaHudElements.CHAT, HUD_ELEMENT_ID, HudIconRenderer::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) {
            return;
        }

        if (!PlayerHighlighterMod.isHighlightActive()) {
            return;
        }

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        List<GetDirections.ScreenResult> results =
                GetDirections.projectAllPlayersHud(screenWidth, screenHeight);

        int size = 9;
        for (GetDirections.ScreenResult screenResult : results) {
            int x = Math.round(screenResult.x() - size / 2.0F);
            int y = Math.round(screenResult.y() - size / 2.0F);

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED, ICON, x, y, 0.0F, 0.0F, size, size, size, size);
        }
    }
}
