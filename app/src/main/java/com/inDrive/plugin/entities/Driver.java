package com.inDrive.plugin.entities;

import com.inDrive.plugin.entities.Vehicle;

public class Driver
{
    private String driverName;
    private Vehicle vehicle;
    private String driverContact;
    private int stars;

    public Driver(String driverName, Vehicle vehicle, String driverContact, int stars) {
        this.driverName = driverName;
        this.vehicle = vehicle;
        this.driverContact = driverContact;
        this.stars = stars;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getDriverContact() {
        return driverContact;
    }

    public void setDriverContact(String driverContact) {
        this.driverContact = driverContact;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
