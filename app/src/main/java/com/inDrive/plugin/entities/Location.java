package com.inDrive.plugin.entities;

// TODO: Remove this class and directly use Google's location class
public class Location {
    String locationName;
    String locationCoordinates;

    public Location(String locationName, String locationCoordinates) {
        this.locationName = locationName;
        this.locationCoordinates = locationCoordinates;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getLocationCoordinates() {
        return locationCoordinates;
    }

    public void setLocationCoordinates(String locationCoordinates) {
        this.locationCoordinates = locationCoordinates;
    }
}
