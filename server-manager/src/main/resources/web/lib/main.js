const showMessage = function setupShowMessage() {
	const messageEle = document.getElementById("message");
	let messageTimeout = null;
	return message => {
		if (!message) {
			clearTimeout(messageTimeout);
			messageEle.classList.remove("info");
			messageTimeout = null;
			return
		}
		messageEle.classList.add("info");
		messageEle.innerText = String(message);
		clearTimeout(messageTimeout);
		messageTimeout = setTimeout(() => {
			messageEle.classList.remove("info");
			messageTimeout = null
		}, 2e3)
	}
}();
let socket = null;
let reconnectTimer = null;
let reconnectEnabled = true;
let socketAuthenticated = false;
const reconnectDelays = [0, 1e3, 2e3, 5e3, 1e4, 2e4, 3e4, 6e4, 12e4, 3e5, 6e5, 12e5, 18e5, 36e5, 72e5];
let reconnectAttempt = 0;
let term = null;
let fitAddon = null;
const ruleInputs = {
	gamerule: new Map,
	"carpet-rule": new Map
};
const playerStore = new Map;
const accessControlState = {
	whitelistEnabled: false,
	whitelist: new Map,
	blacklist: new Map,
	ops: new Map
};
const statsState = {};
const worldStatsState = {};
const resourceStatsState = {};
let commandTree = null;

function fixCommandTree() {
	const executeIndex = commandTree.nodes.findIndex(node => node.type === "literal" && node.name === "execute");
	if (executeIndex === -1) {
		return
	}
	const executeNode = commandTree.nodes[executeIndex];
	for (let nodeIndex = 0; nodeIndex < commandTree.nodes.length; nodeIndex++) {
		if (nodeIndex === commandTree.root) {
			continue
		}
		const node = commandTree.nodes[nodeIndex];
		for (const key of ["requiredNext", "next"]) {
			const source = node[key];
			if (!source.includes(executeIndex)) {
				continue
			}
			const executeTargets = executeNode[key];
			const result = [...new Set(source.flatMap(index => index === executeIndex ? executeTargets : [index]))];
			source.splice(0, source.length, ...result)
		}
	}
}

function isPlainObject(value) {
	return value !== null && typeof value === "object" && !Array.isArray(value)
}

function getWebSocketUrl() {
	const protocol = location.protocol === "https:" ? "wss:" : "ws:";
	return `${protocol}//${location.host}`
}

function normalizeTerminalText(content) {
	return content.replace(/\r?\n/g, "\r\n")
}

function sendPacket(packet) {
	if (!socket || socket.readyState !== WebSocket.OPEN) {
		showMessage("WebSocket is not connected");
		return false
	}
	try {
		socket.send(JSON.stringify(packet));
		return true
	} catch (error) {
		console.error("Failed to send WebSocket packet", error, packet);
		showMessage("Failed to send WebSocket packet");
		return false
	}
}

function sendFeedPlayer(name, heal) {
	return sendPacket({
		type: "feed-player",
		name,
		heal
	})
}

function createFeedPlayerButton(label, player, heal) {
	const button = document.createElement("button");
	button.type = "button";
	button.innerText = label;
	button.addEventListener("click", () => sendFeedPlayer(player.name, heal));
	return button
}

function requestCurrentView(needResend) {
	switch (location.hash) {
		case "#logs":
			if (needResend) {
				sendPacket({
					type: "request-full-log"
				})
			}
			break;
		case "#gamerule":
			sendPacket({
				type: "request-gamerule"
			});
			break;
		case "#carpet-rule":
			sendPacket({
				type: "request-carpet-rule"
			});
			break;
		case "#command":
			sendPacket({
				type: "request-command-tree"
			});
			break;
		case "#players":
			sendPacket({
				type: "request-players"
			});
			sendPacket({
				type: "request-access-control"
			});
			break;
		default:
			sendPacket({
				type: "request-stats"
			})
	}
}

function scheduleReconnect() {
	if (!reconnectEnabled || reconnectTimer !== null) return;
	const delayIndex = Math.min(reconnectAttempt, reconnectDelays.length - 1);
	const delay = reconnectDelays[delayIndex];
	reconnectAttempt = Math.min(reconnectAttempt + 1, reconnectDelays.length - 1);
	reconnectTimer = setTimeout(() => {
		reconnectTimer = null;
		connectWebSocket()
	}, delay)
}

function discardCurrentSocket() {
	const oldSocket = socket;
	socket = null;
	socketAuthenticated = false;
	if (oldSocket && (oldSocket.readyState === WebSocket.CONNECTING || oldSocket.readyState === WebSocket.OPEN)) {
		oldSocket.close(1e3, "Replacing connection")
	}
}

function connectWebSocket() {
	clearTimeout(reconnectTimer);
	reconnectTimer = null;
	if (socket && (socket.readyState === WebSocket.CONNECTING || socket.readyState === WebSocket.OPEN)) {
		return
	}
	reconnectEnabled = true;
	socketAuthenticated = false;
	const currentSocket = new WebSocket(getWebSocketUrl());
	socket = currentSocket;
	currentSocket.addEventListener("open", () => {
		if (socket !== currentSocket) return;
		socketAuthenticated = true;
		reconnectAttempt = 0;
		showMessage("");
		if (location.hash === "#login" || !location.hash) {
			location.replace("#dashboard");
			return
		}
		requestCurrentView(true)
	});
	currentSocket.addEventListener("message", event => {
		if (socket !== currentSocket) return;
		let packet;
		try {
			packet = JSON.parse(event.data)
		} catch (error) {
			console.error("Invalid WebSocket packet", error, event.data);
			return
		}
		if (!isPlainObject(packet) || typeof packet.type !== "string") {
			console.error("Invalid WebSocket packet", packet);
			return
		}
		console.log(packet);
		switch (packet.type) {
			case "stats":
				renderStats(packet.data, packet.worldData, packet.resourceData);
				break;
			case "players":
				replacePlayers(packet.data);
				break;
			case "player-add":
			case "player-update":
				upsertPlayers(packet.data);
				break;
			case "player-remove":
				removePlayers(packet.data);
				break;
			case "access-control":
				renderAccessControl(packet);
				break;
			case "gamerule":
				renderRules("gamerule", packet.data);
				break;
			case "carpet-rule":
				renderRules("carpet-rule", packet.data);
				break;
			case "update-gamerule":
				applyRuleUpdate("gamerule", packet);
				break;
			case "update-carpet-rule":
				applyRuleUpdate("carpet-rule", packet);
				break;
			case "command-tree":
				if (Number.isInteger(packet.root) && Array.isArray(packet.nodes)) {
					commandTree = packet;
					fixCommandTree()
				} else {
					console.error("Invalid command-tree packet", packet)
				}
				break;
			case "append-log":
				if (typeof packet.content === "string") {
					term.write(normalizeTerminalText(packet.content))
				} else {
					console.error("Invalid append-log packet", packet)
				}
				break;
			case "full-log":
				if (typeof packet.content === "string") {
					term.reset();
					term.write(normalizeTerminalText(packet.content))
				} else {
					console.error("Invalid full-log packet", packet)
				}
				break;
			default:
				console.warn("Unknown WebSocket packet type", packet.type, packet)
		}
	});
	currentSocket.addEventListener("close", event => {
		if (socket !== currentSocket) {
			return
		}
		socket = null;
		socketAuthenticated = false;
		if (event.code === 1008) {
			reconnectEnabled = false;
			clearTimeout(reconnectTimer);
			reconnectTimer = null;
			location.replace("#login");
			return
		}
		if (reconnectEnabled) {
			showMessage("WebSocket disconnected");
			scheduleReconnect()
		}
	});
	currentSocket.addEventListener("error", event => {
		if (socket !== currentSocket) {
			return
		}
		console.error("WebSocket error", event)
	})
}

function renderStats(data, worldData, resourceData) {
	if (data !== undefined) {
		if (!isPlainObject(data)) {
			console.error("Invalid stats data", data)
		} else {
			Object.assign(statsState, data)
		}
	}
	if (worldData !== undefined) {
		if (!isPlainObject(worldData)) {
			console.error("Invalid world stats data", worldData)
		} else {
			Object.assign(worldStatsState, worldData)
		}
	}
	if (resourceData !== undefined) {
		if (!isPlainObject(resourceData)) {
			console.error("Invalid resource stats data", resourceData)
		} else {
			Object.assign(resourceStatsState, resourceData)
		}
	}
	renderStatsSection("stats", statsState);
	renderStatsSection("world-stats", worldStatsState);
	renderStatsSection("resource-stats", resourceStatsState)
}

function renderStatsSection(elementId, data) {
	const statsEle = document.getElementById(elementId);
	statsEle.innerHTML = "";
	Object.entries(data).forEach(([item, value]) => {
		const liEle = document.createElement("li");
		const spanEle = document.createElement("span");
		spanEle.insertAdjacentText("beforeend", item + ": ");
		spanEle.append(document.createElement("wbr"));
		spanEle.insertAdjacentText("beforeend", String(value));
		liEle.append(spanEle);
		statsEle.append(liEle)
	})
}

function replacePlayers(players) {
	if (!Array.isArray(players)) {
		console.error("Invalid players packet", players);
		return
	}
	playerStore.clear();
	players.forEach(player => {
		if (isCompletePlayer(player)) {
			playerStore.set(player.uuid, player)
		} else {
			console.error("Incomplete player in full players packet", player)
		}
	});
	renderPlayers()
}

function isCompletePlayer(player) {
	return isPlainObject(player) && typeof player.name === "string" && typeof player.uuid === "string" && typeof player.dimension === "string" && isPlainObject(player.position) && Number.isFinite(player.position.x) && Number.isFinite(player.position.y) && Number.isFinite(player.position.z) && Number.isFinite(player.health) && Number.isFinite(player.hunger) && Number.isFinite(player.level) && typeof player.gamemode === "string" && Number.isFinite(player.ping) && typeof player.op === "boolean" && typeof player.alive === "boolean"
}

function upsertPlayers(players) {
	if (!Array.isArray(players)) {
		console.error("Invalid player-add/player-update packet", players);
		return
	}
	players.forEach(player => {
		if (isCompletePlayer(player)) {
			playerStore.set(player.uuid, player)
		} else {
			console.error("Incomplete player in incremental packet", player)
		}
	});
	renderPlayers()
}

function removePlayers(uuids) {
	if (!Array.isArray(uuids) || uuids.some(uuid => typeof uuid !== "string")) {
		console.error("Invalid player-remove packet", uuids);
		return
	}
	uuids.forEach(uuid => playerStore.delete(uuid));
	renderPlayers()
}

function runPlayerCommand(command) {
	fetch("/api", {
		method: "POST",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			type: "run-command",
			data: [command]
		})
	}).catch(error => console.error("Failed to send command", error))
}

function createActionButton(label, command, player) {
	const button = document.createElement("button");
	button.type = "button";
	button.innerText = label;
	button.addEventListener("click", () => runPlayerCommand(`${command} ${player.name}`));
	return button
}

function renderPlayers() {
	const playersEle = document.getElementById("player-entries");
	const actionsEle = document.getElementById("player-actions");
	const players = Array.from(playerStore.values());
	document.getElementById("player-count").innerText = `Online Players: ${players.length}`;
	playersEle.innerHTML = "";
	actionsEle.innerHTML = "";
	if (players.length === 0) {
		for (const [body, span] of [
				[playersEle, 9],
				[actionsEle, 3]
			]) {
			const row = document.createElement("tr");
			const cell = document.createElement("td");
			cell.colSpan = span;
			cell.className = "players-empty";
			cell.innerText = "No players online";
			row.append(cell);
			body.append(row)
		}
		return
	}
	players.forEach(player => {
		const row = document.createElement("tr");
		const position = isPlainObject(player.position) ? player.position : {};
		const health = `${formatNumber(player.health,1)}/${formatNumber(player.maxHealth,1)}`;
		const positionText = `${formatNumber(position.x,1)}, ${formatNumber(position.y,1)}, ${formatNumber(position.z,1)}`;
		const ping = Number.isFinite(Number(player.ping)) && Number(player.ping) >= 0 ? `${Math.round(Number(player.ping))} ms` : "N/A";
		const values = [player.name, player.alive ? "Alive" : "Dead", player.dimension, positionText];
		values.forEach(value => {
			const cell = document.createElement("td");
			cell.innerText = value == null ? "N/A" : String(value);
			row.append(cell)
		});
		const healthCell = document.createElement("td");
		healthCell.append(String(health), " ", document.createElement("wbr"), createFeedPlayerButton("Heal", player, true));
		row.append(healthCell);
		const hungerCell = document.createElement("td");
		hungerCell.append(String(player.hunger), " ", document.createElement("wbr"), createFeedPlayerButton("Feed", player, false));
		row.append(hungerCell);
		[player.level, player.gamemode, ping].forEach(value => {
			const cell = document.createElement("td");
			cell.innerText = value == null ? "N/A" : String(value);
			row.append(cell)
		});
		playersEle.append(row);
		const actionRow = document.createElement("tr");
		for (const value of [player.uuid, player.name]) {
			const cell = document.createElement("td");
			cell.innerText = value;
			actionRow.append(cell)
		}
		const operationCell = document.createElement("td");
		const operationButtons = document.createElement("div");
		operationButtons.className = "player-operation-buttons";
		operationButtons.append(createActionButton("Clear Inventory", "clear", player), createActionButton("Kill", "kill", player), createActionButton("Kick", "kick", player), createActionButton("Ban", "ban", player));
		if (!accessControlState.whitelist.has(player.uuid)) {
			operationButtons.append(createActionButton("Add to Whitelist", "whitelist add", player))
		}
		if (!player.op) {
			operationButtons.append(createActionButton("OP", "op", player))
		}
		operationCell.append(operationButtons);
		actionRow.append(operationCell);
		actionsEle.append(actionRow)
	})
}

function renderAccessControl(packet) {
	if (typeof packet.whitelistEnabled !== "boolean" || !Array.isArray(packet.whitelist) || !Array.isArray(packet.blacklist) || !Array.isArray(packet.ops)) {
		console.error("Invalid access-control packet", packet);
		return
	}
	accessControlState.whitelistEnabled = packet.whitelistEnabled;
	accessControlState.whitelist.clear();
	accessControlState.blacklist.clear();
	accessControlState.ops.clear();
	for (const entry of packet.whitelist) {
		if (isIdentityEntry(entry)) {
			accessControlState.whitelist.set(entry.uuid, entry)
		}
	}
	for (const entry of packet.blacklist) {
		if (isIdentityEntry(entry)) {
			accessControlState.blacklist.set(entry.uuid, entry)
		}
	}
	for (const entry of packet.ops) {
		if (isIdentityEntry(entry)) {
			accessControlState.ops.set(entry.uuid, entry)
		}
	}
	const toggle = document.getElementById("toggle-whitelist");
	toggle.innerText = accessControlState.whitelistEnabled ? "On" : "Off";
	renderIdentityTable("whitelist-entries", accessControlState.whitelist, "Remove", "whitelist remove");
	renderIdentityTable("blacklist-entries", accessControlState.blacklist, "Remove", "pardon");
	renderIdentityTable("ops-entries", accessControlState.ops, "De-OP", "deop");
	renderPlayers()
}

function isIdentityEntry(entry) {
	return isPlainObject(entry) && typeof entry.uuid === "string" && typeof entry.name === "string"
}

function renderIdentityTable(bodyId, entries, label, command) {
	const body = document.getElementById(bodyId);
	body.innerHTML = "";
	if (entries.size === 0) {
		const row = document.createElement("tr");
		const cell = document.createElement("td");
		cell.colSpan = 3;
		cell.className = "players-empty";
		cell.innerText = "No entries";
		row.append(cell);
		body.append(row);
		return
	}
	for (const entry of entries.values()) {
		const row = document.createElement("tr");
		for (const value of [entry.uuid, entry.name]) {
			const cell = document.createElement("td");
			cell.innerText = value;
			row.append(cell)
		}
		const operationCell = document.createElement("td");
		const button = document.createElement("button");
		button.type = "button";
		button.innerText = label;
		button.addEventListener("click", () => runPlayerCommand(`${command} ${entry.name}`));
		operationCell.append(button);
		row.append(operationCell);
		body.append(row)
	}
}

function formatNumber(value, digits) {
	const number = Number(value);
	if (!Number.isFinite(number)) {
		return "N/A"
	}
	return number.toFixed(digits).replace(/\.0+$/, "")
}

function setRuleInputValue(input, type, value) {
	const text = type === "boolean" ? value ? "true" : "false" : value == null ? "" : String(value);
	input.value = text;
	if (input instanceof HTMLButtonElement) {
		input.innerText = text
	}
}

function isValidRuleValue(type, value) {
	if (type === "boolean") {
		return typeof value === "boolean"
	}
	if (type === "integer") {
		return Number.isInteger(value)
	}
	if (type === "number") {
		return typeof value === "number" && Number.isFinite(value)
	}
	return typeof value === "string"
}

function parseRuleInputValue(type, value) {
	if (type === "boolean") {
		return value === "true"
	}
	if (type === "integer") {
		const number = Number(value);
		return Number.isInteger(number) ? number : null
	}
	if (type === "number") {
		const number = Number(value);
		return Number.isFinite(number) ? number : null
	}
	return value
}

function applyRuleUpdate(ruleKind, packet) {
	if (typeof packet.id !== "string" || !("value" in packet)) {
		console.error("Invalid rule update packet", packet);
		return
	}
	const entry = ruleInputs[ruleKind].get(packet.id);
	if (!entry) {
		return
	}
	if (!isValidRuleValue(entry.type, packet.value)) {
		console.error("Invalid rule update value", packet, entry.type);
		return
	}
	entry.value = packet.value;
	setRuleInputValue(entry.input, entry.type, packet.value)
}

function sendRuleUpdate(ruleKind, id, value) {
	return sendPacket({
		type: ruleKind === "gamerule" ? "update-gamerule" : "update-carpet-rule",
		id,
		value
	})
}

function createRuleValueButton(label, value, input, inputEntry, ruleKind, id, editable, title) {
	const button = document.createElement("button");
	button.type = "button";
	button.innerText = String(label);
	button.title = title;
	button.disabled = editable === false;
	button.addEventListener("click", () => {
		if (button.disabled || input.disabled) {
			return
		}
		const parsedValue = parseRuleInputValue(inputEntry.type, String(value));
		if (parsedValue === null) {
			return
		}
		const previousValue = inputEntry.value;
		setRuleInputValue(input, inputEntry.type, parsedValue);
		if (!sendRuleUpdate(ruleKind, id, parsedValue)) {
			setRuleInputValue(input, inputEntry.type, previousValue);
			return
		}
		inputEntry.value = parsedValue
	});
	return button
}

function appendRuleValueButtons(item, label, values, input, inputEntry, ruleKind, id, editable, titlePrefix) {
	if (!Array.isArray(values) || values.length === 0) {
		return
	}
	const row = document.createElement("span");
	row.append(`${label}: `);
	values.forEach((value, index) => {
		if (index > 0) row.append(" ");
		row.append(document.createElement("wbr"), createRuleValueButton(value, value, input, inputEntry, ruleKind, id, editable, titlePrefix))
	});
	item.append(row)
}

function renderRules(ruleKind, data) {
	const container = document.getElementById(ruleKind);
	const inputMap = ruleInputs[ruleKind];
	container.innerHTML = "";
	inputMap.clear();
	if (!isPlainObject(data)) {
		console.error(`Invalid ${ruleKind} data`, data);
		return
	}
	Object.entries(data).forEach(([category, rules]) => {
		if (!isPlainObject(rules)) {
			console.error(`Invalid ${ruleKind} category`, category, rules);
			return
		}
		const categoryDetails = document.createElement("details");
		categoryDetails.open = true;
		const categorySummary = document.createElement("summary");
		const categoryHeader = document.createElement("h2");
		categoryHeader.innerText = category;
		categorySummary.append(categoryHeader);
		categoryDetails.append(categorySummary);
		Object.entries(rules).forEach(([id, rule]) => {
			if (!isPlainObject(rule)) {
				console.error(`Invalid ${ruleKind} rule`, id, rule);
				return
			}
			const {
				name,
				currentValue,
				type,
				defaultValue,
				editable,
				allowedValues,
				recommendedValues
			} = rule;
			if (typeof type !== "string" || !isValidRuleValue(type, currentValue)) {
				console.error(`Invalid ${ruleKind} rule value`, id, rule);
				return
			}
			const item = document.createElement("div");
			item.classList.add("gamerule-item");
			const title = document.createElement("h2");
			title.innerText = name;
			item.append(title);
			const typeEle = document.createElement("span");
			typeEle.innerText = `Type: ${type}`;
			item.append(typeEle);
			const currentRow = document.createElement("span");
			currentRow.append("Current Value: ");
			let input;
			const hasAllowedValues = Array.isArray(allowedValues) && allowedValues.length > 0;
			if (type === "boolean") {
				input = document.createElement("button");
				input.type = "button";
				input.title = "Toggle current value"
			} else if (hasAllowedValues) {
				input = document.createElement("select");
				allowedValues.forEach(optionValue => {
					const option = document.createElement("option");
					option.value = String(optionValue);
					option.innerText = String(optionValue);
					input.append(option)
				})
			} else {
				input = document.createElement("input")
			}
			const inputEntry = {
				input,
				type,
				value: currentValue
			};
			input.disabled = editable === false;
			if (type === "boolean") {
				setRuleInputValue(input, type, currentValue);
				input.addEventListener("click", () => {
					const previousValue = inputEntry.value;
					const value = !previousValue;
					setRuleInputValue(input, type, value);
					if (!sendRuleUpdate(ruleKind, id, value)) {
						setRuleInputValue(input, type, previousValue);
						return
					}
					inputEntry.value = value
				})
			} else {
				if (input instanceof HTMLInputElement) {
					if (type === "integer") {
						input.type = "number";
						input.step = "1"
					} else if (type === "number") {
						input.type = "number";
						input.step = "any"
					} else {
						input.type = "text"
					}
				}
				setRuleInputValue(input, type, currentValue);
				input.addEventListener("change", () => {
					const previousValue = inputEntry.value;
					const value = parseRuleInputValue(type, input.value);
					if (value === null) {
						setRuleInputValue(input, type, previousValue);
						showMessage(type === "integer" ? "Value must be an integer" : "Value must be a number");
						return
					}
					if (!sendRuleUpdate(ruleKind, id, value)) {
						setRuleInputValue(input, type, previousValue);
						return
					}
					inputEntry.value = value
				})
			}
			inputMap.set(id, inputEntry);
			const defaultRow = document.createElement("span");
			defaultRow.append("Default Value: ", document.createElement("wbr"), createRuleValueButton(defaultValue, defaultValue, input, inputEntry, ruleKind, id, editable, "Set as current value"));
			item.append(defaultRow);
			appendRuleValueButtons(item, "Allowed Values", allowedValues, input, inputEntry, ruleKind, id, editable, "Set as current value");
			appendRuleValueButtons(item, "Recommended Values", recommendedValues, input, inputEntry, ruleKind, id, editable, "Set as current value");
			currentRow.append(document.createElement("wbr"), input);
			item.append(currentRow);
			categoryDetails.append(item)
		});
		container.append(categoryDetails)
	})
}

function confirmWorldEntityAction(message, secondConfirmationMessage = null) {
	if (!confirm(message)) {
		return false
	}
	if (secondConfirmationMessage !== null && !confirm(secondConfirmationMessage)) {
		return false
	}
	return true
}

function registerWorldEntityAction(buttonId, action, confirmationMessage, secondConfirmationMessage = null) {
	document.getElementById(buttonId).addEventListener("click", () => {
		if (!confirmWorldEntityAction(confirmationMessage, secondConfirmationMessage)) return;
		sendPacket({
			type: "world-entity-action",
			action
		})
	})
}
registerWorldEntityAction("kill-all-players", "kill-all-players", "Kill all players?", "Are you sure to kill all players?");
registerWorldEntityAction("clear-all-entities", "clear-all-entities", "Clear all loaded entities?", "Are you sure to clear all loaded entities?");
registerWorldEntityAction("clear-falling-blocks", "clear-falling-blocks", "Clear all loaded falling blocks?");
registerWorldEntityAction("clear-enemies", "clear-enemies", "Clear all loaded enemies?");
registerWorldEntityAction("clear-projectiles", "clear-projectiles", "Clear all loaded impact projectiles?");
registerWorldEntityAction("clear-item-entities", "clear-item-entities", "Clear all loaded item entities?");
document.getElementById("stop-server").addEventListener("click", () => {
	if (!confirm("Stop the Minecraft server?")) return;
	runPlayerCommand("stop")
});
document.getElementById("toggle-whitelist").addEventListener("click", () => {
	const command = accessControlState.whitelistEnabled ? "whitelist off" : "whitelist on";
	runPlayerCommand(command)
});
document.getElementById("login-form").addEventListener("submit", async event => {
	event.preventDefault();
	const username = document.getElementById("username");
	const password = document.getElementById("password");
	if (!/^[a-zA-Z][a-zA-Z0-9_]{2,15}$/.test(username.value)) {
		showMessage("Invalid username, username must start with a letter and can only contain letters, numbers, and underscores, and be 3-16 characters long");
		return
	}
	if (!password.value) {
		showMessage("Password cannot be empty");
		return
	}
	try {
		const response = await fetch("/api", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({
				type: "login",
				username: username.value,
				password: password.value
			})
		});
		if (!response.ok) {
			showMessage(`Server error: ${response.status}` + (response.statusText ? " " + response.statusText : ""));
			return
		}
		const result = await response.json();
		if (result.succeed) {
			username.value = password.value = "";
			reconnectEnabled = true;
			reconnectAttempt = 0;
			clearTimeout(reconnectTimer);
			reconnectTimer = null;
			discardCurrentSocket();
			location.replace("#dashboard");
			connectWebSocket()
		} else {
			showMessage(result.reason || "Login failed");
			location.replace("#login")
		}
	} catch (error) {
		console.error(error);
		showMessage(error)
	}
});
term = new Terminal({
	cursorBlink: false,
	fontFamily: "Consolas, Courier, monospace",
	theme: {
		background: "#4E0E3B",
		foreground: "#eeeeee",
		cursor: "#dd4814",
		selection: "#FFFFFF",
		black: "#2E3436",
		red: "#CC0000",
		green: "#4E9A06",
		yellow: "#C4A000",
		blue: "#3465A4",
		magenta: "#75507B",
		cyan: "#06989A",
		white: "#D3D7CF",
		brightBlack: "#555753",
		brightRed: "#EF2929",
		brightGreen: "#8AE234",
		brightYellow: "#FCE94F",
		brightBlue: "#729FCF",
		brightMagenta: "#AD7FA8",
		brightCyan: "#34E2E2",
		brightWhite: "#EEEEEC"
	}
});
const logsEle = document.getElementById("logs-terminal");
term.open(logsEle);
fitAddon = new window.FitAddon.FitAddon;
term.loadAddon(fitAddon);
term.attachCustomKeyEventHandler(() => false);
fitAddon.fit();
window.addEventListener("resize", () => fitAddon.fit());
logsEle.addEventListener("contextmenu", event => event.preventDefault());
window.addEventListener("hashchange", () => {
	if (!socketAuthenticated) {
		if (location.hash !== "#login") {
			location.replace("#login")
		}
		return
	}
	if (location.hash === "#login" || !location.hash) {
		location.replace("#dashboard");
		return
	}
	if (location.hash === "#logs") {
		fitAddon.fit()
	}
	requestCurrentView(false)
});
async function toggleFullscreen() {
	if (document.fullscreenElement) {
		await document.exitFullscreen()
	} else {
		await document.documentElement.requestFullscreen()
	}
}
document.getElementById("full").addEventListener("click", toggleFullscreen);
document.getElementById("unfull").addEventListener("click", toggleFullscreen);
window.addEventListener("beforeunload", () => {
	reconnectEnabled = false;
	clearTimeout(reconnectTimer);
	reconnectTimer = null;
	const currentSocket = socket;
	socket = null;
	socketAuthenticated = false;
	if (currentSocket && (currentSocket.readyState === WebSocket.CONNECTING || currentSocket.readyState === WebSocket.OPEN)) {
		currentSocket.close(1e3, "Page unloading")
	}
});
connectWebSocket();
const commandInputEle = document.getElementById("command-input");
const commandFeedbackEle = document.getElementById("command-feedback");
let lastOutput = [];
let history = [];
let nowHistory = [""];
let nowpos = 0;
let suggests = [];
let suggestsPos = -1;

function commandForServer(command) {
	return command.startsWith("/") ? command.slice(1) : `say ${command}`
}

function getPlayers() {
	return [...playerStore.values()].map(({
		name
	}) => name)
}

function splitCommand(command) {
	if (typeof command !== "string") {
		throw new TypeError("command must be a string")
	}
	command = command.trim();
	if (command.startsWith("/")) {
		command = command.slice(1)
	}
	const openingToClosing = {
		"{": "}",
		"[": "]",
		"(": ")"
	};
	const closingBrackets = new Set(Object.values(openingToClosing));
	const argumentsList = [];
	const bracketStack = [];
	let currentArgument = "";
	let activeQuote = null;
	let escaped = false;
	for (let index = 0; index < command.length; index += 1) {
		const character = command[index];
		if (activeQuote !== null) {
			currentArgument += character;
			if (escaped) {
				escaped = false;
				continue
			}
			if (character === "\\") {
				escaped = true;
				continue
			}
			if (character === activeQuote) {
				activeQuote = null
			}
			continue
		}
		if (character === '"' || character === "'") {
			activeQuote = character;
			currentArgument += character;
			continue
		}
		if (Object.hasOwn(openingToClosing, character)) {
			bracketStack.push({
				openingCharacter: character,
				closingCharacter: openingToClosing[character],
				index
			});
			currentArgument += character;
			continue
		}
		if (closingBrackets.has(character)) {
			const lastBracket = bracketStack.pop();
			if (lastBracket === undefined) {
				throw new SyntaxError(`Unexpected closing bracket "${character}" at index ${index}`)
			}
			if (lastBracket.closingCharacter !== character) {
				throw new SyntaxError(`Closing bracket "${character}" at index ${index} does not match ` + `opening bracket "${lastBracket.openingCharacter}" at index ` + `${lastBracket.index}`)
			}
			currentArgument += character;
			continue
		}
		if (/\s/u.test(character) && bracketStack.length === 0) {
			if (currentArgument.length > 0) {
				argumentsList.push(currentArgument);
				currentArgument = ""
			}
			continue
		}
		currentArgument += character
	}
	if (activeQuote !== null) {
		throw new SyntaxError(`Unclosed ${activeQuote} quote`)
	}
	if (bracketStack.length > 0) {
		const lastBracket = bracketStack[bracketStack.length - 1];
		throw new SyntaxError(`Opening bracket "${lastBracket.openingCharacter}" at index ` + `${lastBracket.index} is not closed. Expected ` + `"${lastBracket.closingCharacter}"`)
	}
	if (currentArgument.length > 0) {
		argumentsList.push(currentArgument)
	}
	return argumentsList
}

function findMatchPositions(source, target) {
	const sourceChars = Array.from(source);
	const targetChars = Array.from(target);
	const positions = [];
	let targetIndex = 0;
	for (let sourceIndex = 0; sourceIndex < sourceChars.length && targetIndex < targetChars.length; sourceIndex++) {
		if (sourceChars[sourceIndex] === targetChars[targetIndex]) {
			positions.push(sourceIndex);
			targetIndex++
		}
	}
	return targetIndex === targetChars.length ? positions : null
}

function appendMatchedText(container, path, name, match) {
	const bEle = document.createElement("b");
	bEle.innerText = path;
	container.append(bEle);
	if (match.length === 0) {
		container.append(document.createTextNode(name));
		return
	}
	const positions = [...new Set(match)].sort((a, b) => a - b);
	let textStart = 0;
	let rangeStart = positions[0];
	let rangeEnd = positions[0];
	const appendRange = (start, end) => {
		if (textStart < start) {
			container.append(document.createTextNode(name.slice(textStart, start)))
		}
		const bold = document.createElement("b");
		bold.textContent = name.slice(start, end + 1);
		container.append(bold);
		textStart = end + 1
	};
	for (let i = 1; i < positions.length; i++) {
		const position = positions[i];
		if (position === rangeEnd + 1) {
			rangeEnd = position
		} else {
			appendRange(rangeStart, rangeEnd);
			rangeStart = position;
			rangeEnd = position
		}
	}
	appendRange(rangeStart, rangeEnd);
	if (textStart < name.length) {
		container.append(document.createTextNode(name.slice(textStart)))
	}
}

function showPrompt() {
	suggests = [];
	suggestsPos = -1;
	commandFeedbackEle.innerHTML = "";
	if (!commandInputEle.value) {
		lastOutput.forEach(([command, feedback]) => {
			commandFeedbackEle.insertAdjacentText("beforeend", ">" + command);
			commandFeedbackEle.append(document.createElement("br"));
			commandFeedbackEle.insertAdjacentText("beforeend", feedback);
			commandFeedbackEle.append(document.createElement("br"));
			commandFeedbackEle.append(document.createElement("br"))
		})
	} else if (commandInputEle.value.startsWith("/")) {
		try {
			if (!commandTree) {
				return
			}
			const args = splitCommand(commandInputEle.value);
			let pos = commandTree.nodes[commandTree.root];
			path = "/";
			const last = args.length && !commandInputEle.value.endsWith(" ") ? args.pop() : "";
			let found = true;
			let id = 0;
			args.forEach(arg => {
				if (!found) {
					return
				}
				found = pos.requiredNext.some(node => {
					if (commandTree.nodes[node].type === "literal") {
						if (arg === commandTree.nodes[node].name) {
							id = node;
							pos = commandTree.nodes[node];
							path += `${pos.name} `;
							return true
						}
					} else if ((commandTree.nodes[node].values ?? -1) != -1) {
						if (commandTree.values[commandTree.nodes[node].values].some(val => val === arg)) {
							id = node;
							pos = commandTree.nodes[node];
							path += `<${pos.name}:${pos.argumentType}> `;
							return true
						}
					} else {
						id = node;
						pos = commandTree.nodes[node];
						path += `<${pos.name}:${pos.argumentType}> `;
						return true
					}
				}) || pos.next.some(node => {
					if (commandTree.nodes[node].type === "literal") {
						if (arg === commandTree.nodes[node].name) {
							id = node;
							pos = commandTree.nodes[node];
							path += `${pos.name} `;
							return true
						}
					} else if ((commandTree.nodes[node].values ?? -1) != -1) {
						if (commandTree.values[commandTree.nodes[node].values].some(val => val === arg)) {
							id = node;
							pos = commandTree.nodes[node];
							path += `[${pos.name}:${pos.argumentType}] `;
							return true
						}
					} else {
						switch (commandTree.nodes[node].argumentType) {
							case "coordinate":
								if (/^(\^|~)?-?(\d+(\.\d*)?|\.\d+)$/.test(arg)) {
									break
								}
								return false;
							case "integer":
							case "long":
							case "ints":
								if (/^-?\d+$/.test(arg)) {
									break
								}
								return false;
							case "float":
							case "double":
								if (/^-?(\d+(\.\d*)?|\.\d+)$/.test(arg)) {
									break
								}
								return false;
							case "uuid":
								if (/^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/.test(arg)) {
									break
								}
								return false
						}
						id = node;
						pos = commandTree.nodes[node];
						path += `[${pos.name}:${pos.argumentType}] `;
						return true
					}
				})
			});
			const realPath = "/" + args.join(" ") + (args.length ? " " : "");
			if (!found) {
				const bEle = document.createElement("b");
				bEle.innerText = "command not found";
				commandFeedbackEle.append(bEle);
				return
			}
			if (path !== "/") {
				const bEle = document.createElement("b");
				bEle.innerText = path;
				commandFeedbackEle.append(bEle);
				commandFeedbackEle.append(document.createElement("hr"))
			}
			let haveSuggest = false;
			if (!last && (pos.next.length || !pos.requiredNext.length)) {
				haveSuggest = true;
				const bEle = document.createElement("b");
				bEle.innerText = realPath;
				commandFeedbackEle.append(bEle);
				commandFeedbackEle.append(document.createElement("br"))
			}
			pos.requiredNext.forEach(index => {
				if (commandTree.nodes[index].type !== "literal") {
					haveSuggest = true;
					const bEle = document.createElement("b");
					bEle.innerText = realPath;
					commandFeedbackEle.append(bEle);
					commandFeedbackEle.insertAdjacentText("beforeend", `<${commandTree.nodes[index].name}:${commandTree.nodes[index].argumentType}>`);
					commandFeedbackEle.append(document.createElement("br"))
				}
			});
			pos.next.forEach(index => {
				if (commandTree.nodes[index].type !== "literal") {
					haveSuggest = true;
					const bEle = document.createElement("b");
					bEle.innerText = realPath;
					commandFeedbackEle.append(bEle);
					commandFeedbackEle.insertAdjacentText("beforeend", `[${commandTree.nodes[index].name}:${commandTree.nodes[index].argumentType}]`);
					commandFeedbackEle.append(document.createElement("br"))
				}
			});
			const suggest = [...pos.requiredNext, ...pos.next].flatMap(index => {
				if (commandTree.nodes[index].type === "literal") {
					return commandTree.nodes[index].name
				} else if ((commandTree.nodes[index].values ?? -1) !== -1) {
					return commandTree.values[commandTree.nodes[index].values]
				}
				switch (commandTree.nodes[index].argumentType) {
					case "target":
						return ["@p", "@r", "@a", "@e", "@s", "@n", ...getPlayers()];
					case "gameProfile":
						return getPlayers()
				}
				return []
			}).map(name => {
				if (last) {
					const match = findMatchPositions(name, last);
					if (match) {
						return [match.join(""), match, name]
					}
					return null
				}
				return ["", [], name]
			}).filter(Boolean).sort((a, b) => {
				const firstResult = a[0].localeCompare(b[0]);
				if (firstResult !== 0) {
					return firstResult
				}
				return a[2].localeCompare(b[2])
			}).map(([matchStr, match, name]) => [match, name]);
			if (haveSuggest && suggest.length) {
				commandFeedbackEle.append(document.createElement("hr"))
			}
			suggest.forEach(([match, name]) => {
				appendMatchedText(commandFeedbackEle, realPath, name, match);
				commandFeedbackEle.append(document.createElement("br"))
			});
			suggests = suggest.map(([match, name]) => realPath + name);
			if (!suggest.length && !haveSuggest) {
				const bEle = document.createElement("b");
				bEle.innerText = "command not found";
				commandFeedbackEle.append(bEle)
			}
		} catch (e) {
			const bEle = document.createElement("b");
			bEle.innerText = e.message;
			commandFeedbackEle.append(bEle);
			console.error(e)
		}
	} else {
		const bEle = document.createElement("b");
		bEle.innerText = "/say ";
		commandFeedbackEle.append(bEle);
		commandFeedbackEle.insertAdjacentText("beforeend", commandInputEle.value)
	}
}

function show() {
	if (nowHistory.length === 0) {
		nowHistory = [""]
	}
	if (nowpos < 0) {
		nowpos = 0
	}
	if (nowpos >= nowHistory.length) {
		nowpos = nowHistory.length - 1
	}
	commandInputEle.value = nowHistory[nowpos] || ""
}

function setHistory(cmd) {
	if (nowHistory.length === 0) {
		nowHistory = [""]
	}
	if (nowpos < 0) {
		nowpos = 0
	}
	if (nowpos >= nowHistory.length) {
		nowpos = nowHistory.length - 1
	}
	nowHistory[nowpos] = cmd;
	show()
}

function up() {
	if (nowHistory.length === 0) {
		nowHistory = [""]
	}
	if (nowpos < 0) {
		nowpos = 0
	}
	if (nowpos >= nowHistory.length) {
		nowpos = nowHistory.length - 1
	}
	if (nowpos > 0) {
		nowpos--
	}
	show();
	showPrompt()
}

function down() {
	if (nowHistory.length === 0) {
		nowHistory = [""]
	}
	if (nowpos < 0) {
		nowpos = 0
	}
	if (nowpos >= nowHistory.length) {
		nowpos = nowHistory.length - 1
	}
	if (nowpos < nowHistory.length - 1) {
		nowpos++
	}
	show();
	showPrompt()
}

function addHistory(cmd) {
	if (history.at?.(-1) !== cmd) {
		history.push(cmd)
	}
	if (history.length > 100) {
		history.shift()
	}
	nowHistory = [...history, ""];
	nowpos = nowHistory.length - 1
}

function clearHistory() {
	history.length = 0;
	nowHistory = [""];
	nowpos = 0;
	commandInputEle.value = ""
}
let nowKey = null;
let keyinterval = null;
commandInputEle.addEventListener("keydown", async e => {
	if (e.repeat) {
		if (e.code === "ArrowUp" || e.code === "ArrowDown" || e.code === "Escape") {
			e.preventDefault();
			e.stopImmediatePropagation()
		}
		return
	}
	if (e.code === "Tab") {
		e.preventDefault();
		e.stopImmediatePropagation();
		if (!suggests.length) {
			return
		}
		if (suggestsPos === -1) {
			suggestsPos = 0
		} else if (e.shiftKey) {
			suggestsPos--;
			if (suggestsPos < 0) {
				suggestsPos = suggests.length - 1
			}
		} else {
			suggestsPos++;
			if (suggestsPos >= suggests.length) {
				suggestsPos = 0
			}
		}
		commandInputEle.value = suggests[suggestsPos]
	} else if (e.code === "ArrowUp" || e.code === "ArrowDown") {
		e.preventDefault();
		e.stopImmediatePropagation();
		nowKey = e.code;
		clearInterval(keyinterval);
		if (e.code === "ArrowUp") {
			keyinterval = setInterval(up, 300);
			up()
		} else {
			keyinterval = setInterval(down, 300);
			down()
		}
	} else if (e.code === "Escape") {
		e.preventDefault();
		e.stopImmediatePropagation();
		nowKey = null;
		if (keyinterval) {
			clearInterval(keyinterval);
			keyinterval = null
		}
		nowpos = nowHistory.length - 1;
		setHistory("")
	} else if (e.code === "Enter" && !e.isComposing) {
		let cmd = e.target.value;
		if (!cmd.includes("\n")) {
			e.preventDefault();
			e.stopImmediatePropagation();
			setHistory("");
			cmd = cmd.trim();
			if (cmd !== "") {
				addHistory(cmd);
				try {
					const response = await fetch("/api", {
						method: "POST",
						headers: {
							"Content-Type": "application/json"
						},
						body: JSON.stringify({
							type: "run-command",
							data: [commandForServer(cmd)]
						})
					});
					if (response.ok) {
						const result = await response.json();
						if (Array.isArray(result)) {
							lastOutput = [
								[cmd, result?.[0] || ""]
							]
						}
					} else {
						showMessage(`Server error: ${response.status}` + (response.statusText ? " " + response.statusText : ""));
						lastOutput = []
					}
				} catch (error) {
					console.error(error);
					showMessage(error);
					lastOutput = []
				}
			}
			showPrompt()
		}
	}
}, true);
commandInputEle.addEventListener("keyup", e => {
	if (e.repeat) {
		if (e.code === "ArrowUp" || e.code === "ArrowDown" || e.code === "Escape") {
			e.preventDefault();
			e.stopImmediatePropagation()
		}
		return
	}
	if (e.code === "ArrowUp" || e.code === "ArrowDown") {
		if (e.code === nowKey) {
			nowKey = null;
			clearInterval(keyinterval);
			keyinterval = null
		}
		e.preventDefault();
		e.stopImmediatePropagation()
	}
	if (e.code === "Escape") {
		e.preventDefault();
		e.stopImmediatePropagation()
	}
}, true);
commandInputEle.addEventListener("blur", () => {
	if (nowKey) {
		nowKey = null;
		clearInterval(keyinterval);
		keyinterval = null
	}
});
commandInputEle.addEventListener("paste", async e => {
	const pasted = e.clipboardData.getData("text/plain");
	if (/[\r\n]/.test(pasted)) {
		e.preventDefault();
		e.stopImmediatePropagation();
		const val = commandInputEle.value + "\n" + pasted;
		setHistory("");
		const line = val.replace(/\r/g, "\n").split("\n").map(item => item.trim()).filter(Boolean);
		if (line.length) {
			line.forEach(addHistory);
			try {
				const response = await fetch("/api", {
					method: "POST",
					headers: {
						"Content-Type": "application/json"
					},
					body: JSON.stringify({
						type: "run-command",
						data: line.map(commandForServer)
					})
				});
				if (response.ok) {
					const result = await response.json();
					if (Array.isArray(result)) {
						lastOutput = line.reduce((arr, item, index) => {
							const resultItem = result[index] ?? "";
							result[index] = resultItem;
							arr.push([item, resultItem]);
							return arr
						}, [])
					}
				} else {
					showMessage(`Server error: ${response.status}` + (response.statusText ? " " + response.statusText : ""));
					lastOutput = []
				}
			} catch (error) {
				console.error(error);
				showMessage(error);
				lastOutput = []
			}
		}
		showPrompt()
	}
});
commandInputEle.addEventListener("input", async () => {
	let val = commandInputEle.value;
	const v2 = val.replace(/\r/g, "");
	if (val !== v2) {
		commandInputEle.value = v2;
		val = v2
	}
	const line = val.split("\n").map(item => item.trim()).filter(Boolean);
	const last = line.pop();
	if (last !== val.trim()) {
		setHistory(last)
	}
	if (line.length) {
		line.forEach(addHistory);
		try {
			const response = await fetch("/api", {
				method: "POST",
				headers: {
					"Content-Type": "application/json"
				},
				body: JSON.stringify({
					type: "run-command",
					data: line
				})
			});
			if (response.ok) {
				const result = await response.json();
				if (Array.isArray(result)) {
					lastOutput = line.reduce((arr, item, index) => {
						const resultItem = result[index] ?? "";
						result[index] = resultItem;
						arr.push([item, resultItem]);
						return arr
					}, [])
				}
			} else {
				showMessage(`Server error: ${response.status}` + (response.statusText ? " " + response.statusText : ""));
				lastOutput = []
			}
		} catch (error) {
			console.error(error);
			showMessage(error);
			lastOutput = []
		}
	}
	showPrompt()
}, true);
