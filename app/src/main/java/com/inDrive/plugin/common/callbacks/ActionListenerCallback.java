package com.inDrive.plugin.common.callbacks;

import java.util.Map;

public interface ActionListenerCallback {
    void onActionStarted();

    void onActionCompleted(Map<String, Object> resultMap);

    void onActionFailed();
}
