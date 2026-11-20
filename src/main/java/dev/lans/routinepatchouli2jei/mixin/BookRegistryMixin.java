package dev.lans.routinepatchouli2jei.mixin;

import dev.lans.routinepatchouli2jei.client.RoutineJeiPlugin;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vazkii.patchouli.common.book.BookRegistry;

@Mixin(value = BookRegistry.class, remap = false)
public abstract class BookRegistryMixin {
    @Inject(method = "reloadContents", at = @At("TAIL"))
    private void routinepatchouli2jei$refreshJeiEntries(Level level, CallbackInfo ci) {
        if (level != null && level.isClientSide()) RoutineJeiPlugin.refreshPatchouliEntries();
    }
}
