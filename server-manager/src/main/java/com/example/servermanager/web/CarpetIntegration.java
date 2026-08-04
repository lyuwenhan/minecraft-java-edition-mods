package com.example.servermanager.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Optional integration with Carpet and Carpet extensions through Carpet's public settings API. */
final class CarpetIntegration {
	private static final String CARPET_MOD_ID = "carpet";
	private static final String CARPET_SERVER_CLASS = "carpet.CarpetServer";
	private static final String CARPET_EXTENSION_CLASS = "carpet.CarpetExtension";
	private static final String SETTINGS_MANAGER_CLASS = "carpet.api.settings.SettingsManager";
	private static final String CARPET_RULE_CLASS = "carpet.api.settings.CarpetRule";
	private static final String RULE_HELPER_CLASS = "carpet.api.settings.RuleHelper";

	private static boolean observerRegistered;

	private CarpetIntegration() {}

	static synchronized void registerObserver() {
		if (observerRegistered || !FabricLoader.getInstance().isModLoaded(CARPET_MOD_ID)) return;
		try {
			Class<?> managerClass = Class.forName(SETTINGS_MANAGER_CLASS);
			Class<?> observerClass = Class.forName(SETTINGS_MANAGER_CLASS + "$RuleObserver");
			Method register = managerClass.getMethod("registerGlobalRuleObserver", observerClass);
			Object observer =
					Proxy.newProxyInstance(
							observerClass.getClassLoader(),
							new Class<?>[] {observerClass},
							(proxy, method, arguments) -> {
								if ("ruleChanged".equals(method.getName())
										&& arguments != null
										&& arguments.length >= 2
										&& arguments[1] != null) {
									Object rule = arguments[1];
									BackendService.onCarpetRuleChanged(
											ruleId(rule), normalizedRuleValue(rule));
								}
								return null;
							});
			register.invoke(null, observer);
			observerRegistered = true;
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Failed to register Carpet rule observer", exception);
		}
	}

	static String packet(MinecraftServer server) {
		JsonObject root = new JsonObject();
		root.addProperty("type", "carpet-rule");
		JsonObject data = new JsonObject();

		if (!FabricLoader.getInstance().isModLoaded(CARPET_MOD_ID)) {
			root.add("data", data);
			return root.toString();
		}

		try {
			for (Object manager : managers()) {
				String managerId = managerIdentifier(manager);
				boolean editable = !managerLocked(manager);
				List<Object> rules = new ArrayList<>(managerRules(manager));
				rules.sort(Comparator.comparing(CarpetIntegration::ruleName));
				for (Object rule : rules) {
					for (String category : ruleCategories(rule)) {
						String translatedCategory = translatedCategory(managerId, category);
						String categoryName =
								"carpet".equals(managerId)
										? translatedCategory
										: managerId + " / " + translatedCategory;
						JsonObject categoryData =
								data.has(categoryName)
										? data.getAsJsonObject(categoryName)
										: new JsonObject();
						if (!data.has(categoryName)) data.add(categoryName, categoryData);
						categoryData.add(ruleId(rule), ruleEntry(rule, editable));
					}
				}
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Failed to read Carpet rules", exception);
		}

		root.add("data", data);
		return root.toString();
	}

	static void update(MinecraftServer server, String id, String value) {
		if (!FabricLoader.getInstance().isModLoaded(CARPET_MOD_ID)) return;
		int separator = id.indexOf(':');
		if (separator <= 0 || separator == id.length() - 1) return;
		String managerId = id.substring(0, separator);
		String ruleName = id.substring(separator + 1);
		try {
			for (Object manager : managers()) {
				if (!managerId.equals(managerIdentifier(manager)) || managerLocked(manager))
					continue;
				Object rule =
						manager.getClass()
								.getMethod("getCarpetRule", String.class)
								.invoke(manager, ruleName);
				if (rule == null) return;
				Class<?> commandSourceClass =
						Class.forName("net.minecraft.commands.CommandSourceStack");
				rule.getClass()
						.getMethod("set", commandSourceClass, String.class)
						.invoke(rule, server.createCommandSourceStack(), value);
				return;
			}
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Failed to update Carpet rule " + id, exception);
		}
	}

	static void setDefault(MinecraftServer server, String id, String value) {
		if (!FabricLoader.getInstance().isModLoaded(CARPET_MOD_ID)) return;
		String ruleName = commandRuleName(id);
		if (ruleName == null) return;
		try {
			server.getCommands()
					.getDispatcher()
					.execute(
							"carpet setDefault " + ruleName + " " + value,
							server.createCommandSourceStack());
		} catch (Exception exception) {
			throw new IllegalStateException("Failed to set default Carpet rule " + id, exception);
		}
	}

	private static String commandRuleName(String id) {
		int separator = id.indexOf(':');
		if (separator <= 0 || separator == id.length() - 1) return null;
		String managerId = id.substring(0, separator);
		String ruleName = id.substring(separator + 1);
		return "carpet".equals(managerId) ? ruleName : id;
	}

	private static JsonObject ruleEntry(Object rule, boolean editable)
			throws ReflectiveOperationException {
		JsonObject entry = new JsonObject();
		String type = ruleType(rule);
		entry.addProperty("name", translatedName(rule));
		addValue(entry, "currentValue", type, ruleValue(rule));
		entry.addProperty("type", type);
		addValue(entry, "defaultValue", type, ruleDefaultValue(rule));
		entry.addProperty("editable", editable);

		Collection<String> suggestions = ruleSuggestions(rule);
		if (!suggestions.isEmpty()) {
			JsonArray values = new JsonArray();
			for (String suggestion : suggestions) addArrayValue(values, type, suggestion);
			entry.add(ruleStrict(rule) ? "allowedValues" : "recommendedValues", values);
		}
		return entry;
	}

	private static List<Object> managers() throws ReflectiveOperationException {
		Class<?> carpetServer = Class.forName(CARPET_SERVER_CLASS);
		Map<String, Object> result = new LinkedHashMap<>();
		Field settingsManagerField = carpetServer.getField("settingsManager");
		Object mainManager = settingsManagerField.get(null);
		if (mainManager != null) result.put(managerIdentifier(mainManager), mainManager);

		Field extensionsField = carpetServer.getField("extensions");
		Class<?> extensionClass = Class.forName(CARPET_EXTENSION_CLASS);
		Method extensionSettingsManager = extensionClass.getMethod("extensionSettingsManager");
		for (Object extension : (Collection<?>) extensionsField.get(null)) {
			Object manager = extensionSettingsManager.invoke(extension);
			if (manager != null) result.put(managerIdentifier(manager), manager);
		}
		return new ArrayList<>(result.values());
	}

	private static String managerIdentifier(Object manager) throws ReflectiveOperationException {
		return (String) manager.getClass().getMethod("identifier").invoke(manager);
	}

	private static boolean managerLocked(Object manager) throws ReflectiveOperationException {
		return (boolean) manager.getClass().getMethod("locked").invoke(manager);
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object> managerRules(Object manager)
			throws ReflectiveOperationException {
		return (Collection<Object>) manager.getClass().getMethod("getCarpetRules").invoke(manager);
	}

	private static String ruleId(Object rule) throws ReflectiveOperationException {
		Object manager = rule.getClass().getMethod("settingsManager").invoke(rule);
		return managerIdentifier(manager) + ":" + ruleName(rule);
	}

	private static String ruleName(Object rule) {
		try {
			return (String) rule.getClass().getMethod("name").invoke(rule);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException(exception);
		}
	}

	private static Object ruleValue(Object rule) throws ReflectiveOperationException {
		return rule.getClass().getMethod("value").invoke(rule);
	}

	private static Object ruleDefaultValue(Object rule) throws ReflectiveOperationException {
		return rule.getClass().getMethod("defaultValue").invoke(rule);
	}

	private static Object normalizedRuleValue(Object rule) throws ReflectiveOperationException {
		Object value = ruleValue(rule);
		return value instanceof Boolean || value instanceof Number ? value : ruleString(value);
	}

	@SuppressWarnings("unchecked")
	private static Collection<String> ruleCategories(Object rule)
			throws ReflectiveOperationException {
		return (Collection<String>) rule.getClass().getMethod("categories").invoke(rule);
	}

	@SuppressWarnings("unchecked")
	private static Collection<String> ruleSuggestions(Object rule)
			throws ReflectiveOperationException {
		return (Collection<String>) rule.getClass().getMethod("suggestions").invoke(rule);
	}

	private static boolean ruleStrict(Object rule) throws ReflectiveOperationException {
		return (boolean) rule.getClass().getMethod("strict").invoke(rule);
	}

	private static String translatedName(Object rule) throws ReflectiveOperationException {
		Class<?> ruleHelper = Class.forName(RULE_HELPER_CLASS);
		Class<?> carpetRule = Class.forName(CARPET_RULE_CLASS);
		return (String) ruleHelper.getMethod("translatedName", carpetRule).invoke(null, rule);
	}

	private static String translatedCategory(String manager, String category)
			throws ReflectiveOperationException {
		Class<?> ruleHelper = Class.forName(RULE_HELPER_CLASS);
		return (String)
				ruleHelper
						.getMethod("translatedCategory", String.class, String.class)
						.invoke(null, manager, category);
	}

	private static String ruleType(Object rule) throws ReflectiveOperationException {
		Class<?> type = (Class<?>) rule.getClass().getMethod("type").invoke(rule);
		if (type == Boolean.class) return "boolean";
		if (type == Byte.class
				|| type == Short.class
				|| type == Integer.class
				|| type == Long.class) return "integer";
		if (Number.class.isAssignableFrom(type)) return "number";
		if (type.isEnum()) return "enum";
		return "string";
	}

	private static String ruleString(Object value) throws ReflectiveOperationException {
		Class<?> ruleHelper = Class.forName(RULE_HELPER_CLASS);
		return (String) ruleHelper.getMethod("toRuleString", Object.class).invoke(null, value);
	}

	private static void addValue(JsonObject object, String key, String type, Object value)
			throws ReflectiveOperationException {
		if ("boolean".equals(type)) object.addProperty(key, (Boolean) value);
		else if ("integer".equals(type) || "number".equals(type))
			object.addProperty(key, (Number) value);
		else object.addProperty(key, ruleString(value));
	}

	private static void addArrayValue(JsonArray array, String type, String value) {
		try {
			if ("boolean".equals(type)) array.add(Boolean.parseBoolean(value));
			else if ("integer".equals(type)) array.add(Long.parseLong(value));
			else if ("number".equals(type)) array.add(Double.parseDouble(value));
			else array.add(value);
		} catch (NumberFormatException exception) {
			array.add(value);
		}
	}
}
