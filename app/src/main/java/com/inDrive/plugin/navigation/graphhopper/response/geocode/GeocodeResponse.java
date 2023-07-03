package com.inDrive.plugin.navigation.graphhopper.response.geocode;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class GeocodeResponse {
    private List<Geocode> hits;

    private String locale;
}
