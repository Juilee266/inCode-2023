package com.inDrive.plugin.common.servicebinding;

import android.app.Service;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

public class ConcreteServiceConnection <T extends Service> implements ServiceConnection {
    private T service;
    private boolean isBound = false;

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        ServiceBinder<T> binder = (ServiceBinder<T>) service;
        this.service = (T) binder.getService();
        this.isBound = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        this.service = null;
        this.isBound = false;
    }

    @Override
    public void onBindingDied(ComponentName name) {
        ServiceConnection.super.onBindingDied(name);
    }

    @Override
    public void onNullBinding(ComponentName name) {
        ServiceConnection.super.onNullBinding(name);
    }

    public boolean isBound() {
        return isBound;
    }

    public T getService() {
        return this.service;
    }
}
