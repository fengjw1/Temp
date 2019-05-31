package com.mp3.launcher4.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.SliderMenuBean;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.utils.FontUtils;

import java.util.List;

/**
 * @author longzj
 */
public class SliderMenuAdapter extends BaseAdapter<SliderMenuBean> {

    private FontUtils mFontUtils;

    public SliderMenuAdapter(Context context, List<SliderMenuBean> data) {
        super(context, data);
        mFontUtils = FontUtils.getInstance(context.getApplicationContext());
    }

    @Override
    public void updateView(RecyclerView.ViewHolder holder, int position) {
        SliderHolder sliderHolder = (SliderHolder) holder;
        SliderMenuBean bean = getData().get(position);
        ((SliderHolder) holder).icon.setImageResource(bean.getIconId());
        final TextView title = sliderHolder.title;
        title.setText(bean.getTitleId());
        mFontUtils.setRegularFont(title);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderHolder(getInflater().inflate(R.layout.item_slider, null, false));
    }

    public class SliderHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView title;

        public SliderHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.slider_icon);
            title = (TextView) itemView.findViewById(R.id.slider_title);
        }

        public ImageView getIcon() {
            return icon;
        }

        public TextView getTitle() {
            return title;
        }
    }
}
