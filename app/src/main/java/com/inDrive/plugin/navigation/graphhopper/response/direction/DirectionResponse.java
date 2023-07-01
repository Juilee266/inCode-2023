package com.inDrive.plugin.navigation.graphhopper.response.direction;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DirectionResponse {
    private Hints hints;

    private Info info;

    private List<Path> paths;
}
