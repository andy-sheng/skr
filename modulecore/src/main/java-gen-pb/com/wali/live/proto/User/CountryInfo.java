// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: User.proto
package com.wali.live.proto.User;

import com.squareup.wire.FieldEncoding;
import com.squareup.wire.Message;
import com.squareup.wire.ProtoAdapter;
import com.squareup.wire.ProtoReader;
import com.squareup.wire.ProtoWriter;
import com.squareup.wire.WireField;
import java.io.IOException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import okio.ByteString;

public final class CountryInfo extends Message<CountryInfo, CountryInfo.Builder> {
  public static final ProtoAdapter<CountryInfo> ADAPTER = new ProtoAdapter_CountryInfo();

  private static final long serialVersionUID = 0L;

  public static final String DEFAULT_COUNTRY = "";

  public static final String DEFAULT_COUNTRY_CODE = "";

  /**
   * 具体国家，比如中国、美国、中国台湾等
   */
  @WireField(
      tag = 1,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String country;

  /**
   * 国家编码，ISO A2标准，中国为CN   台湾为TW
   */
  @WireField(
      tag = 2,
      adapter = "com.squareup.wire.ProtoAdapter#STRING",
      label = WireField.Label.REQUIRED
  )
  public final String country_code;

  public CountryInfo(String country, String country_code) {
    this(country, country_code, ByteString.EMPTY);
  }

  public CountryInfo(String country, String country_code, ByteString unknownFields) {
    super(ADAPTER, unknownFields);
    this.country = country;
    this.country_code = country_code;
  }

  @Override
  public Builder newBuilder() {
    Builder builder = new Builder();
    builder.country = country;
    builder.country_code = country_code;
    builder.addUnknownFields(unknownFields());
    return builder;
  }

  @Override
  public boolean equals(Object other) {
    if (other == this) return true;
    if (!(other instanceof CountryInfo)) return false;
    CountryInfo o = (CountryInfo) other;
    return unknownFields().equals(o.unknownFields())
        && country.equals(o.country)
        && country_code.equals(o.country_code);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode;
    if (result == 0) {
      result = unknownFields().hashCode();
      result = result * 37 + country.hashCode();
      result = result * 37 + country_code.hashCode();
      super.hashCode = result;
    }
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(", country=").append(country);
    builder.append(", country_code=").append(country_code);
    return builder.replace(0, 2, "CountryInfo{").append('}').toString();
  }

  public byte[] toByteArray() {
    return CountryInfo.ADAPTER.encode(this);
  }

  public static final CountryInfo parseFrom(byte[] data) throws IOException {
    CountryInfo c = null;
       c = CountryInfo.ADAPTER.decode(data);
    return c;
  }

  /**
   * 具体国家，比如中国、美国、中国台湾等
   */
  public String getCountry() {
    if(country==null){
        return DEFAULT_COUNTRY;
    }
    return country;
  }

  /**
   * 国家编码，ISO A2标准，中国为CN   台湾为TW
   */
  public String getCountryCode() {
    if(country_code==null){
        return DEFAULT_COUNTRY_CODE;
    }
    return country_code;
  }

  /**
   * 具体国家，比如中国、美国、中国台湾等
   */
  public boolean hasCountry() {
    return country!=null;
  }

  /**
   * 国家编码，ISO A2标准，中国为CN   台湾为TW
   */
  public boolean hasCountryCode() {
    return country_code!=null;
  }

  public static final class Builder extends Message.Builder<CountryInfo, Builder> {
    public String country;

    public String country_code;

    public Builder() {
    }

    /**
     * 具体国家，比如中国、美国、中国台湾等
     */
    public Builder setCountry(String country) {
      this.country = country;
      return this;
    }

    /**
     * 国家编码，ISO A2标准，中国为CN   台湾为TW
     */
    public Builder setCountryCode(String country_code) {
      this.country_code = country_code;
      return this;
    }

    @Override
    public CountryInfo build() {
      return new CountryInfo(country, country_code, super.buildUnknownFields());
    }
  }

  private static final class ProtoAdapter_CountryInfo extends ProtoAdapter<CountryInfo> {
    public ProtoAdapter_CountryInfo() {
      super(FieldEncoding.LENGTH_DELIMITED, CountryInfo.class);
    }

    @Override
    public int encodedSize(CountryInfo value) {
      return ProtoAdapter.STRING.encodedSizeWithTag(1, value.country)
          + ProtoAdapter.STRING.encodedSizeWithTag(2, value.country_code)
          + value.unknownFields().size();
    }

    @Override
    public void encode(ProtoWriter writer, CountryInfo value) throws IOException {
      ProtoAdapter.STRING.encodeWithTag(writer, 1, value.country);
      ProtoAdapter.STRING.encodeWithTag(writer, 2, value.country_code);
      writer.writeBytes(value.unknownFields());
    }

    @Override
    public CountryInfo decode(ProtoReader reader) throws IOException {
      Builder builder = new Builder();
      long token = reader.beginMessage();
      for (int tag; (tag = reader.nextTag()) != -1;) {
        switch (tag) {
          case 1: builder.setCountry(ProtoAdapter.STRING.decode(reader)); break;
          case 2: builder.setCountryCode(ProtoAdapter.STRING.decode(reader)); break;
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
    public CountryInfo redact(CountryInfo value) {
      Builder builder = value.newBuilder();
      builder.clearUnknownFields();
      return builder.build();
    }
  }
}
