package com.nxmbit.wxcompare.util;

import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.event.KeyboardShortcutEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Utility class for registering keyboard shortcuts in the application.
 */
public final class KeyboardShortcutRegistrar {

    // Private constructor to prevent instantiation
    private KeyboardShortcutRegistrar() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Registers all application keyboard shortcuts for the given scene.
     *
     * @param scene The JavaFX scene to attach shortcuts to
     */
    public static void registerShortcuts(Scene scene) {
        if (scene == null) return;

        // Alt+T for theme toggle
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.TOGGLE_THEME))
        );

        // Alt+S for settings
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.OPEN_SETTINGS))
        );

        // Alt+D for dashboard
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_DASHBOARD))
        );

        // Alt+L for locations
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_LOCATIONS))
        );

        // Alt+C for comparison
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_COMPARISON))
        );

        // Alt+M for map
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_MAP))
        );

        // Alt+A for about
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_ABOUT))
        );
    }

    /**
     * Registers a single keyboard shortcut.
     *
     * @param scene The JavaFX scene to attach the shortcut to
     * @param keyCode The key code of the shortcut
     * @param modifiers The modifiers to apply (Alt, Ctrl, etc.)
     * @param shortcutAction The action identifier to post to the event bus
     */
    public static void registerShortcut(Scene scene, KeyCode keyCode,
                                        KeyCombination.Modifier[] modifiers,
                                        String shortcutAction) {
        if (scene == null) return;

        KeyCodeCombination keyCombination = new KeyCodeCombination(keyCode, modifiers);
        scene.getAccelerators().put(
                keyCombination,
                () -> EventBusService.post(new KeyboardShortcutEvent(shortcutAction))
        );
    }
}