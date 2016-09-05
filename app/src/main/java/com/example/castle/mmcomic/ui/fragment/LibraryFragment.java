package com.example.castle.mmcomic.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.managers.DirectoryListManager;
import com.example.castle.mmcomic.managers.LocalCoverHandler;
import com.example.castle.mmcomic.managers.Scanner;
import com.example.castle.mmcomic.models.Comic;
import com.example.castle.mmcomic.models.Storage;
import com.example.castle.mmcomic.ui.activity.MainActivity;
import com.example.castle.mmcomic.ui.view.DirectorySelectDialog;
import com.example.castle.mmcomic.utils.ImageUtil;
import com.example.castle.mmcomic.utils.SharedPrefUtil;
import com.example.castle.mmcomic.utils.UiUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by castle on 16-9-1.
 * 首页fragment，扫描指定文件夹并显示相应内容
 */
public class LibraryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener, DirectorySelectDialog.OnDirectorySelectListener {
    public static final String BUNDLE_DIRECTORY_DIALOG_SHOW = "BUNDLE_DIRECTORY_DIALOG_SHOW";
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
    private Handler mUpdateHandler = new UpdateHandler(this);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectDialog = new DirectorySelectDialog(getActivity());
        mSelectDialog.setCurrentDirectory(Environment.getExternalStorageDirectory());
        mSelectDialog.setOnDirectorySelectListener(this);

        getComics();
        setHasOptionsMenu(true);
    }

    //进入运行状态，判断是否在加载
    @Override
    public void onResume() {
        super.onResume();
        Scanner.getInstance().addUpdateHandler(mUpdateHandler);
        if (Scanner.getInstance().isRunning()) {
            setLoading(true);
        }
    }

    //暂停状态，释放掉更新handler
    @Override
    public void onPause() {
        Scanner.getInstance().removeUpdateHandler(mUpdateHandler);
        super.onPause();
    }

    /**
     * 得到当前漫画列表
     */
    private void getComics() {
        List<Comic> comics = Storage.getStorage(UiUtils.getContext()).listDirectoryComics();
        mListManager = new DirectoryListManager(getLibraryDir(), comics);
    }

    private void refreshLibraryDisplayed() {
        if (!mIsRefreshPlanned) {
            Runnable updateRunnable = new Runnable() {
                @Override
                public void run() {
                    getComics();
                    ((BaseAdapter) mGvGroup.getAdapter()).notifyDataSetChanged();
                    mIsRefreshPlanned = false;
                }
            };
            mIsRefreshPlanned = true;
            mGvGroup.postDelayed(updateRunnable, 100);
        }
    }

    //设置加载界面
    private void setLoading(boolean isLoading) {
        if (isLoading) {
            mSwipeLayoutLibrary.setRefreshing(true);
            mGvGroup.setOnItemClickListener(null);
        } else {
            mSwipeLayoutLibrary.setRefreshing(false);
            showEmptyMessage(mListManager.getCount() == 0);
            mGvGroup.setOnItemClickListener(this);
        }
    }

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
        int columWidth = UiUtils.getInteger(R.integer.grid_group_column_width);
        int numColums = Math.round((float) deviceWidth / columWidth);
        mGvGroup.setNumColumns(numColums);

        showEmptyMessage(mListManager.getCount() == 0);
        getActivity().setTitle("Library");

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.library, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_set_dir:
                if (Scanner.getInstance().isRunning()) {
                    Scanner.getInstance().stop();
                }
                mSelectDialog.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_DIRECTORY_DIALOG_SHOW,
                (mSelectDialog != null) && mSelectDialog.isShowing());
        super.onSaveInstanceState(outState);
    }

    //没有数据，显示空页面和信息
    private void showEmptyMessage(boolean show) {
        mLlLibraryEmpty.setVisibility(show ? View.VISIBLE : View.GONE);
        mSwipeLayoutLibrary.setEnabled(!show);
    }

    private String getLibraryDir() {
        return SharedPrefUtil.getPreferences()
                .getString(Constant.SETTINGS_LIBRARY_DIR, null);
    }

    @Override
    public void onRefresh() {
        if (!Scanner.getInstance().isRunning()) {
            setLoading(true);
            Scanner.getInstance().scanLibrary();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String path = mListManager.getDirectoryAtIndex(i);
        LibraryBrowserFragment fragment = LibraryBrowserFragment.newInstance(path);
        ((MainActivity) getActivity()).pushFragment(fragment);
    }

    /**
     * 保存文件目录信息
     *
     * @param file 文件
     */
    @Override
    public void onDirectorySelect(File file) {
        SharedPreferences.Editor editor = SharedPrefUtil.getPreferences().edit();
        editor.putString(Constant.SETTINGS_LIBRARY_DIR, file.getAbsolutePath()).apply();
        Scanner.getInstance().forceScanLibary();
        showEmptyMessage(false);
        setLoading(true);
    }

    private static class UpdateHandler extends Handler {
        private WeakReference<LibraryFragment> mLibraryFragment;

        public UpdateHandler(LibraryFragment ref) {
            mLibraryFragment = new WeakReference<>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            LibraryFragment fragment = mLibraryFragment.get();
            if (fragment == null) {
                return;
            }
            switch (msg.what) {
                case Constant.MESSAGE_MEDIA_UPDATED:
                    fragment.refreshLibraryDisplayed();
                    break;
                case Constant.MESSAGE_MEDIA_UPDATE_FINISHED:
                    fragment.getComics();
                    ((BaseAdapter) fragment.mGvGroup.getAdapter()).notifyDataSetChanged();
                    fragment.setLoading(false);
                    //加入一行刷新侧滑栏检查效果
                    // TODO: 16-9-5 要动态刷新侧滑栏效果
                    ((MainActivity) mLibraryFragment.get().getActivity()).setNavBar();
                    break;
            }
        }
    }

    private final class GroupBrowserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mListManager.getCount();
        }

        @Override
        public Object getItem(int i) {
            return mListManager.getComicAtIndex(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            Comic comic = mListManager.getComicAtIndex(i);
            String displayAtIndex = mListManager.getDirectoryDisplayAtIndex(i);
            if (view == null) {
                view = getActivity().getLayoutInflater()
                        .inflate(R.layout.card_group, viewGroup, false);
            }
            ImageView imageView = (ImageView) view.findViewById(R.id.card_group_imageview);
            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic))
                    .into(imageView);
            TextView textView = (TextView) view.findViewById(R.id.comic_group_folder);
            textView.setText(displayAtIndex);
            return view;
        }
    }
}
