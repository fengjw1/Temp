package com.mp3.launcher4.holders;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mp3.launcher4.R;
import com.mp3.launcher4.beans.CategoryDetailBean;
import com.mp3.launcher4.utils.FontUtils;

/**
 * @author longzj
 */
public class CategoryVideoHolder extends RecyclerView.ViewHolder {

    private TextView mTitle;
    private ImageView mCover;

    public CategoryVideoHolder(View itemView) {
        super(itemView);
        FontUtils fontUtils = FontUtils.getInstance(itemView.getContext().getApplicationContext());
        mTitle = (TextView) itemView.findViewById(R.id.category_title);
        mCover = (ImageView) itemView.findViewById(R.id.category_cover);
        fontUtils.setRegularFont(mTitle);
    }

    public void bindData(CategoryDetailBean bean) {
        if (bean == null) {
            return;
        }
        String title = bean.getTitle();
        String coverUrl = bean.getImage();

        if (!TextUtils.isEmpty(title)) {
            mTitle.setText(title);
        }
        if (!TextUtils.isEmpty(coverUrl)) {
            Glide.with(mCover.getContext()).load(coverUrl).centerCrop().into(mCover);
        }

    }

    public TextView getTitle() {
        return mTitle;
    }

}
