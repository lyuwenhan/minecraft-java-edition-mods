package com.example.nohungry;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NoHungryMod implements ModInitializer {
	public static final String MOD_ID = "no-hungry";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static NoHungryConfig config;

	@Override
	public void onInitialize() {
		config = NoHungryConfig.load();
		CommandRegistrationCallback.EVENT.register(
				(dispatcher, registryAccess, environment) -> registerCommands(dispatcher));
	}

	private static void registerCommands(
			com.mojang.brigadier.CommandDispatcher<net.minecraft.commands.CommandSourceStack>
					dispatcher) {
		var rootCommand = Commands.literal("nohungry");
		rootCommand.requires(Commands.hasPermission(Commands.LEVEL_ADMINS));
		var setCommand = Commands.literal("set");
		var minHungerCommand = Commands.literal("minHunger");
		var minHungerValue = Commands.argument("value", IntegerArgumentType.integer(0, 20));
		minHungerValue.executes(
				context -> {
					int value = IntegerArgumentType.getInteger(context, "value");
					config.setFoodMinimum(value);
					config.save();
					context
							.getSource()
							.sendSuccess(
									() -> Component.literal("Minimum hunger level set to " + value + "."), true);
					return 1;
				});
		minHungerCommand.then(minHungerValue);
		setCommand.then(minHungerCommand);
		var minSaturationCommand = Commands.literal("minSaturation");
		var minSaturationValue = Commands.argument("value", IntegerArgumentType.integer(0, 20));
		minSaturationValue.executes(
				context -> {
					int value = IntegerArgumentType.getInteger(context, "value");
					config.setSaturationMinimum(value);
					config.save();
					context
							.getSource()
							.sendSuccess(
									() ->
											Component.literal(
													"Minimum saturation level set to "
															+ value
															+ "; hunger level fixed at 20."),
									true);
					return 1;
				});
		minSaturationCommand.then(minSaturationValue);
		setCommand.then(minSaturationCommand);
		rootCommand.then(setCommand);
		var onCommand = Commands.literal("on");
		onCommand.executes(
				context -> {
					config.setEnabled(true);
					config.save();
					context.getSource().sendSuccess(() -> Component.literal("No Hungry enabled."), true);
					return 1;
				});
		rootCommand.then(onCommand);
		var offCommand = Commands.literal("off");
		offCommand.executes(
				context -> {
					config.setEnabled(false);
					config.save();
					context.getSource().sendSuccess(() -> Component.literal("No Hungry disabled."), true);
					return 1;
				});
		rootCommand.then(offCommand);
		var toggleCommand = Commands.literal("toggle");
		toggleCommand.executes(
				context -> {
					boolean enabled = config.toggleEnabled();
					config.save();
					context
							.getSource()
							.sendSuccess(
									() -> Component.literal("No Hungry " + (enabled ? "enabled" : "disabled") + "."),
									true);
					return 1;
				});
		rootCommand.then(toggleCommand);
		var statusCommand = Commands.literal("status");
		statusCommand.executes(
				context -> {
					context.getSource().sendSuccess(() -> Component.literal(getStatusMessage()), false);
					return 1;
				});
		rootCommand.then(statusCommand);
		dispatcher.register(rootCommand);
	}

	public static NoHungryConfig getConfig() {
		return config;
	}

	private static String getStatusMessage() {
		boolean enabled = config.isEnabled();
		if (!enabled) {
			return "No Hungry disabled.";
		}
		int minimumFoodLevel = config.getMinimumFoodLevel();
		int minimumSaturationLevel = (int) config.getMinimumSaturationLevel();
		return "No Hungry enabled.\nminimum hunger: "
				+ minimumFoodLevel
				+ "\nminimum saturation: "
				+ minimumSaturationLevel;
	}
}
