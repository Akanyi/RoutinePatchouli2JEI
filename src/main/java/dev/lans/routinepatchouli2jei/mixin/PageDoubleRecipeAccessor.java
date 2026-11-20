package dev.lans.routinepatchouli2jei.mixin;

import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipe;

@Mixin(value = PageDoubleRecipe.class, remap = false)
public interface PageDoubleRecipeAccessor {
    @Accessor("recipe1")
    RecipeDisplay routinepatchouli2jei$getFirstRecipe();

    @Accessor("recipe2")
    RecipeDisplay routinepatchouli2jei$getSecondRecipe();

    @Accessor("context")
    ContextMap routinepatchouli2jei$getContext();
}
