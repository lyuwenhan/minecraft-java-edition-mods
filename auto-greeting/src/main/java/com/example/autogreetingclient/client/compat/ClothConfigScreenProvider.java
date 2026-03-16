package com.example.autogreetingclient.client.compat;

import com.example.autogreetingclient.AutoGreetingClientConfig;
import com.example.autogreetingclient.AutoGreetingClientConfigHolder;
import com.example.autogreetingclient.rules.StringMatchRules;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;

public final class ClothConfigScreenProvider {
	private ClothConfigScreenProvider() {
	}

	public static Screen create(Screen parent) {
		AutoGreetingClientConfig current = AutoGreetingClientConfigHolder.get();
		AutoGreetingClientConfig editing = current.copy();

		ConfigBuilder builder = ConfigBuilder.create()
			.setParentScreen(parent)
			.setTitle(Text.literal("Auto Greeting Config"));

		builder.setSavingRunnable(() -> {
			AutoGreetingClientConfigHolder.set(editing);
			AutoGreetingClientConfigHolder.save();
		});

		ConfigEntryBuilder eb = builder.entryBuilder();

		ConfigCategory self = builder.getOrCreateCategory(Text.literal("Self"));
		self.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), editing.selfEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(v -> editing.selfEnabled = v)
			.build());

		self.addEntry(eb.startStrList(Text.literal("Greetings"), new ArrayList<>(editing.selfGreetings))
			.setDefaultValue(new ArrayList<>())
			.setSaveConsumer(v -> {
				editing.selfGreetings.clear();
				editing.selfGreetings.addAll(v);
			})
			.build());

		ConfigCategory other = builder.getOrCreateCategory(Text.literal("Other"));
		other.addEntry(eb.startBooleanToggle(Text.literal("Enabled"), editing.otherEnabled)
			.setDefaultValue(true)
			.setSaveConsumer(v -> editing.otherEnabled = v)
			.build());

		other.addEntry(eb.startStrList(Text.literal("Greetings"), new ArrayList<>(editing.otherGreetings))
			.setDefaultValue(new ArrayList<>())
			.setSaveConsumer(v -> {
				editing.otherGreetings.clear();
				editing.otherGreetings.addAll(v);
			})
			.build());

		addRulesCategory(builder, eb, "Other Blacklist", editing.otherBlacklist);
		addRulesCategory(builder, eb, "Other Blacklist Except", editing.otherBlacklistExcept);
		addRulesCategory(builder, eb, "Other Whitelist", editing.otherWhitelist);
		addRulesCategory(builder, eb, "Other Whitelist Except", editing.otherWhitelistExcept);

		return builder.build();
	}

	private static void addRulesCategory(
		ConfigBuilder builder,
		ConfigEntryBuilder eb,
		String title,
		StringMatchRules rules
	) {
		ConfigCategory cat = builder.getOrCreateCategory(Text.literal(title));

		cat.addEntry(eb.startStrList(Text.literal("Equal"), new ArrayList<>(rules.equal))
			.setDefaultValue(new ArrayList<>())
			.setSaveConsumer(v -> {
				rules.equal.clear();
				rules.equal.addAll(v);
			})
			.build());

		cat.addEntry(eb.startStrList(Text.literal("Contain"), new ArrayList<>(rules.contain))
			.setDefaultValue(new ArrayList<>())
			.setSaveConsumer(v -> {
				rules.contain.clear();
				rules.contain.addAll(v);
			})
			.build());

		cat.addEntry(eb.startStrList(Text.literal("Start With"), new ArrayList<>(rules.startWith))
			.setDefaultValue(new ArrayList<>())
			.setSaveConsumer(v -> {
				rules.startWith.clear();
				rules.startWith.addAll(v);
			})
			.build());

		cat.addEntry(eb.startStrList(Text.literal("End With"), new ArrayList<>(rules.endWith))
			.setDefaultValue(new ArrayList<>())
			.setSaveConsumer(v -> {
				rules.endWith.clear();
				rules.endWith.addAll(v);
			})
			.build());
	}
}