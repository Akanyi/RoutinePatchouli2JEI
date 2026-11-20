package dev.lans.routinepatchouli2jei.client;

import dev.lans.routinepatchouli2jei.RoutinePatchouli2JEI;
import dev.lans.routinepatchouli2jei.jei.PatchouliEntryCategory;
import dev.lans.routinepatchouli2jei.jei.PatchouliEntryRecipe;
import dev.lans.routinepatchouli2jei.mixin.PageDoubleRecipeAccessor;
import dev.lans.routinepatchouli2jei.mixin.PageSpotlightAccessor;
import dev.lans.routinepatchouli2jei.mixin.PageWithTextAccessor;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.FurnaceRecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapedCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.crafting.display.StonecutterRecipeDisplay;
import com.google.gson.JsonElement;
import vazkii.patchouli.client.book.BookEntry;
import vazkii.patchouli.client.book.BookContents;
import vazkii.patchouli.client.book.BookPage;
import vazkii.patchouli.client.book.page.PageSpotlight;
import vazkii.patchouli.client.book.page.abstr.PageDoubleRecipe;
import vazkii.patchouli.client.book.page.abstr.PageWithText;
import vazkii.patchouli.common.book.Book;
import vazkii.patchouli.common.book.BookRegistry;
import vazkii.patchouli.common.util.ItemStackUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@JeiPlugin
public final class RoutineJeiPlugin implements IModPlugin {
    private static final Set<Identifier> registeredEntryIds = new HashSet<>();
    private static IJeiRuntime runtime;

    @Override
    public Identifier getPluginUid() {
        return Identifier.fromNamespaceAndPath(RoutinePatchouli2JEI.ID, "patchouli_bookmarks");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper gui = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(new PatchouliEntryCategory(gui));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<PatchouliEntryRecipe> recipes = collectEntries(registration.getIngredientManager().getAllItemStacks());
        registeredEntryIds.clear();
        recipes.stream().map(PatchouliEntryCategory::recipeId).forEach(registeredEntryIds::add);
        registration.addRecipes(PatchouliEntryCategory.TYPE, recipes);
    }

    private static List<PatchouliEntryRecipe> collectEntries(Collection<ItemStack> jeiStacks) {
        Map<ItemStackUtil.StackWrapper, List<PatchouliEntryRecipe>> byStack = new LinkedHashMap<>();
        Map<ItemStackUtil.StackWrapper, ItemStack> indexedStacks = new LinkedHashMap<>();
        for (ItemStack stack : jeiStacks) {
            if (!stack.isEmpty()) indexedStacks.putIfAbsent(ItemStackUtil.wrapStack(stack), stack);
        }
        Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> aliases = createItemAliases(indexedStacks);

        for (Book book : BookRegistry.INSTANCE.books.values()) {
            BookContents contents = book.getContents();
            if (contents == null) continue;
            for (BookEntry entry : contents.entries.values()) {
                if (entry.shouldHide()) continue;
                var mentions = new LinkedHashSet<ItemStackUtil.StackWrapper>();
                var text = new ArrayList<String>();
                text.add(entry.getName().getString());
                for (BookPage page : entry.getPages()) {
                    if (page.sourceObject != null) collectStrings(page.sourceObject, text);
                    if (page instanceof PageWithText textPage) {
                        var variable = ((PageWithTextAccessor) (Object) textPage).routinepatchouli2jei$getText();
                        if (variable != null) {
                            var component = variable.as(net.minecraft.network.chat.Component.class);
                            if (component != null) text.add(component.getString());
                        }
                    }
                    if (page instanceof PageSpotlight spotlight) {
                        for (ItemStack stack : ((PageSpotlightAccessor) (Object) spotlight).routinepatchouli2jei$getStacks()) {
                            if (!stack.isEmpty()) mentions.add(ItemStackUtil.wrapStack(stack));
                        }
                    }
                    if (page instanceof PageDoubleRecipe<?> recipePage) {
                        PageDoubleRecipeAccessor recipe = (PageDoubleRecipeAccessor) (Object) recipePage;
                        ContextMap context = recipe.routinepatchouli2jei$getContext();
                        if (context != null) {
                            addRecipeMentions(recipe.routinepatchouli2jei$getFirstRecipe(), context, mentions);
                            addRecipeMentions(recipe.routinepatchouli2jei$getSecondRecipe(), context, mentions);
                        }
                    }
                }
                addTextMentions(text, aliases, mentions);
                for (ItemStackUtil.StackWrapper mention : mentions) {
                    Component bookName = book.i18n ? Component.translatable(book.name) : Component.literal(book.name);
                    byStack.computeIfAbsent(mention, ignored -> new ArrayList<>())
                            .add(new PatchouliEntryRecipe(book.id, entry.getId(), 0, mention.stack, bookName,
                                    entry.getName(), book.getIcon()));
                }
            }
        }

        List<PatchouliEntryRecipe> recipes = new ArrayList<>();
        for (ItemStackUtil.StackWrapper key : indexedStacks.keySet()) {
            recipes.addAll(byStack.getOrDefault(key, List.of()));
        }
        return recipes;
    }

    private static Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> createItemAliases(
            Map<ItemStackUtil.StackWrapper, ItemStack> stacks) {
        Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> aliases = new LinkedHashMap<>();
        Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> paths = new LinkedHashMap<>();
        Map<String, LinkedHashSet<Identifier>> pathIds = new LinkedHashMap<>();
        for (var entry : stacks.entrySet()) {
            var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(entry.getValue().getItem());
            addAlias(aliases, id.toString(), entry.getKey());
            paths.computeIfAbsent(id.getPath(), ignored -> new LinkedHashSet<>()).add(entry.getKey());
            pathIds.computeIfAbsent(id.getPath(), ignored -> new LinkedHashSet<>()).add(id);
            addDisplayNameAliases(aliases, entry.getValue().getHoverName().getString(), entry.getKey());
        }
        paths.forEach((path, matches) -> {
            if (pathIds.get(path).size() == 1) matches.forEach(stack -> addAlias(aliases, path, stack));
        });
        return aliases;
    }

    private static void addDisplayNameAliases(Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> aliases,
                                              String name, ItemStackUtil.StackWrapper stack) {
        addAlias(aliases, name, stack);
        if (!net.minecraft.client.Minecraft.getInstance().getLanguageManager().getSelected().startsWith("en_")) return;

        String normalized = normalizeText(name);
        int wordStart = normalized.lastIndexOf(' ') + 1;
        String word = normalized.substring(wordStart);
        if (word.length() < 2) return;
        String plural;
        if (word.endsWith("y") && word.length() > 2 && !"aeiou".contains(word.substring(word.length() - 2, word.length() - 1))) {
            plural = word.substring(0, word.length() - 1) + "ies";
        } else if (word.endsWith("s") || word.endsWith("x") || word.endsWith("z")
                || word.endsWith("ch") || word.endsWith("sh")) {
            plural = word + "es";
        } else {
            plural = word + "s";
        }
        addAlias(aliases, normalized.substring(0, wordStart) + plural, stack);
    }

    private static void addAlias(Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> aliases,
                                 String alias, ItemStackUtil.StackWrapper stack) {
        String normalized = normalizeText(alias);
        if (normalized.length() > 1)
            aliases.computeIfAbsent(normalized, ignored -> new LinkedHashSet<>()).add(stack);
    }

    private static void addTextMentions(List<String> text,
                                        Map<String, LinkedHashSet<ItemStackUtil.StackWrapper>> aliases,
                                        LinkedHashSet<ItemStackUtil.StackWrapper> mentions) {
        for (String line : text) {
            String normalized = normalizeText(line);
            aliases.forEach((alias, stacks) -> {
                if (containsName(normalized, alias)) mentions.addAll(stacks);
            });
        }
    }

    private static String normalizeText(String text) {
        return text.strip().toLowerCase(java.util.Locale.ROOT).replaceAll("\\s+", " ");
    }

    private static boolean containsName(String text, String name) {
        int start = 0;
        while ((start = text.indexOf(name, start)) >= 0) {
            int end = start + name.length();
            if (containsCjk(name) || (!isWordCharacter(text, start - 1) && !isWordCharacter(text, end))) return true;
            start = end;
        }
        return false;
    }

    private static boolean containsCjk(String text) {
        return text.codePoints().anyMatch(codePoint -> {
            var script = Character.UnicodeScript.of(codePoint);
            return script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
                    || script == Character.UnicodeScript.KATAKANA || script == Character.UnicodeScript.HANGUL;
        });
    }

    private static boolean isWordCharacter(String text, int index) {
        return index >= 0 && index < text.length()
                && (Character.isLetterOrDigit(text.charAt(index)) || text.charAt(index) == '_');
    }

    private static void collectStrings(JsonElement element, List<String> strings) {
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            strings.add(element.getAsString());
        } else if (element.isJsonArray()) {
            element.getAsJsonArray().forEach(value -> collectStrings(value, strings));
        } else if (element.isJsonObject()) {
            element.getAsJsonObject().entrySet().forEach(entry -> collectStrings(entry.getValue(), strings));
        }
    }

    private static void addRecipeMentions(RecipeDisplay recipe, ContextMap context,
                                          LinkedHashSet<ItemStackUtil.StackWrapper> mentions) {
        if (recipe == null) return;
        if (recipe instanceof ShapedCraftingRecipeDisplay shaped) {
            shaped.ingredients().forEach(slot -> addSlotMentions(slot, context, mentions));
        } else if (recipe instanceof ShapelessCraftingRecipeDisplay shapeless) {
            shapeless.ingredients().forEach(slot -> addSlotMentions(slot, context, mentions));
        } else if (recipe instanceof FurnaceRecipeDisplay furnace) {
            addSlotMentions(furnace.ingredient(), context, mentions);
        } else if (recipe instanceof SmithingRecipeDisplay smithing) {
            addSlotMentions(smithing.template(), context, mentions);
            addSlotMentions(smithing.base(), context, mentions);
            addSlotMentions(smithing.addition(), context, mentions);
        } else if (recipe instanceof StonecutterRecipeDisplay stonecutter) {
            addSlotMentions(stonecutter.input(), context, mentions);
        }
        addSlotMentions(recipe.result(), context, mentions);
        addSlotMentions(recipe.craftingStation(), context, mentions);
    }

    private static void addSlotMentions(SlotDisplay slot, ContextMap context,
                                        LinkedHashSet<ItemStackUtil.StackWrapper> mentions) {
        for (ItemStack stack : slot.resolveForStacks(context)) {
            if (!stack.isEmpty()) mentions.add(ItemStackUtil.wrapStack(stack));
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        RoutineJeiPlugin.runtime = runtime;
        JeiBridge.setRuntime(runtime);
        refreshPatchouliEntries();
    }

    @Override
    public void onRuntimeUnavailable() {
        runtime = null;
        registeredEntryIds.clear();
        JeiBridge.setRuntime(null);
    }

    public static void refreshPatchouliEntries() {
        IJeiRuntime jei = runtime;
        if (jei == null) return;
        var recipes = collectEntries(jei.getIngredientManager().getAllItemStacks());
        var additions = recipes.stream()
                .filter(recipe -> registeredEntryIds.add(PatchouliEntryCategory.recipeId(recipe)))
                .toList();
        if (!additions.isEmpty()) jei.getRecipeManager().addRecipes(PatchouliEntryCategory.TYPE, additions);
    }
}
