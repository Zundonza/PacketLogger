package ru.zundonza.mixin;

import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.zundonza.screens.game.LoggerScreen;

@Mixin(Keyboard.class)
public class KeyEventMixin {

    @Inject(method = "onKey", at = @At("RETURN"))
    public void onKey(long window, int key, int scancode, int action, int modifiers, CallbackInfo ci) {
        if(action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_RIGHT_SHIFT && MinecraftClient.getInstance().currentScreen == null) {
            MinecraftClient.getInstance().setScreen(new LoggerScreen());
        }
    }

}
