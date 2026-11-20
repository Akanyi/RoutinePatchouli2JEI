package dev.lans.routinepatchouli2jei.jei;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import vazkii.patchouli.client.book.BookIcon;

public record PatchouliEntryRecipe(Identifier bookId, Identifier entryId, int page, ItemStack stack,
                                   Component bookName, Component entryName, BookIcon bookIcon) {
    public PatchouliEntryRecipe {
        stack = stack.copy();
        stack.setCount(1);
    }
}
