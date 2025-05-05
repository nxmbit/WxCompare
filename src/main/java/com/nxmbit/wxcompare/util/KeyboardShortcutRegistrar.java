package com.nxmbit.wxcompare.util;

import com.nxmbit.wxcompare.service.EventBusService;
import com.nxmbit.wxcompare.event.KeyboardShortcutEvent;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

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
                new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.OPEN_SETTINGS))
        );


        // Alt+L for locations
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_LOCATIONS_LIST))
        );

        // Alt+L for locations search
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_LOCATIONS_SEARCH))
        );

        // Alt+K for keyboard shortcuts
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN),
                () -> EventBusService.post(new KeyboardShortcutEvent(KeyboardShortcutEvent.NAVIGATE_KEYBOARD_SHORTCUTS))
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
}