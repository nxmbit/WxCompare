package com.nxmbit.wxcompare.event;

public class KeyboardShortcutEvent {
    private final String action;

    public KeyboardShortcutEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public static final String TOGGLE_THEME = "TOGGLE_THEME";
    public static final String OPEN_SETTINGS = "OPEN_SETTINGS";
    public static final String NAVIGATE_LOCATIONS_LIST = "NAVIGATE_LOCATIONS_LIST";
    public static final String NAVIGATE_LOCATIONS_SEARCH = "NAVIGATE_LOCATIONS_SEARCH";
    public static final String NAVIGATE_KEYBOARD_SHORTCUTS = "NAVIGATE_KEYBOARD_SHORTCUTS";
    public static final String NAVIGATE_MAP = "NAVIGATE_MAP";
    public static final String NAVIGATE_ABOUT = "NAVIGATE_ABOUT";
}