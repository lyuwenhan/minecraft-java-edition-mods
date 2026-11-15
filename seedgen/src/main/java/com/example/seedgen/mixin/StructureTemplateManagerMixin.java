package com.example.seedgen.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(StructureTemplateManager.class)
abstract class StructureTemplateManagerMixin {
	@Redirect(
			method = "<init>",
			at =
					@At(
							value = "INVOKE",
							target =
									"Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;getLevelPath(Lnet/minecraft/world/level/storage/LevelResource;)Ljava/nio/file/Path;"))
	private Path seedgen$allowResourceOnlyTemplates(
			LevelStorageSource.LevelStorageAccess storage, LevelResource resource) {
		if (storage != null) {
			return storage.getLevelPath(resource);
		}
		// Seedgen only reads built-in data-pack templates. Point the unused world-template
		// source below a regular file so it remains non-existent without creating a directory.
		return Minecraft.getInstance()
				.gameDirectory
				.toPath()
				.resolve("options.txt")
				.resolve("seedgen-resource-only-templates");
	}
}
