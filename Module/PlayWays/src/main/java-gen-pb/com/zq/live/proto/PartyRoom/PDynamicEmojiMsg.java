// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: party_room.proto
package com.zq.live.proto.PartyRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import java.io.IOException;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class PDynamicEmojiMsg extends Message<PDynamicEmojiMsg, PDynamicEmojiMsg.Builder> {
  public static final ProtoAdapter<PDynamicEmojiMsg> ADAPTER = new ProtoAdapter_PDynamicEmojiMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_ID = 0;

  public static final String DEFAULT_SMALLEMOJIURL = "";

  public static final String DEFAULT_BIGEMOJIURL = "";

  /**
   * 表情包id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer id;

  /**
   * 小图
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String smallEmojiURL;

  /**
   * 大图
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String bigEmojiURL;

  /**
   * 发送用户信息
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.PartyRoom.POnlineInfo#ADAPTER"
  )
  private final POnlineInfo user;

  public PDynamicEmojiMsg(Integer id, String smallEmojiURL, String bigEmojiURL, POnlineInfo user) {
    this(id, smallEmojiURL, bigEmojiURL, user, ByteString.EMPTY);
  }

  public PDynamicEmojiMsg(Integer id, String smallEmojiURL, String bigEmojiURL, POnlineInfo user,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.id = id;
    this.smallEmojiURL = smallEmojiURL;
    this.bigEmojiURL = bigEmojiURL;
    this.user = user;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.id = id;
    builder.smallEmojiURL = smallEmojiURL;
    builder.bigEmojiURL = bigEmojiURL;
    builder.user = user;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PDynamicEmojiMsg)) return false;
    PDynamicEmojiMsg o = (PDynamicEmojiMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(id, o.id)
        && Internal.equals(smallEmojiURL, o.smallEmojiURL)
        && Internal.equals(bigEmojiURL, o.bigEmojiURL)
        && Internal.equals(user, o.user);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (id != null ? id.hashCode() : 0);
      result = result * 37 + (smallEmojiURL != null ? smallEmojiURL.hashCode() : 0);
      result = result * 37 + (bigEmojiURL != null ? bigEmojiURL.hashCode() : 0);
      result = result * 37 + (user != null ? user.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (id != null) builder.append(", id=").append(id);
    if (smallEmojiURL != null) builder.append(", smallEmojiURL=").append(smallEmojiURL);
    if (bigEmojiURL != null) builder.append(", bigEmojiURL=").append(bigEmojiURL);
    if (user != null) builder.append(", user=").append(user);
    return builder.replace(0, 2, "PDynamicEmojiMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PDynamicEmojiMsg.ADAPTER.encode(this);
  }

  public static final PDynamicEmojiMsg parseFrom(byte[] data) throws IOException {
    PDynamicEmojiMsg c = null;
       c = PDynamicEmojiMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 表情包id
   */
  public Integer getId() {
    if(id==null){
        return DEFAULT_ID;
    }
    return id;
  }

  /**
   * 小图
   */
  public String getSmallEmojiURL() {
    if(smallEmojiURL==null){
        return DEFAULT_SMALLEMOJIURL;
    }
    return smallEmojiURL;
  }

  /**
   * 大图
   */
  public String getBigEmojiURL() {
    if(bigEmojiURL==null){
        return DEFAULT_BIGEMOJIURL;
    }
    return bigEmojiURL;
  }

  /**
   * 发送用户信息
   */
  public POnlineInfo getUser() {
    if(user==null){
        return new POnlineInfo.Builder().build();
    }
    return user;
  }

  /**
   * 表情包id
   */
  public boolean hasId() {
    return id!=null;
  }

  /**
   * 小图
   */
  public boolean hasSmallEmojiURL() {
    return smallEmojiURL!=null;
  }

  /**
   * 大图
   */
  public boolean hasBigEmojiURL() {
    return bigEmojiURL!=null;
  }

  /**
   * 发送用户信息
   */
  public boolean hasUser() {
    return user!=null;
  }

  public static final class Builder extends Message.Builder<PDynamicEmojiMsg, Builder> {
    private Integer id;

    private String smallEmojiURL;

    private String bigEmojiURL;

    private POnlineInfo user;

    public Builder() {
    }

    /**
     * 表情包id
     */
    public Builder setId(Integer id) {
      this.id = id;
      return this;
    }

    /**
     * 小图
     */
    public Builder setSmallEmojiURL(String smallEmojiURL) {
      this.smallEmojiURL = smallEmojiURL;
      return this;
    }

    /**
     * 大图
     */
    public Builder setBigEmojiURL(String bigEmojiURL) {
      this.bigEmojiURL = bigEmojiURL;
      return this;
    }

    /**
     * 发送用户信息
     */
    public Builder setUser(POnlineInfo user) {
      this.user = user;
      return this;
    }

    @Override
    public PDynamicEmojiMsg build() {
      return new PDynamicEmojiMsg(id, smallEmojiURL, bigEmojiURL, user, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PDynamicEmojiMsg extends ProtoAdapter<PDynamicEmojiMsg> {
    public ProtoAdapter_PDynamicEmojiMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, PDynamicEmojiMsg.class);
    }

    @Override
    public int encodedSize(PDynamicEmojiMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.id)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.smallEmojiURL)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.bigEmojiURL)
          + POnlineInfo.ADAPTER.encodedSizeWithTag(4, value.user)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PDynamicEmojiMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.id);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.smallEmojiURL);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.bigEmojiURL);
      POnlineInfo.ADAPTER.encodeWithTag(writer, 4, value.user);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PDynamicEmojiMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setId(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setSmallEmojiURL(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.setBigEmojiURL(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.setUser(POnlineInfo.ADAPTER.decode(reader)); break;
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
    public PDynamicEmojiMsg redact(PDynamicEmojiMsg value) {
      Builder builder = value.newBuilder();
      if (builder.user != null) builder.user = POnlineInfo.ADAPTER.redact(builder.user);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
