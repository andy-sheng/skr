// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: CombineRoom.proto
package com.zq.live.proto.CombineRoom;

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

public final class SceneGameSyncStatusMsg extends Message<SceneGameSyncStatusMsg, SceneGameSyncStatusMsg.Builder> {
  public static final ProtoAdapter<SceneGameSyncStatusMsg> ADAPTER = new ProtoAdapter_SceneGameSyncStatusMsg();

  private static final long serialVersionUID = 0L;

  public static final EGameStage DEFAULT_GAMESTAGE = EGameStage.GS_Unknown;

  public static final Integer DEFAULT_PANELSEQ = 0;

  public static final Integer DEFAULT_ITEMID = 0;

  /**
   * 游戏阶段
   */
  @WireField(
      tag = 1,
      adapter = "com.zq.live.proto.CombineRoom.EGameStage#ADAPTER"
  )
  private final EGameStage gameStage;

  /**
   * 如果是选择游戏阶段，则panelSeq为当前面板信息
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer panelSeq;

  /**
   * 如果是游戏进行阶段，则itemID为正在进行的游戏id
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32"
  )
  private final Integer itemID;

  public SceneGameSyncStatusMsg(EGameStage gameStage, Integer panelSeq, Integer itemID) {
    this(gameStage, panelSeq, itemID, ByteString.EMPTY);
  }

  public SceneGameSyncStatusMsg(EGameStage gameStage, Integer panelSeq, Integer itemID,
      ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.gameStage = gameStage;
    this.panelSeq = panelSeq;
    this.itemID = itemID;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.gameStage = gameStage;
    builder.panelSeq = panelSeq;
    builder.itemID = itemID;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof SceneGameSyncStatusMsg)) return false;
    SceneGameSyncStatusMsg o = (SceneGameSyncStatusMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(gameStage, o.gameStage)
        && Internal.equals(panelSeq, o.panelSeq)
        && Internal.equals(itemID, o.itemID);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (gameStage != null ? gameStage.hashCode() : 0);
      result = result * 37 + (panelSeq != null ? panelSeq.hashCode() : 0);
      result = result * 37 + (itemID != null ? itemID.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (gameStage != null) builder.append(", gameStage=").append(gameStage);
    if (panelSeq != null) builder.append(", panelSeq=").append(panelSeq);
    if (itemID != null) builder.append(", itemID=").append(itemID);
    return builder.replace(0, 2, "SceneGameSyncStatusMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return SceneGameSyncStatusMsg.ADAPTER.encode(this);
  }

  public static final SceneGameSyncStatusMsg parseFrom(byte[] data) throws IOException {
    SceneGameSyncStatusMsg c = null;
       c = SceneGameSyncStatusMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 游戏阶段
   */
  public EGameStage getGameStage() {
    if(gameStage==null){
        return new EGameStage.Builder().build();
    }
    return gameStage;
  }

  /**
   * 如果是选择游戏阶段，则panelSeq为当前面板信息
   */
  public Integer getPanelSeq() {
    if(panelSeq==null){
        return DEFAULT_PANELSEQ;
    }
    return panelSeq;
  }

  /**
   * 如果是游戏进行阶段，则itemID为正在进行的游戏id
   */
  public Integer getItemID() {
    if(itemID==null){
        return DEFAULT_ITEMID;
    }
    return itemID;
  }

  /**
   * 游戏阶段
   */
  public boolean hasGameStage() {
    return gameStage!=null;
  }

  /**
   * 如果是选择游戏阶段，则panelSeq为当前面板信息
   */
  public boolean hasPanelSeq() {
    return panelSeq!=null;
  }

  /**
   * 如果是游戏进行阶段，则itemID为正在进行的游戏id
   */
  public boolean hasItemID() {
    return itemID!=null;
  }

  public static final class Builder extends Message.Builder<SceneGameSyncStatusMsg, Builder> {
    private EGameStage gameStage;

    private Integer panelSeq;

    private Integer itemID;

    public Builder() {
    }

    /**
     * 游戏阶段
     */
    public Builder setGameStage(EGameStage gameStage) {
      this.gameStage = gameStage;
      return this;
    }

    /**
     * 如果是选择游戏阶段，则panelSeq为当前面板信息
     */
    public Builder setPanelSeq(Integer panelSeq) {
      this.panelSeq = panelSeq;
      return this;
    }

    /**
     * 如果是游戏进行阶段，则itemID为正在进行的游戏id
     */
    public Builder setItemID(Integer itemID) {
      this.itemID = itemID;
      return this;
    }

    @Override
    public SceneGameSyncStatusMsg build() {
      return new SceneGameSyncStatusMsg(gameStage, panelSeq, itemID, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_SceneGameSyncStatusMsg extends ProtoAdapter<SceneGameSyncStatusMsg> {
    public ProtoAdapter_SceneGameSyncStatusMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, SceneGameSyncStatusMsg.class);
    }

    @Override
    public int encodedSize(SceneGameSyncStatusMsg value) {
      return EGameStage.ADAPTER.encodedSizeWithTag(1, value.gameStage)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.panelSeq)
          + ProtoAdapter.UINT32.encodedSizeWithTag(3, value.itemID)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, SceneGameSyncStatusMsg value) throws IOException {
      EGameStage.ADAPTER.encodeWithTag(writer, 1, value.gameStage);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.panelSeq);
      ProtoAdapter.UINT32.encodeWithTag(writer, 3, value.itemID);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public SceneGameSyncStatusMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: {
            try {
              builder.setGameStage(EGameStage.ADAPTER.decode(reader));
            } catch (ProtoAdapter.EnumConstantNotFoundException e) {
              builder.addUnknownField(tag, FieldEncoding.VARINT, (long) e.value);
            }
            break;
          }
          case 2: builder.setPanelSeq(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setItemID(ProtoAdapter.UINT32.decode(reader)); break;
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
    public SceneGameSyncStatusMsg redact(SceneGameSyncStatusMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
