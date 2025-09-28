package com.hotel.models;

import java.io.Serializable;

public class Reservation implements Serializable {
    private String reservationId;
    private int roomNumber;
    private String guestName;
    private String checkIn;
    private String checkOut;
    private double totalAmount;

    public Reservation(String reservationId, int roomNumber, String guestName,
                         String checkIn, String checkOut, double totalAmount) {
        this.reservationId = reservationId;
        this.roomNumber = roomNumber;
        this.guestName = guestName;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.totalAmount = totalAmount;
    }

    public String getReservationId() { return reservationId; }
    public int getRoomNumber() { return roomNumber; }
    public String getGuestName() { return guestName; }
}