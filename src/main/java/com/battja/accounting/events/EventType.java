package com.battja.accounting.events;

public enum EventType {

    RECEIVED(ReceivedEvent.class),
    AUTHORISED(AuthorisedEvent.class),
    PAID(PaidEvent.class),
    REFUSED(RefusedEvent.class);

    private final Class<? extends BookingEvent> eventClass;

    EventType(Class<? extends BookingEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends BookingEvent> getEventClass() {
        return eventClass;
    }
}
