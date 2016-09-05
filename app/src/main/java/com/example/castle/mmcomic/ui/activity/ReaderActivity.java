package com.example.castle.mmcomic.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.ui.fragment.ReaderFragment;
import com.example.castle.mmcomic.utils.DoubleClickExit;
import com.example.castle.mmcomic.utils.ToastUtil;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by castle on 16-9-4.
 * 阅读活动
 */
public class ReaderActivity extends AppCompatActivity {
    @BindView(R.id.content_frame_reader)
    FrameLayout mContentFrameReader;
    @BindView(R.id.toolbar_reader)
    Toolbar mToolbarReader;
    @BindView(R.id.pageNavTextView)
    TextView mPageNavTextView;
    @BindView(R.id.pageSeekBar)
    SeekBar mPageSeekBar;
    @BindView(R.id.pageNavLayout)
    LinearLayout mPageNavLayout;
    private ReaderFragment mFragment;

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        setContentView(R.layout.layout_reader);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbarReader);
        if (savedInstanceState == null) {
            if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
                ReaderFragment readerFragment = ReaderFragment.newInstance(new File(getIntent().getData().getPath()));
                setFragment(readerFragment);
            } else {
                Bundle extras = getIntent().getExtras();
                ReaderFragment fragment;
                ReaderFragment.Mode mode = (ReaderFragment.Mode) extras.getSerializable(ReaderFragment.PARAM_MODE);
                if (mode == ReaderFragment.Mode.MODE_LIBRARY) {
                    fragment = ReaderFragment.newInstance(extras.getInt(ReaderFragment.PARAM_HANDLER));
                } else {
                    fragment = ReaderFragment.newInstance((File) extras.getSerializable(ReaderFragment.PARAM_HANDLER));
                }
                setFragment(fragment);
            }
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void setFragment(ReaderFragment fragment) {
        mFragment = fragment;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame_reader, fragment)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!DoubleClickExit.check()) {
            ToastUtil.showShort("再按一次退出");
        } else {
            super.onBackPressed();
        }
    }
}
