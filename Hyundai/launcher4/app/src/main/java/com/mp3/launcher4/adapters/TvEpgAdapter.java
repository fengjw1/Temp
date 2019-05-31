package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mstar.android.tv.TvEpgManager;
import com.mstar.android.tvapi.dtv.vo.EpgEventInfo;

import java.util.Calendar;
import java.util.List;

/**
 * @author longzj
 */
public class TvEpgAdapter extends BaseAdapter<EpgEventInfo> {

    public TvEpgAdapter(Context context, List<EpgEventInfo> data) {
        super(context, data);
    }

    private static String formatData(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return String.valueOf(android.text.format.DateFormat.format("HH:mm", calendar));
    }

    private static long getOffsetTime(long time) {
        Time currentTime = new Time();
        currentTime.setToNow();
        currentTime.toMillis(true);
        long offset = TvEpgManager.getInstance().getEpgEventOffsetTime(currentTime, true) * 1000;
        return time * 1000 - offset;
    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        EpgEventInfo info = getItemData(position);
        EpgHolder epgHolder = (EpgHolder) holder;
        final TextView textView = epgHolder.mInfoText;
        long currentTime = System.currentTimeMillis();
        final Context context = getContext();
        StringBuilder builder = new StringBuilder();
        int color;
        long startTimeWithOffset = getOffsetTime(info.startTime);
        long endTimeWithOffset = getOffsetTime(info.endTime);
        if (startTimeWithOffset <= currentTime && endTimeWithOffset >= currentTime) {
            builder.append(context.getString(R.string.epg_now));
            color = android.R.color.white;
        } else {
            builder.append(formatData(startTimeWithOffset));
            color = R.color.colorSourceTvName;
        }
        builder.append(getContext().getString(R.string.epg_colon));
        builder.append(" ");
        builder.append(info.name);
        textView.setTextColor(context.getResources().getColor(color));
        textView.setText(builder.toString());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EpgHolder(getInflater().inflate(R.layout.item_home_tv_epg, null, false));
    }

    private class EpgHolder extends RecyclerView.ViewHolder {

        private TextView mInfoText;

        public EpgHolder(View itemView) {
            super(itemView);
            mInfoText = (TextView) itemView;
        }
    }
}
