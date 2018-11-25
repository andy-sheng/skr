//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import io.rong.imkit.R;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.adapter.ConversationAddMemberAdapter;
import io.rong.imkit.widget.adapter.ConversationAddMemberAdapter.OnDeleteIconListener;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.OperationCallback;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

public class ConversationAddMemberFragment extends BaseFragment implements OnItemClickListener, OnDeleteIconListener {
  static final int PREPARE_LIST = 1;
  static final int REMOVE_ITEM = 2;
  static final int SHOW_TOAST = 3;
  private ConversationType mConversationType;
  private String mTargetId;
  private ConversationAddMemberAdapter mAdapter;
  private List<String> mIdList = new ArrayList();
  private ArrayList<UserInfo> mMembers = new ArrayList();
  private GridView mGridList;

  public ConversationAddMemberFragment() {
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    RongContext.getInstance().getEventBus().register(this);
    if (this.getActivity() != null) {
      Intent intent = this.getActivity().getIntent();
      if (intent.getData() != null) {
        this.mConversationType = ConversationType.valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.US));
        this.mTargetId = intent.getData().getQueryParameter("targetId");
      }
    }

    this.mAdapter = new ConversationAddMemberAdapter(this.getActivity());
    this.mAdapter.setDeleteIconListener(this);
    this.initData();
  }

  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.rc_fr_conversation_member_list, (ViewGroup)null);
    this.mGridList = (GridView)this.findViewById(view, R.id.rc_list);
    return view;
  }

  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    this.mGridList.setAdapter(this.mAdapter);
    this.mGridList.setOnItemClickListener(this);
    this.mGridList.setOnTouchListener(new OnTouchListener() {
      public boolean onTouch(View v, MotionEvent event) {
        if (1 == event.getAction() && io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.isDeleteState()) {
          UserInfo addBtn = new UserInfo("RongAddBtn", (String)null, (Uri)null);
          io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.add(addBtn);
          String curUserId = RongIM.getInstance().getCurrentUserId();
          if (io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.getCreatorId() != null && io.rong.imkit.fragment.ConversationAddMemberFragment.this.mConversationType.equals(ConversationType.DISCUSSION) && curUserId.equals(io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.getCreatorId())) {
            UserInfo deleteBtn = new UserInfo("RongDelBtn", (String)null, (Uri)null);
            io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.add(deleteBtn);
          }

          io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.setDeleteState(false);
          io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.notifyDataSetChanged();
          return true;
        } else {
          return false;
        }
      }
    });
  }

  private void initData() {
    if (this.mConversationType.equals(ConversationType.DISCUSSION)) {
      RongIM.getInstance().getDiscussion(this.mTargetId, new ResultCallback<Discussion>() {
        public void onSuccess(Discussion discussion) {
          io.rong.imkit.fragment.ConversationAddMemberFragment.this.mIdList = discussion.getMemberIdList();
          io.rong.imkit.fragment.ConversationAddMemberFragment.this.mAdapter.setCreatorId(discussion.getCreatorId());
          Message msg = new Message();
          msg.what = 1;
          msg.obj = io.rong.imkit.fragment.ConversationAddMemberFragment.this.mIdList;
          io.rong.imkit.fragment.ConversationAddMemberFragment.this.getHandler().sendMessage(msg);
        }

        public void onError(ErrorCode errorCode) {
          io.rong.imkit.fragment.ConversationAddMemberFragment.this.getHandler().sendEmptyMessage(3);
        }
      });
    } else if (this.mConversationType.equals(ConversationType.PRIVATE)) {
      this.mIdList.add(this.mTargetId);
      Message msg = new Message();
      msg.what = 1;
      msg.obj = this.mIdList;
      this.getHandler().sendMessage(msg);
    }

  }

  public void onEventMainThread(UserInfo userInfo) {
    int count = this.mAdapter.getCount();

    for(int i = 0; i < count; ++i) {
      UserInfo temp = (UserInfo)this.mAdapter.getItem(i);
      if (userInfo.getUserId().equals(temp.getUserId())) {
        temp.setName(userInfo.getName());
        temp.setPortraitUri(userInfo.getPortraitUri());
        this.mAdapter.getView(i, this.mGridList.getChildAt(i - this.mGridList.getFirstVisiblePosition()), this.mGridList);
      }
    }

  }

  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    UserInfo userInfo = (UserInfo)this.mAdapter.getItem(position);
    if (userInfo.getUserId().equals("RongDelBtn")) {
      this.mAdapter.setDeleteState(true);
      int count = this.mAdapter.getCount();
      this.mAdapter.remove(count - 1);
      this.mAdapter.remove(count - 2);
      this.mAdapter.notifyDataSetChanged();
    } else if (userInfo.getUserId().equals("RongAddBtn")) {
      if (RongContext.getInstance().getMemberSelectListener() == null) {
        throw new ExceptionInInitializerError("The OnMemberSelectListener hasn't been set!");
      }

      RongContext.getInstance().getMemberSelectListener().startSelectMember(this.getActivity(), this.mConversationType, this.mTargetId);
    }

  }

  public void onDeleteIconClick(View view, final int position) {
    UserInfo temp = (UserInfo)this.mAdapter.getItem(position);
    RongIM.getInstance().removeMemberFromDiscussion(this.mTargetId, temp.getUserId(), new OperationCallback() {
      public void onSuccess() {
        Message msg = new Message();
        msg.what = 2;
        msg.obj = position;
        io.rong.imkit.fragment.ConversationAddMemberFragment.this.getHandler().sendMessage(msg);
      }

      public void onError(ErrorCode errorCode) {
        io.rong.imkit.fragment.ConversationAddMemberFragment.this.getHandler().sendEmptyMessage(3);
      }
    });
  }

  public boolean handleMessage(Message msg) {
    switch(msg.what) {
      case 1:
        List<String> mMemberInfo = (List)msg.obj;
        int i = 0;

        String id;
        UserInfo userInfo;
        for(Iterator var4 = mMemberInfo.iterator(); var4.hasNext(); ++i) {
          id = (String)var4.next();
          if (i >= 50) {
            break;
          }

          userInfo = RongUserInfoManager.getInstance().getUserInfo(id);
          if (userInfo == null) {
            this.mMembers.add(new UserInfo(id, (String)null, (Uri)null));
          } else {
            this.mMembers.add(userInfo);
          }
        }

        UserInfo addBtn = new UserInfo("RongAddBtn", (String)null, (Uri)null);
        this.mMembers.add(addBtn);
        id = RongIM.getInstance().getCurrentUserId();
        if (this.mAdapter.getCreatorId() != null && this.mConversationType.equals(ConversationType.DISCUSSION) && id.equals(this.mAdapter.getCreatorId())) {
          userInfo = new UserInfo("RongDelBtn", (String)null, (Uri)null);
          this.mMembers.add(userInfo);
        }

        this.mAdapter.addCollection(this.mMembers);
        this.mAdapter.notifyDataSetChanged();
        break;
      case 2:
        int position = (Integer)msg.obj;
        this.mAdapter.remove(position);
        this.mAdapter.notifyDataSetChanged();
      case 3:
    }

    return true;
  }

  public boolean onBackPressed() {
    return false;
  }

  public void onRestoreUI() {
    this.initData();
  }
}
