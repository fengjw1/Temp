package com.mp3.launcher4.customs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author longzj
 */
public abstract class BaseAdapter<T> extends RecyclerView.Adapter {

    private List<T> mData;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private OnItemSelectListener mOnItemSelectListener;
    private OnItemDispatchKeyListener mOnItemDispatchKeyListener;

    private LayoutInflater mInflater;

    public BaseAdapter(Context context, List<T> data) {
        mData = data;
        mContext = context;
        mInflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    public LayoutInflater getInflater() {
        return mInflater;
    }

    public List<T> getData() {
        return mData;
    }

    public T getItemData(int index) {
        return mData.get(index);
    }

    protected Context getContext() {
        return mContext;
    }

    private void initListener(final RecyclerView.ViewHolder holder) {
        final WeakReference<RecyclerView.ViewHolder> reference = new WeakReference<>(holder);
        final View child = holder.itemView;
        if (child == null) {
            return;
        }
        child.setSelected(false);
        child.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView.ViewHolder refHolder = reference.get();
                if (refHolder == null) {
                    return;
                }
                int pos = refHolder.getAdapterPosition();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClicked(v, pos);
                }
            }
        });

        child.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                RecyclerView.ViewHolder refHolder = reference.get();
                if (refHolder == null) {
                    return;
                }
                int pos = refHolder.getAdapterPosition();
                if (mOnItemSelectListener != null) {
                    if (hasFocus) {
                        mOnItemSelectListener.onItemSelected(refHolder.itemView, pos);
                    } else {
                        mOnItemSelectListener.onItemUnselected(v, pos);
                    }
                    refHolder.itemView.setSelected(hasFocus);
                }
            }
        });
        child.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return mOnItemDispatchKeyListener != null && mOnItemDispatchKeyListener.onDispatchKey(v, event, keyCode);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (!mData.isEmpty()) {
            initListener(holder);
            updateView(holder, position);
        }
    }

//    @Override
//    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List payloads) {
//        if (!mData.isEmpty()) {
//            initListener(holder);
//            updateView(holder, position);
//        }
//    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    /**
     * 初始化视图用
     *
     * @param holder   holder
     * @param position position
     */
    public abstract void updateView(RecyclerView.ViewHolder holder, int position);

    public OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public OnItemSelectListener getOnItemSelectListener() {
        return mOnItemSelectListener;
    }

    public void setOnItemSelectListener(OnItemSelectListener onItemSelectListener) {
        mOnItemSelectListener = onItemSelectListener;
    }

    public void setOnItemDispatchKeyListener(OnItemDispatchKeyListener listener) {
        this.mOnItemDispatchKeyListener = listener;
    }

    public void removeAllListener() {
        mOnItemClickListener = null;
        mOnItemDispatchKeyListener = null;
        mOnItemSelectListener = null;
    }

    public interface OnItemClickListener {
        /**
         * 点击时调用
         *
         * @param view     被点击的视图
         * @param position pos
         */
        void onItemClicked(View view, int position);
    }

    public interface OnItemSelectListener {
        /**
         * 选定时调用
         *
         * @param view     被选定的视图
         * @param position pos
         */
        void onItemSelected(View view, int position);

        /**
         * 未选定时调用
         *
         * @param view     被选定的视图
         * @param position pos
         */
        void onItemUnselected(View view, int position);
    }

    public interface OnItemDispatchKeyListener {
        boolean onDispatchKey(View view, KeyEvent event, int keycode);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
