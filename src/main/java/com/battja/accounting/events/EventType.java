package com.battja.accounting.events;

public enum EventType {

    RECEIVED(ReceivedEvent.class),
    AUTHORISED(AuthorisedEvent.class),
    REFUSED(RefusedEvent.class),
    PAID(PaidEvent.class),
    CANCELLED(CancelEvent.class),
    SETTLED_TO_MERCHANT(SettledToMerchantEvent.class),
    SETTLEMENT_FAILED(SettlementFailedEvent.class),
    MERCHANT_WITHDRAWAL(MerchantWithdrawalEvent.class);

    private final Class<? extends BookingEvent> eventClass;

    EventType(Class<? extends BookingEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends BookingEvent> getEventClass() {
        return eventClass;
    }
}
