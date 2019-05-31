package com.mp3.launcher4.holders;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.AppDetailBean;
import com.mp3.launcher4.utils.FontUtils;


/**
 * @author longzj
 */
public class AppHolder extends RecyclerView.ViewHolder {

    private ImageView mIcon;
    private CardView  mCard;
    private Context   mContext;
    private TextView  mName;

    public AppHolder(View itemView) {
        super(itemView);
        mContext = itemView.getContext();
        mIcon = (ImageView) itemView.findViewById(R.id.category_cover);
        mCard = (CardView) itemView.findViewById(R.id.card);
        mName = (TextView) itemView.findViewById(R.id.category_title);
        View shadow = itemView.findViewById(R.id.app_shadow);
        shadow.setVisibility(View.GONE);

        FontUtils utils = FontUtils.getInstance(mContext.getApplicationContext());
        utils.setRegularFont(mName);

    }


    public ImageView getIcon() {
        return mIcon;
    }

    public CardView getCard() {
        return mCard;
    }

    public TextView getName() {
        return mName;
    }

    public void bindData(AppDetailBean bean) {
            handleStoreApp(bean);

    }


    private void handleStoreApp(AppDetailBean bean) {
        Glide.with(mContext.getApplicationContext())
                .load(bean.getImage())
                .centerCrop()
                .into(mIcon);
        mName.setText(bean.getTitle());
    }

}
