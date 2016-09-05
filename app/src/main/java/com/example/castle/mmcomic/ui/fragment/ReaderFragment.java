package com.example.castle.mmcomic.ui.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.base.Constant;
import com.example.castle.mmcomic.managers.LocalComicHandler;
import com.example.castle.mmcomic.models.Comic;
import com.example.castle.mmcomic.models.Storage;
import com.example.castle.mmcomic.parser.BaseParser;
import com.example.castle.mmcomic.parser.ParserFactory;
import com.example.castle.mmcomic.parser.RarParser;
import com.example.castle.mmcomic.ui.activity.ReaderActivity;
import com.example.castle.mmcomic.ui.view.PageImageView;
import com.example.castle.mmcomic.utils.SysUtil;
import com.example.castle.mmcomic.utils.UiUtils;
import com.example.castle.mmcomic.utils.Utils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by castle on 16-9-4.
 * 阅读页
 */
public class ReaderFragment extends Fragment implements View.OnTouchListener {
    public static final String RESULT_CURRENT_PAGE = "fragment.reader.currentPage";
    public static final String PARAM_HANDLER = "PARAM_HANDLER";
    public static final String PARAM_MODE = "PARAM_MODE";
    public static final String STATE_FULLSCREEN = "STATE_FULLSCREEN";
    public static final String STATE_NEW_COMIC = "STATE_NEW_COMIC";
    public static final String STATE_NEW_COMIC_TITLE = "STATE_NEW_COMIC_TITLE";
    private static final int RESULT = 1;
    private static HashMap<Integer, Constant.PageViewMode> RESOURCE_VIEW_MODE;

    static {
        RESOURCE_VIEW_MODE = new HashMap<>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, Constant.PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, Constant.PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, Constant.PageViewMode.FIT_WIDTH);
    }

    @BindView(R.id.viewPager)
    ComicViewPager mViewPager;
    private SparseArray<Target> mTargets = new SparseArray<>();
    private Comic mComic;
    private Comic mNewComic;
    private int mCurrentPage;
    private BaseParser mParser;
    private String mFileName;
    private LocalComicHandler mComicHandler;
    private SharedPreferences mPreferences;
    private Constant.PageViewMode mPageViewMode;
    private boolean mIsLeftToRight;
    private LinearLayout mPageNavLayout;
    private SeekBar mPageSeekBar;
    private Picasso mPicasso;
    private boolean mFullScreen;
    private int mNewComicTitle;
    private TextView mPageNavTextView;
    private ComicPagerAdapter mPagerAdapter;
    private GestureDetector mGestureDetector;

    /**
     * 根据comicId创建fragment
     *
     * @param comicId 漫画id
     * @return 新的fragment
     */
    public static ReaderFragment newInstance(int comicId) {
        Bundle args = new Bundle();
        ReaderFragment fragment = new ReaderFragment();
        args.putSerializable(PARAM_MODE, Mode.MODE_LIBRARY);
        args.putInt(PARAM_HANDLER, comicId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 根据漫画路径创建Fragment
     *
     * @param comicPath 漫画路径
     * @return 创建好的fragment
     */
    public static ReaderFragment newInstance(File comicPath) {
        Bundle args = new Bundle();
        ReaderFragment fragment = new ReaderFragment();
        args.putSerializable(PARAM_MODE, Mode.MODE_BROWSER);
        args.putSerializable(PARAM_HANDLER, comicPath);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * 提供重载
     *
     * @param currentPage 当前页面
     * @param animated    是否动态滑动
     */
    public void setCurrentPage(int currentPage, boolean animated) {
        if (mIsLeftToRight) {
            mViewPager.setCurrentItem(currentPage - 1);
            mPageSeekBar.setProgress(currentPage - 1);
        } else {
            mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() - currentPage, animated);
            mPageSeekBar.setProgress(mViewPager.getAdapter().getCount() - currentPage);
        }
        String navPage = String.format("%s/%s", currentPage, mParser.pageCount());
        mPageNavTextView.setText(navPage);
    }

    public int getCurrentPage() {
        if (mIsLeftToRight) {
            return mViewPager.getCurrentItem() + 1;
        }
        return mViewPager.getAdapter().getCount() - mViewPager.getCurrentItem();
    }

    /**
     * 设置当前页面
     *
     * @param currentPage 当前页
     */
    public void setCurrentPage(int currentPage) {
        setCurrentPage(currentPage, true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        //根据参数类型决定接下来的行为
        Mode mode = (Mode) bundle.getSerializable(PARAM_MODE);


        mPagerAdapter = new ComicPagerAdapter();
        File file = null;
        if (mode == Mode.MODE_LIBRARY) {
            int comicId = bundle.getInt(PARAM_HANDLER);
            mComic = Storage.getStorage(getActivity()).getComic(comicId);
            file = mComic.getFile();
            mCurrentPage = mComic.getCurrentPage();
        } else if (mode == Mode.MODE_BROWSER) {
            file = (File) bundle.getSerializable(PARAM_HANDLER);
        }
        mParser = ParserFactory.create(file);
        mComicHandler = new LocalComicHandler(mParser);
        mPicasso = new Picasso.Builder(getActivity())
                .addRequestHandler(mComicHandler)
                .build();
        mFileName = file.getName();

        mCurrentPage = Math.max(1, Math.min(mCurrentPage, mParser.pageCount()));

        mComicHandler = new LocalComicHandler(mParser);
        mGestureDetector = new GestureDetector(getContext(), new MyTouchListener());
        mPreferences = getActivity().getSharedPreferences(Constant.SETTINGS_NAME, Context.MODE_PRIVATE);
        int viewModeInt = mPreferences.getInt(Constant.SETTINGS_PAGE_VIEW_MODE,
                Constant.PageViewMode.ASPECT_FILL.native_int);
        mPageViewMode = Constant.PageViewMode.values()[viewModeInt];
        mIsLeftToRight = mPreferences.getBoolean(Constant.SETTINGS_READING_LEFT_TO_RIGHT, true);

        //    如果是rar格式则进行解压
        if (mParser instanceof RarParser) {
            File cacheDir = new File(getActivity().getExternalCacheDir(), "c");
            if (!cacheDir.exists()) {
                cacheDir.mkdir();
            } else {
                for (File file1 : cacheDir.listFiles()) {
                    file1.delete();
                }
            }
            ((RarParser) mParser).setCacheDirectory(cacheDir);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reader, container, false);
        ButterKnife.bind(this, view);
        initPageSeekBar();
        mPageNavTextView = (TextView) mPageNavLayout.findViewById(R.id.pageNavTextView);

        initViewPager();
        if (mCurrentPage != -1) {
            setCurrentPage(mCurrentPage);
            mCurrentPage = -1;
        }
        if (savedInstanceState != null) {
            boolean fullScreen = savedInstanceState.getBoolean(STATE_FULLSCREEN);
            setFullScreen(fullScreen);
            int newComicId = savedInstanceState.getInt(STATE_NEW_COMIC);
            if (newComicId != -1) {
                int titleRes = savedInstanceState.getInt(STATE_NEW_COMIC_TITLE);
                confirmSwitch(Storage.getStorage(getActivity()).getComic(newComicId), titleRes);
            }
        } else {
            setFullScreen(true);
        }
        getActivity().setTitle(mFileName);
        updateSeekBar();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);
        switch (mPageViewMode) {
            case ASPECT_FILL:
                menu.findItem(R.id.view_mode_aspect_fill).setChecked(true);
                break;
            case ASPECT_FIT:
                menu.findItem(R.id.view_mode_aspect_fit).setChecked(true);
                break;
            case FIT_WIDTH:
                menu.findItem(R.id.view_mode_fit_width).setChecked(true);
                break;
        }
        if (mIsLeftToRight) {
            menu.findItem(R.id.reading_left_to_right).setChecked(true);
        } else {
            menu.findItem(R.id.reading_right_to_left).setChecked(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_FULLSCREEN, isFullScreen());
        outState.putInt(STATE_NEW_COMIC, mNewComic != null ? mNewComic.getId() : -1);
        outState.putInt(STATE_NEW_COMIC_TITLE, mNewComic != null ? mNewComicTitle : -1);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        if (mComic != null) {
            mComic.setCurrentPage(getCurrentPage());
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        try {
            mParser.destroy();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mPicasso.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SharedPreferences.Editor editor = mPreferences.edit();
        switch (item.getItemId()) {
            case R.id.view_mode_aspect_fill:
            case R.id.view_mode_aspect_fit:
            case R.id.view_mode_fit_width:
                item.setChecked(true);
                mPageViewMode = RESOURCE_VIEW_MODE.get(item.getItemId());
                editor.putInt(Constant.SETTINGS_PAGE_VIEW_MODE, mPageViewMode.native_int).apply();
                updatePageViews(mViewPager);
                break;
            case R.id.reading_left_to_right:
            case R.id.reading_right_to_left:
                item.setChecked(true);
                int page = getCurrentPage();
                mIsLeftToRight = (item.getItemId() == R.id.reading_left_to_right);
                editor.putBoolean(Constant.SETTINGS_READING_LEFT_TO_RIGHT, mIsLeftToRight).apply();
                setCurrentPage(page, false);
                mViewPager.getAdapter().notifyDataSetChanged();
                updateSeekBar();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updatePageViews(ComicViewPager viewPager) {

    }

    public boolean isFullScreen() {
        return mFullScreen;
    }

    private void setFullScreen(boolean fullScreen) {
        setFullScreen(fullScreen, false);
    }

    private void updateSeekBar() {
        int seekRes = mIsLeftToRight ? R.drawable.reader_nav_progress : R.drawable.reader_nav_progress_inverse;
        Drawable drawable = UiUtils.getDrawable(seekRes);
        Rect bounds = mPageSeekBar.getProgressDrawable().getBounds();
        drawable.setBounds(bounds);
        mPageSeekBar.setProgressDrawable(drawable);
    }

    //确认切换
    private void confirmSwitch(Comic comic, int titleRes) {
        if (comic == null) {
            return;
        }
        mNewComic = comic;
        mNewComicTitle = titleRes;
        AlertDialog dialog = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleRes)
                .setMessage(comic.getFile().getName())
                .setPositiveButton(R.string.switch_action_positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ReaderActivity activity = (ReaderActivity) getActivity();
                        activity.setFragment(ReaderFragment.newInstance(mNewComic.getId()));
                    }
                })
                .setNegativeButton(R.string.switch_action_negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNewComic = null;
                    }
                })
                .create();
        dialog.show();
    }

    //设置沉浸式
    private void setFullScreen(boolean fullScreen, boolean animated) {
        mFullScreen = fullScreen;

        ActionBar actionBar = getActionBar();

        if (fullScreen) {
            if (actionBar != null) actionBar.hide();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (SysUtil.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.INVISIBLE);
        } else {
            if (actionBar != null) actionBar.show();

            int flag =
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.VISIBLE);

            // status bar & navigation bar background won't show in some cases
            if (SysUtil.isLollipopOrLater()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Window w = getActivity().getWindow();
                        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                    }
                }, 300);
            }
        }

    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void initViewPager() {
        mViewPager.setAdapter(mPagerAdapter);
        //设置保留在内存中的页面
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnTouchListener(this);
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //根据是否从左向右进行计算
                if (mIsLeftToRight) {
                    setCurrentPage(position + 1);
                } else {
                    setCurrentPage(mViewPager.getAdapter().getCount() - position);
                }
            }
        });
        mViewPager.setOnSwipeOutListener(new ComicViewPager.OnSwipeOutListener() {
            //实现自定义控件的回调
            @Override
            public void onSwipeOutAtStart() {
                if (mIsLeftToRight) {
                    hitBegining();
                } else
                    hitEnding();
            }

            @Override
            public void onSwipeOutAtEnd() {
                if (mIsLeftToRight) {
                    hitEnding();
                } else
                    hitBegining();
            }
        });
    }

    private void hitEnding() {
        if (mComic != null) {
            Comic comic = Storage.getStorage(getActivity()).getNextComic(mComic);
            confirmSwitch(comic, R.string.switch_next_comic);
        }

    }

    private void hitBegining() {
        if (mComic != null) {
            Comic comic = Storage.getStorage(getActivity()).getNextComic(mComic);
            confirmSwitch(comic, R.string.switch_prev_comic);
        }
    }

    private void initPageSeekBar() {
        mPageNavLayout = (LinearLayout) getActivity().findViewById(R.id.pageNavLayout);
        mPageSeekBar = (SeekBar) mPageNavLayout.findViewById(R.id.pageSeekBar);
        mPageSeekBar.setMax(mParser.pageCount() - 1);
        mPageSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (mIsLeftToRight) {
                        setCurrentPage(progress + 1);
                    } else {
                        setCurrentPage(mPageSeekBar.getMax() - progress + 1);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mPicasso.pauseTag(ReaderFragment.this.getActivity());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mPicasso.resumeTag(ReaderFragment.this.getActivity());
            }
        });
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return mGestureDetector.onTouchEvent(motionEvent);
    }


    private void loadImage(MyTarget target) {
        int pos;
        if (mIsLeftToRight) {
            pos = target.position;
        } else {
            pos = mViewPager.getAdapter().getCount() - target.position - 1;
        }
        mPicasso.load(LocalComicHandler.getPageUri(pos))
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(getActivity())
                .resize(Constant.MAX_PAGE_WIDTH, Constant.MAX_PAGE_HEIGHT)
                .centerInside()
                .onlyScaleDown()
                .into(target);
    }

    private void updatePageViews(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            final View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                updatePageViews((ViewGroup) child);
            } else if (child instanceof PageImageView) {
                PageImageView view = (PageImageView) child;
                if (mPageViewMode == Constant.PageViewMode.ASPECT_FILL) {
                    view.setTranslateToRightEdge(!mIsLeftToRight);
                }
                view.setViewMode(mPageViewMode);
            }

        }
    }

    public enum Mode {
        MODE_LIBRARY,
        MODE_BROWSER
    }

    private class ComicPagerAdapter extends PagerAdapter {

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mParser.pageCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.fragment_reader_page, container, false);
            PageImageView pageImageView = ButterKnife.findById(layout, R.id.pageImageView);
            if (mPageViewMode == Constant.PageViewMode.ASPECT_FILL) {
                pageImageView.setTranslateToRightEdge(!mIsLeftToRight);
            }
            pageImageView.setViewMode(mPageViewMode);
            pageImageView.setOnTouchListener(ReaderFragment.this);
            container.addView(layout);

            MyTarget myTarget = new MyTarget(position, layout);
            loadImage(myTarget);
            mTargets.put(position, myTarget);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View layout = (View) object;
            mPicasso.cancelRequest(mTargets.get(position));
            mTargets.delete(position);

            container.removeView(layout);
            ImageView iv = ButterKnife.findById(layout, R.id.pageImageView);
            Drawable drawable = iv.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) drawable;
                Bitmap bitmap = bd.getBitmap();
                if (bitmap != null) {
                    bitmap.recycle();
                }
            }
        }

    }

    private class MyTarget implements Target, View.OnClickListener {
        public final int position;
        private WeakReference<View> mLayout;

        public MyTarget(int position, View view) {
            this.position = position;
            mLayout = new WeakReference<>(view);
        }

        /**
         * 设置三个控件是否可见
         *
         * @param imageView    图片可见性
         * @param progresssBar 进度条可见性
         * @param reloadButton 重新加载按钮可见性
         */
        private void setVisibility(int imageView, int progresssBar, int reloadButton) {
            View view = mLayout.get();
            view.findViewById(R.id.pageImageView).setVisibility(imageView);
            view.findViewById(R.id.pageProgressBar).setVisibility(progresssBar);
            view.findViewById(R.id.reloadButton).setVisibility(reloadButton);
        }

        @Override
        public void onClick(View view) {
            View layout = mLayout.get();
            if (layout == null) {
                return;
            }
            setVisibility(View.GONE, View.VISIBLE, View.GONE);
            loadImage(this);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            View view = mLayout.get();
            if (view == null) {
                return;
            }
            Log.d("TAG", "onBitmapLoaded: ");
            setVisibility(View.VISIBLE, View.GONE, View.GONE);
            ImageView imageView = (ImageView) view.findViewById(R.id.pageImageView);
            imageView.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            View view = mLayout.get();
            if (view == null) {
                return;
            }
            Log.d("TAG", "onBitmapFailed: ");
            setVisibility(View.GONE, View.GONE, View.VISIBLE);
            ImageButton imageButton = (ImageButton) view.findViewById(R.id.reloadButton);
            imageButton.setOnClickListener(this);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    //自定义触摸回调，可以实现根据点击范围进行换页等操作
    private class MyTouchListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!isFullScreen()) {
                setFullScreen(true, true);
                return true;
            }
            float x = e.getX();
            //点击左边边缘
            if (x < (float) mViewPager.getWidth() / 3) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == 1) {
                        hitBegining();
                    } else {
                        setCurrentPage(getCurrentPage() - 1);
                    }
                } else {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount()) {
                        hitEnding();
                    } else {
                        setCurrentPage(getCurrentPage() + 1);
                    }
                }
            }
            //点击右边边缘
            if (x > (float) mViewPager.getWidth() / 3 * 1) {
                if (mIsLeftToRight) {
                    if (getCurrentPage() == mViewPager.getAdapter().getCount()) {
                        hitEnding();
                    } else {
                        setCurrentPage(getCurrentPage() + 1);
                    }
                } else {
                    if (getCurrentPage() == 1) {
                        hitBegining();
                    } else {
                        setCurrentPage(getCurrentPage() - 1);
                    }
                }
            } else {
                setFullScreen(false, true);
            }
            return true;
        }
    }


}
