package dev.lans.routinepatchouli2jei.mixin;

import dev.lans.routinepatchouli2jei.client.JeiBridge;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vazkii.patchouli.client.book.gui.GuiBook;

@Mixin(value = GuiBook.class, remap = false)
public abstract class GuiBookMixin {
    @Shadow private ItemStack tooltipStack;

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void routinepatchouli2jei$bridgeRecipeLookup(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (JeiBridge.handleRecipeKeybind(event, tooltipStack)) cir.setReturnValue(true);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void routinepatchouli2jei$bookmarkHoveredItem(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (JeiBridge.handleBookmarkKeybind(event, tooltipStack)) cir.setReturnValue(true);
    }
}
