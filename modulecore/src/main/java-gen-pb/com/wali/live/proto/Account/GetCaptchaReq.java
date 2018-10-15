// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: Account.proto
package com.wali.live.proto.Account;

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
 * 获取验证码
 * cmd:zhibo.account.getcaptcha
 */
public final class GetCaptchaReq extends Message<GetCaptchaReq, GetCaptchaReq.Builder> {
  public static final ProtoAdapter<GetCaptchaReq> ADAPTER = new ProtoAdapter_GetCaptchaReq();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_PHONENUM = "";

  public static final Integer DEFAULT_TYPE = 0;

  public static final String DEFAULT_LANG = "";

  /**
   * 手机号
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String phoneNum;

  /**
   * 1: 注册验证码  2:找回密码验证码  3：更改密码验证码
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#UINT32",
      label = WireField.Label.REQUIRED
  )
  public final Integer type;

  /**
   * 下发短信的语言类型，lang的值可以为 en, zh-cn, zh-hant 之一，分别对应英文，中文简体，中文繁体。此参数为空，返回文案为简体中文格式
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String lang;

  public GetCaptchaReq(String phoneNum, Integer type, String lang) {
    this(phoneNum, type, lang, ByteString.EMPTY);
  }

  public GetCaptchaReq(String phoneNum, Integer type, String lang, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.phoneNum = phoneNum;
    this.type = type;
    this.lang = lang;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.phoneNum = phoneNum;
    builder.type = type;
    builder.lang = lang;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof GetCaptchaReq)) return false;
    GetCaptchaReq o = (GetCaptchaReq) other;
    return unknownFields().equals(o.unknownFields())
        && phoneNum.equals(o.phoneNum)
        && type.equals(o.type)
        && Internal.equals(lang, o.lang);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + phoneNum.hashCode();
      result = result * 37 + type.hashCode();
      result = result * 37 + (lang != null ? lang.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", phoneNum=").append(phoneNum);
    builder.append(", type=").append(type);
    if (lang != null) builder.append(", lang=").append(lang);
    return builder.replace(0, 2, "GetCaptchaReq{").append('}').toString();
  }

  public byte[] toByteArray() {
    return GetCaptchaReq.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<GetCaptchaReq, Builder> {
    public String phoneNum;

    public Integer type;

    public String lang;

    public Builder() {
    }

    /**
     * 手机号
     */
    public Builder setPhoneNum(String phoneNum) {
      this.phoneNum = phoneNum;
      return this;
    }

    /**
     * 1: 注册验证码  2:找回密码验证码  3：更改密码验证码
     */
    public Builder setType(Integer type) {
      this.type = type;
      return this;
    }

    /**
     * 下发短信的语言类型，lang的值可以为 en, zh-cn, zh-hant 之一，分别对应英文，中文简体，中文繁体。此参数为空，返回文案为简体中文格式
     */
    public Builder setLang(String lang) {
      this.lang = lang;
      return this;
    }

    @Override
    public GetCaptchaReq build() {
      if (phoneNum == null
          || type == null) {
        throw Internal.missingRequiredFields(phoneNum, "phoneNum",
            type, "type");
      }
      return new GetCaptchaReq(phoneNum, type, lang, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_GetCaptchaReq extends ProtoAdapter<GetCaptchaReq> {
    public ProtoAdapter_GetCaptchaReq() {
      super(FieldEncoding.LENGTH_DELIMITED, GetCaptchaReq.class);
    }

    @Override
    public int encodedSize(GetCaptchaReq value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.phoneNum)
          + ProtoAdapter.UINT32.encodedSizeWithTag(2, value.type)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.lang)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, GetCaptchaReq value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.phoneNum);
      ProtoAdapter.UINT32.encodeWithTag(writer, 2, value.type);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.lang);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public GetCaptchaReq decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setPhoneNum(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.setType(ProtoAdapter.UINT32.decode(reader)); break;
          case 3: builder.setLang(ProtoAdapter.STRING.decode(reader)); break;
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
    public GetCaptchaReq redact(GetCaptchaReq value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
