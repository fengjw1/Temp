package com.mp3.launcher4.customs.views.complex.holds;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.customs.layoutmanagers.HorizontalLayoutManager;
import com.mp3.launcher4.customs.views.SpecialLimitRecyclerView;

/**
 * @author longzj
 */
public class TitleRecyclerHolder extends RecyclerView.ViewHolder {
    private TextView mTitleText;
    private SpecialLimitRecyclerView mRecyclerView;

    public TitleRecyclerHolder(View itemView) {
        super(itemView);
        mTitleText = (TextView) itemView.findViewById(R.id.proxy_title);
        mRecyclerView = (SpecialLimitRecyclerView) itemView.findViewById(R.id.proxy_recycle);
    }

    public TextView getTitleText() {
        return mTitleText;
    }

    public SpecialLimitRecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public void setTitle(String text) {
        mTitleText.setText(text);
    }

    public HorizontalLayoutManager getLayoutManager() {
        return (HorizontalLayoutManager) mRecyclerView.getLayoutManager();
    }

    public void notifyDataSetChanged() {
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    public void notifyItemChanged(int pos) {
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        adapter.notifyItemChanged(pos, "");
    }

    public void notifyItemRemoved(int pos) {
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        adapter.notifyItemRemoved(pos);
    }

    public void notifyItemInsert(int pos) {
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        adapter.notifyItemInserted(pos);
    }

    public void notifyItemMove(int from, int to) {
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        adapter.notifyItemMoved(from, to);
    }

    public void notifyItemRangeChanged(int from, int size, Object payloads) {
        final RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        adapter.notifyItemRangeChanged(from, size, payloads);
    }

    public boolean isNotifyAnimRunning() {
        return mRecyclerView.getItemAnimator().isRunning();
    }
}
