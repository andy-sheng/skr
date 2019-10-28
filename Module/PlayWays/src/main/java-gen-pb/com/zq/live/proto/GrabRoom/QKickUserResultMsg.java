// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Room.proto
package com.zq.live.proto.GrabRoom;

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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.util.List;
import okio.ByteString;

public final class QKickUserResultMsg extends Message<QKickUserResultMsg, QKickUserResultMsg.Builder> {
  public static final ProtoAdapter<QKickUserResultMsg> ADAPTER = new ProtoAdapter_QKickUserResultMsg();

  private static final long serialVersionUID = 0L;

  public static final Integer DEFAULT_SOURCEUSERID = 0;

  public static final Integer DEFAULT_KICKUSERID = 0;

  public static final Boolean DEFAULT_ISKICKSUCCESS = false;

  public static final EQKickFailedReason DEFAULT_KICKFAILEDREASON = EQKickFailedReason.KFR_UNKNOWN;

  public static final String DEFAULT_KICKRESULTCONTENT = "";

  /**
   * 发起者id
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer sourceUserID;

  /**
   * 被踢者id
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer kickUserID;

  /**
   * 投同意票用户id
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REPEATED
  )
  private final List<Integer> giveYesVoteUserIDs;

  /**
   * 投不同意票用户id
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REPEATED
  )
  private final List<Integer> giveNoVoteUserIDs;

  /**
   * 未知票用户id
   */
  @WireField(
      tag = 5,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REPEATED
  )
  private final List<Integer> giveUnknownVoteUserIDs;

  /**
   * 踢人是否成功
   */
  @WireField(
      tag = 6,
      adapter = "com.squareup.wire.ProtoAdapter#BOOL"
  )
  private final Boolean isKickSuccess;

  /**
   * 踢人失败原因
   */
  @WireField(
      tag = 7,
      adapter = "com.zq.live.proto.GrabRoom.EQKickFailedReason#ADAPTER"
  )
  private final EQKickFailedReason kickFailedReason;

  /**
   * 踢人结果信息内容
   */
  @WireField(
      tag = 8,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String kickResultContent;

  public QKickUserResultMsg(Integer sourceUserID, Integer kickUserID,
      List<Integer> giveYesVoteUserIDs, List<Integer> giveNoVoteUserIDs,
      List<Integer> giveUnknownVoteUserIDs, Boolean isKickSuccess,
      EQKickFailedReason kickFailedReason, String kickResultContent) {
    this(sourceUserID, kickUserID, giveYesVoteUserIDs, giveNoVoteUserIDs, giveUnknownVoteUserIDs, isKickSuccess, kickFailedReason, kickResultContent, ByteString.EMPTY);
  }

  public QKickUserResultMsg(Integer sourceUserID, Integer kickUserID,
      List<Integer> giveYesVoteUserIDs, List<Integer> giveNoVoteUserIDs,
      List<Integer> giveUnknownVoteUserIDs, Boolean isKickSuccess,
      EQKickFailedReason kickFailedReason, String kickResultContent, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.sourceUserID = sourceUserID;
    this.kickUserID = kickUserID;
    this.giveYesVoteUserIDs = Internal.immutableCopyOf("giveYesVoteUserIDs", giveYesVoteUserIDs);
    this.giveNoVoteUserIDs = Internal.immutableCopyOf("giveNoVoteUserIDs", giveNoVoteUserIDs);
    this.giveUnknownVoteUserIDs = Internal.immutableCopyOf("giveUnknownVoteUserIDs", giveUnknownVoteUserIDs);
    this.isKickSuccess = isKickSuccess;
    this.kickFailedReason = kickFailedReason;
    this.kickResultContent = kickResultContent;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.sourceUserID = sourceUserID;
    builder.kickUserID = kickUserID;
    builder.giveYesVoteUserIDs = Internal.copyOf("giveYesVoteUserIDs", giveYesVoteUserIDs);
    builder.giveNoVoteUserIDs = Internal.copyOf("giveNoVoteUserIDs", giveNoVoteUserIDs);
    builder.giveUnknownVoteUserIDs = Internal.copyOf("giveUnknownVoteUserIDs", giveUnknownVoteUserIDs);
    builder.isKickSuccess = isKickSuccess;
    builder.kickFailedReason = kickFailedReason;
    builder.kickResultContent = kickResultContent;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof QKickUserResultMsg)) return false;
    QKickUserResultMsg o = (QKickUserResultMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(sourceUserID, o.sourceUserID)
        && Internal.equals(kickUserID, o.kickUserID)
        && giveYesVoteUserIDs.equals(o.giveYesVoteUserIDs)
        && giveNoVoteUserIDs.equals(o.giveNoVoteUserIDs)
        && giveUnknownVoteUserIDs.equals(o.giveUnknownVoteUserIDs)
        && Internal.equals(isKickSuccess, o.isKickSuccess)
        && Internal.equals(kickFailedReason, o.kickFailedReason)
        && Internal.equals(kickResultContent, o.kickResultContent);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (sourceUserID != null ? sourceUserID.hashCode() : 0);
      result = result * 37 + (kickUserID != null ? kickUserID.hashCode() : 0);
      result = result * 37 + giveYesVoteUserIDs.hashCode();
      result = result * 37 + giveNoVoteUserIDs.hashCode();
      result = result * 37 + giveUnknownVoteUserIDs.hashCode();
      result = result * 37 + (isKickSuccess != null ? isKickSuccess.hashCode() : 0);
      result = result * 37 + (kickFailedReason != null ? kickFailedReason.hashCode() : 0);
      result = result * 37 + (kickResultContent != null ? kickResultContent.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (sourceUserID != null) builder.append(", sourceUserID=").append(sourceUserID);
    if (kickUserID != null) builder.append(", kickUserID=").append(kickUserID);
    if (!giveYesVoteUserIDs.isEmpty()) builder.append(", giveYesVoteUserIDs=").append(giveYesVoteUserIDs);
    if (!giveNoVoteUserIDs.isEmpty()) builder.append(", giveNoVoteUserIDs=").append(giveNoVoteUserIDs);
    if (!giveUnknownVoteUserIDs.isEmpty()) builder.append(", giveUnknownVoteUserIDs=").append(giveUnknownVoteUserIDs);
    if (isKickSuccess != null) builder.append(", isKickSuccess=").append(isKickSuccess);
    if (kickFailedReason != null) builder.append(", kickFailedReason=").append(kickFailedReason);
    if (kickResultContent != null) builder.append(", kickResultContent=").append(kickResultContent);
    return builder.replace(0, 2, "QKickUserResultMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return QKickUserResultMsg.ADAPTER.encode(this);
  }

  public static final QKickUserResultMsg parseFrom(byte[] data) throws IOException {
    QKickUserResultMsg c = null;
       c = QKickUserResultMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 发起者id
   */
  public Integer getSourceUserID() {
    if(sourceUserID==null){
        return DEFAULT_SOURCEUSERID;
    }
    return sourceUserID;
  }

  /**
   * 被踢者id
   */
  public Integer getKickUserID() {
    if(kickUserID==null){
        return DEFAULT_KICKUSERID;
    }
    return kickUserID;
  }

  /**
   * 投同意票用户id
   */
  public List<Integer> getGiveYesVoteUserIDsList() {
    if(giveYesVoteUserIDs==null){
        return new java.util.ArrayList<Integer>();
    }
    return giveYesVoteUserIDs;
  }

  /**
   * 投不同意票用户id
   */
  public List<Integer> getGiveNoVoteUserIDsList() {
    if(giveNoVoteUserIDs==null){
        return new java.util.ArrayList<Integer>();
    }
    return giveNoVoteUserIDs;
  }

  /**
   * 未知票用户id
   */
  public List<Integer> getGiveUnknownVoteUserIDsList() {
    if(giveUnknownVoteUserIDs==null){
        return new java.util.ArrayList<Integer>();
    }
    return giveUnknownVoteUserIDs;
  }

  /**
   * 踢人是否成功
   */
  public Boolean getIsKickSuccess() {
    if(isKickSuccess==null){
        return DEFAULT_ISKICKSUCCESS;
    }
    return isKickSuccess;
  }

  /**
   * 踢人失败原因
   */
  public EQKickFailedReason getKickFailedReason() {
    if(kickFailedReason==null){
        return new EQKickFailedReason.Builder().build();
    }
    return kickFailedReason;
  }

  /**
   * 踢人结果信息内容
   */
  public String getKickResultContent() {
    if(kickResultContent==null){
        return DEFAULT_KICKRESULTCONTENT;
    }
    return kickResultContent;
  }

  /**
   * 发起者id
   */
  public boolean hasSourceUserID() {
    return sourceUserID!=null;
  }

  /**
   * 被踢者id
   */
  public boolean hasKickUserID() {
    return kickUserID!=null;
  }

  /**
   * 投同意票用户id
   */
  public boolean hasGiveYesVoteUserIDsList() {
    return giveYesVoteUserIDs!=null;
  }

  /**
   * 投不同意票用户id
   */
  public boolean hasGiveNoVoteUserIDsList() {
    return giveNoVoteUserIDs!=null;
  }

  /**
   * 未知票用户id
   */
  public boolean hasGiveUnknownVoteUserIDsList() {
    return giveUnknownVoteUserIDs!=null;
  }

  /**
   * 踢人是否成功
   */
  public boolean hasIsKickSuccess() {
    return isKickSuccess!=null;
  }

  /**
   * 踢人失败原因
   */
  public boolean hasKickFailedReason() {
    return kickFailedReason!=null;
  }

  /**
   * 踢人结果信息内容
   */
  public boolean hasKickResultContent() {
    return kickResultContent!=null;
  }

  public static final class Builder extends Message.Builder<QKickUserResultMsg, Builder> {
    private Integer sourceUserID;

    private Integer kickUserID;

    private List<Integer> giveYesVoteUserIDs;

    private List<Integer> giveNoVoteUserIDs;

    private List<Integer> giveUnknownVoteUserIDs;

    private Boolean isKickSuccess;

    private EQKickFailedReason kickFailedReason;

    private String kickResultContent;

    public Builder() {
      giveYesVoteUserIDs = Internal.newMutableList();
      giveNoVoteUserIDs = Internal.newMutableList();
      giveUnknownVoteUserIDs = Internal.newMutableList();
    }

    /**
     * 发起者id
     */
    public Builder setSourceUserID(Integer sourceUserID) {
      this.sourceUserID = sourceUserID;
      return this;
    }

    /**
     * 被踢者id
     */
    public Builder setKickUserID(Integer kickUserID) {
      this.kickUserID = kickUserID;
      return this;
    }

    /**
     * 投同意票用户id
     */
    public Builder addAllGiveYesVoteUserIDs(List<Integer> giveYesVoteUserIDs) {
      Internal.checkElementsNotNull(giveYesVoteUserIDs);
      this.giveYesVoteUserIDs = giveYesVoteUserIDs;
      return this;
    }

    /**
     * 投不同意票用户id
     */
    public Builder addAllGiveNoVoteUserIDs(List<Integer> giveNoVoteUserIDs) {
      Internal.checkElementsNotNull(giveNoVoteUserIDs);
      this.giveNoVoteUserIDs = giveNoVoteUserIDs;
      return this;
    }

    /**
     * 未知票用户id
     */
    public Builder addAllGiveUnknownVoteUserIDs(List<Integer> giveUnknownVoteUserIDs) {
      Internal.checkElementsNotNull(giveUnknownVoteUserIDs);
      this.giveUnknownVoteUserIDs = giveUnknownVoteUserIDs;
      return this;
    }

    /**
     * 踢人是否成功
     */
    public Builder setIsKickSuccess(Boolean isKickSuccess) {
      this.isKickSuccess = isKickSuccess;
      return this;
    }

    /**
     * 踢人失败原因
     */
    public Builder setKickFailedReason(EQKickFailedReason kickFailedReason) {
      this.kickFailedReason = kickFailedReason;
      return this;
    }

    /**
     * 踢人结果信息内容
     */
    public Builder setKickResultContent(String kickResultContent) {
      this.kickResultContent = kickResultContent;
      return this;
    }

    @Override
    public QKickUserResultMsg build() {
      return new QKickUserResultMsg(sourceUserID, kickUserID, giveYesVoteUserIDs, giveNoVoteUserIDs, giveUnknownVoteUserIDs, isKickSuccess, kickFailedReason, kickResultContent, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_QKickUserResultMsg extends ProtoAdapter<QKickUserResultMsg> {
    public ProtoAdapter_QKickUserResultMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, QKickUserResultMsg.class);
    }

    @Override
    public int encodedSize(QKickUserResultMsg value) {
      return ProtoAdapter.UINT32.encodedSizeWithTag(1, value.sourceUserID)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.kickUserID)
          + ProtoAdapter.UINT32.asRepeated().encodedSizeWithTag(3, value.giveYesVoteUserIDs)
          + ProtoAdapter.UINT32.asRepeated().encodedSizeWithTag(4, value.giveNoVoteUserIDs)
          + ProtoAdapter.UINT32.asRepeated().encodedSizeWithTag(5, value.giveUnknownVoteUserIDs)
          + ProtoAdapter.BOOL.encodedSizeWithTag(6, value.isKickSuccess)
          + EQKickFailedReason.ADAPTER.encodedSizeWithTag(7, value.kickFailedReason)
          + ProtoAdapter.STRING.encodedSizeWithTag(8, value.kickResultContent)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, QKickUserResultMsg value) throws IOException {
      ProtoAdapter.UINT32.encodeWithTag(writer, 1, value.sourceUserID);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.kickUserID);
      ProtoAdapter.UINT32.asRepeated().encodeWithTag(writer, 3, value.giveYesVoteUserIDs);
      ProtoAdapter.UINT32.asRepeated().encodeWithTag(writer, 4, value.giveNoVoteUserIDs);
      ProtoAdapter.UINT32.asRepeated().encodeWithTag(writer, 5, value.giveUnknownVoteUserIDs);
      ProtoAdapter.BOOL.encodeWithTag(writer, 6, value.isKickSuccess);
      EQKickFailedReason.ADAPTER.encodeWithTag(writer, 7, value.kickFailedReason);
      ProtoAdapter.STRING.encodeWithTag(writer, 8, value.kickResultContent);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public QKickUserResultMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setSourceUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 2: builder.setKickUserID(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.giveYesVoteUserIDs.add(ProtoAdapter.UINT32.decode(reader)); break;
          case 4: builder.giveNoVoteUserIDs.add(ProtoAdapter.UINT32.decode(reader)); break;
          case 5: builder.giveUnknownVoteUserIDs.add(ProtoAdapter.UINT32.decode(reader)); break;
          case 6: builder.setIsKickSuccess(ProtoAdapter.BOOL.decode(reader)); break;
          case 7: {
            try {
              builder.setKickFailedReason(EQKickFailedReason.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 8: builder.setKickResultContent(ProtoAdapter.STRING.decode(reader)); break;
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
    public QKickUserResultMsg redact(QKickUserResultMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}