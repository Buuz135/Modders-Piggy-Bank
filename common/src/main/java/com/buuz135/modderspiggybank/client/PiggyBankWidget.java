package com.buuz135.modderspiggybank.client;

import com.buuz135.modderspiggybank.AuthorInformation;
import com.buuz135.modderspiggybank.AuthorPiggyBank;
import com.buuz135.modderspiggybank.Constants;
import com.buuz135.modderspiggybank.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class PiggyBankWidget extends AbstractWidget {

    public static final int WIDTH = 100;
    public static final int HEIGHT = 50;

    private final AuthorInformation authorInformation;
    private final AuthorPiggyBank authorPiggyBank;
    private final List<String> mods = new ArrayList<>();
    private int donationButtonY;

    public PiggyBankWidget(int x, int y, AuthorInformation authorInformation, AuthorPiggyBank piggyBank, List<String> mods) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.authorInformation = authorInformation;
        this.authorPiggyBank = piggyBank;
        this.donationButtonY = 0;
        this.mods.addAll(this.authorInformation.modNames());
        this.mods.addAll(mods);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0,0,100);
        var scale = 1;
        guiGraphics.pose().scale(scale, scale, scale);


        var lines = new ArrayList<Component>();
        lines.add(Component.translatable("modders_piggy_bank.widget.title"));
        lines.add(Component.translatable("modders_piggy_bank.widget.title2", authorInformation.name()));

        this.width = WIDTH;
        for (var line : lines) {
            this.width = Math.max(width, Minecraft.getInstance().font.width(line));
        }
        this.width += 10;
        var seconds = 1d;
        if (mods.size() > 3) seconds = System.currentTimeMillis() / 4000d;
        var modIndex = (int) (seconds % mods.size());
        lines.add(Component.literal(mods.get(modIndex)));
        if (mods.size() > 1) lines.add(Component.literal(mods.get((modIndex + 1) % mods.size())));
        if (mods.size() > 2) lines.add(Component.literal(mods.get((modIndex + 2) % mods.size())));


        this.height = lines.size()*Minecraft.getInstance().font.lineHeight + 8 + 3;
        if (!this.authorPiggyBank.links().isEmpty()){
            this.height += 24;
        }
        guiGraphics.fill(this.getX() + 1, this.getY(), this.getX() + width + 1, this.getY() + height, 0x66000000);

        var y = this.getY() + 2;
        RenderUtil.renderFrameGradient(guiGraphics, this.getX(), this.getY(), width, height, 100, 0xFF000000 | authorPiggyBank.primary_color(), 0xFF000000 | authorPiggyBank.secondary_color());
        y += 1;
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, lines.get(0), this.getX() + 2, y, this.getX() + width, y + Minecraft.getInstance().font.lineHeight, 0xFFFFFF);
        y += Minecraft.getInstance().font.lineHeight;
        renderScrollingString(guiGraphics, Minecraft.getInstance().font, lines.get(1), this.getX() + 2, y, this.getX() + width, y + Minecraft.getInstance().font.lineHeight , 0xFFFFFF);
        y += Minecraft.getInstance().font.lineHeight;
        y += 1;
        RenderUtil.renderHorizontalLine(guiGraphics, this.getX() + 4, y +1, width - 8, 0, 0xFF000000 | authorPiggyBank.primary_color());
        y += 3;

        for (int i = 2; i < lines.size(); i++) {
            renderScrollingString(guiGraphics, Minecraft.getInstance().font, lines.get(i), this.getX() + 3, y, this.getX() + width - 4, y + Minecraft.getInstance().font.lineHeight, 0xFFFFFF);
            y += Minecraft.getInstance().font.lineHeight;
        }
        this.donationButtonY = y;
        if (!this.authorPiggyBank.links().isEmpty()){
            RenderUtil.renderHorizontalLine(guiGraphics, this.getX() + 4, y +1, width - 8, 0, 0xFF000000 | authorPiggyBank.secondary_color());
            var i = 0;
            for (AuthorPiggyBank.Link link : this.authorPiggyBank.links()) {
                if (!Constants.ALLOWED_LINKS.containsKey(link.type())) continue;
                if (isMouseOver(this.getX() + i * 22 + 7, this.getY() + y + 1, mouseX, mouseY)) {
                    //RenderUtil.renderFrameGradient(guiGraphics, this.getX() + i * 22 + 6, this.getY() + y + 2, 19, 19, 100, 0xFF000000 | authorPiggyBank.primary_color(), 0xFF000000 | authorPiggyBank.secondary_color());
                    guiGraphics.renderTooltip(Minecraft.getInstance().font, Component.translatable(Constants.ALLOWED_LINKS.get(link.type())), mouseX, mouseY);
                } else {
                    //RenderUtil.renderFrameGradient(guiGraphics, this.getX() + i * 22 + 6, this.getY() + y + 2, 19, 19, 100, 0x55000000 | authorPiggyBank.primary_color(), 0x55000000 | authorPiggyBank.secondary_color());
                }
                guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "textures/gui/" + link.type() + "_logo.png"), this.getX() + i * 22 + 7, this.getY() + y  +1, 0,0,20, 21,20,21);
                ++i;
            }
        }
        guiGraphics.pose().popPose();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    protected boolean clicked(double pMouseX, double pMouseY) {
        if (super.clicked(pMouseX, pMouseY)){
            if (!this.authorPiggyBank.links().isEmpty()){
                var i = 0;
                for (AuthorPiggyBank.Link link : this.authorPiggyBank.links()) {
                    if (!Constants.ALLOWED_LINKS.containsKey(link.type())) continue;
                    if (isMouseOver(this.getX() + i * 22 + 7, this.getY() + this.donationButtonY + 1, pMouseX, pMouseY)) {
                        ConfirmLinkScreen.confirmLink(Minecraft.getInstance().screen, link.url()).onPress(null);
                        return true;
                    }
                    ++i;
                }
            }
        }
        return false;
    }

    public boolean isMouseOver(int x, int y, double pMouseX, double pMouseY) {
        return pMouseX >= x && pMouseX <= x + 19 && pMouseY >= y && pMouseY <= y + 19;
    }
}
