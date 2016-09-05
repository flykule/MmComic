package com.example.castle.mmcomic.ui.fragment;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.managers.LocalCoverHandler;
import com.example.castle.mmcomic.models.Comic;
import com.example.castle.mmcomic.models.Storage;
import com.example.castle.mmcomic.ui.activity.ReaderActivity;
import com.example.castle.mmcomic.ui.view.CoverImageView;
import com.example.castle.mmcomic.utils.ImageLoader;
import com.example.castle.mmcomic.utils.ToastUtil;
import com.example.castle.mmcomic.utils.UiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by castle on 16-9-3.
 * 浏览漫画库的Fragment,
 */
public class LibraryBrowserFragment extends Fragment
        implements SearchView.OnQueryTextListener {

    public static final String PARAM_PATH = "PARAM_PATH";

    private static final int ITEM_VIEW_TYPE_COMIC = 1;
    private static final int ITEM_VIEW_TYPE_HEADER_RECENT = 2;
    private static final int ITEM_VIEW_TYPE_HEADER_ALL = 3;

    private static final int NUM_HEADEARS = 2;
    @BindView(R.id.library_grid)
    RecyclerView mLibraryGrid;

    private List<Comic> mComics = new ArrayList<>();
    private List<Comic> mAllItems = new ArrayList<>();
    private List<Comic> mRecentItems = new ArrayList<>();

    private String mPath;
    //搜索关键字
    private String mFIlterSearch = "";
    private int mFilterRead = R.id.menu_browser_filter_all;


    public static LibraryBrowserFragment newInstance(String path) {
        Bundle args = new Bundle();
        args.putString(PARAM_PATH, path);
        LibraryBrowserFragment fragment = new LibraryBrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPath = getArguments().getString(PARAM_PATH);
        getComics();
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_librarybrowser, container, false);
        ButterKnife.bind(this, view);
        final int numColumns = calculateNumColumns();

        int spacing = (int) UiUtils.getDimension(R.dimen.grid_margin);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(createSpanSizeLookup());

        mLibraryGrid.setHasFixedSize(true);
        mLibraryGrid.setLayoutManager(layoutManager);
        mLibraryGrid.setAdapter(new ComicGridAdapter());
        mLibraryGrid.addItemDecoration(new GridSpacingItemDecoration(numColumns, spacing));

        getActivity().setTitle(new File(getArguments().getString(PARAM_PATH)).getName());
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.broswer, menu);
        //初始化search项
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_browser_filter_all:
            case R.id.menu_browser_filter_read:
            case R.id.menu_browser_filter_unread:
            case R.id.menu_browser_filter_unfinished:
            case R.id.menu_browser_filter_reading:
                item.setChecked(true);
                mFilterRead = item.getItemId();
                filterContent();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * 根据条件更新数据
     * 首先判断是搜索还是按条件过滤，统一过滤掉不符合条件的即可
     */
    private void filterContent() {
        mAllItems.clear();
        for (Comic comic : mComics) {
            //不区分大小写进行搜索
            if (mFIlterSearch.length() > 0 && !comic.getFile().getName().toLowerCase()
                    .contains(mFIlterSearch.toLowerCase()))
                continue;
            if (mFilterRead != R.id.menu_browser_filter_all) {
                //只过滤出符合条件的漫画
                if (mFilterRead == R.id.menu_browser_filter_read &&
                        comic.getCurrentPage() != comic.getTotalPages())
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_read &&
                        comic.getCurrentPage() != 0)
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_unfinished &&
                        comic.getCurrentPage() == comic.getTotalPages())
                    continue;
                if (mFilterRead == R.id.menu_browser_filter_reading &&
                        (comic.getCurrentPage() == 0 || comic.getCurrentPage() == comic.getTotalPages()))
                    continue;
            }
            mAllItems.add(comic);
        }
        //确保不为空
        if (mLibraryGrid != null) {
            if (mLibraryGrid.getAdapter() != null) {
                mLibraryGrid.getAdapter().notifyDataSetChanged();
            }
        }
    }

    /**
     * 返回指定位置的comic
     *
     * @param position 索引
     * @return 指定的comic
     */
    private Comic getComicAtPosition(int position) {
        Comic comic;
        if (hasRecent()) {
            if (position > 0 && position < mRecentItems.size() + 1)
                comic = mRecentItems.get(position - 1);
            else
                comic = mAllItems.get(position - mRecentItems.size() - NUM_HEADEARS);
        } else {
            comic = mAllItems.get(position);
        }
        return comic;
    }

    /**
     * 得到item类型
     *
     * @param position 索引
     * @return item类型
     */
    private int getItemViewTypeAtPosition(int position) {
        if (hasRecent()) {
            if (position == 0) {
                return ITEM_VIEW_TYPE_HEADER_RECENT;
            } else if (position == mRecentItems.size() + 1) {
                return ITEM_VIEW_TYPE_HEADER_ALL;
            }
        }
        return ITEM_VIEW_TYPE_COMIC;
    }

    /**
     * @return 最近是否看过漫画
     */
    private boolean hasRecent() {
        return mFIlterSearch.length() == 0
                && mFilterRead == R.id.menu_browser_filter_all
                && mRecentItems.size() > 0;
    }

    /**
     * 动态改变列宽
     *
     * @return 列宽查看器
     */
    private GridLayoutManager.SpanSizeLookup createSpanSizeLookup() {
        final int numColumns = calculateNumColumns();
        return new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                //如果是漫画则长度为1
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_COMIC) {
                    return 1;
                }
                //如果是其他则占据一行
                return numColumns;
            }
        };
    }

    /**
     * 根据屏幕信息计算列数
     *
     * @return 列数
     */
    private int calculateNumColumns() {
        int deviceWidth = UiUtils.getDeviceWidth();
        Integer columnWidth = UiUtils.getInteger(R.integer.grid_comic_column_width);
        return Math.round((float) deviceWidth / columnWidth);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFIlterSearch = newText;
        filterContent();
        return true;
    }


    public void openComic(Comic comic) {
        if (!comic.getFile().exists()) {
            ToastUtil.showShort(R.string.warning_missing_file);
            return;
        }
        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderFragment.PARAM_HANDLER, comic.getId());
        intent.putExtra(ReaderFragment.PARAM_MODE, ReaderFragment.Mode.MODE_LIBRARY);
        startActivity(intent);
    }

    private void getComics() {
        mComics = Storage.getStorage(getActivity()).listComics(mPath);
        findRecents();
        filterContent();
    }

    private void findRecents() {
        mRecentItems.clear();
        for (Comic comic : mComics) {
            if (comic.updatedAt > 0) {
                mRecentItems.add(comic);
            }
        }
        //根据更新时间重新排名
        if (mRecentItems.size() > 0) {
            Collections.sort(mRecentItems, new Comparator<Comic>() {
                @Override
                public int compare(Comic comic, Comic t1) {
                    return comic.updatedAt > t1.updatedAt ? -1 : 1;
                }
            });
        }
        if (mRecentItems.size() > Constant.MAX_RECENT_COUNT) {
            mRecentItems.subList(Constant.MAX_RECENT_COUNT, mRecentItems.size())
                    .clear();
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_header)
        TextView mTvHeader;

        HeaderViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        /**
         * 设置标题
         *
         * @param titleRes string资源
         */
        private void setTitle(@StringRes int titleRes) {
            mTvHeader.setText(titleRes);
        }
    }

    private final class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public GridSpacingItemDecoration(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        //设置间距
        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (hasRecent()) {
                //这些是头部
                if (position == 0 || position == mRecentItems.size() + 1) {
                    return;
                }
                //最近观看
                if (position > 0 && position < mRecentItems.size() + 1) {
                    position -= 1;
                } else {
                    position -= (NUM_HEADEARS + mRecentItems.size());
                }
            }

            int column = position % mSpanCount;

            //设置间距
            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;
            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }

    private final class ComicGridAdapter extends RecyclerView.Adapter {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            switch (viewType) {
                //是最上面的textview，进行设置
                case ITEM_VIEW_TYPE_HEADER_RECENT:
                    TextView view = (TextView) inflater.inflate(R.layout.header_library, parent, false);
                    view.setText(R.string.library_header_recent);
                    int spacing = (int) UiUtils.getDimension(R.dimen.grid_margin);
                    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
                    layoutParams.setMargins(0, spacing, 0, 0);
                    return new HeaderViewHolder(view);
                case ITEM_VIEW_TYPE_HEADER_ALL:
                    TextView allView = (TextView) inflater.inflate(R.layout.header_library, parent, false);
                    allView.setText(R.string.library_header_all);
                    return new HeaderViewHolder(allView);
                default:
                    View view1 = inflater.inflate(R.layout.card_comic, parent, false);
                    return new ComicViewHolder(view1);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == ITEM_VIEW_TYPE_COMIC) {
                Comic comic = getComicAtPosition(position);
                ((ComicViewHolder) holder).setupComic(comic);
            }
        }

        @Override
        public int getItemCount() {
            if (hasRecent()) {
                return mAllItems.size() + mRecentItems.size() + NUM_HEADEARS;
            }
            return mAllItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeAtPosition(position);
        }


    }

    class ComicViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.comicImageView)
        CoverImageView mComicImageView;
        @BindView(R.id.comicTitleTextView)
        TextView mComicTitleTextView;
        @BindView(R.id.comicPagerTextView)
        TextView mComicPagerTextView;
        @BindView(R.id.comicLayout)
        LinearLayout mComicLayout;

        ComicViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setClickable(true);
            view.setOnClickListener(this);
        }

        public void setupComic(Comic comic) {
            mComicTitleTextView.setText(comic.getFile().getName());
            mComicPagerTextView.setText(String.format("%s/%s",
                    comic.getCurrentPage(), comic.getTotalPages()));
            ImageLoader.load(LocalCoverHandler.getComicCoverUri(comic), mComicImageView);

        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            Comic comic = getComicAtPosition(position);
            openComic(comic);
        }
    }
}
