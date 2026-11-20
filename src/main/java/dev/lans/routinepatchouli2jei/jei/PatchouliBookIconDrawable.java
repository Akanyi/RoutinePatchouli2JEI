package dev.lans.routinepatchouli2jei.jei;

import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import vazkii.patchouli.client.book.BookIcon;

public record PatchouliBookIconDrawable(BookIcon icon) implements IDrawable {
    private static final int SIZE = 16;

    @Override
    public int getWidth() {
        return SIZE;
    }

    @Override
    public int getHeight() {
        return SIZE;
    }

    @Override
    public void draw(GuiGraphicsExtractor graphics, int x, int y) {
        icon.extractRenderState(graphics, x, y);
    }
}
