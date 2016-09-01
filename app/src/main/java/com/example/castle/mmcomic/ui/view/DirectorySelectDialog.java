package com.example.castle.mmcomic.ui.view;

import android.content.Context;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.utils.StringUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by castle on 16-9-1.
 * 文件目录选择对话框
 */
public class DirectorySelectDialog extends AppCompatDialog
        implements AdapterView.OnItemClickListener {

    private final FileFilter mFileFilter;
    private final DirectoryListAdapter mAdapter;
    @BindView(R.id.directory_current_text)
    TextView mDirectoryCurrentText;
    @BindView(R.id.directory_listview)
    ListView mDirectoryListview;
    @BindView(R.id.directory_picker_confirm)
    Button mDirectoryPickerConfirm;
    @BindView(R.id.directory_picker_cancel)
    Button mDirectoryPickerCancel;
    private File mCurrentDir;
    private File mRootDir = new File("/");
    private File[] mSubDirs;
    private OnDirectorySelectListener mListener;

    public DirectorySelectDialog(Context context) {
        super(context);
        setContentView(R.layout.dialog_directorypicker);
        ButterKnife.bind(this);
        //初始化listView以及文件过滤器
        mAdapter = new DirectoryListAdapter();
        mDirectoryListview.setAdapter(mAdapter);
        mDirectoryListview.setOnItemClickListener(this);
        mFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
    }

    public void setCurrentDirectory(File path) {
        mCurrentDir = path;
        File[] subs = mCurrentDir.listFiles(mFileFilter);
        ArrayList<File> subDirs = null;
        if (subs != null) {
            subDirs = new ArrayList<>(Arrays.asList(subs));
        } else {
            subDirs = new ArrayList<>();
        }
        if (!StringUtil.isSame(mCurrentDir.getAbsolutePath(), mRootDir.getAbsolutePath())) {
            subDirs.add(0, mCurrentDir.getParentFile());
        }
        Collections.sort(subDirs);
        mSubDirs = subDirs.toArray(new File[subDirs.size()]);
        mDirectoryCurrentText.setText(mCurrentDir.getPath());
        mAdapter.notifyDataSetChanged();
    }

    //设置回调接口
    public void setOnDirectorySelectListener(OnDirectorySelectListener listener) {
        mListener = listener;
    }

    //设置点击事件，调用回调设置当前目录
    @OnClick({R.id.directory_picker_confirm, R.id.directory_picker_cancel})
    public void onClick(View view) {
        if (view == mDirectoryPickerConfirm) {
            if (mListener != null) {
                mListener.onDirectorySelect(mCurrentDir);
            }
        }
        dismiss();
    }

    //子项点击事件处理
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        File subDir = mSubDirs[i];
        setCurrentDirectory(subDir);
    }

    public interface OnDirectorySelectListener {
        void onDirectorySelect(File file);
    }

    private class DirectoryListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSubDirs == null ? 0 : mSubDirs.length;
        }

        @Override
        public Object getItem(int i) {
            return mSubDirs[i];
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = LayoutInflater.from(getContext())
                        .inflate(R.layout.row_directory, viewGroup, false);
            }
            File subDir = mSubDirs[i];
            TextView textView = (TextView) view.findViewById(R.id.directory_row_text);
            if (i == 0 && StringUtil.isSame(mCurrentDir.getPath(), mRootDir.getPath())) {
                textView.setText("..");
            } else {
                textView.setText(subDir.getName());
            }
            return view;
        }
    }

}
