package com.mp3.launcher4.customs.views.complex;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.mp3.launcher4.R;
import com.mp3.launcher4.customs.views.complex.holds.EmptyHolder;
import com.mp3.launcher4.customs.views.complex.holds.TitleRecyclerHolder;
import com.mp3.launcher4.customs.views.complex.holds.TvHolder;

import java.util.List;

/**
 * @author longzj
 */
public class ComplexAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_TV = 0;
    public static final int TYPE_RECYCLER_WITH_TITLE = 1;
    private Context mContext;
    private List<BaseComplexProxy> mData;

    public ComplexAdapter(Context context, List<BaseComplexProxy> data) {
        this.mContext = context;
        this.mData = data;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        viewType = mData.get(viewType).getViewType();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        switch (viewType) {
            case TYPE_TV:
                return new TvHolder(inflater.inflate(R.layout.proxy_source, null, false));
            case TYPE_RECYCLER_WITH_TITLE:
                return new TitleRecyclerHolder(inflater.inflate(R.layout.proxy_title_recycler, null, false));
            default:
                return new EmptyHolder(new ViewStub(mContext));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        BaseComplexProxy proxy = mData.get(position);
        proxy.load(holder);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
//        mData.get(holder.getAdapterPosition()).onViewRecycled();
    }

}
