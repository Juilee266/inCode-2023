package com.inDrive.plugin.navigation.graphhopper.response.direction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Path {
    private double distance;

    private double weight;

    private long time;

    private int transfers;

    @JsonProperty("points_encoded")
    private boolean pointsEncoded;

    @JsonProperty("bbox")
    private List<Double> bBox;

    private Point points;

    private List<Instruction> instructions;

    private List<String> legs;

    private Object details;

    private double ascend;

    private double descend;

    @JsonProperty("snapped_waypoints")
    private Point snappedWaypoints;
}
