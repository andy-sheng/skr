// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Common.proto
package com.zq.live.proto.Common;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Boolean;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class GiftExtraInfo extends Message<GiftExtraInfo, GiftExtraInfo.Builder> {
  public static final ProtoAdapter<GiftExtraInfo> ADAPTER = new ProtoAdapter_GiftExtraInfo();

  private static final long serialVersionUID = 0L;

  public static final Long DEFAULT_DURATION = 0L;

  public static final Integer DEFAULT_WIDTH = 0;

  public static final Integer DEFAULT_HEIGHT = 0;

  public static final Integer DEFAULT_LEFT = 0;

  public static final Integer DEFAULT_RIGHT = 0;

  public static final Integer DEFAULT_TOP = 0;

  public static final Integer DEFAULT_BOTTOM = 0;

  public static final Boolean DEFAULT_ISFULLSCREEN = false;

  public static final Boolean DEFAULT_ISFULLX = false;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#SINT64"
  )
  private final Long duration;

  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer width;

  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer height;

  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer left;

  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer right;

  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer top;

  @WireField(
      tag = 7,
      adapter = "com.squareup.wire.ProtoAdapter#SINT32"
  )
  private final Integer bottom;

  /**
   * 是否全屏
   */
  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean isFullScreen;

  /**
   * true 水平平铺  false 垂直平铺
   */
  @WireField(
      tag = 9,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean isFullX;

  public GiftExtraInfo(Long duration, Integer width, Integer height, Integer left, Integer right,
      Integer top, Integer bottom, Boolean isFullScreen, Boolean isFullX) {
    this(duration, width, height, left, right, top, bottom, isFullScreen, isFullX, ByteString.EMPTY);
  }

  public GiftExtraInfo(Long duration, Integer width, Integer height, Integer left, Integer right,
      Integer top, Integer bottom, Boolean isFullScreen, Boolean isFullX,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.duration = duration;
    this.width = width;
    this.height = height;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.isFullScreen = isFullScreen;
    this.isFullX = isFullX;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.duration = duration;
    builder.width = width;
    builder.height = height;
    builder.left = left;
    builder.right = right;
    builder.top = top;
    builder.bottom = bottom;
    builder.isFullScreen = isFullScreen;
    builder.isFullX = isFullX;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof GiftExtraInfo)) return false;
    GiftExtraInfo o = (GiftExtraInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(duration, o.duration)
        && Internal.equals(width, o.width)
        && Internal.equals(height, o.height)
        && Internal.equals(left, o.left)
        && Internal.equals(right, o.right)
        && Internal.equals(top, o.top)
        && Internal.equals(bottom, o.bottom)
        && Internal.equals(isFullScreen, o.isFullScreen)
        && Internal.equals(isFullX, o.isFullX);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (duration != null ? duration.hashCode() : 0);
      result = result * 37 + (width != null ? width.hashCode() : 0);
      result = result * 37 + (height != null ? height.hashCode() : 0);
      result = result * 37 + (left != null ? left.hashCode() : 0);
      result = result * 37 + (right != null ? right.hashCode() : 0);
      result = result * 37 + (top != null ? top.hashCode() : 0);
      result = result * 37 + (bottom != null ? bottom.hashCode() : 0);
      result = result * 37 + (isFullScreen != null ? isFullScreen.hashCode() : 0);
      result = result * 37 + (isFullX != null ? isFullX.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (duration != null) builder.append(", duration=").append(duration);
    if (width != null) builder.append(", width=").append(width);
    if (height != null) builder.append(", height=").append(height);
    if (left != null) builder.append(", left=").append(left);
    if (right != null) builder.append(", right=").append(right);
    if (top != null) builder.append(", top=").append(top);
    if (bottom != null) builder.append(", bottom=").append(bottom);
    if (isFullScreen != null) builder.append(", isFullScreen=").append(isFullScreen);
    if (isFullX != null) builder.append(", isFullX=").append(isFullX);
    return builder.replace(0, 2, "GiftExtraInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return GiftExtraInfo.ADAPTER.encode(this);
  }

  public static final GiftExtraInfo parseFrom(byte[] data) throws IOException {
    GiftExtraInfo c = null;
       c = GiftExtraInfo.ADAPTER.decode(data);
    return c;
  }

  public Long getDuration() {
    if(duration==null){
        return DEFAULT_DURATION;
    }
    return duration;
  }

  public Integer getWidth() {
    if(width==null){
        return DEFAULT_WIDTH;
    }
    return width;
  }

  public Integer getHeight() {
    if(height==null){
        return DEFAULT_HEIGHT;
    }
    return height;
  }

  public Integer getLeft() {
    if(left==null){
        return DEFAULT_LEFT;
    }
    return left;
  }

  public Integer getRight() {
    if(right==null){
        return DEFAULT_RIGHT;
    }
    return right;
  }

  public Integer getTop() {
    if(top==null){
        return DEFAULT_TOP;
    }
    return top;
  }

  public Integer getBottom() {
    if(bottom==null){
        return DEFAULT_BOTTOM;
    }
    return bottom;
  }

  /**
   * 是否全屏
   */
  public Boolean getIsFullScreen() {
    if(isFullScreen==null){
        return DEFAULT_ISFULLSCREEN;
    }
    return isFullScreen;
  }

  /**
   * true 水平平铺  false 垂直平铺
   */
  public Boolean getIsFullX() {
    if(isFullX==null){
        return DEFAULT_ISFULLX;
    }
    return isFullX;
  }

  public boolean hasDuration() {
    return duration!=null;
  }

  public boolean hasWidth() {
    return width!=null;
  }

  public boolean hasHeight() {
    return height!=null;
  }

  public boolean hasLeft() {
    return left!=null;
  }

  public boolean hasRight() {
    return right!=null;
  }

  public boolean hasTop() {
    return top!=null;
  }

  public boolean hasBottom() {
    return bottom!=null;
  }

  /**
   * 是否全屏
   */
  public boolean hasIsFullScreen() {
    return isFullScreen!=null;
  }

  /**
   * true 水平平铺  false 垂直平铺
   */
  public boolean hasIsFullX() {
    return isFullX!=null;
  }

  public static final class Builder extends Message.Builder<GiftExtraInfo, Builder> {
    private Long duration;

    private Integer width;

    private Integer height;

    private Integer left;

    private Integer right;

    private Integer top;

    private Integer bottom;

    private Boolean isFullScreen;

    private Boolean isFullX;

    public Builder() {
    }

    public Builder setDuration(Long duration) {
      this.duration = duration;
      return this;
    }

    public Builder setWidth(Integer width) {
      this.width = width;
      return this;
    }

    public Builder setHeight(Integer height) {
      this.height = height;
      return this;
    }

    public Builder setLeft(Integer left) {
      this.left = left;
      return this;
    }

    public Builder setRight(Integer right) {
      this.right = right;
      return this;
    }

    public Builder setTop(Integer top) {
      this.top = top;
      return this;
    }

    public Builder setBottom(Integer bottom) {
      this.bottom = bottom;
      return this;
    }

    /**
     * 是否全屏
     */
    public Builder setIsFullScreen(Boolean isFullScreen) {
      this.isFullScreen = isFullScreen;
      return this;
    }

    /**
     * true 水平平铺  false 垂直平铺
     */
    public Builder setIsFullX(Boolean isFullX) {
      this.isFullX = isFullX;
      return this;
    }

    @Override
    public GiftExtraInfo build() {
      return new GiftExtraInfo(duration, width, height, left, right, top, bottom, isFullScreen, isFullX, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_GiftExtraInfo extends ProtoAdapter<GiftExtraInfo> {
    public ProtoAdapter_GiftExtraInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, GiftExtraInfo.class);
    }

    @Override
    public int encodedSize(GiftExtraInfo value) {
      return ProtoAdapter.SINT64.encodedSizeWithTag(1, value.duration)
          + ProtoAdapter.SINT32.encodedSizeWithTag(2, value.width)
          + ProtoAdapter.SINT32.encodedSizeWithTag(3, value.height)
          + ProtoAdapter.SINT32.encodedSizeWithTag(4, value.left)
          + ProtoAdapter.SINT32.encodedSizeWithTag(5, value.right)
          + ProtoAdapter.SINT32.encodedSizeWithTag(6, value.top)
          + ProtoAdapter.SINT32.encodedSizeWithTag(7, value.bottom)
          + ProtoAdapter.BOOL.encodedSizeWithTag(8, value.isFullScreen)
          + ProtoAdapter.BOOL.encodedSizeWithTag(9, value.isFullX)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, GiftExtraInfo value) throws IOException {
      ProtoAdapter.SINT64.encodeWithTag(writer, 1, value.duration);
      ProtoAdapter.SINT32.encodeWithTag(writer, 2, value.width);
      ProtoAdapter.SINT32.encodeWithTag(writer, 3, value.height);
      ProtoAdapter.SINT32.encodeWithTag(writer, 4, value.left);
      ProtoAdapter.SINT32.encodeWithTag(writer, 5, value.right);
      ProtoAdapter.SINT32.encodeWithTag(writer, 6, value.top);
      ProtoAdapter.SINT32.encodeWithTag(writer, 7, value.bottom);
      ProtoAdapter.BOOL.encodeWithTag(writer, 8, value.isFullScreen);
      ProtoAdapter.BOOL.encodeWithTag(writer, 9, value.isFullX);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public GiftExtraInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setDuration(ProtoAdapter.SINT64.decode(reader)); break;
          case 2: builder.setWidth(ProtoAdapter.SINT32.decode(reader)); break;
          case 3: builder.setHeight(ProtoAdapter.SINT32.decode(reader)); break;
          case 4: builder.setLeft(ProtoAdapter.SINT32.decode(reader)); break;
          case 5: builder.setRight(ProtoAdapter.SINT32.decode(reader)); break;
          case 6: builder.setTop(ProtoAdapter.SINT32.decode(reader)); break;
          case 7: builder.setBottom(ProtoAdapter.SINT32.decode(reader)); break;
          case 8: builder.setIsFullScreen(ProtoAdapter.BOOL.decode(reader)); break;
          case 9: builder.setIsFullX(ProtoAdapter.BOOL.decode(reader)); break;
          default: {
            FieldEncoding fieldEncoding = reader.peekFieldEncoding();
            Object value = fieldEncoding.rawProtoAdapter().decode(reader);
            builder.addUnknownField(tag, fieldEncoding, value);
          }
        }
      }
      reader.endMessage(token);
      return builder.build();
    }

    @Override
    public GiftExtraInfo redact(GiftExtraInfo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
