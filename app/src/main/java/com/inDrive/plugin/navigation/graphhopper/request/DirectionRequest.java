package com.inDrive.plugin.navigation.graphhopper.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

// TODO: Refer https://docs.graphhopper.com/#operation/postRoute for more request fields
@Getter
@Setter
@ToString
public class DirectionRequest {
    private List<List<Double>> points;

    @JsonProperty("snap_preventions")
    private List<String> snapPreventions;

    private List<String> details;

    private String vehicle;

    private String locale;

    private boolean instructions;

    @JsonProperty("calc_points")
    private boolean calcPoints;

    @JsonProperty("points_encoded")
    private boolean pointsEncoded;

    private String algorithm;
}
