package com.ktc.ecuador.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ktc.ecuador.R;
import com.ktc.ecuador.data.protocal.CategoryDetail;
import com.ktc.ecuador.data.protocal.ItemBean;
import com.ktc.ecuador.data.protocal.SearchResultApp;
import com.ktc.ecuador.utils.FontUtils;

import java.util.List;


public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    public static final int TYPE_APPS = 0x001;
    public static final int TYPE_VIDEO = 0x002;
    public static final int TYPE_MOVIE = 0x003;
    private List<ItemBean> mSearchResponseBeans;
    private Context mContext;
    private OnItemClickListener listener;
    private OnItemSelectListener selectedListener;

    public SearchResultAdapter(Context context, List<ItemBean> responseBeans) {
        this.mContext = context;
        this.mSearchResponseBeans = responseBeans;
        this.setHasStableIds(true);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public OnItemSelectListener getOnItemSelectListener() {
        return selectedListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        selectedListener = onItemSelectListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_search_result, null);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        switch (getItemViewType(position)) {
            case TYPE_APPS: {
                holder.searchResultImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.selector_search_result_app, null));
                break;
            }
            case TYPE_MOVIE: {
                holder.searchResultImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.selector_search_result_movie, null));
                break;
            }
            case TYPE_VIDEO: {
                holder.searchResultImage.setImageDrawable(mContext.getResources().getDrawable(R.drawable.selector_search_result_video, null));
                break;
            }
            default:
        }
        holder.searchResultName.setText(mSearchResponseBeans.get(position).getTitle());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClick(position);
            }
        });

        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (selectedListener != null) {
                    if (b) {
                        selectedListener.onItemSelect(position, getItemViewType(position));
                        holder.searchResultName.setTextColor(mContext.getResources().getColor(R.color.colorSearchName, null));
                        holder.searchResultName.setSelected(true);
                    } else {
                        selectedListener.omItemUnSelect(position);
                        holder.searchResultName.setTextColor(mContext.getResources().getColor(R.color.colorWhite, null));
                        holder.searchResultName.setSelected(false);
                    }
                }

            }
        });


    }

    @Override
    public int getItemViewType(int position) {
        if ((mSearchResponseBeans.get(position)) instanceof SearchResultApp.ListBean) {
            return TYPE_APPS;
        } else {
            if ("1".equals(((CategoryDetail.CategoryItem) mSearchResponseBeans.get(position)).getType_id())) {
                return TYPE_MOVIE;
            } else {
                return TYPE_VIDEO;
            }
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mSearchResponseBeans.size();
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemSelectListener {
        void onItemSelect(int position, int type);

        void omItemUnSelect(int position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView searchResultImage;
        private TextView searchResultName;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            searchResultImage = itemView.findViewById(R.id.search_result_img);
            searchResultName = itemView.findViewById(R.id.search_result_name);
            FontUtils.getInstance(mContext).setRegularFont(searchResultName);
        }
    }
}
