package com.example.castle.mmcomic.ui.activity;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.utils.SysUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 主页活动
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_CURRENT_MENU_ITEM = "STATE_CURRENT_MENU_ITEM";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.content_frame)
    FrameLayout mContentFrame;
    @BindView(R.id.navigation_view)
    NavigationView mNavigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    //记录当前侧滑item
    private int mCurrentMenuItem;
    //侧滑栏切换按钮
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

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

    }

    private void setUpSlideMenu() {
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                //如果当前打开的就是所选页面，直接关闭即可
                if (mCurrentMenuItem == item.getItemId()) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }
                switch (item.getItemId()) {
                    case R.id.drawer_menu_about:

                        break;
                    case R.id.drawer_menu_browser:

                        break;
                    case R.id.drawer_menu_library:

                        break;
                }
                //打开新页面以后保存信息
                mCurrentMenuItem = item.getItemId();
                item.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    //在这里设置侧滑栏显示与关闭
    @Override
    public boolean onSupportNavigateUp() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawers();
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onSupportNavigateUp();
    }
}
