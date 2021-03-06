package com.example.castle.mmcomic.ui.activity;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.managers.Scanner;
import com.example.castle.mmcomic.ui.fragment.BrowserFragment;
import com.example.castle.mmcomic.ui.fragment.HeaderFragment;
import com.example.castle.mmcomic.ui.fragment.LibraryFragment;
import com.example.castle.mmcomic.utils.DoubleClickExit;
import com.example.castle.mmcomic.utils.SysUtil;
import com.example.castle.mmcomic.utils.ToastUtil;


/**
 * 主页活动
 * TODO: 加入6.0权限管理
 */
public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    private static final String STATE_CURRENT_MENU_ITEM = "STATE_CURRENT_MENU_ITEM";

    Toolbar mToolbar;

    FrameLayout mContentFrame;

    NavigationView mNavigationView;

    DrawerLayout mDrawerLayout;


    //记录当前侧滑item
    private int mCurrentNavItem;
    //侧滑栏切换按钮
    private ActionBarDrawerToggle mDrawerToggle;
    private FragmentManager mFragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化fragment manager
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mContentFrame = (FrameLayout) findViewById(R.id.content_frame);
        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);


        //初始化toolbar,设置相应属性
        setSupportActionBar(mToolbar);
        //如果是lollipop以后的版本，那么可以设置视图高度
        if (SysUtil.isLollipopOrLater()) {
            mToolbar.setElevation(8f);
        }


        if (getSupportActionBar() != null) {
            //回退键
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        //设置侧滑栏,以及切换按钮
        setUpSlideMenu();
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close
        );
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        Scanner.getInstance().scanLibrary();
        if (savedInstanceState == null) {
            setFragment(new LibraryFragment());
            setNavBar();
            mCurrentNavItem = R.id.drawer_menu_library;
        } else {
            //强制更新指示
            onBackStackChanged();
            mCurrentNavItem = savedInstanceState.getInt(STATE_CURRENT_MENU_ITEM);
        }
        mNavigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
    }


    private void setUpSlideMenu() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //close if the selected item equals current fragment
                if (mCurrentNavItem == item.getItemId()) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }
                switch (item.getItemId()) {
                    case R.id.drawer_menu_about:
                        break;
                    case R.id.drawer_menu_browser:
                        setFragment(new BrowserFragment());
                        break;
                    case R.id.drawer_menu_library:
                        setFragment(new LibraryFragment());
                        break;
                }
                //save selected id
                mCurrentNavItem = item.getItemId();
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    //在这里设置侧滑栏显示与关闭
    @Override
    public boolean onSupportNavigateUp() {
        if (!popFragment()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (popFragment()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawers();
            }
        } else {
            //实现一定时间内双击退出
            if (!DoubleClickExit.check()) {
                ToastUtil.showShort("再按一次退出");
            } else {
                finish();
            }
        }
    }


    public void pushFragment(Fragment fragment) {
        mFragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }


    //如果返回栈里面有就弹出fragment，否则返回false
    private boolean popFragment() {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            mFragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    //pop all and set a new fragment
    private void setFragment(Fragment fragment) {
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            //pop all fragment
            mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        mFragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void setNavBar() {
        //添加观察树，确保渲染完成后再添加头
        mNavigationView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        //api 24以上不能直接找到header并用这种方式替换
                        //api 24以下可以直接这么操作
                        mFragmentManager.beginTransaction()
                                .replace(R.id.header, new HeaderFragment())
                                .commit();
                        // Once data has been obtained, this listener is no longer needed, so remove it...
                        //完成切换，及时销毁避免内存泄漏
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mNavigationView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mNavigationView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                });

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outPersistentState.putInt(STATE_CURRENT_MENU_ITEM, mCurrentNavItem);
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackStackChanged() {
        mDrawerToggle.setDrawerIndicatorEnabled(mFragmentManager.getBackStackEntryCount() == 0);
    }
}
