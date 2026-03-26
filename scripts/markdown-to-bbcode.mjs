import {
	unified
} from "unified";
import remarkParse from "remark-parse";
import remarkGfm from "remark-gfm";
export function markdownToBBCode(markdown) {
	const tree = unified().use(remarkParse).use(remarkGfm).parse(markdown);
	let result = renderNodes(tree.children).trim() + "\n";
	result = result.replace(/\n\n\n+/g, "\n\n").replace(/(\[\/h[1-6]\])\n\n/g, "$1\n");
	return result;
}

function renderNodes(nodes) {
	return nodes.map(renderNode).join("")
}

function renderInline(nodes) {
	return nodes.map(renderNode).join("")
}

function escapeBBCodeText(text) {
	return text.replace(/\r\n/g, "\n")
}

function renderNode(node) {
	switch (node.type) {
		case "heading": {
			const text = renderInline(node.children).trim();
			return `[h${node.depth}]${text}[/h${node.depth}]\n\n`
		}
		case "paragraph": {
			return `${renderInline(node.children).trim()}\n\n`
		}
		case "text": {
			return escapeBBCodeText(node.value)
		}
		case "strong": {
			return `[b]${renderInline(node.children)}[/b]`
		}
		case "inlineCode": {
			return `[b]${escapeBBCodeText(node.value)}[/b]`
		}
		case "link": {
			const text = renderInline(node.children);
			return `[url=${node.url}]${text}[/url]`
		}
		case "list": {
			const items = node.children.map(item => renderListItem(item)).join("");
			return `[list]\n${items}[/list]\n\n`
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
			return `[quote]\n${content}\n[/quote]\n\n`
		}
		case "thematicBreak": {
			return `[hr]\n\n`
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
	result += "[/tbody]\n[/table]\n\n";
	return result
}
