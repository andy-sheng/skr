//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget.provider;

import android.content.Context;

import io.rong.imkit.model.UIMessage;

public class MessageItemLongClickAction {
  private String title;
  private int titleResId;
  public int priority;
  public io.rong.imkit.widget.provider.MessageItemLongClickAction.MessageItemLongClickListener listener;
  public io.rong.imkit.widget.provider.MessageItemLongClickAction.Filter filter;

  private MessageItemLongClickAction(int titleResId, String title, io.rong.imkit.widget.provider.MessageItemLongClickAction.MessageItemLongClickListener listener, io.rong.imkit.widget.provider.MessageItemLongClickAction.Filter filter) {
    this.titleResId = titleResId;
    this.title = title;
    this.listener = listener;
    this.filter = filter;
  }

  public String getTitle(Context context) {
    return context != null && this.titleResId > 0 ? context.getResources().getString(this.titleResId) : this.title;
  }

  public boolean filter(UIMessage message) {
    return this.filter == null || this.filter.filter(message);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && this.getClass() == o.getClass()) {
      io.rong.imkit.widget.provider.MessageItemLongClickAction that = (io.rong.imkit.widget.provider.MessageItemLongClickAction)o;
      if (this.titleResId != that.titleResId) {
        return false;
      } else {
        return this.title != null ? this.title.equals(that.title) : that.title == null;
      }
    } else {
      return false;
    }
  }

  public int hashCode() {
    int result = this.title != null ? this.title.hashCode() : 0;
    result = 31 * result + this.titleResId;
    return result;
  }

  public interface Filter {
    boolean filter(UIMessage var1);
  }

  public interface MessageItemLongClickListener {
    boolean onMessageItemLongClick(Context var1, UIMessage var2);
  }

  public static class Builder {
    private String title;
    private int titleResId;
    private io.rong.imkit.widget.provider.MessageItemLongClickAction.MessageItemLongClickListener listener;
    private io.rong.imkit.widget.provider.MessageItemLongClickAction.Filter filter;
    private int priority;

    public Builder() {
    }

    public io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder title(String title) {
      this.title = title;
      return this;
    }

    public io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder titleResId(int resId) {
      this.titleResId = resId;
      return this;
    }

    public io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder actionListener(io.rong.imkit.widget.provider.MessageItemLongClickAction.MessageItemLongClickListener listener) {
      this.listener = listener;
      return this;
    }

    public io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder showFilter(io.rong.imkit.widget.provider.MessageItemLongClickAction.Filter filter) {
      this.filter = filter;
      return this;
    }

    public io.rong.imkit.widget.provider.MessageItemLongClickAction.Builder priority(int priority) {
      this.priority = priority;
      return this;
    }

    public io.rong.imkit.widget.provider.MessageItemLongClickAction build() {
      io.rong.imkit.widget.provider.MessageItemLongClickAction action = new io.rong.imkit.widget.provider.MessageItemLongClickAction(this.titleResId, this.title, this.listener, this.filter);
      action.priority = this.priority;
      return action;
    }
  }
}
