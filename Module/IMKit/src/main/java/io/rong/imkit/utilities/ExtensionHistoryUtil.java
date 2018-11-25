//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

import io.rong.imlib.model.Conversation.ConversationType;

public class ExtensionHistoryUtil {
  private static boolean enableHistory;
  private static final String EMOJI_POS = "EMOJI_POS";
  private static final String EXTENSION_BAR_STATE = "EXTENSION_BAR_STATE";
  private static List<ConversationType> sExceptConversationTypes = new ArrayList();

  public ExtensionHistoryUtil() {
  }

  public static void setEnableHistory(boolean enable) {
    enableHistory = enable;
  }

  public static void addExceptConversationType(ConversationType conversationType) {
    sExceptConversationTypes.add(conversationType);
  }

  public static void setEmojiPosition(Context context, String id, int position) {
    if (enableHistory) {
      SharedPreferences sp = context.getSharedPreferences("RongKitConfig", 0);
      sp.edit().putInt(id + "EMOJI_POS", position).commit();
    }

  }

  public static int getEmojiPosition(Context context, String id) {
    if (!enableHistory) {
      return 0;
    } else {
      SharedPreferences sp = context.getSharedPreferences("RongKitConfig", 0);
      return sp.getInt(id + "EMOJI_POS", 0);
    }
  }

  public static void setExtensionBarState(Context context, String id, ConversationType conversationType, io.rong.imkit.utilities.ExtensionHistoryUtil.ExtensionBarState state) {
    if (enableHistory && !sExceptConversationTypes.contains(conversationType)) {
      SharedPreferences sp = context.getSharedPreferences("RongKitConfig", 0);
      sp.edit().putString(id + "EXTENSION_BAR_STATE", state.toString()).commit();
    }

  }

  public static io.rong.imkit.utilities.ExtensionHistoryUtil.ExtensionBarState getExtensionBarState(Context context, String id, ConversationType conversationType) {
    if (enableHistory && !sExceptConversationTypes.contains(conversationType)) {
      SharedPreferences sp = context.getSharedPreferences("RongKitConfig", 0);
      String v = sp.getString(id + "EXTENSION_BAR_STATE", io.rong.imkit.utilities.ExtensionHistoryUtil.ExtensionBarState.NORMAL.toString());
      return io.rong.imkit.utilities.ExtensionHistoryUtil.ExtensionBarState.valueOf(v);
    } else {
      return io.rong.imkit.utilities.ExtensionHistoryUtil.ExtensionBarState.NORMAL;
    }
  }

  public static enum ExtensionBarState {
    NORMAL,
    VOICE;

    private ExtensionBarState() {
    }
  }
}
