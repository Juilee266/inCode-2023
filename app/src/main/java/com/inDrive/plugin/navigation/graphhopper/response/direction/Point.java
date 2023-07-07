package com.inDrive.plugin.navigation.graphhopper.response.direction;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Point {
    private String type;

    private List<List<Double>> coordinates;
}
