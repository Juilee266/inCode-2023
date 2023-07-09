package com.inDrive.plugin.navigation.graphhopper.response.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Point {
    @JsonProperty("lat")
    private double latitude;

    @JsonProperty("lng")
    private double longitude;
}
