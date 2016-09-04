package com.example.castle.mmcomic.ui.fragment;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.castle.mmcomic.R;
import com.example.castle.mmcomic.base.MyBaseAdapter;
import com.example.castle.mmcomic.parser.ParserFactory;
import com.example.castle.mmcomic.utils.FileUtils;
import com.example.castle.mmcomic.utils.StringUtil;
import com.example.castle.mmcomic.utils.UiUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by castle on 16-9-2.
 * 浏览界面
 */
public class BrowserFragment extends Fragment {
    public static final String STATE_CURRENT_DIR = "STATE_CURRENT_DIR";
    @BindView(R.id.recycle_browser)
    RecyclerView mRecycleBrowser;

    private TextView mDirTextView;
    private File mCurrentDir;
    private File mRootDir = new File("/");
    private List<File> mSubDirs = new ArrayList<>();
    private BrowserAdapter mAdapter;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentDir = ((File) savedInstanceState.getSerializable(STATE_CURRENT_DIR));
        } else {
            mCurrentDir = Environment.getExternalStorageDirectory();
        }
        getActivity().setTitle("Browser");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_browser, container, false);
        ButterKnife.bind(this, v);
        ViewGroup tool = ButterKnife.findById(getActivity(), R.id.toolbar);
        ViewGroup breadCrumbLayout = (ViewGroup) inflater.inflate(R.layout.breadcrumb, tool, false);
        tool.addView(breadCrumbLayout);
        mDirTextView = ButterKnife.findById(breadCrumbLayout, R.id.dir_textview);

        setCurrentDir(mCurrentDir);
        mAdapter = new BrowserAdapter(mSubDirs);
        mRecycleBrowser.setAdapter(mAdapter);
        mRecycleBrowser.setLayoutManager(new LinearLayoutManager(getContext()));

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(STATE_CURRENT_DIR, mCurrentDir);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        ViewGroup tool = ButterKnife.findById(getActivity(), R.id.toolbar);
        ViewGroup breadCrumbLayout = ButterKnife.findById(tool, R.id.breadcrumb_layout);
        tool.removeView(breadCrumbLayout);
        super.onDestroyView();
    }

    public void setCurrentDir(File currentDir) {
        mCurrentDir = currentDir;
        ArrayList<File> subDirs = new ArrayList<>();
        if (!StringUtil.isSame(mCurrentDir.getAbsolutePath(), mRootDir.getAbsolutePath())) {
            subDirs.add(mCurrentDir.getParentFile());
        }
        File[] files = mCurrentDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() || FileUtils.isArchive(file.getName())) {
                    subDirs.add(file);
                }
            }
        }
        Collections.sort(subDirs);
        mSubDirs.clear();
        mSubDirs.addAll(subDirs);
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        mDirTextView.setText(currentDir.getAbsolutePath());
    }

    private void setIcon(BrowserAdapter.ViewHolder holder, File file) {
        int colorRes = R.color.circle_grey;
        if (file.isDirectory()) {
            holder.mDirectoryRowIcon.setImageResource(R.drawable.ic_folder_white_24dp);
        } else {
            holder.mDirectoryRowIcon.setImageResource(R.drawable.ic_file_document_box_white_24dp);
            String name = file.getName();
            if (FileUtils.isZip(name)) {
                colorRes = R.color.circle_green;
            } else if (FileUtils.isRar(name)) {
                colorRes = R.color.circle_red;
            }
        }
        GradientDrawable shape = (GradientDrawable) holder.mDirectoryRowIcon.getBackground();
        shape.setColor(UiUtils.getColor(colorRes));
    }

    class BrowserAdapter extends MyBaseAdapter<File, BrowserAdapter.ViewHolder> {

        public BrowserAdapter(List<File> dataSet) {
            super(dataSet);
        }

        @Override
        protected BrowserAdapter.ViewHolder getViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_directory, parent, false);
            return new ViewHolder(view);
        }

        @Override
        protected void popHolder(BrowserAdapter.ViewHolder holder, final File item, int position) {
            if (position == 0 && !StringUtil.isSame(mCurrentDir.getAbsolutePath(), mRootDir.getAbsolutePath())) {
                holder.mDirectoryRowText.setText("..");
            } else {
                holder.mDirectoryRowText.setText(item.getName());
            }
            setIcon(holder, item);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (item.isDirectory()) {
                        if (ParserFactory.create(item) == null) {
                            setCurrentDir(item);
                            return;
                        }
                    }
                    //// TODO: 16-9-2 如果是漫画，在这里直接进入阅读
                }
            });
        }

        @Override
        protected boolean hasMoreData() {
            return false;
        }

        @Override
        protected List<File> getMoreData() throws Exception {
            return null;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.directory_row_icon)
            ImageView mDirectoryRowIcon;
            @BindView(R.id.directory_row_text)
            TextView mDirectoryRowText;

            ViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }
}
