package com.mp3.launcher4.customs.views.complex.holds;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import com.mp3.launcher4.R;
import com.mp3.launcher4.adapters.BaseTitleRecyclerAdapter;
import com.mp3.launcher4.customs.BaseAdapter;
import com.mp3.launcher4.customs.views.SpecialLimitRecyclerView;
import com.mp3.launcher4.utils.FontUtils;

/**
 * @author longzj
 */
public class TvHolder extends RecyclerView.ViewHolder {

    private SurfaceView mSurfaceView;
    private TextView mTvName;
    private TextView mExpanded;
    private RecyclerView mEpgRecycler;
    private SpecialLimitRecyclerView mSourceRecycler;
    private Context mContext;

    public TvHolder(View itemView) {
        super(itemView);
        mContext=itemView.getContext();
        mSurfaceView = (SurfaceView) itemView.findViewById(R.id.source_tv_window);
        mTvName = (TextView) itemView.findViewById(R.id.source_name);
        mExpanded = (TextView) itemView.findViewById(R.id.source_expand);
        mEpgRecycler = (RecyclerView) itemView.findViewById(R.id.source_epg_recycler);
        mSourceRecycler = (SpecialLimitRecyclerView) itemView.findViewById(R.id.source_list_recycler);

        FontUtils fontUtils = FontUtils.getInstance(mContext);
        fontUtils.setRegularFont(mTvName);
        fontUtils.setRegularFont(mExpanded);

    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public TextView getTvName() {
        return mTvName;
    }

    public void setTvName(String tvName) {
        mTvName.setText(tvName);
    }

    public void setTvName(int textId) {
        mTvName.setText(textId);
    }

    public TextView getExpanded() {
        return mExpanded;
    }

    public void setExpanded(String expanded) {
        mExpanded.setText(expanded);
    }

    public RecyclerView getEpgRecycler() {
        return mEpgRecycler;
    }

    public SpecialLimitRecyclerView getSourceRecycler() {
        return mSourceRecycler;
    }

    public void initSourceRecycler(RecyclerView.LayoutManager layoutManager,
                                   BaseAdapter baseAdapter,
                                   RecyclerView.ItemDecoration itemDecoration) {
        mSourceRecycler.setLayoutManager(layoutManager);
        mSourceRecycler.setAdapter(baseAdapter);
        mSourceRecycler.addItemDecoration(itemDecoration);
    }

    public BaseTitleRecyclerAdapter getSourceAdapter() {
        return (BaseTitleRecyclerAdapter) mSourceRecycler.getAdapter();
    }

    public void notifySourceDataChange() {
        getSourceAdapter().notifyDataSetChanged();
    }

    public void initEpgRecyclerView(RecyclerView.LayoutManager layoutManager,
                                    BaseAdapter baseAdapter,
                                    RecyclerView.ItemDecoration itemDecoration) {
        mEpgRecycler.setLayoutManager(layoutManager);
        mEpgRecycler.setAdapter(baseAdapter);
        mEpgRecycler.addItemDecoration(itemDecoration);
    }

    public void notifyEpgDataChanged() {
        mEpgRecycler.getAdapter().notifyDataSetChanged();
    }

}
