package com.familiasantana.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;

public class SettingsGui extends GuiScreen {

    @Override
    public void initGui() {
        buttonList.clear();
        for (int i = 0; i < Config.NAMES.length; i++) {
            int col = i % 2;
            int row = i / 2;
            int x = width / 2 - 155 + col * 160;
            int y = 50 + row * 24;
            buttonList.add(new GuiButton(i, x, y, 150, 20, label(Config.NAMES[i])));
        }
    }

    private String label(String name) {
        return name + ": " + (Config.get(name) ? "\u00a7aON" : "\u00a7cOFF");
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        String name = Config.NAMES[button.id];
        Config.toggle(name);
        Config.save();
        button.displayString = label(name);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "\u00a76Familia Santana Client", width / 2, 20, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "Clique para ligar/desligar cada modulo", width / 2, 33, 0xAAAAAA);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
