// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Private.proto
package com.zq.live.proto.Private;

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

/**
 * 文字消息
 */
public final class TextMsg extends Message<TextMsg, TextMsg.Builder> {
  public static final ProtoAdapter<TextMsg> ADAPTER = new ProtoAdapter_TextMsg();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_TEXT = "";

  /**
   * 文字内容
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  private final String text;

  public TextMsg(String text) {
    this(text, ByteString.EMPTY);
  }

  public TextMsg(String text, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.text = text;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.text = text;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof TextMsg)) return false;
    TextMsg o = (TextMsg) other;
    return unknownFields().equals(o.unknownFields())
        && Internal.equals(text, o.text);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + (text != null ? text.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (text != null) builder.append(", text=").append(text);
    return builder.replace(0, 2, "TextMsg{").append('}').toString();
  }

  public byte[] toByteArray() {
    return TextMsg.ADAPTER.encode(this);
  }

  public static final TextMsg parseFrom(byte[] data) throws IOException {
    TextMsg c = null;
       c = TextMsg.ADAPTER.decode(data);
    return c;
  }

  /**
   * 文字内容
   */
  public String getText() {
    if(text==null){
        return DEFAULT_TEXT;
    }
    return text;
  }

  /**
   * 文字内容
   */
  public boolean hasText() {
    return text!=null;
  }

  public static final class Builder extends Message.Builder<TextMsg, Builder> {
    private String text;

    public Builder() {
    }

    /**
     * 文字内容
     */
    public Builder setText(String text) {
      this.text = text;
      return this;
    }

    @Override
    public TextMsg build() {
      return new TextMsg(text, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_TextMsg extends ProtoAdapter<TextMsg> {
    public ProtoAdapter_TextMsg() {
      super(FieldEncoding.LENGTH_DELIMITED, TextMsg.class);
    }

    @Override
    public int encodedSize(TextMsg value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.text)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, TextMsg value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.text);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public TextMsg decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setText(ProtoAdapter.STRING.decode(reader)); break;
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
    public TextMsg redact(TextMsg value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
