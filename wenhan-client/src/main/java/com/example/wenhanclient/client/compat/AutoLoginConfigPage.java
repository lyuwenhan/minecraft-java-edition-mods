package com.example.wenhanclient.client.compat;

import com.example.wenhanclient.WenhanClientMod;
import com.example.wenhanclient.autologin.AutoLoginConfig;
import com.example.wenhanclient.autologin.AutoLoginSubMod;
import com.example.wenhanclient.autologin.Crypto;
import com.example.wenhanclient.autologin.DeviceKey;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.AbstractWidget;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import dev.isxander.yacl3.gui.utils.GuiUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.network.chat.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

final class AutoLoginConfigPage {
	private static final String PREFIX = "option.wenhan-client.autologin.";
	private static final SystemToast.SystemToastId ERROR_TOAST = new SystemToast.SystemToastId();
	private final Screen rootParent;

	AutoLoginConfigPage(Screen rootParent) {
		this.rootParent = rootParent;
	}

	ConfigCategory category() {
		ConfigCategory.Builder category =
				ConfigCategory.createBuilder()
						.name(Component.translatable("title.wenhan-client.autologin.config"))
						.option(
								ButtonOption.createBuilder()
										.name(Component.translatable(PREFIX + "new_server"))
										.text(Component.translatable(PREFIX + "open"))
										.description(
												OptionDescription.of(
														Component.translatable(
																PREFIX + "new_server.tooltip")))
										.action(screen -> openEditor(screen, null))
										.build());

		TreeMap<String, AutoLoginConfig.Credential> configured =
				new TreeMap<>(AutoLoginConfig.load().copy().servers);
		String currentServer = AutoLoginSubMod.getCurrentServerKey(Minecraft.getInstance());
		if (currentServer != null) {
			configured.putIfAbsent(currentServer, null);
		}
		for (Map.Entry<String, AutoLoginConfig.Credential> entry : configured.entrySet()) {
			String address = entry.getKey();
			boolean enabled = entry.getValue() != null && entry.getValue().enabled;
			category.option(
					ButtonOption.createBuilder()
							.name(
									Component.translatable(
											PREFIX + "server_entry",
											address,
											Component.translatable(
													enabled
															? PREFIX + "status.enabled"
															: PREFIX + "status.disabled")))
							.text(Component.translatable(PREFIX + "edit"))
							.description(
									OptionDescription.of(
											Component.translatable(
													entry.getValue() != null
															? PREFIX + "edit.tooltip"
															: PREFIX + "current_server.tooltip")))
							.action(screen -> openEditor(screen, address))
							.build());
		}
		return category.build();
	}

	void save() {
		// Entries are saved by their dedicated editor screens.
	}

	private void openEditor(YACLScreen screen, String address) {
		// Preserve changes made in other categories before changing screens.
		screen.finishOrSave();
		Screen refreshedParent = new RefreshingParentScreen(rootParent);
		GuiUtils.setScreen(new Editor(address).createScreen(refreshedParent));
	}

	private static final class Editor {
		private String persistedAddress;
		private String address;
		private String password = "";
		private boolean enabled;
		private boolean hasSavedPassword;
		private Option<String> addressOption;
		private Option<String> passwordOption;
		private ButtonOption clearPasswordButton;
		private ButtonOption deleteButton;

		private Editor(String originalAddress) {
			address = originalAddress == null ? "" : originalAddress;
			AutoLoginConfig.Credential credential =
					originalAddress == null
							? null
							: AutoLoginConfig.load().servers.get(originalAddress);
			persistedAddress = credential == null ? null : originalAddress;
			enabled = credential == null || credential.enabled;
			hasSavedPassword = credential != null && credential.hasPassword();
		}

		private Screen createScreen(Screen parent) {
			addressOption =
					Option.<String>createBuilder()
							.name(Component.translatable(PREFIX + "address"))
							.description(
									OptionDescription.of(
											Component.translatable(PREFIX + "address.tooltip")))
							.binding(address, () -> address, value -> address = value)
							.customController(AddressController::new)
							.build();
			passwordOption = passwordOption(() -> password, value -> password = value);
			clearPasswordButton =
					ButtonOption.createBuilder()
							.name(Component.translatable(PREFIX + "clear_password"))
							.text(Component.translatable(PREFIX + "clear"))
							.description(
									OptionDescription.of(
											Component.translatable(
													PREFIX + "clear_password.tooltip")))
							.available(hasSavedPassword)
							.action(this::clearPassword)
							.build();
			deleteButton =
					ButtonOption.createBuilder()
							.name(Component.translatable(PREFIX + "remove"))
							.text(Component.translatable(PREFIX + "remove_button"))
							.description(
									OptionDescription.of(
											Component.translatable(PREFIX + "remove.tooltip")))
							.available(persistedAddress != null)
							.action(this::deleteEntry)
							.build();

			ConfigCategory category =
					ConfigCategory.createBuilder()
							.name(Component.translatable("title.wenhan-client.autologin.editor"))
							.group(
									OptionGroup.createBuilder()
											.name(
													Component.translatable(
															persistedAddress == null
																	? PREFIX + "new_server"
																	: PREFIX + "server_settings"))
											.option(addressOption)
											.option(
													enabledOption(
															() -> enabled,
															value -> enabled = value))
											.option(passwordOption)
											.option(clearPasswordButton)
											.option(deleteButton)
											.build())
							.build();
			return YetAnotherConfigLib.createBuilder()
					.title(Component.translatable("title.wenhan-client.autologin.editor"))
					.category(category)
					.save(this::save)
					.build()
					.generateScreen(parent);
		}

		private void save() {
			String normalizedAddress = address.trim();
			if (normalizedAddress.isEmpty() || !ServerAddress.isValidAddress(normalizedAddress)) {
				showError("invalid_address");
				return;
			}
			AutoLoginConfig previous = AutoLoginConfig.load();
			AutoLoginConfig updated = previous.copy();
			try {
				AutoLoginConfig.Credential credential =
						persistedAddress == null
								? updated.servers.get(normalizedAddress)
								: updated.servers.get(persistedAddress);
				if (credential == null) {
					credential = new AutoLoginConfig.Credential();
				}
				credential.enabled = enabled;
				if (!password.isEmpty()) {
					setEncryptedPassword(credential, password);
					hasSavedPassword = true;
				}
				if (persistedAddress != null && !persistedAddress.equals(normalizedAddress)) {
					updated.servers.remove(persistedAddress);
				}
				updated.servers.put(normalizedAddress, credential);
				updated.save();
				persistedAddress = normalizedAddress;
				addressOption.requestSet(normalizedAddress);
				addressOption.applyValue();
				passwordOption.requestSet("");
				passwordOption.applyValue();
				clearPasswordButton.setAvailable(hasSavedPassword);
				deleteButton.setAvailable(true);
			} catch (Exception ignored) {
				WenhanClientMod.CONFIG.autoLogin = previous;
				showError("save_failed");
			}
		}

		private void clearPassword(YACLScreen screen) {
			if (persistedAddress == null) {
				return;
			}
			AutoLoginConfig updated = AutoLoginConfig.load().copy();
			AutoLoginConfig.Credential credential = updated.servers.get(persistedAddress);
			if (credential == null || !credential.hasPassword()) {
				clearPasswordButton.setAvailable(false);
				return;
			}
			credential.clearPassword();
			updated.save();
			hasSavedPassword = false;
			passwordOption.requestSet("");
			passwordOption.applyValue();
			clearPasswordButton.setAvailable(false);
			screen.setSaveButtonMessage(
					Component.translatable(PREFIX + "password_cleared"),
					Component.translatable(PREFIX + "password_cleared.tooltip"));
		}

		private void deleteEntry(YACLScreen screen) {
			if (persistedAddress == null) {
				return;
			}
			AutoLoginConfig updated = AutoLoginConfig.load().copy();
			updated.servers.remove(persistedAddress);
			updated.save();
			persistedAddress = null;
			screen.onClose();
		}
	}

	private static Option<Boolean> enabledOption(
			Supplier<Boolean> getter, Consumer<Boolean> setter) {
		return Option.<Boolean>createBuilder()
				.name(Component.translatable(PREFIX + "enabled"))
				.description(
						OptionDescription.of(Component.translatable(PREFIX + "enabled.tooltip")))
				.binding(AutoLoginConfig.Credential.DEFAULT_ENABLED, getter, setter)
				.controller(BooleanControllerBuilder::create)
				.build();
	}

	private static Option<String> passwordOption(Supplier<String> getter, Consumer<String> setter) {
		return Option.<String>createBuilder()
				.name(Component.translatable(PREFIX + "password"))
				.description(
						OptionDescription.of(Component.translatable(PREFIX + "password.tooltip")))
				.binding("", getter, setter)
				.customController(PasswordController::new)
				.build();
	}

	private static void setEncryptedPassword(AutoLoginConfig.Credential credential, String password)
			throws Exception {
		byte[] key = DeviceKey.get();
		if (key == null) {
			throw new IllegalStateException("Device key unavailable");
		}
		Crypto.Result encrypted = Crypto.encrypt(password, key);
		credential.enc = encrypted.enc;
		credential.salt = encrypted.salt;
		credential.iv = encrypted.iv;
	}

	private static void showError(String key) {
		SystemToast.add(
				Minecraft.getInstance().gui.toastManager(),
				ERROR_TOAST,
				Component.translatable(PREFIX + "error.title"),
				Component.translatable(PREFIX + "error." + key));
	}

	private static final class RefreshingParentScreen extends Screen {
		private final Screen rootParent;

		private RefreshingParentScreen(Screen rootParent) {
			super(Component.empty());
			this.rootParent = rootParent;
		}

		@Override
		protected void init() {
			GuiUtils.setScreen(
					WenhanClientConfigScreen.create(
							rootParent, WenhanClientConfigScreen.AUTO_LOGIN_CATEGORY_INDEX));
		}
	}

	private record AddressController(Option<String> option) implements IStringController<String> {
		@Override
		public String getString() {
			return option.pendingValue();
		}

		@Override
		public void setFromString(String value) {
			option.requestSet(value);
		}

		@Override
		public boolean isInputValid(String value) {
			String address = value.trim();
			return !address.isEmpty() && ServerAddress.isValidAddress(address);
		}

		@Override
		public Component formatValue() {
			return Component.literal(getString());
		}

		@Override
		public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> dimension) {
			return new StringControllerElement(this, screen, dimension, false);
		}
	}

	private record PasswordController(Option<String> option) implements IStringController<String> {
		@Override
		public String getString() {
			return option.pendingValue();
		}

		@Override
		public void setFromString(String value) {
			option.requestSet(value);
		}

		@Override
		public boolean isInputValid(String value) {
			return value.chars().noneMatch(Character::isISOControl);
		}

		@Override
		public Component formatValue() {
			return Component.literal("*".repeat(getString().length()));
		}

		@Override
		public AbstractWidget provideWidget(YACLScreen screen, Dimension<Integer> dimension) {
			return new StringControllerElement(this, screen, dimension, false) {
				@Override
				protected Component getValueText() {
					return Component.literal("*".repeat(inputField.length()));
				}
			};
		}
	}
}
