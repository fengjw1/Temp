package com.ktc.ecuador.livedata;

import android.arch.lifecycle.LiveData;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeTickLiveData extends LiveData<String> {
    private TimeChangeListener mTimeChangeListener;
    private TimeTickLiveData instance;
    private Context mContext;

    public TimeTickLiveData(Context context) {
        this.mContext = context;
        instance = this;
        mTimeChangeListener = new TimeChangeListener();
    }

    //change to 1 from 0.
    @Override
    protected void onActive() {
        super.onActive();
        instance.setValue(getTime());
        IntentFilter timeFilter = new IntentFilter();
        timeFilter.addAction(Intent.ACTION_TIME_TICK);
        timeFilter.addAction(Intent.ACTION_DATE_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIME_CHANGED);
        timeFilter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mTimeChangeListener = new TimeChangeListener();
        mContext.registerReceiver(mTimeChangeListener, timeFilter);
    }

    // from 1 to 0
    @Override
    protected void onInactive() {
        super.onInactive();
        mContext.unregisterReceiver(mTimeChangeListener);
    }

    public String getTime() {
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return format.format(curDate);
    }

    class TimeChangeListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            instance.setValue(getTime());
        }
    }
}
