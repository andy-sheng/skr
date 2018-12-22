package com.common.log.screenlog;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.DiffAdapter;
import com.common.view.recyclerview.RecyclerOnItemClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class TagSelectView extends RelativeLayout {

    LinearLayout mControlPanel;
    ExTextView mOkBtn;
    ExTextView mAllBtn;
    RecyclerView mTagsRv;
    TagsAdapter mTagsAdapter;

    ExTextView mCheckTipsBtn;

    Handler mUiHanlder = new Handler();

    HashSet<String> mHasSelectedSet = new HashSet<>();
    HashMap<String, Integer> mTotalMap;
    Listener mListener;

    public TagSelectView(Context context) {
        super(context);
        init();
    }

    public TagSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TagSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.tag_select_view_layout, this);
        setBackgroundResource(R.color.white_trans_50);

        mControlPanel = (LinearLayout) this.findViewById(R.id.control_panel);
        mOkBtn = (ExTextView) this.findViewById(R.id.ok_btn);
        mAllBtn = (ExTextView) this.findViewById(R.id.all_btn);
        mTagsRv = (RecyclerView) this.findViewById(R.id.tags_rv);
        mCheckTipsBtn = (ExTextView) this.findViewById(R.id.check_tips_btn);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        mTagsRv.setLayoutManager(staggeredGridLayoutManager);
        mTagsAdapter = new TagsAdapter(new RecyclerOnItemClickListener<TagsModel>() {
            @Override
            public void onItemClicked(View view, int position, TagsModel model) {
                if (model.checked) {
                    mHasSelectedSet.add(model.tag);
                } else {
                    mHasSelectedSet.remove(model.tag);
                }
            }
        });
        mTagsRv.setAdapter(mTagsAdapter);

        mOkBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onResult(mHasSelectedSet);
                }
            }
        });

        mAllBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onResult(null);
                }
            }
        });
        mCheckTipsBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                recommend();
            }
        });
    }

    private void recommend() {
        HashSet<String> m = new HashSet<>();
        if (getContext() instanceof FragmentActivity) {
            FragmentActivity activity = (FragmentActivity) getContext();
            tryAddPrefix(activity.getClass().getSimpleName(), m);
            List<Fragment> fl = activity.getSupportFragmentManager().getFragments();
            for (Fragment fragment : fl) {
                tryAddPrefix(fragment.getClass().getSimpleName(), m);
            }
        }
        for (String key : mTotalMap.keySet()) {
            for (String t : m) {
                if (key.startsWith(t)) {
                    mHasSelectedSet.add(key);
                } else {

                }
            }
        }
        setListener(mListener, mTotalMap, mHasSelectedSet);
    }

    private void tryAddPrefix(String str, HashSet<String> m) {
        if (str.endsWith("Activity")) {
            String a = str.substring(0, str.length() - "Activity".length());
            m.add(a);
            return;
        }
        if (str.endsWith("Fragment")) {
            String a = str.substring(0, str.length() - "Fragment".length());
            m.add(a);
            return;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        destroy();
    }

    private void destroy() {

    }

    public void setListener(Listener l, HashMap<String, Integer> totalMap, HashSet<String> selectedSet) {
        mListener = l;
        mTotalMap = totalMap;
        mHasSelectedSet.addAll(selectedSet);
        List<TagsModel> tagsModels = new ArrayList<>();

        for (String key : totalMap.keySet()) {
            TagsModel tagsModel = new TagsModel();
            tagsModel.tag = key;
            tagsModel.num = totalMap.get(key);
            tagsModel.checked = mHasSelectedSet.contains(key);
            tagsModels.add(tagsModel);
        }
        Collections.sort(tagsModels, new Comparator<TagsModel>() {
            @Override
            public int compare(TagsModel o1, TagsModel o2) {
                return o2.num - o1.num;
            }
        });
        mTagsAdapter.setDataList(tagsModels);
    }


    public interface Listener {
        void onResult(HashSet<String> set);
    }

    public static class TagsModel {
        String tag;
        int num;
        boolean checked = false;
    }

    public static class TagsHolder extends RecyclerView.ViewHolder {

        TagsModel mTagsModel;
        CheckBox mTagCb;
        RecyclerOnItemClickListener<TagsModel> mItemClickListener;

        public TagsHolder(View itemView) {
            super(itemView);
            mTagCb = (CheckBox) itemView.findViewById(R.id.tag_cb);
            mTagCb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    mTagsModel.checked = isChecked;
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(mTagCb, -1, mTagsModel);
                    }
                }
            });
        }

        public void bindData(TagsModel tagsModel) {
            mTagsModel = tagsModel;
            mTagCb.setText(mTagsModel.tag + "(" + mTagsModel.num + ")");
            if (mTagsModel.checked) {
                mTagCb.setChecked(true);
            } else {
                mTagCb.setChecked(false);
            }
        }

        public void setItemClickListener(RecyclerOnItemClickListener l) {
            mItemClickListener = l;
        }

    }

    public static class TagsAdapter extends DiffAdapter<TagsModel, RecyclerView.ViewHolder> {
        RecyclerOnItemClickListener<TagsModel> mItemClickListener;

        public TagsAdapter(RecyclerOnItemClickListener<TagsModel> l) {
            mItemClickListener = l;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_select_view_holder, parent, false);
            TagsHolder itemHolder = new TagsHolder(view);
            itemHolder.setItemClickListener(mItemClickListener);
            return itemHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TagsHolder tagsHolder = (TagsHolder) holder;
            tagsHolder.bindData(mDataList.get(position));
        }

        @Override
        public int getItemCount() {
            return mDataList.size();
        }
    }


}
