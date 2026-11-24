package com.example.wenhanclient.client.compat;

import com.example.wenhanclient.entityhighlighter.EntityHighlighterConfig;
import com.example.wenhanclient.entityhighlighter.EntityHighlighterSubMod;
import com.example.wenhanclient.entityhighlighter.GroupHighlightRule;
import com.example.wenhanclient.entityhighlighter.TypeHighlightRule;
import com.example.wenhanclient.itemhighlight.ItemHighlightConfig;
import com.example.wenhanclient.itemhighlight.ItemHighlightSubMod;

import dev.isxander.yacl3.api.ButtonOption;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ColorControllerBuilder;
import dev.isxander.yacl3.gui.YACLScreen;
import dev.isxander.yacl3.gui.utils.GuiUtils;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.awt.Color;
import java.util.Comparator;
import java.util.List;

final class EntityHighlighterConfigPage {
	private static final String PREFIX = "option.wenhan-client.entity-highlighter.";
	private static final Identifier DROPPED_ITEM_ID = Identifier.parse("minecraft:item");
	private static final List<String> GROUPS = List.of("animal", "monster", "everything");
	private final Screen rootParent;

	EntityHighlighterConfigPage(Screen rootParent) {
		this.rootParent = rootParent;
	}

	ConfigCategory category() {
		EntityHighlighterConfig config = EntityHighlighterSubMod.config();
		OptionGroup.Builder groups =
				OptionGroup.createBuilder()
						.name(Component.translatable(PREFIX + "groups"))
						.description(
								OptionDescription.of(
										Component.translatable(PREFIX + "groups.tooltip")));
		GroupHighlightRule attackable =
				findGroupRule(config, EntityHighlighterConfig.ATTACKABLE_GROUP);
		groups.option(
				entryButton(
						Component.translatable(PREFIX + "group.attackable"),
						PREFIX + "group.attackable.tooltip",
						EntityHighlighterConfig.ATTACKABLE_GROUP,
						attackable.enabled,
						attackable.color,
						screen ->
								openEditor(
										screen,
										Target.group(EntityHighlighterConfig.ATTACKABLE_GROUP))));
		ItemHighlightConfig items = ItemHighlightSubMod.config();
		groups.option(
				ButtonOption.createBuilder()
						.name(Component.translatable(PREFIX + "group.dropped_items"))
						.text(
								Component.translatable(
										PREFIX + (items.enabled ? "enabled" : "disabled")))
						.description(
								OptionDescription.of(
										Component.translatable(
												PREFIX + "group.dropped_items.tooltip",
												items.blacklist.size())))
						.action(this::openDroppedItemsEditor)
						.build());
		for (String group : GROUPS) {
			GroupHighlightRule rule = findGroupRule(config, group);
			groups.option(
					entryButton(
							Component.translatable(PREFIX + "group." + group),
							PREFIX + "group.tooltip",
							group,
							rule.enabled,
							rule == null ? EntityHighlighterSubMod.DEFAULT_COLOR : rule.color,
							screen -> openEditor(screen, Target.group(group))));
		}

		OptionGroup.Builder types =
				OptionGroup.createBuilder()
						.name(Component.translatable(PREFIX + "types"))
						.description(
								OptionDescription.of(
										Component.translatable(PREFIX + "types.tooltip")));
		BuiltInRegistries.ENTITY_TYPE.keySet().stream()
				.filter(id -> !DROPPED_ITEM_ID.equals(id))
				.sorted(Comparator.comparing(Identifier::toString))
				.forEach(
						id -> {
							TypeHighlightRule rule = findTypeRule(config, id.toString());
							var type = BuiltInRegistries.ENTITY_TYPE.getValue(id);
							types.option(
									entryButton(
											Component.translatable(type.getDescriptionId()),
											PREFIX + "type.tooltip",
											id.toString(),
											rule != null,
											rule == null
													? EntityHighlighterSubMod.DEFAULT_COLOR
													: rule.color,
											screen ->
													openEditor(
															screen, Target.type(id.toString()))));
						});

		return ConfigCategory.createBuilder()
				.name(Component.translatable("title.wenhan-client.entity-highlighter.config"))
				.group(groups.build())
				.group(types.build())
				.build();
	}

	private ButtonOption entryButton(
			Component name,
			String tooltipKey,
			String identifier,
			boolean enabled,
			int color,
			java.util.function.Consumer<YACLScreen> action) {
		return ButtonOption.createBuilder()
				.name(name)
				.text(Component.translatable(PREFIX + (enabled ? "enabled" : "disabled")))
				.description(
						OptionDescription.of(
								Component.translatable(tooltipKey, identifier, formatColor(color))))
				.action(action)
				.build();
	}

	private void openEditor(YACLScreen screen, Target target) {
		screen.finishOrSave();
		Screen refreshedParent = new RefreshingParentScreen(rootParent);
		GuiUtils.setScreen(new Editor(target).createScreen(refreshedParent));
	}

	private void openDroppedItemsEditor(YACLScreen screen) {
		screen.finishOrSave();
		Screen refreshedParent = new RefreshingParentScreen(rootParent);
		GuiUtils.setScreen(new DroppedItemsEditor().createScreen(refreshedParent));
	}

	private static TypeHighlightRule findTypeRule(EntityHighlighterConfig config, String entityId) {
		return config.typeRules.get(entityId);
	}

	private static GroupHighlightRule findGroupRule(
			EntityHighlighterConfig config, String groupName) {
		return config.groupRules.get(groupName);
	}

	private static String formatColor(int color) {
		return String.format("#%06X", color & 0xFFFFFF);
	}

	private static final class Editor {
		private final Target target;
		private boolean enabled;
		private int color;

		private Editor(Target target) {
			this.target = target;
			EntityHighlighterConfig config = EntityHighlighterSubMod.config();
			if (target.group) {
				GroupHighlightRule rule = findGroupRule(config, target.identifier);
				enabled = rule != null && rule.enabled;
				color = rule == null ? EntityHighlighterSubMod.DEFAULT_COLOR : rule.color;
			} else {
				TypeHighlightRule rule = findTypeRule(config, target.identifier);
				enabled = rule != null && rule.enabled;
				color = rule == null ? EntityHighlighterSubMod.DEFAULT_COLOR : rule.color;
			}
		}

		private Screen createScreen(Screen parent) {
			Component targetName;
			if (target.group) {
				targetName = Component.translatable(PREFIX + "group." + target.identifier);
			} else {
				targetName =
						Component.translatable(
								BuiltInRegistries.ENTITY_TYPE
										.getValue(Identifier.parse(target.identifier))
										.getDescriptionId());
			}
			ConfigCategory category =
					ConfigCategory.createBuilder()
							.name(targetName)
							.option(
									Option.<Boolean>createBuilder()
											.name(Component.translatable(PREFIX + "highlight"))
											.description(
													OptionDescription.of(
															Component.translatable(
																	PREFIX + "highlight.tooltip")))
											.binding(false, () -> enabled, value -> enabled = value)
											.controller(BooleanControllerBuilder::create)
											.build())
							.option(
									Option.<Color>createBuilder()
											.name(Component.translatable(PREFIX + "color"))
											.description(
													OptionDescription.of(
															Component.translatable(
																	PREFIX + "color.tooltip")))
											.binding(
													new Color(
															EntityHighlighterSubMod.DEFAULT_COLOR),
													() -> new Color(color & 0xFFFFFF),
													value -> color = value.getRGB() & 0xFFFFFF)
											.controller(
													option ->
															ColorControllerBuilder.create(option)
																	.allowAlpha(false))
											.build())
							.build();
			return YetAnotherConfigLib.createBuilder()
					.title(Component.translatable("title.wenhan-client.entity-highlighter.editor"))
					.category(category)
					.save(this::save)
					.build()
					.generateScreen(parent);
		}

		private void save() {
			EntityHighlighterConfig updated = EntityHighlighterSubMod.config().copy();
			if (target.group) {
				GroupHighlightRule rule = updated.groupRules.get(target.identifier);
				if (rule != null) {
					rule.enabled = enabled;
					rule.color = color;
				}
			} else {
				TypeHighlightRule rule = updated.typeRules.get(target.identifier);
				if (rule != null) {
					rule.enabled = enabled;
					rule.color = color;
				}
			}
			EntityHighlighterSubMod.setConfig(updated);
		}
	}

	private static final class DroppedItemsEditor {
		private final ItemHighlightConfig draft = ItemHighlightSubMod.config();

		private Screen createScreen(Screen parent) {
			ConfigCategory.Builder category =
					ConfigCategory.createBuilder()
							.name(Component.translatable(PREFIX + "group.dropped_items"))
							.option(
									Option.<Boolean>createBuilder()
											.name(Component.translatable(PREFIX + "highlight"))
											.description(
													OptionDescription.of(
															Component.translatable(
																	PREFIX + "highlight.tooltip")))
											.binding(
													ItemHighlightConfig.DEFAULT_ENABLED,
													() -> draft.enabled,
													value -> draft.enabled = value)
											.controller(BooleanControllerBuilder::create)
											.build())
							.option(
									Option.<Color>createBuilder()
											.name(Component.translatable(PREFIX + "color"))
											.description(
													OptionDescription.of(
															Component.translatable(
																	PREFIX + "color.tooltip")))
											.binding(
													new Color(ItemHighlightConfig.DEFAULT_COLOR),
													() -> new Color(draft.color & 0xFFFFFF),
													value ->
															draft.color = value.getRGB() & 0xFFFFFF)
											.controller(
													option ->
															ColorControllerBuilder.create(option)
																	.allowAlpha(false))
											.build());
			addItemOptions(category, draft);
			return YetAnotherConfigLib.createBuilder()
					.title(Component.translatable(PREFIX + "group.dropped_items"))
					.category(category.build())
					.save(() -> ItemHighlightSubMod.setConfig(draft))
					.build()
					.generateScreen(parent);
		}

		private static void addItemOptions(
				ConfigCategory.Builder category, ItemHighlightConfig draft) {
			BuiltInRegistries.ITEM.keySet().stream()
					.sorted(Comparator.comparing(Identifier::toString))
					.forEach(
							id -> {
								var item = BuiltInRegistries.ITEM.getValue(id);
								String key = id.toString();
								Component itemName =
										Component.translatable(item.getDescriptionId());
								category.option(
										Option.<Boolean>createBuilder()
												.name(
														Component.translatable(
																"option.wenhan-client.item-highlight.item.name",
																itemName))
												.description(
														OptionDescription.of(
																Component.translatable(
																		"option.wenhan-client.item-highlight.item.tooltip",
																		itemName)))
												.binding(
														true,
														() -> !draft.blacklist.contains(key),
														enabled -> {
															if (enabled) {
																draft.blacklist.remove(key);
															} else {
																draft.blacklist.add(key);
															}
														})
												.controller(
														option ->
																BooleanControllerBuilder.create(
																				option)
																		.yesNoFormatter())
												.build());
							});
		}
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
							rootParent,
							WenhanClientConfigScreen.ENTITY_HIGHLIGHTER_CATEGORY_INDEX));
		}
	}

	private record Target(boolean group, String identifier) {
		private static Target group(String name) {
			return new Target(true, name);
		}

		private static Target type(String id) {
			return new Target(false, id);
		}
	}
}
