package dev.lans.routinepatchouli2jei.mixin;

import dev.lans.routinepatchouli2jei.client.JeiBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 从 JEI 打开的手册在关闭时，改为回到原来的 JEI 界面。 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow public @Nullable Screen screen;
    @Shadow private boolean clientLevelTeardownInProgress;

    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @Inject(method = "setScreen", at = @At("HEAD"), cancellable = true)
    private void routinepatchouli2jei$returnToJei(@Nullable Screen next, CallbackInfo ci) {
        if (clientLevelTeardownInProgress) return;
        Screen target = JeiBridge.onSetScreen(screen, next);
        if (target != null) {
            ci.cancel();
            setScreen(target);
        }
    }
}
