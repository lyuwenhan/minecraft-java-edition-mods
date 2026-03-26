import {
	unified
} from "unified";
import remarkParse from "remark-parse";
import remarkGfm from "remark-gfm";
export function markdownToBBCode(markdown) {
	if (typeof markdown !== "string") {
		throw new TypeError(`markdownToBBCode expected a string, got ${typeof markdown}`)
	}
	const tree = unified().use(remarkParse).use(remarkGfm).parse(markdown.replace(/\r\n/g, "\n"));
	return renderNodes(tree.children).trim() + "\n"
}

function renderNodes(nodes) {
	return nodes.map(renderNode).join("")
}

function renderInline(nodes) {
	return nodes.map(renderNode).join("")
}

function renderNode(node) {
	switch (node.type) {
		case "heading": {
			const text = renderInline(node.children).trim();
			return `[h${node.depth}]${text}[/h${node.depth}]`
		}
		case "paragraph": {
			return `${renderInline(node.children).trim()}\n`
		}
		case "text": {
			return node.value
		}
		case "strong": {
			return `[b]${renderInline(node.children)}[/b]`
		}
		case "inlineCode": {
			return `[b]${node.value}[/b]`
		}
		case "link": {
			const text = renderInline(node.children);
			return `[url=${node.url}]${text}[/url]`
		}
		case "list": {
			const items = node.children.map(item => renderListItem(item)).join("");
			return `[list]\n${items}[/list]`
		}
		case "listItem": {
			return renderListItem(node)
		}
		case "code": {
			return `[code]${node.value}[/code]\n`
		}
		case "table": {
			return renderTable(node)
		}
		case "blockquote": {
			const content = renderNodes(node.children).trim();
			return `[quote]\n${content}\n[/quote]\n`
		}
		case "thematicBreak": {
			return `[hr]\n`
		}
		case "break": {
			return "\n"
		}
		case "em": {
			return renderInline(node.children)
		}
		case "delete": {
			return renderInline(node.children)
		}
		case "root": {
			return renderNodes(node.children)
		}
		default: {
			if (node.children) {
				return renderInline(node.children)
			}
			return ""
		}
	}
}

function renderListItem(node) {
	let content = "";
	for (const child of node.children) {
		if (child.type === "paragraph") {
			content += renderInline(child.children).trim()
		} else if (child.type === "list") {
			content += `\n${renderNode(child).trim()}`
		} else {
			content += renderNode(child).trim()
		}
	}
	return `[*]${content}[/*]\n`
}

function renderTable(tableNode) {
	let result = "[table]\n[tbody]\n";
	const rows = tableNode.children || [];
	rows.forEach((row, rowIndex) => {
		result += "[tr]\n";
		const cells = row.children || [];
		const tag = rowIndex === 0 ? "th" : "td";
		for (const cell of cells) {
			const content = renderInline(cell.children || []).trim();
			result += `[${tag}]${content}[/${tag}]\n`
		}
		result += "[/tr]\n"
	});
	result += "[/tbody]\n[/table]\n";
	return result
}
