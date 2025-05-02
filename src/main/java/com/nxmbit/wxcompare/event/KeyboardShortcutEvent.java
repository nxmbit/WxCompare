package com.nxmbit.wxcompare.event;

public class KeyboardShortcutEvent {
    private final String action;

    public KeyboardShortcutEvent(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    // Constants for common actions
    public static final String TOGGLE_THEME = "TOGGLE_THEME";
    public static final String OPEN_SETTINGS = "OPEN_SETTINGS";
    public static final String NAVIGATE_DASHBOARD = "NAVIGATE_DASHBOARD";
    public static final String NAVIGATE_LOCATIONS = "NAVIGATE_LOCATIONS";
    public static final String NAVIGATE_COMPARISON = "NAVIGATE_COMPARISON";
    public static final String NAVIGATE_MAP = "NAVIGATE_MAP";
    public static final String NAVIGATE_ABOUT = "NAVIGATE_ABOUT";
}