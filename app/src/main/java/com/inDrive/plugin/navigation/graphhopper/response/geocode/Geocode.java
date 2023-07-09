package com.inDrive.plugin.navigation.graphhopper.response.geocode;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Geocode {
    private Point point;

    private List<Double> extent;

    private String name;

    private String country;

    @JsonProperty("countrycode")
    private String countryCode;

    private String city;

    private String state;

    private String street;

    @JsonProperty("postcode")
    private String postalCode;

    @JsonProperty("osm_id")
    private long osmId;

    @JsonProperty("osm_type")
    private String osmType;

    @JsonProperty("osm_key")
    private String osmKey;

    @JsonProperty("osm_value")
    private String osmValue;
}
