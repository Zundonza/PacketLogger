package ru.zundonza.screens.game;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.zundonza.imgui.ImGuiImpl;
import ru.zundonza.screens.imgui.ImGuiPacketLogScreen;

public class LoggerScreen extends Screen {

    public LoggerScreen() {
        super(Text.literal("packetlogger"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ImGuiImpl.draw(io -> ImGuiPacketLogScreen.render());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

}
