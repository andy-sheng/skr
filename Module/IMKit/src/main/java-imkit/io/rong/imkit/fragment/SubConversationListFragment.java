//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import io.rong.imkit.R;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imkit.widget.adapter.SubConversationListAdapter;
import io.rong.imlib.model.Conversation.ConversationType;

public class SubConversationListFragment extends ConversationListFragment {
    private static final String TAG = "SubConversationListFragment";
    private ListView mList;
    private SubConversationListAdapter mAdapter;

    public SubConversationListFragment() {
    }

    public void setAdapter(SubConversationListAdapter adapter) {
        this.mAdapter = adapter;
    }

    public ConversationListAdapter onResolveAdapter(Context context) {
        if (this.mAdapter == null) {
            this.mAdapter = new SubConversationListAdapter(context);
        }

        return this.mAdapter;
    }

    public boolean getGatherState(ConversationType conversationType) {
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        this.mList = (ListView) this.findViewById(view, R.id.rc_list);
        return view;
    }
}
