package dev.lans.routinepatchouli2jei.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.patchouli.client.book.page.PageSpotlight;

@Mixin(value = PageSpotlight.class, remap = false)
public interface PageSpotlightAccessor {
    @Accessor("stacks")
    ItemStack[] routinepatchouli2jei$getStacks();
}
