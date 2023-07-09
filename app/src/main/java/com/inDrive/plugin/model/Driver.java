package com.inDrive.plugin.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Driver
{
    private String driverName;
    private Vehicle vehicle;
    private String driverContact;
    private int stars;
}
