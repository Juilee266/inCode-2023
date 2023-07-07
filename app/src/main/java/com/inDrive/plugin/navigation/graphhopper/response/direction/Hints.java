package com.inDrive.plugin.navigation.graphhopper.response.direction;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Hints {
    @JsonProperty("visited_nodes.sum")
    private int visitedNodesSum;

    @JsonProperty("visited_nodes.average")
    private int visitedNodesAverage;
}
