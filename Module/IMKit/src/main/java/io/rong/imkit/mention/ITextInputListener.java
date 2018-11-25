//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.mention;

import android.widget.EditText;

import io.rong.imlib.model.Conversation.ConversationType;
import io.rong.imlib.model.MentionedInfo;

public interface ITextInputListener {
  void onTextEdit(ConversationType var1, String var2, int var3, int var4, String var5);

  MentionedInfo onSendButtonClick();

  void onDeleteClick(ConversationType var1, String var2, EditText var3, int var4);
}
