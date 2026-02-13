const fs = require("fs");
const path = require("path");
const {
	execSync
} = require("child_process");
const root = process.cwd();
const excluded = [".git", ".github", "docs", "node_modules", "scripts", "gradle"];
const dataDir = path.join(root, "docs");
if (!fs.existsSync(dataDir)) {
	fs.mkdirSync(dataDir, {
		recursive: true
	})
}
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
	needsUpdate: false
};
const dirs = fs.readdirSync(root).filter(d => !excluded.includes(d) && fs.existsSync(path.join(root, d, "src", "main", "resources", "fabric.mod.json")) && fs.existsSync(path.join(root, d, "build.gradle")));
let hasError = false;
for (const dir of dirs) {
	try {
		console.log(`Processing ${dir}...`);
		const extensionsDir = path.join(dataDir, dir);
		const extPath = path.join(root, dir);
		const statusPath = path.join(extPath, "status.json");
		let status = {};
		try {
			status = JSON.parse(fs.readFileSync(statusPath, "utf8"))
		} catch {
			console.warn(`${dir}: invalid or missing status.json, using default.`)
		}
		if (status?.needsUpdate) {
			fs.mkdirSync(extensionsDir, {
				recursive: true
			});
			const settingPath = path.join(extPath, "build.gradle");
			console.log(fs.readFileSync(settingPath, "utf8")?.match(/^version = "(.+)"/));
			const version = fs.readFileSync(settingPath, "utf8")?.match(/^version = "(.+)"/)?.[1];
			if (!version) {
				hasError = true;
				console.warn(`${dir}: Version not found.`);
				continue
			}
			let hasIcon = false;
			const iconPath = path.join(extPath, "src", "main", "resources", "assets", dir, "icon.png");
			if (fs.existsSync(iconPath)) {
				const targetPath = path.join(extensionsDir, "icon.png");
				fs.copyFileSync(iconPath, targetPath);
				console.log(`Icon copied: ${iconPath} -> ${targetPath}`);
				hasIcon = true
			} else {
				console.warn(`icon.png not found for ${dir}`)
			}
			const readmePath = path.join(extPath, "README.md");
			if (fs.existsSync(readmePath)) {
				const targetPath = path.join(extensionsDir, "README.md");
				fs.copyFileSync(readmePath, targetPath);
				console.log(`README copied: ${readmePath} -> ${targetPath}`)
			} else {
				console.warn(`README.md not found for ${dir}`)
			}
			execSync(`../gradlew build`, {
				cwd: extPath,
				stdio: "inherit"
			});
			const exportPath = path.join(extPath, "build", "libs", `${dir}-${version}.jar`);
			if (fs.existsSync(exportPath)) {
				const jarDir = path.join(extensionsDir, "jars");
				fs.mkdirSync(jarDir, {
					recursive: true
				});
				const targetPath = path.join(jarDir, `${dir}-${version}.jar`);
				fs.copyFileSync(exportPath, targetPath);
				console.log(`README copied: ${exportPath} -> ${targetPath}`)
			} else {
				hasError = true;
				console.warn(`Exported jar not found`);
				continue
			}
			const manifestPath = path.join(extPath, "src", "main", "resources", "fabric.mod.json");
			const manifest = JSON.parse(fs.readFileSync(manifestPath, "utf8"));
			const displayName = manifest.name;
			const description = manifest.description || "";
			const isNew = !versions[dir];
			if (isNew) {
				console.log(`New mod detected: ${dir}`);
				versions[dir] = {
					description,
					displayName,
					link: "",
					versions: [version],
					hasIcon
				}
			} else {
				if (!Array.isArray(versions[dir].versions)) {
					versions[dir].versions = []
				}
				if (versions[dir].versions.at(-1) !== version) {
					versions[dir].versions.push(version)
				}
				versions[dir].hasIcon = hasIcon;
				versions[dir].displayName = displayName;
				versions[dir].description = description
			}
			console.log(`${dir} built successfully.`)
		} else {
			console.log(`Skip ${dir}: no new content`)
		}
		fs.writeFileSync(statusPath, JSON.stringify(defaultStatus, null, "\t") + "\n", "utf8")
	} catch (err) {
		hasError = true;
		console.error(`Failed processing ${dir}: ${err.message}`)
	}
}
fs.writeFileSync(versionsPath, JSON.stringify(versions) + "\n", "utf8");
if (hasError) {
	process.exit(1)
}
