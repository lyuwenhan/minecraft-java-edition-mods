package com.example.wenhanclient.find;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Client-side commands for locating registered entity types and blocks in loaded world data. */
public final class FindSubMod {
	private static final int DEFAULT_RESULT_LIMIT = 10;
	private static final int ALL_RESULTS = Integer.MAX_VALUE;

	private FindSubMod() {}

	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess) -> registerCommands(dispatcher));
	}

	static void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
		var entityIdArgument =
				ClientCommands.argument("id", IdentifierArgument.id())
						.suggests(
								(ctx, builder) ->
										SharedSuggestionProvider.suggestResource(
												BuiltInRegistries.ENTITY_TYPE.keySet(), builder))
						.executes(
								ctx ->
										findEntities(
												ctx.getSource(),
												ctx.getArgument("id", Identifier.class),
												DEFAULT_RESULT_LIMIT))
						.then(
								ClientCommands.argument("count", IntegerArgumentType.integer(1))
										.executes(
												ctx ->
														findEntities(
																ctx.getSource(),
																ctx.getArgument(
																		"id", Identifier.class),
																IntegerArgumentType.getInteger(
																		ctx, "count"))))
						.then(
								ClientCommands.literal("all")
										.executes(
												ctx ->
														findEntities(
																ctx.getSource(),
																ctx.getArgument(
																		"id", Identifier.class),
																ALL_RESULTS)));
		var blockIdArgument =
				ClientCommands.argument("id", IdentifierArgument.id())
						.suggests(
								(ctx, builder) ->
										SharedSuggestionProvider.suggestResource(
												BuiltInRegistries.BLOCK.keySet(), builder))
						.executes(
								ctx ->
										findBlocks(
												ctx.getSource(),
												ctx.getArgument("id", Identifier.class),
												DEFAULT_RESULT_LIMIT))
						.then(
								ClientCommands.argument("count", IntegerArgumentType.integer(1))
										.executes(
												ctx ->
														findBlocks(
																ctx.getSource(),
																ctx.getArgument(
																		"id", Identifier.class),
																IntegerArgumentType.getInteger(
																		ctx, "count"))))
						.then(
								ClientCommands.literal("all")
										.executes(
												ctx ->
														findBlocks(
																ctx.getSource(),
																ctx.getArgument(
																		"id", Identifier.class),
																ALL_RESULTS)));

		dispatcher.register(
				ClientCommands.literal("wc")
						.then(ClientCommands.literal("findmob").then(entityIdArgument))
						.then(ClientCommands.literal("findblock").then(blockIdArgument)));
	}

	private static int findEntities(
			FabricClientCommandSource source, Identifier id, int resultLimit) {
		EntityType<?> targetType = BuiltInRegistries.ENTITY_TYPE.getValue(id);
		if (targetType == null) {
			source.sendError(
					Component.translatable("command.wenhan-client.find.unknown_entity", id));
			return 0;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) {
			source.sendError(Component.translatable("command.wenhan-client.find.no_world"));
			return 0;
		}

		List<Entity> matches = new ArrayList<>();
		for (Entity entity : client.level.entitiesForRendering()) {
			if (entity.getType() == targetType) {
				matches.add(entity);
			}
		}
		matches.sort(
				Comparator.comparingDouble(
						entity ->
								horizontalDistanceSquared(
										client.player, entity.getX(), entity.getZ())));

		Component targetName = targetType.getDescription();
		if (matches.isEmpty()) {
			source.sendFeedback(
					Component.translatable("command.wenhan-client.find.not_found", targetName)
							.withStyle(ChatFormatting.YELLOW));
			return 0;
		}

		source.sendFeedback(
				Component.translatable(
								"command.wenhan-client.find.found", matches.size(), targetName)
						.withStyle(ChatFormatting.GREEN));
		for (int index = 0; index < Math.min(resultLimit, matches.size()); index++) {
			Entity entity = matches.get(index);
			source.sendFeedback(
					resultLine(
							client.player, entity.getX(), entity.getZ(), entity.blockPosition()));
		}
		return matches.size();
	}

	private static int findBlocks(
			FabricClientCommandSource source, Identifier id, int resultLimit) {
		Block targetBlock = BuiltInRegistries.BLOCK.getValue(id);
		if (targetBlock == null) {
			source.sendError(
					Component.translatable("command.wenhan-client.find.unknown_block", id));
			return 0;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.level == null || client.player == null) {
			source.sendError(Component.translatable("command.wenhan-client.find.no_world"));
			return 0;
		}

		ClientLevel level = client.level;
		ChunkPos center = client.player.chunkPosition();
		int chunkRadius = client.options.getEffectiveRenderDistance() + 3;
		List<BlockPos> matches = new ArrayList<>();

		for (int chunkZ = center.z() - chunkRadius; chunkZ <= center.z() + chunkRadius; chunkZ++) {
			for (int chunkX = center.x() - chunkRadius;
					chunkX <= center.x() + chunkRadius;
					chunkX++) {
				LevelChunk chunk =
						level.getChunkSource().getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
				if (chunk != null) {
					chunk.findBlocks(
							state -> state.is(targetBlock),
							(position, state) -> matches.add(position.immutable()));
				}
			}
		}

		matches.sort(
				Comparator.comparingDouble(
						position ->
								horizontalDistanceSquared(
										client.player,
										position.getX() + 0.5D,
										position.getZ() + 0.5D)));
		Component targetName = targetBlock.getName();
		if (matches.isEmpty()) {
			source.sendFeedback(
					Component.translatable("command.wenhan-client.find.not_found", targetName)
							.withStyle(ChatFormatting.YELLOW));
			return 0;
		}

		source.sendFeedback(
				Component.translatable(
								"command.wenhan-client.find.found", matches.size(), targetName)
						.withStyle(ChatFormatting.GREEN));
		for (int index = 0; index < Math.min(resultLimit, matches.size()); index++) {
			BlockPos position = matches.get(index);
			source.sendFeedback(
					resultLine(
							client.player,
							position.getX() + 0.5D,
							position.getZ() + 0.5D,
							position));
		}
		return matches.size();
	}

	private static Component resultLine(
			Entity viewer, double targetX, double targetZ, BlockPos position) {
		int distance = (int) Math.sqrt(horizontalDistanceSquared(viewer, targetX, targetZ));
		return Component.literal(
				getArrow(viewer, targetX, targetZ)
						+ " "
						+ distance
						+ "m ("
						+ position.getX()
						+ ", "
						+ position.getY()
						+ ", "
						+ position.getZ()
						+ ")");
	}

	private static double horizontalDistanceSquared(Entity viewer, double targetX, double targetZ) {
		double dx = targetX - viewer.getX();
		double dz = targetZ - viewer.getZ();
		return dx * dx + dz * dz;
	}

	private static String getArrow(Entity viewer, double targetX, double targetZ) {
		double targetYaw =
				Math.toDegrees(Math.atan2(-(targetX - viewer.getX()), targetZ - viewer.getZ()));
		double diff = Mth.wrapDegrees(targetYaw - Mth.wrapDegrees(viewer.getYRot()));
		if (diff >= -22.5D && diff < 22.5D) {
			return "↑";
		}
		if (diff >= 22.5D && diff < 67.5D) {
			return "↗";
		}
		if (diff >= 67.5D && diff < 112.5D) {
			return "→";
		}
		if (diff >= 112.5D && diff < 157.5D) {
			return "↘";
		}
		if (diff >= -67.5D && diff < -22.5D) {
			return "↖";
		}
		if (diff >= -112.5D && diff < -67.5D) {
			return "←";
		}
		if (diff >= -157.5D && diff < -112.5D) {
			return "↙";
		}
		return "↓";
	}
}
