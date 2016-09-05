package com.example.castle.mmcomic.ui.fragment;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.managers.LocalCoverHandler;
import com.example.castle.mmcomic.models.Comic;
import com.example.castle.mmcomic.models.Storage;
import com.example.castle.mmcomic.utils.ImageUtil;
import com.example.castle.mmcomic.utils.SysUtil;
import com.example.castle.mmcomic.utils.UiUtils;
import com.orhanobut.logger.Logger;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Random;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by castle on 16-9-2.
 * 用于显示侧滑头部图像的fragment
 */
public class HeaderFragment extends Fragment implements View.OnLayoutChangeListener, Target {


    ImageView mNavbarCover;

    ImageView mNavbarIcon;
    private Picasso mPicasso;
    //当前要显示的图片
    private Drawable mDrawable;

    //当前是否在运行
    private boolean mIsRunning;
    private Subscription mSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //View view = inflater.inflate(R.layout.fragment_header, container, false);
        View view = inflater.inflate(R.layout.fragment_header, null);
        mNavbarIcon = (ImageView) view.findViewById(R.id.navbar_icon);
        mNavbarCover = (ImageView) view.findViewById(R.id.navbar_cover);

        mPicasso = ImageUtil.getPicasso();
        //如果之前已经有图片，则使用之前的状态
        if (savedInstanceState == null) {
            mNavbarCover.addOnLayoutChangeListener(this);
        } else {
            if (mDrawable != null) showDrawable(mDrawable);
        }

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //保存状态
        setRetainInstance(true);
    }

    /**
     * 显示图片
     */
    private void showDrawable(Drawable drawable) {
        mNavbarCover.setImageDrawable(drawable);
        mNavbarIcon.animate().alpha(0).setDuration(500).setListener(null);
        mNavbarCover.animate().alpha(1).setDuration(500).setListener(null);
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (view.getWidth() > 0 && view.getHeight() > 0 && !mIsRunning) {
            createBitmap();
        }
    }

    //创建bitmap
    private void createBitmap() {
        //首先判断版本防止出错
        if (!SysUtil.isJellyBeanMR1orLater()) return;
        mIsRunning = true;
        ArrayList<Comic> comics = Storage.getStorage(UiUtils.getContext()).listComics();
        if (comics.size() > 0) {
            //使用随机的漫画作为封面
            Comic comic = comics.get(new Random().nextInt(comics.size()));
            mPicasso.load(LocalCoverHandler.getComicCoverUri(comic)).into(this);
        }
    }

    //bitmap加载完成，在这里进行图片压缩
    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        //立刻发送数据加入观察，缩放完成后值给成员变量可，直接加载即可
        mSubscription = Observable.just(bitmap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onCompleted() {
                        showDrawable(mDrawable);

                    }

                    @Override
                    public void onError(Throwable e) {
                        Logger.e(e.getMessage());
                    }

                    @Override
                    public void onNext(Bitmap bitmap) {
                        scaleBitmap(bitmap);
                    }
                });

    }

    @Override
    public void onDestroy() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void scaleBitmap(Bitmap bitmap) {
        //加载的bitmap高宽
        double bw = bitmap.getWidth();
        double bh = bitmap.getHeight();
        //控件的高宽
        double vw = mNavbarCover.getWidth();
        double vh = mNavbarCover.getHeight();

        int nbw, nbh, bx, by;
        //计算高宽比，并计算新的缩放高宽
        if (bh / bw > vh / vw) {
            nbw = (int) vw;
            nbh = (int) (bh * (vw / bw));
            bx = 0;
            by = (int) ((double) nbh / 2 - vh / 2);
        } else {
            nbw = (int) (bw * (vh / bh));
            nbh = (int) vh;
            bx = (int) ((double) nbw / 2 - vw / 2);
            by = 0;
        }

        // 创建新的缩放图片
        bitmap = Bitmap.createScaledBitmap(bitmap, nbw, nbh, false);
        mDrawable = new BitmapDrawable(UiUtils.getResources(), bitmap);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {

    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }


}
