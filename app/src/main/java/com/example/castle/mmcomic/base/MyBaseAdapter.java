package com.example.castle.mmcomic.base;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.utils.ThreadPoolProxyFactory;
import com.example.castle.mmcomic.utils.UiUtils;

import java.util.List;

/**
 * Created by castle on 16-8-21.
 * recycler view adapter抽取
 */
public abstract class MyBaseAdapter<T, V extends RecyclerView.ViewHolder>
        extends RecyclerView.Adapter {
    //item_tye标记常量,需要加载更多数据的标记
    private static final int VEIWTYPE_LOADMORE = 0;
    //item_tye标记常量,普通数据的标记
    private static final int VEIWTYPE_NORMAL = 1;
    //数据集合,存放item
    public List<T> mDataSet;
    private int PAGERSIZE;
    //加载更多任务
    private LoadMoreTask mLoadMoreTask;

    public MyBaseAdapter(List<T> dataSet) {
        mDataSet = dataSet;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VEIWTYPE_LOADMORE:
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_load_more, parent, false);
                return new LoadMoreHolder(view);
            default:
                return getViewHolder(parent);
        }
    }

    /**
     * 根据itemType判断是加载更多还是继续填充
     *
     * @param holder   返回的holder
     * @param position item位置
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VEIWTYPE_NORMAL:
                T currentItem = mDataSet.get(position);
                popHolder((V) holder, currentItem, position);
                break;
            case VEIWTYPE_LOADMORE:
                if (hasMoreData()) {
                    loadMore((LoadMoreHolder) holder);
                    //notifyDataSetChanged();
                }
                break;
        }
    }

    /**
     * 加载更多数据
     */
    protected void loadMore(LoadMoreHolder holder) {
        if (mLoadMoreTask == null) {
            //处理loadmoreholder的ui
            int state = LoadMoreHolder.LOADING;
            holder.refreshHolderView(state);

            mLoadMoreTask = new LoadMoreTask(holder);
            ThreadPoolProxyFactory.getNormalThreadPoolProxy().execute(mLoadMoreTask);
        }
    }

    /**
     * @return 返回item个数
     */
    @Override
    public int getItemCount() {
        if (hasMoreData()) {
            return mDataSet == null ? 0 : mDataSet.size() + 1;
        } else {
            return mDataSet == null ? 0 : mDataSet.size();
        }
    }

    /**
     * 根据位置返回item类型
     *
     * @param position item索引
     * @return item类型, 使用常量标志
     */
    @Override
    public int getItemViewType(int position) {
        //下滑到达底部,刷新并加载更多数据
        if (position == mDataSet.size()) {
            return VEIWTYPE_LOADMORE;
        }
        return getNormalViewType(position);
    }

    /**
     * 返回正常view type
     *
     * @param position 索引
     * @return view type
     */
    public int getNormalViewType(int position) {
        return VEIWTYPE_NORMAL;
    }


    /**
     * 由子类实现创建ViewHolder
     *
     * @param parent 根viewGroup
     * @return
     */
    protected abstract V getViewHolder(ViewGroup parent);

    /**
     * 填充ViewHolder数据
     *
     * @param holder 当前holder
     * @param item   当前数据条目
     */
    protected abstract void popHolder(V holder, T item, int position);

    /**
     * 判断是否有更多数据能够进行加载
     *
     * @return 是否有更多数据
     */
    protected abstract boolean hasMoreData();

    /**
     * 得到更多数据
     *
     * @return 返回数据list
     */
    protected abstract List<T> getMoreData() throws Exception;

    class LoadMoreTask implements Runnable {
        LoadMoreHolder mHolder;

        public LoadMoreTask(LoadMoreHolder holder) {
            mHolder = holder;
        }

        @Override
        public void run() {
            //初始状态和数据
            int state = LoadMoreHolder.LOADING;
            List<T> moreData = null;
            try {
                moreData = getMoreData();
            } catch (Exception e) {
                state = LoadMoreHolder.ERROR;
            }
            //根据数据修正状态
            if (moreData == null) {
                //加载失败
                state = LoadMoreHolder.ERROR;
            } else {
                //返回数据长度等于分页长度
                if (moreData.size() == PAGERSIZE) {
                    //还能加载更多
                    state = LoadMoreHolder.LOADING;
                } else {
                    //数据长度小于请求长度
                    state = LoadMoreHolder.NONE;
                }
            }
            final int resultState = state;
            final List<T> dataList = moreData;
            UiUtils.postTastSafely(new Runnable() {
                @Override
                public void run() {
                    mDataSet.addAll(dataList);
                    notifyDataSetChanged();
                    mHolder.refreshHolderView(resultState);
                }
            });
            mLoadMoreTask = null;
        }
    }

}
