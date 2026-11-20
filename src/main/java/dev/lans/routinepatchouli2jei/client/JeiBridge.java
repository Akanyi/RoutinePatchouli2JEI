package dev.lans.routinepatchouli2jei.client;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.book.gui.GuiBook;

import java.util.List;

public final class JeiBridge {
    private static IJeiRuntime runtime;
    /** 从 JEI 跳转进手册时记下的 JEI 界面；手册关闭时回到这里。 */
    private static @Nullable Screen returnScreen;

    private JeiBridge() {}

    public static void setRuntime(IJeiRuntime value) {
        runtime = value;
        if (value == null) returnScreen = null;
    }

    /** JEI 标签里的“打开手册条目”按钮调用：记住当前 JEI 界面后再打开手册。 */
    public static void openBookEntryFromJei(Identifier bookId, Identifier entryId, int page) {
        Screen previous = Minecraft.getInstance().screen;
        returnScreen = previous instanceof GuiBook ? null : previous;
        PatchouliAPI.get().openBookEntry(bookId, entryId, page);
    }

    /**
     * 在 Minecraft#setScreen 开头调用。
     *
     * @return 需要改为打开的界面（即返回 JEI），不需要拦截时返回 null
     */
    public static @Nullable Screen onSetScreen(@Nullable Screen current, @Nullable Screen next) {
        Screen target = returnScreen;
        if (target == null) return null;

        // 手册内部翻页 / 进入其他条目：GuiBook 之间切换，保持返回目标
        if (next instanceof GuiBook) return null;

        // 正在关闭手册（ESC、物品栏键等）：改为回到 JEI
        if (next == null && current instanceof GuiBook) {
            returnScreen = null;
            return target;
        }

        // 其他情况（回到了 JEI、打开了别的界面、未成功进入手册就关闭等）：放弃返回目标
        if (next != target || !(current instanceof GuiBook)) returnScreen = null;
        return null;
    }

    public static boolean handleBookmarkKeybind(KeyEvent event, ItemStack stack) {
        IJeiRuntime jei = runtime;
        if (jei == null || stack == null || stack.isEmpty()
                || !jei.getKeyMappings().getBookmark().isActiveAndMatches(InputConstants.getKey(event))) {
            return false;
        }

        var ingredient = jei.getIngredientManager().createTypedIngredient(stack, true);
        if (ingredient.isEmpty()) return false;

        var bookmarks = jei.getBookmarkManager();
        if (bookmarks.contains(ingredient.get())) {
            message(bookmarks.remove(ingredient.get()) ? "unbookmarked" : "cannot_bookmark");
        } else {
            message(bookmarks.add(ingredient.get()) ? "bookmarked" : "cannot_bookmark");
        }
        return true;
    }

    public static boolean handleRecipeKeybind(KeyEvent event, ItemStack stack) {
        IJeiRuntime jei = runtime;
        if (jei == null || stack == null || stack.isEmpty()) return false;

        var key = InputConstants.getKey(event);
        RecipeIngredientRole role;
        if (jei.getKeyMappings().getShowRecipe().isActiveAndMatches(key)) role = RecipeIngredientRole.OUTPUT;
        else if (jei.getKeyMappings().getShowUses().isActiveAndMatches(key)) role = RecipeIngredientRole.INPUT;
        else return false;

        var ingredient = jei.getIngredientManager().createTypedIngredient(stack, true);
        if (ingredient.isEmpty()) return false;
        IFocus<?> focus = jei.getJeiHelpers().getFocusFactory().createFocus(role, ingredient.get());
        jei.getRecipesGui().show(List.of(focus));
        return true;
    }

    private static void message(String key) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.gui.setOverlayMessage(Component.translatable("routinepatchouli2jei." + key), false);
        }
    }
}
