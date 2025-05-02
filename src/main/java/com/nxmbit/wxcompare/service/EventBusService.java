package com.nxmbit.wxcompare.service;

import com.google.common.eventbus.EventBus;

/**
 * Singleton EventBus service for application-wide event communication
 */
public class EventBusService {
    private static final EventBus EVENT_BUS = new EventBus("WxCompareEventBus");

    private EventBusService() {
        // Private constructor to prevent instantiation
    }

    public static EventBus getEventBus() {
        return EVENT_BUS;
    }

    public static void register(Object object) {
        EVENT_BUS.register(object);
    }

    public static void unregister(Object object) {
        EVENT_BUS.unregister(object);
    }

    public static void post(Object event) {
        EVENT_BUS.post(event);
    }
}