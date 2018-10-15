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
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

/**
 * 手机号注册
 * cmd:zhibo.account.registerbyphone 注册
 */
public final class RegisterByPhoneReq extends Message<RegisterByPhoneReq, RegisterByPhoneReq.Builder> {
  public static final ProtoAdapter<RegisterByPhoneReq> ADAPTER = new ProtoAdapter_RegisterByPhoneReq();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_PHONENUM = "";

  public static final String DEFAULT_PWD = "";

  public static final String DEFAULT_CAPTCHA = "";

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
   * 密码
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String pwd;

  /**
   * 短信验证码
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String captcha;

  public RegisterByPhoneReq(String phoneNum, String pwd, String captcha) {
    this(phoneNum, pwd, captcha, ByteString.EMPTY);
  }

  public RegisterByPhoneReq(String phoneNum, String pwd, String captcha, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.phoneNum = phoneNum;
    this.pwd = pwd;
    this.captcha = captcha;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.phoneNum = phoneNum;
    builder.pwd = pwd;
    builder.captcha = captcha;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof RegisterByPhoneReq)) return false;
    RegisterByPhoneReq o = (RegisterByPhoneReq) other;
    return unknownFields().equals(o.unknownFields())
        && phoneNum.equals(o.phoneNum)
        && pwd.equals(o.pwd)
        && captcha.equals(o.captcha);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + phoneNum.hashCode();
      result = result * 37 + pwd.hashCode();
      result = result * 37 + captcha.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", phoneNum=").append(phoneNum);
    builder.append(", pwd=").append(pwd);
    builder.append(", captcha=").append(captcha);
    return builder.replace(0, 2, "RegisterByPhoneReq{").append('}').toString();
  }

  public byte[] toByteArray() {
    return RegisterByPhoneReq.ADAPTER.encode(this);
  }

  public static final class Builder extends Message.Builder<RegisterByPhoneReq, Builder> {
    public String phoneNum;

    public String pwd;

    public String captcha;

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
     * 密码
     */
    public Builder setPwd(String pwd) {
      this.pwd = pwd;
      return this;
    }

    /**
     * 短信验证码
     */
    public Builder setCaptcha(String captcha) {
      this.captcha = captcha;
      return this;
    }

    @Override
    public RegisterByPhoneReq build() {
      if (phoneNum == null
          || pwd == null
          || captcha == null) {
        throw Internal.missingRequiredFields(phoneNum, "phoneNum",
            pwd, "pwd",
            captcha, "captcha");
      }
      return new RegisterByPhoneReq(phoneNum, pwd, captcha, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_RegisterByPhoneReq extends ProtoAdapter<RegisterByPhoneReq> {
    public ProtoAdapter_RegisterByPhoneReq() {
      super(FieldEncoding.LENGTH_DELIMITED, RegisterByPhoneReq.class);
    }

    @Override
    public int encodedSize(RegisterByPhoneReq value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.phoneNum)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.pwd)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.captcha)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, RegisterByPhoneReq value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.phoneNum);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.pwd);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.captcha);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public RegisterByPhoneReq decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setPhoneNum(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.setPwd(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.setCaptcha(ProtoAdapter.STRING.decode(reader)); break;
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
    public RegisterByPhoneReq redact(RegisterByPhoneReq value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
