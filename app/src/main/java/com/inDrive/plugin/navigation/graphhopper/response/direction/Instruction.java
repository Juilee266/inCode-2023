package com.inDrive.plugin.navigation.graphhopper.response.direction;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Instruction {
    private double distance;

    private double heading;

    private int sign;

    private List<Integer> interval;

    private String text;

    private long time;

    @JsonProperty("street_name")
    private String streetName;

    @JsonProperty("exit_number")
    private int exitNumber;

    @JsonProperty("turn_angle")
    private double turnAngle;

    private boolean exited;

    @JsonProperty("last_heading")
    private double lastHeading;
}
