package draylar.staffofbuilding.fabric.network;

import com.mojang.blaze3d.platform.InputConstants;
import draylar.staffofbuilding.fabric.item.BuilderStaffItem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public class ClientNetworking {

    // public static KeyMapping undoKey;
    private static boolean wasUndoPressed = false;

    public static void init() {
        // Register keybinding
        /*
         * undoKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
         * "key.staffofbuilding.undo",
         * org.lwjgl.glfw.GLFW.GLFW_KEY_Z, // Constructor mismatch in this env
         * "category.staffofbuilding.title"
         * ));
         */

        // PayloadTypeRegistry.playC2S().register(UndoMessage.ID, UndoMessage.CODEC);
        // Registration handled in ServerNetworking via Common Initializer

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.player.getMainHandItem().getItem() instanceof BuilderStaffItem) {

                com.mojang.blaze3d.platform.Window window = Minecraft.getInstance().getWindow();

                // Manual check for Cmd+Z / Ctrl+Z
                boolean isZ = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_Z);
                boolean ctrlDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL)
                        || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
                boolean cmdDown = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_LEFT_SUPER)
                        || InputConstants.isKeyDown(window, GLFW.GLFW_KEY_RIGHT_SUPER);

                if (isZ && (ctrlDown || cmdDown)) {
                    if (!wasUndoPressed) {
                        ClientPlayNetworking.send(new UndoMessage());
                        // client.player.displayClientMessage(Component.translatable("staffofbuilding.undo_sent"),
                        // true);
                        wasUndoPressed = true;
                    }
                } else {
                    wasUndoPressed = false;
                }
            }
        });
    }
}
