package com.inDrive.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ride {
    private Passenger passenger;
    private Driver driver;
    private Location source;
    private Location destination;
    private int timeInMinutesForDriver;
    private int timeInMinutesToReachDest;

    private String otp;
    private String rideStatus; //BOOKED, NOT_BOOKED, NOT_STARTED, STARTED, DRIVER_ARRIVED

    public Ride(Passenger passenger) {
        this.passenger = passenger;
        rideStatus = "NOT_BOOKED";
        this.otp = "6 5 1 9";
    }
}
