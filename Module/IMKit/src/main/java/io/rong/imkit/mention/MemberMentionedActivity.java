//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.mention;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import io.rong.imkit.R;
import io.rong.imkit.RongBaseNoActionbarActivity;
import io.rong.imkit.RongIM.IGroupMemberCallback;
import io.rong.imkit.RongIM.IGroupMembersProvider;
import io.rong.imkit.mention.SideBar.OnTouchingLetterChangedListener;
import io.rong.imkit.tools.CharacterParser;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.RongIMClient.ErrorCode;
import io.rong.imlib.RongIMClient.ResultCallback;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

public class MemberMentionedActivity extends RongBaseNoActionbarActivity {
  private ListView mListView;
  private List<io.rong.imkit.mention.MemberMentionedActivity.MemberInfo> mAllMemberList;
  private io.rong.imkit.mention.MemberMentionedActivity.MembersAdapter mAdapter;
  private Handler handler = new Handler();

  public MemberMentionedActivity() {
  }

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.requestWindowFeature(1);
    this.setContentView(R.layout.rc_mention_members);
    EditText searchBar = (EditText)this.findViewById(R.id.rc_edit_text);
    this.mListView = (ListView)this.findViewById(R.id.rc_list);
    SideBar mSideBar = (SideBar)this.findViewById(R.id.rc_sidebar);
    TextView letterPopup = (TextView)this.findViewById(R.id.rc_popup_bg);
    mSideBar.setTextView(letterPopup);
    this.mAdapter = new io.rong.imkit.mention.MemberMentionedActivity.MembersAdapter();
    this.mListView.setAdapter(this.mAdapter);
    this.mAllMemberList = new ArrayList();
    String targetId = this.getIntent().getStringExtra("targetId");
    ConversationType conversationType = ConversationType.setValue(this.getIntent().getIntExtra("conversationType", 0));
    IGroupMembersProvider groupMembersProvider = RongMentionManager.getInstance().getGroupMembersProvider();
    if (conversationType.equals(ConversationType.GROUP) && groupMembersProvider != null) {
      groupMembersProvider.getGroupMembers(targetId, new IGroupMemberCallback() {
        public void onGetGroupMembersResult(final List<UserInfo> members) {
          if (members != null && members.size() > 0) {
            io.rong.imkit.mention.MemberMentionedActivity.this.handler.post(new Runnable() {
              public void run() {
                for(int i = 0; i < members.size(); ++i) {
                  UserInfo userInfo = (UserInfo)members.get(i);
                  if (userInfo != null && !userInfo.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                    io.rong.imkit.mention.MemberMentionedActivity.MemberInfo memberInfo = io.rong.imkit.mention.MemberMentionedActivity.this.new MemberInfo(userInfo);
                    String sortString = "#";
                    String pinyin = CharacterParser.getInstance().getSelling(userInfo.getName());
                    if (pinyin != null && pinyin.length() > 0) {
                      sortString = pinyin.substring(0, 1).toUpperCase();
                    }

                    if (sortString.matches("[A-Z]")) {
                      memberInfo.setLetter(sortString.toUpperCase());
                    } else {
                      memberInfo.setLetter("#");
                    }

                    io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList.add(memberInfo);
                  }
                }

                Collections.sort(io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList, io.rong.imkit.mention.MemberMentionedActivity.PinyinComparator.getInstance());
                io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.setData(io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList);
                io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.notifyDataSetChanged();
              }
            });
          }

        }
      });
    } else if (conversationType.equals(ConversationType.DISCUSSION)) {
      RongIMClient.getInstance().getDiscussion(targetId, new ResultCallback<Discussion>() {
        public void onSuccess(Discussion discussion) {
          List<String> memeberIds = discussion.getMemberIdList();
          Iterator var3 = memeberIds.iterator();

          while(var3.hasNext()) {
            String id = (String)var3.next();
            UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(id);
            if (userInfo != null && !userInfo.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
              io.rong.imkit.mention.MemberMentionedActivity.MemberInfo memberInfo = io.rong.imkit.mention.MemberMentionedActivity.this.new MemberInfo(userInfo);
              String sortString = "#";
              String pinyin = CharacterParser.getInstance().getSelling(userInfo.getName());
              if (pinyin != null && pinyin.length() > 0) {
                sortString = pinyin.substring(0, 1).toUpperCase();
              }

              if (sortString.matches("[A-Z]")) {
                memberInfo.setLetter(sortString.toUpperCase());
              } else {
                memberInfo.setLetter("#");
              }

              io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList.add(memberInfo);
            }
          }

          Collections.sort(io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList, io.rong.imkit.mention.MemberMentionedActivity.PinyinComparator.getInstance());
          io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.setData(io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList);
          io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.notifyDataSetChanged();
        }

        public void onError(ErrorCode e) {
        }
      });
    }

    this.mListView.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        io.rong.imkit.mention.MemberMentionedActivity.this.finish();
        io.rong.imkit.mention.MemberMentionedActivity.MemberInfo item = io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.getItem(position);
        if (item != null && item.userInfo != null) {
          RongMentionManager.getInstance().mentionMember(item.userInfo);
        }

      }
    });
    mSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {
      public void onTouchingLetterChanged(String s) {
        int position = io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.getPositionForSection(s.charAt(0));
        if (position != -1) {
          io.rong.imkit.mention.MemberMentionedActivity.this.mListView.setSelection(position);
        }

      }
    });
    searchBar.addTextChangedListener(new TextWatcher() {
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      public void onTextChanged(CharSequence s, int start, int before, int count) {
        List<io.rong.imkit.mention.MemberMentionedActivity.MemberInfo> filterDataList = new ArrayList();
        if (TextUtils.isEmpty(s.toString())) {
          filterDataList = io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList;
        } else {
          ((List)filterDataList).clear();
          Iterator var6 = io.rong.imkit.mention.MemberMentionedActivity.this.mAllMemberList.iterator();

          label27:
          while(true) {
            io.rong.imkit.mention.MemberMentionedActivity.MemberInfo member;
            String name;
            do {
              do {
                if (!var6.hasNext()) {
                  break label27;
                }

                member = (io.rong.imkit.mention.MemberMentionedActivity.MemberInfo)var6.next();
                name = member.userInfo.getName();
              } while(name == null);
            } while(!name.contains(s) && !CharacterParser.getInstance().getSelling(name).startsWith(s.toString()));

            ((List)filterDataList).add(member);
          }
        }

        Collections.sort((List)filterDataList, io.rong.imkit.mention.MemberMentionedActivity.PinyinComparator.getInstance());
        io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.setData((List)filterDataList);
        io.rong.imkit.mention.MemberMentionedActivity.this.mAdapter.notifyDataSetChanged();
      }

      public void afterTextChanged(Editable s) {
      }
    });
    this.findViewById(R.id.rc_btn_cancel).setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        io.rong.imkit.mention.MemberMentionedActivity.this.finish();
      }
    });
  }

  public static class PinyinComparator implements Comparator<io.rong.imkit.mention.MemberMentionedActivity.MemberInfo> {
    public static io.rong.imkit.mention.MemberMentionedActivity.PinyinComparator instance = null;

    public PinyinComparator() {
    }

    public static io.rong.imkit.mention.MemberMentionedActivity.PinyinComparator getInstance() {
      if (instance == null) {
        instance = new io.rong.imkit.mention.MemberMentionedActivity.PinyinComparator();
      }

      return instance;
    }

    public int compare(io.rong.imkit.mention.MemberMentionedActivity.MemberInfo o1, io.rong.imkit.mention.MemberMentionedActivity.MemberInfo o2) {
      if (!o1.getLetter().equals("@") && !o2.getLetter().equals("#")) {
        return !o1.getLetter().equals("#") && !o2.getLetter().equals("@") ? o1.getLetter().compareTo(o2.getLetter()) : 1;
      } else {
        return -1;
      }
    }
  }

  private class MemberInfo {
    UserInfo userInfo;
    String letter;

    MemberInfo(UserInfo userInfo) {
      this.userInfo = userInfo;
    }

    public void setLetter(String letter) {
      this.letter = letter;
    }

    public String getLetter() {
      return this.letter;
    }
  }

  class ViewHolder {
    AsyncImageView portrait;
    TextView name;
    TextView letter;

    ViewHolder() {
    }
  }

  class MembersAdapter extends BaseAdapter implements SectionIndexer {
    private List<io.rong.imkit.mention.MemberMentionedActivity.MemberInfo> mList = new ArrayList();

    MembersAdapter() {
    }

    public void setData(List<io.rong.imkit.mention.MemberMentionedActivity.MemberInfo> list) {
      this.mList = list;
    }

    public int getCount() {
      return this.mList.size();
    }

    public io.rong.imkit.mention.MemberMentionedActivity.MemberInfo getItem(int position) {
      return (io.rong.imkit.mention.MemberMentionedActivity.MemberInfo)this.mList.get(position);
    }

    public long getItemId(int position) {
      return 0L;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      io.rong.imkit.mention.MemberMentionedActivity.ViewHolder viewHolder;
      if (convertView == null) {
        viewHolder = io.rong.imkit.mention.MemberMentionedActivity.this.new ViewHolder();
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.rc_mention_list_item, (ViewGroup)null);
        viewHolder.name = (TextView)convertView.findViewById(R.id.rc_user_name);
        viewHolder.portrait = (AsyncImageView)convertView.findViewById(R.id.rc_user_portrait);
        viewHolder.letter = (TextView)convertView.findViewById(R.id.letter);
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (io.rong.imkit.mention.MemberMentionedActivity.ViewHolder)convertView.getTag();
      }

      UserInfo userInfo = ((io.rong.imkit.mention.MemberMentionedActivity.MemberInfo)this.mList.get(position)).userInfo;
      if (userInfo != null) {
        viewHolder.name.setText(userInfo.getName());
        viewHolder.portrait.setAvatar(userInfo.getPortraitUri());
      }

      int section = this.getSectionForPosition(position);
      if (position == this.getPositionForSection(section)) {
        viewHolder.letter.setVisibility(View.VISIBLE);
        viewHolder.letter.setText(((io.rong.imkit.mention.MemberMentionedActivity.MemberInfo)this.mList.get(position)).getLetter());
      } else {
        viewHolder.letter.setVisibility(View.GONE);
      }

      return convertView;
    }

    public Object[] getSections() {
      return new Object[0];
    }

    public int getPositionForSection(int sectionIndex) {
      for(int i = 0; i < this.getCount(); ++i) {
        String sortStr = ((io.rong.imkit.mention.MemberMentionedActivity.MemberInfo)this.mList.get(i)).getLetter();
        char firstChar = sortStr.toUpperCase().charAt(0);
        if (firstChar == sectionIndex) {
          return i;
        }
      }

      return -1;
    }

    public int getSectionForPosition(int position) {
      return ((io.rong.imkit.mention.MemberMentionedActivity.MemberInfo)this.mList.get(position)).getLetter().charAt(0);
    }
  }
}
