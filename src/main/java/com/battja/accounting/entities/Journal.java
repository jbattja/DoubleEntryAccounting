package com.battja.accounting.entities;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Journal implements Comparable<Journal> {

    @Id
    @GeneratedValue
    private Integer id;
    private String eventType;
    private Date date;
    @OneToMany(mappedBy = "journal")
    private List<Booking> bookings;

    protected Journal() {}

    public Journal(List<Booking> bookings, String eventType) {
        this.bookings = bookings;
        this.eventType = eventType;
        this.date = new Date();
    }

    @Override
    public String toString() {
        return String.format(
                "Journal[id=%d, eventType='%s', date='%s']",
                id, eventType, date);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @Override
    public int compareTo(Journal other) {
        return this.date.compareTo(other.date);
    }
}
