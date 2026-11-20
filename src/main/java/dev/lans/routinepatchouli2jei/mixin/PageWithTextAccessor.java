package dev.lans.routinepatchouli2jei.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.client.book.page.abstr.PageWithText;

@Mixin(value = PageWithText.class, remap = false)
public interface PageWithTextAccessor {
    @Accessor("text")
    IVariable routinepatchouli2jei$getText();
}
