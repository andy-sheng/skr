
package com.common.utils;

import android.text.TextUtils;

import com.alibaba.fastjson.JSON;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @module common.string
 * <p/>
 * Created by MK on 15/8/12.
 */
public class StringUtils {

    StringUtils() {
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing
     * the provided list of elements.
     * </p>
     * <p>
     * No separator is added to the joined String. Null objects or empty strings
     * within the array are represented by empty strings.
     * </p>
     * <p/>
     * <pre>
     * StringUtils.join(null)            = null
     * StringUtils.join([])              = ""
     * StringUtils.join([null])          = ""
     * StringUtils.join(["a", "b", "c"]) = "abc"
     * StringUtils.join([null, "", "a"]) = "a"
     * </pre>
     *
     * @param array the array of values to join together, may be null
     * @return the joined String, <code>null</code> if null array input
     * @since 2.0
     */
    public String join(Object[] array) {
        return join(array, null);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing
     * the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty
     * strings within the array are represented by empty strings.
     * </p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtils.join(["a", "b", "c"], null) = "abc"
     * StringUtils.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array     the array of values to join together, may be null
     * @param separator the separator character to use
     * @return the joined String, <code>null</code> if null array input
     * @since 2.0
     */
    public String join(Object[] array, char separator) {
        if (array == null) {
            return null;
        }

        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing
     * the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty
     * strings within the array are represented by empty strings.
     * </p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)               = null
     * StringUtils.join([], *)                 = ""
     * StringUtils.join([null], *)             = ""
     * StringUtils.join(["a", "b", "c"], ';')  = "a;b;c"
     * StringUtils.join(["a", "b", "c"], null) = "abc"
     * StringUtils.join([null, "", "a"], ';')  = ";;a"
     * </pre>
     *
     * @param array      the array of values to join together, may be null
     * @param separator  the separator character to use
     * @param startIndex the first index to start joining from. It is an error
     *                   to pass in an end index past the end of the array
     * @param endIndex   the index to stop joining from (exclusive). It is an
     *                   error to pass in an end index past the end of the array
     * @return the joined String, <code>null</code> if null array input
     * @since 2.0
     */
    public String join(Object[] array, char separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + 1);
        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    public boolean isJSON(String str) {
        boolean result;
        try {
            Object obj = JSON.parse(str);
            result = true;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing
     * the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A <code>null</code>
     * separator is the same as an empty String (""). Null objects or empty
     * strings within the array are represented by empty strings.
     * </p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array     the array of values to join together, may be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null array input
     */
    public String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return join(array, separator, 0, array.length);
    }

    /**
     * <p>
     * Joins the elements of the provided array into a single String containing
     * the provided list of elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A <code>null</code>
     * separator is the same as an empty String (""). Null objects or empty
     * strings within the array are represented by empty strings.
     * </p>
     * <p/>
     * <pre>
     * StringUtils.join(null, *)                = null
     * StringUtils.join([], *)                  = ""
     * StringUtils.join([null], *)              = ""
     * StringUtils.join(["a", "b", "c"], "--")  = "a--b--c"
     * StringUtils.join(["a", "b", "c"], null)  = "abc"
     * StringUtils.join(["a", "b", "c"], "")    = "abc"
     * StringUtils.join([null, "", "a"], ',')   = ",,a"
     * </pre>
     *
     * @param array      the array of values to join together, may be null
     * @param separator  the separator character to use, null treated as ""
     * @param startIndex the first index to start joining from. It is an error
     *                   to pass in an end index past the end of the array
     * @param endIndex   the index to stop joining from (exclusive). It is an
     *                   error to pass in an end index past the end of the array
     * @return the joined String, <code>null</code> if null array input
     */
    public String join(Object[] array, String separator, int startIndex, int endIndex) {
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }

        // endIndex - startIndex > 0: Len = NofStrings *(len(firstString) +
        // len(separator))
        // (Assuming that all Strings are roughly equally long)
        int bufSize = (endIndex - startIndex);
        if (bufSize <= 0) {
            return "";
        }

        bufSize *= ((array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator
                .length());

        StringBuffer buf = new StringBuffer(bufSize);

        for (int i = startIndex; i < endIndex; i++) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] != null) {
                buf.append(array[i]);
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Iterator</code> into a single
     * String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty
     * strings within the iteration are represented by empty strings.
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[], char)}.
     * </p>
     *
     * @param iterator  the <code>Iterator</code> of values to join together, may
     *                  be null
     * @param separator the separator character to use
     * @return the joined String, <code>null</code> if null iterator input
     * @since 2.0
     */
    public String join(Iterator<?> iterator, char separator) {

        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first.toString();
        }

        // two or more elements
        StringBuffer buf = new StringBuffer(256); // Java default is 16,
        // probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            buf.append(separator);
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }

        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Iterator</code> into a single
     * String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A <code>null</code>
     * separator is the same as an empty String ("").
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[], String)}.
     * </p>
     *
     * @param iterator  the <code>Iterator</code> of values to join together, may
     *                  be null
     * @param separator the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     */
    public String join(Iterator<?> iterator, String separator) {

        // handle null, zero and one elements before building a buffer
        if (iterator == null) {
            return null;
        }
        if (!iterator.hasNext()) {
            return "";
        }
        Object first = iterator.next();
        if (!iterator.hasNext()) {
            return first.toString();
        }

        // two or more elements
        StringBuffer buf = new StringBuffer(256); // Java default is 16,
        // probably too small
        if (first != null) {
            buf.append(first);
        }

        while (iterator.hasNext()) {
            if (separator != null) {
                buf.append(separator);
            }
            Object obj = iterator.next();
            if (obj != null) {
                buf.append(obj);
            }
        }
        return buf.toString();
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Collection</code> into a single
     * String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. Null objects or empty
     * strings within the iteration are represented by empty strings.
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[], char)}.
     * </p>
     *
     * @param collection the <code>Collection</code> of values to join together,
     *                   may be null
     * @param separator  the separator character to use
     * @return the joined String, <code>null</code> if null iterator input
     * @since 2.3
     */
    public String join(Collection<?> collection, char separator) {
        if (collection == null) {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    /**
     * <p>
     * Joins the elements of the provided <code>Collection</code> into a single
     * String containing the provided elements.
     * </p>
     * <p>
     * No delimiter is added before or after the list. A <code>null</code>
     * separator is the same as an empty String ("").
     * </p>
     * <p>
     * See the examples here: {@link #join(Object[], String)}.
     * </p>
     *
     * @param collection the <code>Collection</code> of values to join together,
     *                   may be null
     * @param separator  the separator character to use, null treated as ""
     * @return the joined String, <code>null</code> if null iterator input
     * @since 2.3
     */
    public String join(Collection<?> collection, String separator) {
        if (collection == null) {
            return null;
        }
        return join(collection.iterator(), separator);
    }

    /**
     * 生成一串长度为len的随机字符串，包含[a-z][A-Z][0-9]
     *
     * @param len : 字符串长度
     */
    public String generateRandomString(final int len) {
        final String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        final Random random = new Random();
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < len; i++) {
            final int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public int getStringUTF8Length(final String str) {
        if (TextUtils.isEmpty(str)) {
            return 0;
        }

        try {
            return str.getBytes("UTF-8").length;
        } catch (final UnsupportedEncodingException e) {
            return 0;
        }
    }

    /**
     * 母串是否包含子串。这里的包含比较宽松，只要母串中出现了子串中的字符就可以，字符可以是离散的
     *
     * @param searchableString 母串
     * @param restriction      子串
     * @return
     */
    public boolean contains(final String searchableString, final String restriction) {
        int i = 0, j = 0;
        while ((i < restriction.length()) && (j < searchableString.length())) {
            if (restriction.charAt(i) == searchableString.charAt(j)) {
                ++i;
                ++j;
            } else {
                ++j;
            }
        }
        return i == restriction.length();
    }

    public String getHexString(final byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    // 当字符串为null时，处理成空串
    public String getStringNotNull(String str) {
        return str == null ? "" : str;
    }

    public int stringToInt(final String src, int defaultValue) {
        try {
            int result = Integer.parseInt(src);
            return result;
        } catch (NumberFormatException e) {
        }
        return defaultValue;
    }

    /**
     * @param pInput
     * @return 返回pInput的MD5消息摘要.如果出错, 则返回原始数据pInput。
     */
    public String getMd5Digest(final String pInput) {
        if (pInput != null) {
            try {
                final MessageDigest lDigest = MessageDigest.getInstance("MD5");
                lDigest.update(getBytes(pInput));
                final BigInteger lHashInt = new BigInteger(1, lDigest.digest());
                return String.format("%1$032X", lHashInt);
            } catch (final NoSuchAlgorithmException lException) {
                return pInput;
            }
        }
        return null;

    }

    /**
     * @param pInput
     * @return 返回pInput的SHA1消息摘要.如果出错, 则返回原始数据pInput。
     */
    public String getSHA1Digest(final String pInput) {
        if (pInput != null) {
            try {
                final MessageDigest lDigest = MessageDigest.getInstance("SHA1");
                lDigest.update(getBytes(pInput));
                final BigInteger lHashInt = new BigInteger(1, lDigest.digest());
                return String.format("%1$032X", lHashInt);
            } catch (final NoSuchAlgorithmException lException) {
                return pInput;
            }
        }
        return null;
    }

    /**
     * 获取字符串的UTF-8字节标示形式。如果UTF-8不被支持，返回默认的字符集的字节形式。
     *
     * @return
     */
    public byte[] getBytes(final String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (final UnsupportedEncodingException e) {
            return s.getBytes();
        }
    }

    public String[] toStrArray(final List<String> arrList) {
        final String[] r = new String[arrList.size()];
        arrList.toArray(r);
        return r;
    }

    public long[] toLongArray(final List<Long> arrList) {
        final long[] r = new long[arrList.size()];
        for (int i = 0; i < arrList.size(); i++) {
            r[i] = arrList.get(i);
        }
        return r;
    }

    public int[] toIntArray(final List<Integer> l) {
        final int[] r = new int[l.size()];
        for (int i = 0; i < l.size(); i++) {
            r[i] = l.get(i);
        }
        return r;
    }

    /**
     * @param size 传入long大小
     * @return 超过1M，返回2位MB小数。否则，返回整数KB
     */
    public String getFormatFileSize(long size) {
        long sizeOfKb = size / 1024;
        if (sizeOfKb >= 1024) {
            // 显示两位小数，单位M
            double x = (double) sizeOfKb / 1024.0;
            BigDecimal ci = new BigDecimal(x);
            double x2 = ci.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            return x2 + "MB";
        } else {
            // 显示KB
            return sizeOfKb + "KB";
        }
    }

    /**
     * 获取string的长度 中文算2哥字符
     *
     * @param value
     * @return
     */
    public int getStringLength(String value) {
        int valueLength = 0;
        String chinese = "[\u4e00-\u9fa5]";
        for (int i = 0; i < value.length(); i++) {
            String temp = value.substring(i, i + 1);
            if (temp.matches(chinese)) {
                valueLength += 2;
            } else {
                valueLength += 1;
            }
        }
        return valueLength;
    }

    /**
     * 输入的是一个普通文本，但文本里面包含了一个base64的子串
     * 找到这个子串并返回，常用于口令
     *
     * @param input
     * @return
     */
    public String getLongestBase64SubString(String input) {
        if (input == null) {
            return null;
        }
        // flag 代表以 i 为结尾 最长 base64 子串的长度。
        int max = 0;
        int maxIndex = -1;
        int flag[] = new int[input.length()];
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= 'a' && c <= 'z'
                    || c >= 'A' && c <= 'Z'
                    || c >= '0' && c <= '9'
                    || c == '+'
                    || c == '/'
                    || c == '='
            ) {
                if (i > 0) {
                    flag[i] = flag[i - 1] + 1;
                } else {
                    flag[i] = 1;
                }
                if (flag[i] > max) {
                    max = flag[i];
                    maxIndex = i;
                }
            } else {
                flag[i] = 0;
            }
        }
        if (maxIndex >= 0) {
            String r = input.substring(maxIndex - max + 1, maxIndex + 1);
            return r;
        } else {
            return "";
        }
    }
}
