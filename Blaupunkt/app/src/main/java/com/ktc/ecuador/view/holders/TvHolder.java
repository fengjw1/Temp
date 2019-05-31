package com.ktc.ecuador.view.holders;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.utils.FontUtils;

public class TvHolder extends RecyclerView.ViewHolder {


    private TextView mSourceName;
    private SurfaceView mSurfaceView;
    private View mView;
    private View indicatorView;
    private TextView mSourceNamehl;
    private CardView mCardView;

    public TvHolder(@NonNull View itemView) {
        super(itemView);
        mSourceName = itemView.findViewById(R.id.home_source_name);
        mSurfaceView = itemView.findViewById(R.id.home_surface);
        mView = itemView.findViewById(R.id.home_icon_shadow);
        indicatorView = itemView.findViewById(R.id.item_view_selector);
        mCardView = itemView.findViewById(R.id.home_view);
        mSourceNamehl = itemView.findViewById(R.id.tv_source_name);
        FontUtils fontUtils = FontUtils.getInstance(itemView.getContext().getApplicationContext());
        fontUtils.setRegularFont(mSourceName);
        fontUtils.setRegularFont(mSourceNamehl);
    }

    public TextView getSourceNamehl() {
        return mSourceNamehl;
    }

    public void setSourceNamehl(TextView sourceNamehl) {
        mSourceNamehl = sourceNamehl;
    }

    public TextView getSourceName() {
        return mSourceName;
    }

    public void setSourceName(TextView sourceName) {
        mSourceName = sourceName;
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        mSurfaceView = surfaceView;
    }

    public View getView() {
        return mView;
    }

    public void setView(View view) {
        mView = view;
    }

    public CardView getCardView() {
        return mCardView;
    }

    public void setCardView(CardView cardView) {
        mCardView = cardView;
    }


    public void setVisibility(boolean isSelected) {
        int visibility = isSelected ? View.GONE : View.VISIBLE;
        int isVisibility = isSelected ? View.VISIBLE : View.GONE;
        mSourceName.setVisibility(visibility);
        mView.setVisibility(visibility);
        mSourceNamehl.setVisibility(isVisibility);
        indicatorView.setVisibility(isVisibility);
    }
}
