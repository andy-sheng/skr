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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class PGameItemInfo extends Message<PGameItemInfo, PGameItemInfo.Builder> {
  public static final ProtoAdapter<PGameItemInfo> ADAPTER = new ProtoAdapter_PGameItemInfo();

  private static final long serialVersionUID = 0L;

  public static final EPGameType DEFAULT_GAMETYPE = EPGameType.PGT_Unknown;

  /**
   * 游戏规则
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.PartyRoom.PGameRule#ADAPTER"
  )
  private final PGameRule gameRule;

  /**
   * 游戏类型
   */
  @WireField(
      tag = 2,
      adapter = "com.zq.live.proto.PartyRoom.EPGameType#ADAPTER"
  )
  private final EPGameType gameType;

  /**
   * 剧本类游戏数据
   */
  @WireField(
      tag = 3,
      adapter = "com.zq.live.proto.PartyRoom.PGamePlay#ADAPTER"
  )
  private final PGamePlay play;

  /**
   * 问答类游戏数据
   */
  @WireField(
      tag = 4,
      adapter = "com.zq.live.proto.PartyRoom.PGameQuestion#ADAPTER"
  )
  private final PGameQuestion question;

  public PGameItemInfo(PGameRule gameRule, EPGameType gameType, PGamePlay play,
      PGameQuestion question) {
    this(gameRule, gameType, play, question, ByteString.EMPTY);
  }

  public PGameItemInfo(PGameRule gameRule, EPGameType gameType, PGamePlay play,
      PGameQuestion question, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.gameRule = gameRule;
    this.gameType = gameType;
    this.play = play;
    this.question = question;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.gameRule = gameRule;
    builder.gameType = gameType;
    builder.play = play;
    builder.question = question;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof PGameItemInfo)) return false;
    PGameItemInfo o = (PGameItemInfo) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(gameRule, o.gameRule)
        && Internal.equals(gameType, o.gameType)
        && Internal.equals(play, o.play)
        && Internal.equals(question, o.question);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (gameRule != null ? gameRule.hashCode() : 0);
      result = result * 37 + (gameType != null ? gameType.hashCode() : 0);
      result = result * 37 + (play != null ? play.hashCode() : 0);
      result = result * 37 + (question != null ? question.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (gameRule != null) builder.append(", gameRule=").append(gameRule);
    if (gameType != null) builder.append(", gameType=").append(gameType);
    if (play != null) builder.append(", play=").append(play);
    if (question != null) builder.append(", question=").append(question);
    return builder.replace(0, 2, "PGameItemInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return PGameItemInfo.ADAPTER.encode(this);
  }

  public static final PGameItemInfo parseFrom(byte[] data) throws IOException {
    PGameItemInfo c = null;
       c = PGameItemInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 游戏规则
   */
  public PGameRule getGameRule() {
    if(gameRule==null){
        return new PGameRule.Builder().build();
    }
    return gameRule;
  }

  /**
   * 游戏类型
   */
  public EPGameType getGameType() {
    if(gameType==null){
        return new EPGameType.Builder().build();
    }
    return gameType;
  }

  /**
   * 剧本类游戏数据
   */
  public PGamePlay getPlay() {
    if(play==null){
        return new PGamePlay.Builder().build();
    }
    return play;
  }

  /**
   * 问答类游戏数据
   */
  public PGameQuestion getQuestion() {
    if(question==null){
        return new PGameQuestion.Builder().build();
    }
    return question;
  }

  /**
   * 游戏规则
   */
  public boolean hasGameRule() {
    return gameRule!=null;
  }

  /**
   * 游戏类型
   */
  public boolean hasGameType() {
    return gameType!=null;
  }

  /**
   * 剧本类游戏数据
   */
  public boolean hasPlay() {
    return play!=null;
  }

  /**
   * 问答类游戏数据
   */
  public boolean hasQuestion() {
    return question!=null;
  }

  public static final class Builder extends Message.Builder<PGameItemInfo, Builder> {
    private PGameRule gameRule;

    private EPGameType gameType;

    private PGamePlay play;

    private PGameQuestion question;

    public Builder() {
    }

    /**
     * 游戏规则
     */
    public Builder setGameRule(PGameRule gameRule) {
      this.gameRule = gameRule;
      return this;
    }

    /**
     * 游戏类型
     */
    public Builder setGameType(EPGameType gameType) {
      this.gameType = gameType;
      return this;
    }

    /**
     * 剧本类游戏数据
     */
    public Builder setPlay(PGamePlay play) {
      this.play = play;
      return this;
    }

    /**
     * 问答类游戏数据
     */
    public Builder setQuestion(PGameQuestion question) {
      this.question = question;
      return this;
    }

    @Override
    public PGameItemInfo build() {
      return new PGameItemInfo(gameRule, gameType, play, question, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_PGameItemInfo extends ProtoAdapter<PGameItemInfo> {
    public ProtoAdapter_PGameItemInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, PGameItemInfo.class);
    }

    @Override
    public int encodedSize(PGameItemInfo value) {
      return PGameRule.ADAPTER.encodedSizeWithTag(1, value.gameRule)
          + EPGameType.ADAPTER.encodedSizeWithTag(2, value.gameType)
          + PGamePlay.ADAPTER.encodedSizeWithTag(3, value.play)
          + PGameQuestion.ADAPTER.encodedSizeWithTag(4, value.question)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, PGameItemInfo value) throws IOException {
      PGameRule.ADAPTER.encodeWithTag(writer, 1, value.gameRule);
      EPGameType.ADAPTER.encodeWithTag(writer, 2, value.gameType);
      PGamePlay.ADAPTER.encodeWithTag(writer, 3, value.play);
      PGameQuestion.ADAPTER.encodeWithTag(writer, 4, value.question);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public PGameItemInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setGameRule(PGameRule.ADAPTER.decode(reader)); break;
          case 2: {
            try {
              builder.setGameType(EPGameType.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 3: builder.setPlay(PGamePlay.ADAPTER.decode(reader)); break;
          case 4: builder.setQuestion(PGameQuestion.ADAPTER.decode(reader)); break;
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
    public PGameItemInfo redact(PGameItemInfo value) {
      Builder builder = value.newBuilder();
      if (builder.gameRule != null) builder.gameRule = PGameRule.ADAPTER.redact(builder.gameRule);
      if (builder.play != null) builder.play = PGamePlay.ADAPTER.redact(builder.play);
      if (builder.question != null) builder.question = PGameQuestion.ADAPTER.redact(builder.question);
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}