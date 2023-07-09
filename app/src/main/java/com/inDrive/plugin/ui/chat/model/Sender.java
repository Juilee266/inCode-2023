package com.inDrive.plugin.ui.chat.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Sender {
    USER(1),
    SYSTEM(2);

    private int value;
}
