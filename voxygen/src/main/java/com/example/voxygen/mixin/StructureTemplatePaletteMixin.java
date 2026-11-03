package com.example.voxygen.mixin;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;
import java.util.function.Function;

@Mixin(StructureTemplate.Palette.class)
abstract class StructureTemplatePaletteMixin {
	@Redirect(
			method = "blocks(Lnet/minecraft/world/level/block/Block;)Ljava/util/List;",
			at =
					@At(
							value = "INVOKE",
							target =
									"Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
	private Object voxygen$synchronizePaletteCache(
			Map<Object, Object> cache, Object key, Function<Object, Object> factory) {
		synchronized (cache) {
			return cache.computeIfAbsent(key, factory);
		}
	}
}
