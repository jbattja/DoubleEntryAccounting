package com.battja.accounting.events;

import com.battja.accounting.util.CommonUtil;
import com.battja.accounting.vaadin.components.DisplayableEntity;

public enum EventType implements DisplayableEntity {

    RECEIVED(ReceivedEvent.class),
    AUTHORISED(AuthorisedEvent.class),
    REFUSED(RefusedEvent.class),
    PAID(PaidEvent.class),
    CANCELLED(CancelEvent.class),
    SETTLED_TO_MERCHANT(SettledToMerchantEvent.class),
    SETTLEMENT_FAILED(SettlementFailedEvent.class),
    MERCHANT_WITHDRAWAL(MerchantWithdrawalEvent.class);

    @Override
    public String toString() {
        return CommonUtil.enumNameToString(this.name());
    }

    private final Class<? extends BookingEvent> eventClass;

    EventType(Class<? extends BookingEvent> eventClass) {
        this.eventClass = eventClass;
    }

    public Class<? extends BookingEvent> getEventClass() {
        return eventClass;
    }

    @Override
    public String getDisplayName() {
        return this.toString();
    }
}
