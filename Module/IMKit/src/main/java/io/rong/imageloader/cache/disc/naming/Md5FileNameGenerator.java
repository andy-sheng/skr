//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imageloader.cache.disc.naming;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.rong.imageloader.cache.disc.naming.FileNameGenerator;
import io.rong.imageloader.utils.L;

public class Md5FileNameGenerator implements FileNameGenerator {
  private static final String HASH_ALGORITHM = "MD5";
  private static final int RADIX = 36;

  public Md5FileNameGenerator() {
  }

  public String generate(String imageUri) {
    byte[] md5 = this.getMD5(imageUri.getBytes());
    BigInteger bi = (new BigInteger(md5)).abs();
    return bi.toString(36);
  }

  private byte[] getMD5(byte[] data) {
    byte[] hash = null;

    try {
      MessageDigest digest = MessageDigest.getInstance("MD5");
      digest.update(data);
      hash = digest.digest();
    } catch (NoSuchAlgorithmException var4) {
      L.e(var4);
    }

    return hash;
  }
}
