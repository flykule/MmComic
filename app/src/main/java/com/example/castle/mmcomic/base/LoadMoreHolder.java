package com.example.castle.mmcomic.base;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.castle.mmcomic.R;

import butterknife.ButterKnife;

/**
 * Created by castle on 16-8-25.
 * 加载更多的holder
 */
public class LoadMoreHolder extends RecyclerView.ViewHolder {

    //正在加载更多
    public static final int LOADING = 0;
    //加载失败
    public static final int ERROR = 1;
    //没有加载更多
    public static final int NONE = 2;
    LinearLayout mItemLoadmoreContainerLoading;
    LinearLayout mItemLoadmoreContainerRetry;
    TextView mItemLoadmoreTvRetry;

    public LoadMoreHolder(View itemView) {
        super(itemView);
        //ButterKnife.bind(this, itemView);
        mItemLoadmoreContainerLoading = ButterKnife.findById(itemView, R.id.item_loadmore_container_loading);
        mItemLoadmoreContainerRetry = ButterKnife.findById(itemView, R.id.item_loadmore_container_retry);
    }

    public void refreshHolderView(Integer state) {
        //隐藏全部view
        mItemLoadmoreContainerLoading.setVisibility(View.GONE);
        mItemLoadmoreContainerRetry.setVisibility(View.GONE);
        switch (state) {
            case LOADING:
                mItemLoadmoreContainerLoading.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                mItemLoadmoreContainerRetry.setVisibility(View.VISIBLE);
                break;
            case NONE:

                break;
            default:
                break;
        }


    }

}
