//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.mention;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM.IGroupMembersProvider;
import io.rong.imkit.mention.IMentionedInputListener;
import io.rong.imkit.mention.ITextInputListener;
import io.rong.imkit.mention.MemberMentionedActivity;
import io.rong.imkit.mention.MentionBlock;
import io.rong.imkit.mention.MentionInstance;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.MentionedInfo;
import io.rong.imlib.model.MentionedInfo.MentionedType;
import io.rong.imlib.model.UserInfo;

public class RongMentionManager implements ITextInputListener {
    private static String TAG = "RongMentionManager";
    private Stack<MentionInstance> stack;
    private IGroupMembersProvider mGroupMembersProvider;
    private IMentionedInputListener mMentionedInputListener;

    private RongMentionManager() {
        this.stack = new Stack();
    }

    public static io.rong.imkit.mention.RongMentionManager getInstance() {
        return io.rong.imkit.mention.RongMentionManager.SingletonHolder.sInstance;
    }

    public void createInstance(ConversationType conversationType, String targetId, EditText inputEditText) {
        RLog.i(TAG, "createInstance");
        String key = conversationType.getName() + targetId;
        MentionInstance mentionInstance;
        if (this.stack.size() > 0) {
            mentionInstance = (MentionInstance) this.stack.peek();
            if (mentionInstance.key.equals(key)) {
                return;
            }
        }

        mentionInstance = new MentionInstance();
        mentionInstance.key = key;
        mentionInstance.mentionBlocks = new ArrayList();
        mentionInstance.inputEditText = inputEditText;
        this.stack.add(mentionInstance);
    }

    public void destroyInstance(ConversationType conversationType, String targetId) {
        RLog.i(TAG, "destroyInstance");
        if (this.stack.size() > 0) {
            MentionInstance instance = (MentionInstance) this.stack.peek();
            if (instance.key.equals(conversationType.getName() + targetId)) {
                this.stack.pop();
            } else {
                RLog.e(TAG, "Invalid MentionInstance : " + instance.key);
            }
        } else {
            RLog.e(TAG, "Invalid MentionInstance.");
        }

    }

    public void mentionMember(ConversationType conversationType, String targetId, String userId) {
        RLog.d(TAG, "mentionMember " + userId);
        if (!TextUtils.isEmpty(userId) && conversationType != null && !TextUtils.isEmpty(targetId) && this.stack.size() != 0) {
            String key = conversationType.getName() + targetId;
            MentionInstance instance = (MentionInstance) this.stack.peek();
            if (instance != null && instance.key.equals(key)) {
                UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(userId);
                if (conversationType == ConversationType.GROUP && RongContext.getInstance().getGroupUserInfoProvider() != null) {
                    GroupUserInfo groupUserInfo = RongContext.getInstance().getGroupUserInfoProvider().getGroupUserInfo(targetId, userId);
                    if (groupUserInfo != null && groupUserInfo.getNickname() != null && !"".equals(groupUserInfo.getNickname().trim())) {
                        userInfo.setName(groupUserInfo.getNickname());
                    }
                }

                this.addMentionedMember(userInfo, 0);
            } else {
                RLog.e(TAG, "Invalid mention instance : " + key);
            }
        } else {
            RLog.e(TAG, "Illegal argument");
        }
    }

    public void mentionMember(UserInfo userInfo) {
        if (userInfo != null && !TextUtils.isEmpty(userInfo.getUserId())) {
            this.addMentionedMember(userInfo, 1);
        } else {
            RLog.e(TAG, "Invalid userInfo");
        }
    }

    private void addMentionedMember(UserInfo userInfo, int from) {
        if (this.stack.size() > 0) {
            MentionInstance mentionInstance = (MentionInstance) this.stack.peek();
            EditText editText = mentionInstance.inputEditText;
            if (userInfo != null && editText != null) {
                String mentionContent = from == 0 ? "@" + userInfo.getName() + " " : userInfo.getName() + " ";
                int len = mentionContent.length();
                int cursorPos = editText.getSelectionStart();
                MentionBlock brokenBlock = this.getBrokenMentionedBlock(cursorPos, mentionInstance.mentionBlocks);
                if (brokenBlock != null) {
                    mentionInstance.mentionBlocks.remove(brokenBlock);
                }

                MentionBlock mentionBlock = new MentionBlock();
                mentionBlock.userId = userInfo.getUserId();
                mentionBlock.offset = false;
                mentionBlock.name = userInfo.getName();
                if (from == 1) {
                    mentionBlock.start = cursorPos - 1;
                } else {
                    mentionBlock.start = cursorPos;
                }

                mentionBlock.end = cursorPos + len;
                mentionInstance.mentionBlocks.add(mentionBlock);
                editText.getEditableText().insert(cursorPos, mentionContent);
                editText.setSelection(cursorPos + len);
                mentionBlock.offset = true;
            }
        }

    }

    private MentionBlock getBrokenMentionedBlock(int cursorPos, List<MentionBlock> blocks) {
        MentionBlock brokenBlock = null;
        Iterator var4 = blocks.iterator();

        while (var4.hasNext()) {
            MentionBlock block = (MentionBlock) var4.next();
            if (block.offset && cursorPos < block.end && cursorPos > block.start) {
                brokenBlock = block;
                break;
            }
        }

        return brokenBlock;
    }

    private void offsetMentionedBlocks(int cursorPos, int offset, List<MentionBlock> blocks) {
        MentionBlock block;
        for (Iterator var4 = blocks.iterator(); var4.hasNext(); block.offset = true) {
            block = (MentionBlock) var4.next();
            if (cursorPos <= block.start && block.offset) {
                block.start += offset;
                block.end += offset;
            }
        }

    }

    private MentionBlock getDeleteMentionedBlock(int cursorPos, List<MentionBlock> blocks) {
        MentionBlock deleteBlock = null;
        Iterator var4 = blocks.iterator();

        while (var4.hasNext()) {
            MentionBlock block = (MentionBlock) var4.next();
            if (cursorPos == block.end) {
                deleteBlock = block;
                break;
            }
        }

        return deleteBlock;
    }

    public void onTextEdit(ConversationType conversationType, String targetId, int cursorPos, int offset, String text) {
        RLog.d(TAG, "onTextEdit " + cursorPos + ", " + text);
        if (this.stack != null && this.stack.size() != 0) {
            MentionInstance mentionInstance = (MentionInstance) this.stack.peek();
            if (!mentionInstance.key.equals(conversationType.getName() + targetId)) {
                RLog.w(TAG, "onTextEdit ignore conversation.");
            } else {
                if (offset == 1 && !TextUtils.isEmpty(text)) {
                    boolean showMention = false;
                    String str;
                    if (cursorPos == 0) {
                        str = text.substring(0, 1);
                        showMention = str.equals("@");
                    } else {
                        String preChar = text.substring(cursorPos - 1, cursorPos);
                        str = text.substring(cursorPos, cursorPos + 1);
                        if (str.equals("@") && !preChar.matches("^[a-zA-Z]*") && !preChar.matches("^\\d+$")) {
                            showMention = true;
                        }
                    }

                    if (showMention && (this.mMentionedInputListener == null || !this.mMentionedInputListener.onMentionedInput(conversationType, targetId))) {
                        Intent intent = new Intent(RongContext.getInstance(), MemberMentionedActivity.class);
                        intent.putExtra("conversationType", conversationType.getValue());
                        intent.putExtra("targetId", targetId);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        RongContext.getInstance().startActivity(intent);
                    }
                }

                MentionBlock brokenBlock = this.getBrokenMentionedBlock(cursorPos, mentionInstance.mentionBlocks);
                if (brokenBlock != null) {
                    mentionInstance.mentionBlocks.remove(brokenBlock);
                }

                this.offsetMentionedBlocks(cursorPos, offset, mentionInstance.mentionBlocks);
            }
        } else {
            RLog.w(TAG, "onTextEdit ignore.");
        }
    }

    public MentionedInfo onSendButtonClick() {
        if (this.stack.size() > 0) {
            List<String> userIds = new ArrayList();
            MentionInstance curInstance = (MentionInstance) this.stack.peek();
            Iterator var3 = curInstance.mentionBlocks.iterator();

            while (var3.hasNext()) {
                MentionBlock block = (MentionBlock) var3.next();
                if (!userIds.contains(block.userId)) {
                    userIds.add(block.userId);
                }
            }

            if (userIds.size() > 0) {
                curInstance.mentionBlocks.clear();
                return new MentionedInfo(MentionedType.PART, userIds, (String) null);
            }
        }

        return null;
    }

    public void onDeleteClick(ConversationType type, String targetId, EditText editText, int cursorPos) {
        RLog.d(TAG, "onTextEdit " + cursorPos);
        if (this.stack.size() > 0 && cursorPos > 0) {
            MentionInstance mentionInstance = (MentionInstance) this.stack.peek();
            if (mentionInstance.key.equals(type.getName() + targetId)) {
                MentionBlock deleteBlock = this.getDeleteMentionedBlock(cursorPos, mentionInstance.mentionBlocks);
                if (deleteBlock != null) {
                    mentionInstance.mentionBlocks.remove(deleteBlock);
                    String delText = deleteBlock.name;
                    int start = cursorPos - delText.length() - 1;
                    editText.getEditableText().delete(start, cursorPos);
                    editText.setSelection(start);
                }
            }
        }

    }

    public void setGroupMembersProvider(IGroupMembersProvider groupMembersProvider) {
        this.mGroupMembersProvider = groupMembersProvider;
    }

    public IGroupMembersProvider getGroupMembersProvider() {
        return this.mGroupMembersProvider;
    }

    public void setMentionedInputListener(IMentionedInputListener listener) {
        this.mMentionedInputListener = listener;
    }

    private static class SingletonHolder {
        static io.rong.imkit.mention.RongMentionManager sInstance = new io.rong.imkit.mention.RongMentionManager();

        private SingletonHolder() {
        }
    }
}
