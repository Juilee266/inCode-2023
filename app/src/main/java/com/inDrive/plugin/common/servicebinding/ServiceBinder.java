package com.inDrive.plugin.common.servicebinding;

import android.app.Service;
import android.os.Binder;

public class ServiceBinder<T extends Service> extends Binder {
    private T context;

    public ServiceBinder(T t) {
        this.context = t;
    }
    public T getService() {
        return this.context;
    }
}
