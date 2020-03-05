// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: battle_room.proto
package com.zq.live.proto.BattleRoom;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import com.squareup.wire.internal.Internal;
import com.zq.live.proto.Common.UserInfo;
import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class BTeamInfo extends Message<BTeamInfo, BTeamInfo.Builder> {
  public static final ProtoAdapter<BTeamInfo> ADAPTER = new ProtoAdapter_BTeamInfo();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_TEAMTAG = "";

  /**
   * 队伍标识
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String teamTag;

  /**
   * 队伍玩家信息
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.Common.UserInfo#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<UserInfo> teamUsers;

  public BTeamInfo(String teamTag, List<UserInfo> teamUsers) {
    this(teamTag, teamUsers, ByteString.EMPTY);
  }

  public BTeamInfo(String teamTag, List<UserInfo> teamUsers, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.teamTag = teamTag;
    this.teamUsers = Internal.immutableCopyOf("teamUsers", teamUsers);
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.teamTag = teamTag;
    builder.teamUsers = Internal.copyOf("teamUsers", teamUsers);
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BTeamInfo)) return false;
    BTeamInfo o = (BTeamInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(teamTag, o.teamTag)
        && teamUsers.equals(o.teamUsers);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (teamTag != null ? teamTag.hashCode() : 0);
      result = result * 37 + teamUsers.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (teamTag != null) builder.append(", teamTag=").append(teamTag);
    if (!teamUsers.isEmpty()) builder.append(", teamUsers=").append(teamUsers);
    return builder.replace(0, 2, "BTeamInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return BTeamInfo.ADAPTER.encode(this);
  }

  public static final BTeamInfo parseFrom(byte[] data) throws IOException {
    BTeamInfo c = null;
       c = BTeamInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 队伍标识
   */
  public String getTeamTag() {
    if(teamTag==null){
        return DEFAULT_TEAMTAG;
    }
    return teamTag;
  }

  /**
   * 队伍玩家信息
   */
  public List<UserInfo> getTeamUsersList() {
    if(teamUsers==null){
        return new java.util.ArrayList<UserInfo>();
    }
    return teamUsers;
  }

  /**
   * 队伍标识
   */
  public boolean hasTeamTag() {
    return teamTag!=null;
  }

  /**
   * 队伍玩家信息
   */
  public boolean hasTeamUsersList() {
    return teamUsers!=null;
  }

  public static final class Builder extends Message.Builder<BTeamInfo, Builder> {
    private String teamTag;

    private List<UserInfo> teamUsers;

    public Builder() {
      teamUsers = Internal.newMutableList();
    }

    /**
     * 队伍标识
     */
    public Builder setTeamTag(String teamTag) {
      this.teamTag = teamTag;
      return this;
    }

    /**
     * 队伍玩家信息
     */
    public Builder addAllTeamUsers(List<UserInfo> teamUsers) {
      Internal.checkElementsNotNull(teamUsers);
      this.teamUsers = teamUsers;
      return this;
    }

    @Override
    public BTeamInfo build() {
      return new BTeamInfo(teamTag, teamUsers, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_BTeamInfo extends ProtoAdapter<BTeamInfo> {
    public ProtoAdapter_BTeamInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, BTeamInfo.class);
    }

    @Override
    public int encodedSize(BTeamInfo value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.teamTag)
          + UserInfo.ADAPTER.asRepeated().encodedSizeWithTag(2, value.teamUsers)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, BTeamInfo value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.teamTag);
      UserInfo.ADAPTER.asRepeated().encodeWithTag(writer, 2, value.teamUsers);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public BTeamInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setTeamTag(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.teamUsers.add(UserInfo.ADAPTER.decode(reader)); break;
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
    public BTeamInfo redact(BTeamInfo value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.teamUsers, UserInfo.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}