package com.example.castle.mmcomic.ui.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.managers.DirectoryListManager;
import com.example.castle.mmcomic.ui.view.DirectorySelectDialog;
import com.example.castle.mmcomic.utils.ImageUtil;
import com.example.castle.mmcomic.utils.SharedPrefUtil;
import com.example.castle.mmcomic.utils.UiUtils;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by castle on 16-9-1.
 * 首页fragment，扫描指定文件夹并显示相应内容
 */
public class LibraryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {
    public static final String KEY_BUNDLE_DIRECTORY_DIALOG_SHOW = "KEY_BUNDLE_DIRECTORY_DIALOG_SHOW";
    @BindView(R.id.gv_group)
    GridView mGvGroup;
    @BindView(R.id.swipe_layout_library)
    SwipeRefreshLayout mSwipeLayoutLibrary;
    @BindView(R.id.ll_library_empty)
    LinearLayout mLlLibraryEmpty;

    private Picasso mPicasso;
    private DirectoryListManager mListManager;
    private DirectorySelectDialog mSelectDialog;
    private boolean mIsRefreshPlanned = false;
    private Handler mUpdateHandler = new UpdateHandler();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_library, container, false);
        mPicasso = ImageUtil.getPicasso();
        ButterKnife.bind(this, view);
        //刷新layout初始化
        mSwipeLayoutLibrary.setColorSchemeColors(UiUtils.getColor(R.color.colorPrimary));
        mSwipeLayoutLibrary.setOnRefreshListener(this);
        mSwipeLayoutLibrary.setEnabled(true);

        mGvGroup.setAdapter(new GroupBrowserAdapter());
        mGvGroup.setOnItemClickListener(this);
        //根据屏幕尺寸计算应该有的列数
        int deviceWidth = UiUtils.getDeviceWidth();
        int columWidth = UiUtils.getInteger(R.integer.grid_comic_column_width);
        int numColums = Math.round((float) deviceWidth / columWidth);
        mGvGroup.setNumColumns(numColums);

        getActivity().setTitle("Library");

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    //没有数据，显示空页面和信息
    private void showEmptyMessage(boolean show) {
        mLlLibraryEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        mSwipeLayoutLibrary.setEnabled(!show);
    }

    private String getLibraryDIr() {
        return SharedPrefUtil.getPreferences()
                .getString(Constant.SETTINGS_LIBRARY_DIR, null);
    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private final class GroupBrowserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            return null;
        }
    }
}
