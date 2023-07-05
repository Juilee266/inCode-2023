package com.inDrive.plugin.entities;

public class Passenger {
    private String passengerName;
    private String passengerContact;

    public Passenger(String passengerName, String passengerContact) {
        this.passengerName = passengerName;
        this.passengerContact = passengerContact;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerContact() {
        return passengerContact;
    }

    public void setPassengerContact(String passengerContact) {
        this.passengerContact = passengerContact;
    }
}
