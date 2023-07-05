package com.inDrive.plugin.entities;

public class Ride {
    private Passenger passenger;
    private Driver driver;
    private Location source;
    private Location destination;
    private int timeInMinutesForDriver;
    private int timeInMinutesToReachDest;
    private String rideStatus;

    public Ride(Passenger passenger, Driver driver, Location source, Location destination) {
        this.passenger = passenger;
        this.driver = driver;
        this.source = source;
        this.destination = destination;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public Location getSource() {
        return source;
    }

    public void setSource(Location source) {
        this.source = source;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public int getTimeInMinutesForDriver() {
        return timeInMinutesForDriver;
    }

    public void setTimeInMinutesForDriver(int timeInMinutesForDriver) {
        this.timeInMinutesForDriver = timeInMinutesForDriver;
    }

    public int getTimeInMinutesToReachDest() {
        return timeInMinutesToReachDest;
    }

    public void setTimeInMinutesToReachDest(int timeInMinutesToReachDest) {
        this.timeInMinutesToReachDest = timeInMinutesToReachDest;
    }

    public String getRideStatus() {
        return rideStatus;
    }

    public void setRideStatus(String rideStatus) {
        this.rideStatus = rideStatus;
    }
}
