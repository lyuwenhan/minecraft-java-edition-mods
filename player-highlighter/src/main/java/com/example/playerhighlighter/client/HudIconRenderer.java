package com.example.playerhighlighter.client;

import com.example.playerhighlighter.PlayerHighlighterMod;
import com.example.playerhighlighter.GetDirections;
import com.example.playerhighlighter.GetDirections.ScreenResult;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.List;

public final class HudIconRenderer {

	private static final Identifier ICON = Identifier.of("player-highlighter", "textures/gui/target.png");

	public static void register() {
		HudRenderCallback.EVENT.register(HudIconRenderer::render);
	}

	private static void render(DrawContext ctx, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null) return;

		if (!(PlayerHighlighterMod.HOLD_KEY.isPressed() || PlayerHighlighterMod.config.keep)) {
			return;
		}

		List<ScreenResult> results = GetDirections.projectAllPlayersHud();

		int size = 9;

		for (GetDirections.ScreenResult r : results) {
			int x = Math.round(r.x() - size / 2f);
			int y = Math.round(r.y() - size / 2f);

			ctx.drawTexture(
				RenderPipelines.GUI_TEXTURED,
				ICON,
				x, y,
				0, 0,
				size, size,
				size, size
			);
		}
	}
}
