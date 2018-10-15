// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: User.proto
package com.wali.live.proto.User;

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
import java.util.List;
import okio.ByteString;

/**
 * java -jar -Dfile.encoding=UTF-8 ./proto/wire-compiler-2.3.0-SNAPSHOT-jar-with-dependencies.jar \
 * --proto_path=./proto --java_out=./modulecore/src/main/java-gen-pb/ User.proto
 * 商铺类型用户的扩展信息
 */
public final class BusinessUserInfo extends Message<BusinessUserInfo, BusinessUserInfo.Builder> {
  public static final ProtoAdapter<BusinessUserInfo> ADAPTER = new ProtoAdapter_BusinessUserInfo();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_ADDRESS = "";

  public static final String DEFAULT_BUSINESS_HOURS = "";

  public static final String DEFAULT_INTRO = "";

  /**
   * 电话0
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REPEATED
  )
  public final List<String> service_phone;

  /**
   * 位置
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String address;

  /**
   * 营业时间
   */
  @WireField(
      tag = 3,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String business_hours;

  /**
   * 简介
   */
  @WireField(
      tag = 4,
      adapter = "com.squareup.wire.ProtoAdapter#STRING"
  )
  public final String intro;

  public BusinessUserInfo(List<String> service_phone, String address, String business_hours,
      String intro) {
    this(service_phone, address, business_hours, intro, ByteString.EMPTY);
  }

  public BusinessUserInfo(List<String> service_phone, String address, String business_hours,
      String intro, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.service_phone = Internal.immutableCopyOf("service_phone", service_phone);
    this.address = address;
    this.business_hours = business_hours;
    this.intro = intro;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.service_phone = Internal.copyOf("service_phone", service_phone);
    builder.address = address;
    builder.business_hours = business_hours;
    builder.intro = intro;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof BusinessUserInfo)) return false;
    BusinessUserInfo o = (BusinessUserInfo) other;
    return unknownFields().equals(o.unknownFields())
        && service_phone.equals(o.service_phone)
        && Internal.equals(address, o.address)
        && Internal.equals(business_hours, o.business_hours)
        && Internal.equals(intro, o.intro);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + service_phone.hashCode();
      result = result * 37 + (address != null ? address.hashCode() : 0);
      result = result * 37 + (business_hours != null ? business_hours.hashCode() : 0);
      result = result * 37 + (intro != null ? intro.hashCode() : 0);
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    if (!service_phone.isEmpty()) builder.append(", service_phone=").append(service_phone);
    if (address != null) builder.append(", address=").append(address);
    if (business_hours != null) builder.append(", business_hours=").append(business_hours);
    if (intro != null) builder.append(", intro=").append(intro);
    return builder.replace(0, 2, "BusinessUserInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return BusinessUserInfo.ADAPTER.encode(this);
  }

  public static final BusinessUserInfo parseFrom(byte[] data) throws IOException {
    BusinessUserInfo c = null;
       c = BusinessUserInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 电话0
   */
  public List<String> getServicePhoneList() {
    if(service_phone==null){
        return new java.util.ArrayList<String>();
    }
    return service_phone;
  }

  /**
   * 位置
   */
  public String getAddress() {
    if(address==null){
        return DEFAULT_ADDRESS;
    }
    return address;
  }

  /**
   * 营业时间
   */
  public String getBusinessHours() {
    if(business_hours==null){
        return DEFAULT_BUSINESS_HOURS;
    }
    return business_hours;
  }

  /**
   * 简介
   */
  public String getIntro() {
    if(intro==null){
        return DEFAULT_INTRO;
    }
    return intro;
  }

  /**
   * 电话0
   */
  public boolean hasServicePhoneList() {
    return service_phone!=null;
  }

  /**
   * 位置
   */
  public boolean hasAddress() {
    return address!=null;
  }

  /**
   * 营业时间
   */
  public boolean hasBusinessHours() {
    return business_hours!=null;
  }

  /**
   * 简介
   */
  public boolean hasIntro() {
    return intro!=null;
  }

  public static final class Builder extends Message.Builder<BusinessUserInfo, Builder> {
    public List<String> service_phone;

    public String address;

    public String business_hours;

    public String intro;

    public Builder() {
      service_phone = Internal.newMutableList();
    }

    /**
     * 电话0
     */
    public Builder addAllServicePhone(List<String> service_phone) {
      Internal.checkElementsNotNull(service_phone);
      this.service_phone = service_phone;
      return this;
    }

    /**
     * 位置
     */
    public Builder setAddress(String address) {
      this.address = address;
      return this;
    }

    /**
     * 营业时间
     */
    public Builder setBusinessHours(String business_hours) {
      this.business_hours = business_hours;
      return this;
    }

    /**
     * 简介
     */
    public Builder setIntro(String intro) {
      this.intro = intro;
      return this;
    }

    @Override
    public BusinessUserInfo build() {
      return new BusinessUserInfo(service_phone, address, business_hours, intro, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_BusinessUserInfo extends ProtoAdapter<BusinessUserInfo> {
    public ProtoAdapter_BusinessUserInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, BusinessUserInfo.class);
    }

    @Override
    public int encodedSize(BusinessUserInfo value) {
      return ProtoAdapter.STRING.asRepeated().encodedSizeWithTag(1, value.service_phone)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.address)
          + ProtoAdapter.STRING.encodedSizeWithTag(3, value.business_hours)
          + ProtoAdapter.STRING.encodedSizeWithTag(4, value.intro)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, BusinessUserInfo value) throws IOException {
      ProtoAdapter.STRING.asRepeated().encodeWithTag(writer, 1, value.service_phone);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.address);
      ProtoAdapter.STRING.encodeWithTag(writer, 3, value.business_hours);
      ProtoAdapter.STRING.encodeWithTag(writer, 4, value.intro);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public BusinessUserInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.service_phone.add(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.setAddress(ProtoAdapter.STRING.decode(reader)); break;
          case 3: builder.setBusinessHours(ProtoAdapter.STRING.decode(reader)); break;
          case 4: builder.setIntro(ProtoAdapter.STRING.decode(reader)); break;
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
    public BusinessUserInfo redact(BusinessUserInfo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
