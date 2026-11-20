package dev.lans.routinepatchouli2jei.jei;

import dev.lans.routinepatchouli2jei.client.JeiBridge;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.api.PatchouliAPI;

public final class PatchouliEntryCategory extends AbstractRecipeCategory<PatchouliEntryRecipe> {
    public static final IRecipeType<PatchouliEntryRecipe> TYPE = IRecipeType.create(
            Identifier.fromNamespaceAndPath("routinepatchouli2jei", "patchouli_entry"), PatchouliEntryRecipe.class);

    // 布局常量
    private static final int WIDTH = 160;
    private static final int HEIGHT = 22;
    private static final int TEXT_X = 22;
    private static final int BUTTON_SIZE = 18;
    private static final int BUTTON_X = WIDTH - BUTTON_SIZE - 2;
    private static final int TEXT_WIDTH = BUTTON_X - TEXT_X - 4;

    public PatchouliEntryCategory(IGuiHelper gui) {
        super(TYPE, Component.translatable("category.routinepatchouli2jei.patchouli.title"),
                gui.createDrawableItemStack(new ItemStack(PatchouliAPI.get().getBookItem().value())), WIDTH, HEIGHT);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, PatchouliEntryRecipe recipe,
                          mezz.jei.api.recipe.IFocusGroup focuses) {
        // 左上角只显示一个物品槽；输出角色用不可见原料保留，保证 R / U 两种查询都能找到该条目
        builder.addInputSlot(1, 1).setStandardSlotBackground().add(recipe.stack());
        builder.addInvisibleIngredients(RecipeIngredientRole.OUTPUT).add(recipe.stack());
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, PatchouliEntryRecipe recipe,
                                  mezz.jei.api.recipe.IFocusGroup focuses) {
        // 物品槽右侧：第一行条目名，第二行所属手册（灰色）
        builder.addText(recipe.entryName(), TEXT_WIDTH, 10).setPosition(TEXT_X, 1);
        builder.addText(recipe.bookName().copy().withStyle(ChatFormatting.GRAY), TEXT_WIDTH, 10)
                .setPosition(TEXT_X, 11);

        // 右上角打开按钮
        builder.addButton(BUTTON_SIZE, BUTTON_SIZE,
                        () -> JeiBridge.openBookEntryFromJei(recipe.bookId(), recipe.entryId(), recipe.page()))
                .setMessage(Component.translatable("gui.routinepatchouli2jei.open_patchouli"))
                .setIcon(new PatchouliBookIconDrawable(recipe.bookIcon()))
                .setPosition(BUTTON_X, 2)
                .setTooltip(Component.translatable("gui.routinepatchouli2jei.open_patchouli.tooltip"));
    }

    public static Identifier recipeId(PatchouliEntryRecipe recipe) {
        var itemId = BuiltInRegistries.ITEM.getKey(recipe.stack().getItem());
        var path = "entry/" + recipe.bookId().getNamespace() + "/" + recipe.bookId().getPath()
                + "/" + recipe.entryId().getNamespace() + "/" + recipe.entryId().getPath()
                + "/" + recipe.page() + "/" + itemId.getNamespace() + "/" + itemId.getPath()
                + "/" + Integer.toUnsignedString(recipe.stack().hashCode(), 16);
        return Identifier.fromNamespaceAndPath("routinepatchouli2jei", path);
    }

    @Override
    public Identifier getIdentifier(PatchouliEntryRecipe recipe) {
        return recipeId(recipe);
    }
}
