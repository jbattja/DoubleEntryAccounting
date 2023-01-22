package com.battja.accounting.util;

import com.battja.accounting.entities.Amount;
import com.battja.accounting.entities.BatchEntry;
import com.battja.accounting.entities.Booking;
import com.battja.accounting.entities.Journal;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

public class JournalUtil {
    public static Journal getFirstJournal(@NonNull BatchEntry batchEntry) {
        if (batchEntry.getJournals() == null || batchEntry.getJournals().isEmpty()) {
            return null;
        }
        Journal firstJournal = null;
        for (Journal journal : batchEntry.getJournals()) {
            if (firstJournal == null) {
                firstJournal = journal;
                continue;
            }
            if (journal.getDate().before(firstJournal.getDate())) {
                firstJournal = journal;
            }
        }
        return firstJournal;
    }

    public static String getReference(@NonNull BatchEntry batchEntry) {
        if(batchEntry.getTransaction() != null) {
            return batchEntry.getTransaction().getType() +" - "+ batchEntry.getTransaction().getTransactionReference();
        }
        Journal journal = getFirstJournal(batchEntry);
        if (journal != null) {
            return journal.getEventType();
        }
        return "";
    }

    public static boolean isBalanced(@NonNull Journal journal) {
            Map<Amount.Currency,Long> balancePerCurrency = new HashMap<>();
            for (Booking b : journal.getBookings()) {
                Long currentBalance = balancePerCurrency.get(b.getCurrency());
                if (currentBalance == null) {
                    currentBalance = 0L;
                }
                balancePerCurrency.put(b.getCurrency(),b.getAmount()+currentBalance);
            }
            for (Map.Entry<Amount.Currency,Long> balance : balancePerCurrency.entrySet()) {
                if (balance.getValue() != 0) {
                    return false;
                }
            }
            return true;
    }
}
