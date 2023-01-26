package com.battja.accounting.events;

import com.battja.accounting.entities.*;
import com.battja.accounting.exceptions.BookingException;
import com.battja.accounting.services.FeeService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.*;

public abstract class BookingEvent {

    private final List<Booking> bookings;
    private Set <Transaction> transactions;
    private ReportLine reportLine;
    private Collection <Batch> batches;
    private AdditionalBookingInfo additionalBookingInfo;
    private Map<Account,Fee> fees;

    protected AdditionalBookingInfo getAdditionalInfo() {
        return additionalBookingInfo;
    }
    protected Map<Account,Fee> getFees() {
        if (fees == null) {
            return new HashMap<>();
        }
        return fees;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public BookingEvent() {
        this.bookings = new ArrayList<>();
    }

    public abstract String getEventTypeName();
    protected abstract Transaction.TransactionType[] requiredTransactionTypes();

    private boolean validateTransactions(Set<Transaction> transactions) {
        if (transactions.size() != requiredTransactionTypes().length) {
            return false;
        }
        for (Transaction.TransactionType transactionType : requiredTransactionTypes()) {
            boolean typeFound = false;
            for (Transaction t : transactions) {
                if (t.getType().equals(transactionType)) {
                    typeFound = true;
                    break;
                }
            }
            if (!typeFound) return false;
        }
        return true;
    }

    public void book(@NonNull AdditionalBookingInfo additionalBookingInfo, @Nullable Collection<Batch> batches) throws BookingException {
        this.additionalBookingInfo = additionalBookingInfo;
        if (this.additionalBookingInfo.getTransactions() == null) {
            this.transactions = new HashSet<>();
        } else {
            this.transactions = this.additionalBookingInfo.getTransactions();
        }
        if (!validateTransactions(this.transactions)) {
            throw new BookingException("Incorrect input of transactions for this BookingEvent " + getEventTypeName());
        }
        this.reportLine = this.additionalBookingInfo.getReportLine();
        this.fees = this.additionalBookingInfo.getFees();
        this.batches = batches;
        bookInternal();
        for (Transaction transaction : transactions) {
            transaction.setStatus(this.getEventTypeName());
        }
        deductFeesFromPayable();
    }

    private void deductFeesFromPayable() {
        // deduct fees from Payable
        Booking payableBooking = null;
        for (Booking booking : bookings) {
            if (RegisterType.PAYABLE.equals(booking.getRegister())) {
                payableBooking = booking;
                break;
            }
        }
        if (payableBooking == null) {
            return;
        }
        long deductionAmount = 0;
        List<Booking> bookingsToInvert = new ArrayList<>();
        for (Booking booking : bookings) {
            if (RegisterType.FEES.equals(booking.getRegister()) && payableBooking.getAccount().getId().equals(booking.getAccount().getId())
                    && payableBooking.getCurrency().equals(booking.getCurrency())) {
                deductionAmount += booking.getAmount();
                bookingsToInvert.add(booking);
            }
        }
        if (deductionAmount != 0) {
            payableBooking.setAmount(payableBooking.getAmount() + deductionAmount);
            for (Booking bookingToInvert : bookingsToInvert) {
                addBooking(bookingToInvert.getAccount(), bookingToInvert.getRegister(),
                        new Amount(bookingToInvert.getCurrency(), bookingToInvert.getAmount() * -1), bookingToInvert.getTransaction());
            }
        }
    }

    protected void bookFees(Transaction transaction) {
        for (Map.Entry<Account, Fee> fee : getFees().entrySet()) {
            List<Amount> calculatedFees =  FeeService.calculateFee(fee.getValue(),getCreditAmount(transaction));
            for (Amount amount : calculatedFees) {
                if (amount.getValue() != 0) {
                    if (fee.getKey() !=null && fee.getKey().getAccountType().equals(Account.AccountType.MERCHANT)) {
                        addBooking(fee.getKey(), RegisterType.FEES, new Amount(amount.getCurrency(), amount.getValue() * -1), transaction);
                        addBooking(Account.defaultPspAccount(), RegisterType.REVENUE, new Amount(amount.getCurrency(), amount.getValue()), transaction);
                    } else if (fee.getKey() !=null && fee.getKey().getAccountType().equals(Account.AccountType.PARTNER_ACCOUNT)) {
                        addBooking(fee.getKey(), RegisterType.REVENUE, new Amount(amount.getCurrency(), amount.getValue()), transaction);
                        addBooking(Account.defaultPspAccount(), RegisterType.FEES, new Amount(amount.getCurrency(), amount.getValue() * -1), transaction);
                    }
                }
            }
        }
    }

    protected Batch findBatch(@NonNull Account account, @NonNull RegisterType registerType) {
        if (batches == null || batches.isEmpty()) {
            return null;
        }
        for (Batch b: batches) {
            if (b.getAccount() == null) {
                continue;
            }
            if (b.getRegister().equals(registerType) && b.getAccount().getId().equals(account.getId())) {
                return b;
            }
        }
        return null;
    }

    protected abstract void bookInternal() throws BookingException;

    protected void addBooking(@NonNull Account account, @NonNull RegisterType register, @NonNull Amount amount, Transaction transaction) {
        bookings.add(new Booking(account, register, amount.getValue(),amount.getCurrency(), findBatch(account,register), transaction));
    }
    protected void addBooking(@NonNull Account account, @NonNull RegisterType register, @NonNull Amount amount, ReportLine reportLine) {
        bookings.add(new Booking(account, register, amount.getValue(),amount.getCurrency(), findBatch(account,register), reportLine));
    }

    protected Amount getCreditAmount(Transaction transaction) {
        return new Amount(transaction.getCurrency(),transaction.getAmount());
    }

    protected Amount getDebitAmount(Transaction transaction) {
        return new Amount(transaction.getCurrency(),transaction.getAmount()*-1);
    }

    protected Amount getCreditAmount(ReportLine reportLine) {
        return new Amount(reportLine.getCurrency(),reportLine.getGrossAmount());
    }

    protected Amount getDebitAmount(ReportLine reportLine) {
        return new Amount(reportLine.getCurrency(),reportLine.getGrossAmount()*-1);
    }

    protected Transaction getTransaction(Transaction.TransactionType type) throws BookingException {
        for (Transaction transaction : transactions) {
            if (transaction.getType().equals(type)) {
                return transaction;
            }
        }
        throw new BookingException("Internal booking error: no transaction with transaction type " + type + " found");
    }

    protected ReportLine getReportLine() {
        return reportLine;
    }


}
