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
import okio.ByteString;

/**
 * 分值变动条目
 */
public final class ScoreItem extends Message<ScoreItem, ScoreItem.Builder> {
  public static final ProtoAdapter<ScoreItem> ADAPTER = new ProtoAdapter_ScoreItem();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_WHY = "";

  public static final Integer DEFAULT_SCORE = 0;

  public static final Integer DEFAULT_INDEX = 0;

  /**
   * 分值变动原因
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String why;

  /**
   * 分值变动
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  private final Integer score;

  /**
   * 原因标识
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#INT32"
  )
  private final Integer index;

  public ScoreItem(String why, Integer score, Integer index) {
    this(why, score, index, ByteString.EMPTY);
  }

  public ScoreItem(String why, Integer score, Integer index, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.why = why;
    this.score = score;
    this.index = index;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.why = why;
    builder.score = score;
    builder.index = index;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof ScoreItem)) return false;
    ScoreItem o = (ScoreItem) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(why, o.why)
        && Internal.equals(score, o.score)
        && Internal.equals(index, o.index);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (why != null ? why.hashCode() : 0);
      result = result * 37 + (score != null ? score.hashCode() : 0);
      result = result * 37 + (index != null ? index.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (why != null) builder.append(", why=").append(why);
    if (score != null) builder.append(", score=").append(score);
    if (index != null) builder.append(", index=").append(index);
    return builder.replace(0, 2, "ScoreItem{").append('}').toString();
  }

  public byte[] toByteArray() {
    return ScoreItem.ADAPTER.encode(this);
  }

  public static final ScoreItem parseFrom(byte[] data) throws IOException {
    ScoreItem c = null;
       c = ScoreItem.ADAPTER.decode(data);
    return c;
  }

  /**
   * 分值变动原因
   */
  public String getWhy() {
    if(why==null){
        return DEFAULT_WHY;
    }
    return why;
  }

  /**
   * 分值变动
   */
  public Integer getScore() {
    if(score==null){
        return DEFAULT_SCORE;
    }
    return score;
  }

  /**
   * 原因标识
   */
  public Integer getIndex() {
    if(index==null){
        return DEFAULT_INDEX;
    }
    return index;
  }

  /**
   * 分值变动原因
   */
  public boolean hasWhy() {
    return why!=null;
  }

  /**
   * 分值变动
   */
  public boolean hasScore() {
    return score!=null;
  }

  /**
   * 原因标识
   */
  public boolean hasIndex() {
    return index!=null;
  }

  public static final class Builder extends Message.Builder<ScoreItem, Builder> {
    private String why;

    private Integer score;

    private Integer index;

    public Builder() {
    }

    /**
     * 分值变动原因
     */
    public Builder setWhy(String why) {
      this.why = why;
      return this;
    }

    /**
     * 分值变动
     */
    public Builder setScore(Integer score) {
      this.score = score;
      return this;
    }

    /**
     * 原因标识
     */
    public Builder setIndex(Integer index) {
      this.index = index;
      return this;
    }

    @Override
    public ScoreItem build() {
      return new ScoreItem(why, score, index, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_ScoreItem extends ProtoAdapter<ScoreItem> {
    public ProtoAdapter_ScoreItem() {
      super(FieldEncoding.LENGTH_DELIMITED, ScoreItem.class);
    }

    @Override
    public int encodedSize(ScoreItem value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.why)
          + ProtoAdapter.INT32.encodedSizeWithTag(2, value.score)
          + ProtoAdapter.INT32.encodedSizeWithTag(3, value.index)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, ScoreItem value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.why);
      ProtoAdapter.INT32.encodeWithTag(writer, 2, value.score);
      ProtoAdapter.INT32.encodeWithTag(writer, 3, value.index);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public ScoreItem decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setWhy(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.setScore(ProtoAdapter.INT32.decode(reader)); break;
          case 3: builder.setIndex(ProtoAdapter.INT32.decode(reader)); break;
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
    public ScoreItem redact(ScoreItem value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
