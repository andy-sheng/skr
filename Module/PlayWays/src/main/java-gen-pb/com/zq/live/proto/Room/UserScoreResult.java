// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.Room;

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
import java.util.List;
import okio.ByteString;

public final class UserScoreResult extends Message<UserScoreResult, UserScoreResult.Builder> {
  public static final ProtoAdapter<UserScoreResult> ADAPTER = new ProtoAdapter_UserScoreResult();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_USERID = 0;

  public static final EWinType DEFAULT_WINTYPE = EWinType.InvalidEWinType;

  public static final Integer DEFAULT_SSS = 0;

  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer userID;

  /**
   * 分值状态：初始、中间、最终状态，第0个为占位用
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.Room.ScoreState#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<ScoreState> states;

  /**
   * 星星变动详情，通过累加可以判断是加星还是减星
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.Room.ScoreItem#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<ScoreItem> starChange;

  /**
   * 战力值变动详情，累加可以判断是增加还是减少
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.Room.ScoreItem#ADAPTER",
      label = WireField.Label.REPEATED
  )
  private final List<ScoreItem> battleIndexChange;

  /**
   * 胜负平等等
   */
  @WireField(
      tag = 5,
      adapter = "com.zq.live.proto.Room.EWinType#ADAPTER"
  )
  private final EWinType winType;

  /**
   * 战斗评价, sss or ss or s or a...
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer sss;

  public UserScoreResult(Integer userID, List<ScoreState> states, List<ScoreItem> starChange,
      List<ScoreItem> battleIndexChange, EWinType winType, Integer sss) {
    this(userID, states, starChange, battleIndexChange, winType, sss, ByteString.EMPTY);
  }

  public UserScoreResult(Integer userID, List<ScoreState> states, List<ScoreItem> starChange,
      List<ScoreItem> battleIndexChange, EWinType winType, Integer sss, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.userID = userID;
    this.states = Internal.immutableCopyOf("states", states);
    this.starChange = Internal.immutableCopyOf("starChange", starChange);
    this.battleIndexChange = Internal.immutableCopyOf("battleIndexChange", battleIndexChange);
    this.winType = winType;
    this.sss = sss;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.userID = userID;
    builder.states = Internal.copyOf("states", states);
    builder.starChange = Internal.copyOf("starChange", starChange);
    builder.battleIndexChange = Internal.copyOf("battleIndexChange", battleIndexChange);
    builder.winType = winType;
    builder.sss = sss;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof UserScoreResult)) return false;
    UserScoreResult o = (UserScoreResult) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(userID, o.userID)
        && states.equals(o.states)
        && starChange.equals(o.starChange)
        && battleIndexChange.equals(o.battleIndexChange)
        && Internal.equals(winType, o.winType)
        && Internal.equals(sss, o.sss);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (userID != null ? userID.hashCode() : 0);
      result = result * 37 + states.hashCode();
      result = result * 37 + starChange.hashCode();
      result = result * 37 + battleIndexChange.hashCode();
      result = result * 37 + (winType != null ? winType.hashCode() : 0);
      result = result * 37 + (sss != null ? sss.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (userID != null) builder.append(", userID=").append(userID);
    if (!states.isEmpty()) builder.append(", states=").append(states);
    if (!starChange.isEmpty()) builder.append(", starChange=").append(starChange);
    if (!battleIndexChange.isEmpty()) builder.append(", battleIndexChange=").append(battleIndexChange);
    if (winType != null) builder.append(", winType=").append(winType);
    if (sss != null) builder.append(", sss=").append(sss);
    return builder.replace(0, 2, "UserScoreResult{").append('}').toString();
  }

  public byte[] toByteArray() {
    return UserScoreResult.ADAPTER.encode(this);
  }

  public static final UserScoreResult parseFrom(byte[] data) throws IOException {
    UserScoreResult c = null;
       c = UserScoreResult.ADAPTER.decode(data);
    return c;
  }

  public Integer getUserID() {
    if(userID==null){
        return DEFAULT_USERID;
    }
    return userID;
  }

  /**
   * 分值状态：初始、中间、最终状态，第0个为占位用
   */
  public List<ScoreState> getStatesList() {
    if(states==null){
        return new java.util.ArrayList<ScoreState>();
    }
    return states;
  }

  /**
   * 星星变动详情，通过累加可以判断是加星还是减星
   */
  public List<ScoreItem> getStarChangeList() {
    if(starChange==null){
        return new java.util.ArrayList<ScoreItem>();
    }
    return starChange;
  }

  /**
   * 战力值变动详情，累加可以判断是增加还是减少
   */
  public List<ScoreItem> getBattleIndexChangeList() {
    if(battleIndexChange==null){
        return new java.util.ArrayList<ScoreItem>();
    }
    return battleIndexChange;
  }

  /**
   * 胜负平等等
   */
  public EWinType getWinType() {
    if(winType==null){
        return new EWinType.Builder().build();
    }
    return winType;
  }

  /**
   * 战斗评价, sss or ss or s or a...
   */
  public Integer getSss() {
    if(sss==null){
        return DEFAULT_SSS;
    }
    return sss;
  }

  public boolean hasUserID() {
    return userID!=null;
  }

  /**
   * 分值状态：初始、中间、最终状态，第0个为占位用
   */
  public boolean hasStatesList() {
    return states!=null;
  }

  /**
   * 星星变动详情，通过累加可以判断是加星还是减星
   */
  public boolean hasStarChangeList() {
    return starChange!=null;
  }

  /**
   * 战力值变动详情，累加可以判断是增加还是减少
   */
  public boolean hasBattleIndexChangeList() {
    return battleIndexChange!=null;
  }

  /**
   * 胜负平等等
   */
  public boolean hasWinType() {
    return winType!=null;
  }

  /**
   * 战斗评价, sss or ss or s or a...
   */
  public boolean hasSss() {
    return sss!=null;
  }

  public static final class Builder extends Message.Builder<UserScoreResult, Builder> {
    private Integer userID;

    private List<ScoreState> states;

    private List<ScoreItem> starChange;

    private List<ScoreItem> battleIndexChange;

    private EWinType winType;

    private Integer sss;

    public Builder() {
      states = Internal.newMutableList();
      starChange = Internal.newMutableList();
      battleIndexChange = Internal.newMutableList();
    }

    public Builder setUserID(Integer userID) {
      this.userID = userID;
      return this;
    }

    /**
     * 分值状态：初始、中间、最终状态，第0个为占位用
     */
    public Builder addAllStates(List<ScoreState> states) {
      Internal.checkElementsNotNull(states);
      this.states = states;
      return this;
    }

    /**
     * 星星变动详情，通过累加可以判断是加星还是减星
     */
    public Builder addAllStarChange(List<ScoreItem> starChange) {
      Internal.checkElementsNotNull(starChange);
      this.starChange = starChange;
      return this;
    }

    /**
     * 战力值变动详情，累加可以判断是增加还是减少
     */
    public Builder addAllBattleIndexChange(List<ScoreItem> battleIndexChange) {
      Internal.checkElementsNotNull(battleIndexChange);
      this.battleIndexChange = battleIndexChange;
      return this;
    }

    /**
     * 胜负平等等
     */
    public Builder setWinType(EWinType winType) {
      this.winType = winType;
      return this;
    }

    /**
     * 战斗评价, sss or ss or s or a...
     */
    public Builder setSss(Integer sss) {
      this.sss = sss;
      return this;
    }

    @Override
    public UserScoreResult build() {
      return new UserScoreResult(userID, states, starChange, battleIndexChange, winType, sss, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_UserScoreResult extends ProtoAdapter<UserScoreResult> {
    public ProtoAdapter_UserScoreResult() {
      super(FieldEncoding.LENGTH_DELIMITED, UserScoreResult.class);
    }

    @Override
    public int encodedSize(UserScoreResult value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.userID)
          + ScoreState.ADAPTER.asRepeated().encodedSizeWithTag(2, value.states)
          + ScoreItem.ADAPTER.asRepeated().encodedSizeWithTag(3, value.starChange)
          + ScoreItem.ADAPTER.asRepeated().encodedSizeWithTag(4, value.battleIndexChange)
          + EWinType.ADAPTER.encodedSizeWithTag(5, value.winType)
          + ProtoAdapter.UINT32.encodedSizeWithTag(6, value.sss)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, UserScoreResult value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.userID);
      ScoreState.ADAPTER.asRepeated().encodeWithTag(writer, 2, value.states);
      ScoreItem.ADAPTER.asRepeated().encodeWithTag(writer, 3, value.starChange);
      ScoreItem.ADAPTER.asRepeated().encodeWithTag(writer, 4, value.battleIndexChange);
      EWinType.ADAPTER.encodeWithTag(writer, 5, value.winType);
      ProtoAdapter.UINT32.encodeWithTag(writer, 6, value.sss);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public UserScoreResult decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.states.add(ScoreState.ADAPTER.decode(reader)); break;
          case 3: builder.starChange.add(ScoreItem.ADAPTER.decode(reader)); break;
          case 4: builder.battleIndexChange.add(ScoreItem.ADAPTER.decode(reader)); break;
          case 5: {
            try {
              builder.setWinType(EWinType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 6: builder.setSss(ProtoAdapter.UINT32.decode(reader)); break;
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
    public UserScoreResult redact(UserScoreResult value) {
      Builder builder = value.newBuilder();
      Internal.redactElements(builder.states, ScoreState.ADAPTER);
      Internal.redactElements(builder.starChange, ScoreItem.ADAPTER);
      Internal.redactElements(builder.battleIndexChange, ScoreItem.ADAPTER);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
