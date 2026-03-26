const fs = require("fs");
const path = require("path");
const {
	execSync
} = require("child_process");
const root = process.cwd();
const dataDir = path.join(root, "data");
const distDir = path.join(dataDir, "dist");
const assetsDir = path.join(dataDir, "assets");
fs.mkdirSync(distDir, {
	recursive: true
});
fs.mkdirSync(assetsDir, {
	recursive: true
});
const nextStepPath = path.join(root, "next_steps.md");
const targetNextStepPath = path.join(dataDir, "next_steps.md");
fs.copyFileSync(nextStepPath, targetNextStepPath);
const versionsPath = path.join(dataDir, "versions.json");
let versions = {};
if (fs.existsSync(versionsPath)) {
	try {
		versions = JSON.parse(fs.readFileSync(versionsPath, "utf8"))
	} catch (e) {
		console.error("Invalid versions.json, resetting.")
	}
}
const defaultStatus = {
	needsUpdate: false,
	withPack: false
};
const excluded = [".git", ".github", "data", "node_modules", "scripts", "gradle"];
const dirs = fs.readdirSync(root).filter(d => !excluded.includes(d) && fs.existsSync(path.join(root, d, "src", "main", "resources", "fabric.mod.json")) && fs.existsSync(path.join(root, d, "build.gradle")));
(async () => {
	const {
		markdownToBBCode
	} = await import("./markdown-to-bbcode.mjs");
	let hasError = false;
	for (const dir of dirs) {
		try {
			console.log(`Processing ${dir}...`);
			const extensionsDir = path.join(assetsDir, dir);
			const extPath = path.join(root, dir);
			const statusPath = path.join(extPath, "status.json");
			let status = {};
			try {
				status = JSON.parse(fs.readFileSync(statusPath, "utf8"))
			} catch {
				console.warn(`${dir}: invalid or missing status.json, using default.`)
			}
			if (status.needsUpdate) {
				fs.mkdirSync(extensionsDir, {
					recursive: true
				});
				const settingPath = path.join(extPath, "build.gradle");
				const version = fs.readFileSync(settingPath, "utf8")?.match(/version = "(.+)"/)?.[1];
				if (!version) {
					hasError = true;
					console.warn(`${dir}: Version not found.`);
					continue
				}
				const readmePath = path.join(extPath, "README.md");
				if (fs.existsSync(readmePath)) {
					const readme = fs.readFileSync(readmePath, "utf8");
					const bbcode = markdownToBBCode(readme);
					fs.writeFileSync(path.join(extensionsDir, "README.bbcode"), bbcode, "utf8");
					console.log(`README converted to BBCode: ${readmePath} -> ${path.join(extensionsDir,"README.bbcode")}`)
				} else {
					console.warn(`README not found for ${dir}`)
				}
				let hasIcon = false;
				const iconPath = path.join(extPath, "src", "main", "resources", "assets", dir, "icon.png");
				if (fs.existsSync(iconPath)) {
					const targetPath = path.join(extensionsDir, "icon.png");
					fs.copyFileSync(iconPath, targetPath);
					console.log(`Icon copied: ${iconPath} -> ${targetPath}`);
					hasIcon = true
				} else {
					console.warn(`Icon not found for ${dir}`)
				}
				const files = fs.readdirSync(extPath);
				const readmeFiles = files.filter(name => /^README.*\.md$/.test(name));
				if (readmeFiles.length > 0) {
					const readmeDir = path.join(extensionsDir, "README");
					fs.rmSync(readmeDir, {
						recursive: true,
						force: true
					});
					fs.mkdirSync(readmeDir, {
						recursive: true
					});
					for (const file of readmeFiles) {
						const sourcePath = path.join(extPath, file);
						const targetPath = path.join(readmeDir, file);
						fs.copyFileSync(sourcePath, targetPath);
						console.log(`README copied: ${sourcePath} -> ${targetPath}`)
					}
				} else {
					console.warn(`README*.md not found for ${dir}`)
				}
				if (status.withPack) {
					execSync(`../gradlew build`, {
						cwd: extPath,
						stdio: "inherit"
					});
					const exportPath = path.join(extPath, "build", "libs", `${dir}-${version}.jar`);
					if (fs.existsSync(exportPath)) {
						const targetPath = path.join(distDir, `${dir}-${version}.jar`);
						fs.copyFileSync(exportPath, targetPath);
						console.log(`Exported jar copied: ${exportPath} -> ${targetPath}`)
					} else {
						hasError = true;
						console.warn(`Exported jar not found`);
						continue
					}
				}
				const manifestPath = path.join(extPath, "src", "main", "resources", "fabric.mod.json");
				const manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));
				const displayName = manifest.name;
				const description = manifest.description || "";
				if (!versions[dir]) {
					console.log(`New mod detected: ${dir}`);
					versions[dir] = {
						versions: [version],
						hasIcon,
						displayName,
						description,
						link: {}
					}
				} else {
					const v = versions[dir].versions ?? [];
					versions[dir] = {
						versions: status.withPack ? v.at(-1) === version ? v : [...v, version] : v,
						hasIcon: hasIcon ?? versions[dir].hasIcon,
						displayName: displayName ?? versions[dir].displayName,
						description: description ?? versions[dir].description,
						link: versions[dir].link ?? {}
					}
				}
				console.log(`${dir} built successfully.`)
			} else {
				console.log(`Skip ${dir}: no new content`)
			}
			fs.writeFileSync(statusPath, JSON.stringify(defaultStatus, null, "\t") + "\n", "utf8")
		} catch (err) {
			hasError = true;
			console.error(`Failed processing ${dir}: ${err.message}`);
			console.error(err.stack)
		}
	}
	fs.writeFileSync(versionsPath, JSON.stringify(versions, null, "\t") + "\n", "utf8");
	if (hasError) {
		process.exit(1)
	}
})();
