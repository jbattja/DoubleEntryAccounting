package com.battja.accounting.entities;

import com.battja.accounting.util.CommonUtil;
import com.battja.accounting.vaadin.components.MultiSelectFilterable;
import jakarta.persistence.*;
import org.springframework.lang.NonNull;

import java.util.Date;
import java.util.List;

@Entity
public class Batch {

    public enum BatchStatus implements MultiSelectFilterable { CLOSED, AVAILABLE, ENDED;

        @Override
        public String toString() {
            return CommonUtil.enumNameToString(this.name());
        }

        @Override
        public String getFilterName() {
            return this.toString();
        }
    }

    @Id
    @GeneratedValue
    private Integer id;
    private Integer batchNumber;
    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
    @Enumerated(EnumType.STRING)
    private RegisterType register;
    private Date openDate;
    private Date endDate;
    private Date closeDate;
    @Enumerated(EnumType.STRING)
    private BatchStatus status;
    @OneToMany(mappedBy = "batch")
    private List<Booking> bookings;

    protected Batch() {}

    public Batch(@NonNull Account account, @NonNull RegisterType register, Integer batchNumber) {
        this.batchNumber = batchNumber;
        this.account = account;
        this.register = register;
        this.openDate = new Date();
        this.status = BatchStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return String.format(
                "Batch[id=%d, batchNumber='%d', account='%s', register='%s']",
                id, batchNumber, account == null ? null : account.getAccountName(), register);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(Integer batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public RegisterType getRegister() {
        return register;
    }

    public void setRegister(RegisterType register) {
        this.register = register;
    }

    public Date getOpenDate() {
        return openDate;
    }

    public void setOpenDate(Date openDate) {
        this.openDate = openDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(Date closeDate) {
        this.closeDate = closeDate;
    }

    public BatchStatus getStatus() {
        return status;
    }

    public void setStatus(BatchStatus status) {
        this.status = status;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }
}
