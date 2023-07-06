package com.inDrive.plugin.common;

import java.util.Map;

public interface ActionListenerCallback {
    void onInitialized();

    void onActionStarted();

    void onActionCompleted(Map<String, Object> resultMap);

    void onActionFailed();
}
