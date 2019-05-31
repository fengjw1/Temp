package com.mp3.launcher4.activities;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.utils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author longzj
 */
public class TimeFragment extends Fragment {

    private static final String HOUR_24 = "24";
    private TextView mHourText;
    private TextView mMonthText;
    private TimeReceiver mTimeReceiver;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initReceiver();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time, null, false);
        mMonthText = (TextView) view.findViewById(R.id.time_date);
        mHourText = (TextView) view.findViewById(R.id.time_clock);
        FontUtils fontUtils = FontUtils.getInstance(getContext().getApplicationContext());
        fontUtils.setSemiBoldFont(mHourText);
        fontUtils.setRegularFont(mMonthText);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        changeFormatTime();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTimeReceiver != null) {
            getContext().unregisterReceiver(mTimeReceiver);
        }
    }

    private void initReceiver() {
        mTimeReceiver = new TimeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        getContext().registerReceiver(mTimeReceiver, filter);
    }

    private void changeFormatTime() {
        changeMonth();
        changeHour();
    }

    private void changeHour() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String format;
        if (DateFormat.is24HourFormat(getContext())) {
            format = "HH:mm";
        } else {
            format = "hh:mm a";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        if (mHourText != null) {
            mHourText.setText(dateFormat.format(date));
        }
    }

    private void changeMonth() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String format = "EEEE，MMMM dd";
        SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.getDefault());
        if (mMonthText != null) {
            mMonthText.setText(dateFormat.format(date));
        }
    }

    private class TimeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            changeFormatTime();
        }
    }

}
