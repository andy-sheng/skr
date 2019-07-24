package com.component.lyrics.formats.zrce;

import android.text.TextUtils;
import android.util.Base64;

import com.common.log.MyLog;
import com.component.lyrics.formats.LyricsFileReader;
import com.component.lyrics.model.LyricsInfo;
import com.component.lyrics.model.LyricsLineInfo;
import com.component.lyrics.model.LyricsTag;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okio.BufferedSource;
import okio.Okio;

import static okhttp3.internal.Util.closeQuietly;

public class ZrceLyricsFileReader extends LyricsFileReader {
    /**
     * 歌曲名 字符串
     */
    private final static String LEGAL_SONGNAME_PREFIX = "[ti:";
    /**
     * 歌手名 字符串
     */
    private final static String LEGAL_SINGERNAME_PREFIX = "[ar:";
    /**
     * 时间补偿值 字符串
     */
    private final static String LEGAL_OFFSET_PREFIX = "[offset:";
    /**
     * 歌词上传者
     */
    private final static String LEGAL_BY_PREFIX = "[by:";
    private final static String LEGAL_HASH_PREFIX = "[hash:";
    /**
     * 专辑
     */
    private final static String LEGAL_AL_PREFIX = "[al:";
    private final static String LEGAL_SIGN_PREFIX = "[sign:";
    private final static String LEGAL_QQ_PREFIX = "[icon_qq:";
    private final static String LEGAL_TOTAL_PREFIX = "[total:";
    private final static String LEGAL_LANGUAGE_PREFIX = "[language:";

    @Override
    public LyricsInfo readInputStream(InputStream in) throws Exception {
        LyricsInfo lyricsIfno = new LyricsInfo();
        lyricsIfno.setLyricsFileExt(getSupportFileExt());
        if (in != null) {

            String lyricsTextStr = decodeLrc(in);
            if (!TextUtils.isEmpty(lyricsTextStr)) {
                String[] lyricsTexts = lyricsTextStr.split("\n");
                TreeMap<Integer, LyricsLineInfo> lyricsLineInfos = new TreeMap<Integer, LyricsLineInfo>();
                Map<String, Object> lyricsTags = new HashMap<String, Object>();
                int index = 0;

                for (int i = 0; i < lyricsTexts.length; i++) {
                    String lineInfo = lyricsTexts[i];

                    // 行读取，并解析每行歌词的内容
                    LyricsLineInfo lyricsLineInfo = parserLineInfos(lyricsTags,
                            lineInfo, lyricsIfno);
                    if (lyricsLineInfo != null) {
                        lyricsLineInfos.put(index, lyricsLineInfo);
                        index++;
                    }
                }
                in.close();
                in = null;
                // 设置歌词的标签类
                lyricsIfno.setLyricsTags(lyricsTags);
                //
                lyricsIfno.setLyricsLineInfoTreeMap(lyricsLineInfos);
            }
        }
        return lyricsIfno;
    }

    /**
     * 解析歌词
     *
     * @param lyricsTags
     * @param lineInfo
     * @param lyricsIfno
     * @return
     */
    private LyricsLineInfo parserLineInfos(Map<String, Object> lyricsTags,
                                           String lineInfo, LyricsInfo lyricsIfno) throws Exception {
        LyricsLineInfo lyricsLineInfo = null;
        if (lineInfo.startsWith(LEGAL_SONGNAME_PREFIX)) {
            int startIndex = LEGAL_SONGNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            //
            lyricsTags.put(LyricsTag.TAG_TITLE,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_SINGERNAME_PREFIX)) {
            int startIndex = LEGAL_SINGERNAME_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_ARTIST,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_OFFSET_PREFIX)) {
            int startIndex = LEGAL_OFFSET_PREFIX.length();
            int endIndex = lineInfo.lastIndexOf("]");
            lyricsTags.put(LyricsTag.TAG_OFFSET,
                    lineInfo.substring(startIndex, endIndex));
        } else if (lineInfo.startsWith(LEGAL_BY_PREFIX)
                || lineInfo.startsWith(LEGAL_HASH_PREFIX)
                || lineInfo.startsWith(LEGAL_SIGN_PREFIX)
                || lineInfo.startsWith(LEGAL_QQ_PREFIX)
                || lineInfo.startsWith(LEGAL_TOTAL_PREFIX)
                || lineInfo.startsWith(LEGAL_AL_PREFIX)) {

            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            lyricsTags.put(temp[0], temp.length == 1 ? "" : temp[1]);

        } else if (lineInfo.startsWith(LEGAL_LANGUAGE_PREFIX)) {
            int startIndex = lineInfo.indexOf("[") + 1;
            int endIndex = lineInfo.lastIndexOf("]");
            String temp[] = lineInfo.substring(startIndex, endIndex).split(":");
            // 解析翻译歌词
            // 获取json base64字符串
            String translateJsonBase64String = temp.length == 1 ? "" : temp[1];
            if (!translateJsonBase64String.equals("")) {

                String translateJsonString = new String(
                        Base64.decode(translateJsonBase64String, Base64.NO_WRAP));
                parserOtherLrc(lyricsIfno, translateJsonString);
            }
        } else {
            // 匹配歌词行
            Pattern pattern = Pattern.compile("\\[\\d+,\\d+\\]");
            Matcher matcher = pattern.matcher(lineInfo);
            if (matcher.find()) {
                lyricsLineInfo = new LyricsLineInfo();
                // [此行开始时刻距0时刻的毫秒数,此行持续的毫秒数]<0,此字持续的毫秒数,0>歌<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>词<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>正<此字开始的时刻距此行开始时刻的毫秒数,此字持续的毫秒数,0>文
                // 获取行的出现时间和结束时间
                int mStartIndex = matcher.start();
                int mEndIndex = matcher.end();
                String lineTime[] = lineInfo.substring(mStartIndex + 1,
                        mEndIndex - 1).split(",");
                //

                int startTime = Integer.parseInt(lineTime[0]);
                int endTime = startTime + Integer.parseInt(lineTime[1]);
                lyricsLineInfo.setEndTime(endTime);
                lyricsLineInfo.setStartTime(startTime);
                // 获取歌词信息
                String lineContent = lineInfo.substring(mEndIndex,
                        lineInfo.length());

                // 歌词匹配的正则表达式
                String regex = "\\<\\d+,\\d+,\\d+\\>";
                Pattern lyricsWordsPattern = Pattern.compile(regex);
                Matcher lyricsWordsMatcher = lyricsWordsPattern
                        .matcher(lineContent);

                // 歌词分隔
                String lineLyricsTemp[] = lineContent.split(regex);
                String[] lyricsWords = getLyricsWords(lineLyricsTemp);
                lyricsLineInfo.setLyricsWords(lyricsWords);

                // 获取每个歌词的时间
                int wordsDisInterval[] = new int[lyricsWords.length];
                int index = 0;
                while (lyricsWordsMatcher.find()) {

                    //验证
                    if (index >= wordsDisInterval.length) {
//                        throw new Exception("字标签个数与字时间标签个数不相符");
                        MyLog.e("ZrceLyricsFileReader", "字标签个数与字时间标签个数不相符");
                        continue;
                    }

                    //
                    String wordsDisIntervalStr = lyricsWordsMatcher.group();
                    String wordsDisIntervalStrTemp = wordsDisIntervalStr
                            .substring(wordsDisIntervalStr.indexOf('<') + 1, wordsDisIntervalStr.lastIndexOf('>'));
                    String wordsDisIntervalTemp[] = wordsDisIntervalStrTemp
                            .split(",");
                    wordsDisInterval[index++] = Integer
                            .parseInt(wordsDisIntervalTemp[1]);
                }
                lyricsLineInfo.setWordsDisInterval(wordsDisInterval);

                // 获取当行歌词
                String lineLyrics = lyricsWordsMatcher.replaceAll("");
                lyricsLineInfo.setLineLyrics(lineLyrics);
            }

        }
        return lyricsLineInfo;
    }

    /**
     * 解析翻译和音译歌词
     *
     * @param lyricsIfno
     * @param translateJsonString
     */
    private void parserOtherLrc(LyricsInfo lyricsIfno,
                                String translateJsonString) throws Exception {

        JSONObject resultObj = new JSONObject(translateJsonString);
        JSONArray contentArrayObj = resultObj.getJSONArray("content");
        for (int i = 0; i < contentArrayObj.length(); i++) {
            JSONObject dataObj = contentArrayObj.getJSONObject(i);
            JSONArray lyricContentArrayObj = dataObj
                    .getJSONArray("lyricContent");
            int type = dataObj.getInt("type");
            if (type == 1) {
                // 解析翻译歌词
//                if (lyricsIfno.getTranslateLrcLineInfos() == null || lyricsIfno.getTranslateLrcLineInfos().size() == 0)
//                    parserTranslateLrc(lyricsIfno, lyricContentArrayObj);

            } else if (type == 0) {
                // 解析音译歌词
//                if (lyricsIfno.getTransliterationLrcLineInfos() == null || lyricsIfno.getTransliterationLrcLineInfos().size() == 0)
//                    parserTransliterationLrc(lyricsIfno,
//                            lyricContentArrayObj);
            }
        }
    }

    public static String decodeLrc(InputStream in) {
        BufferedSource bufferedSource = null;
        byte[] bytes;
        try {
            bufferedSource = Okio.buffer(Okio.source(in));
            bytes = bufferedSource.readByteArray();
            byte keys[] = {(byte) 0xCE, (byte) 0xD3, 'n', 'i', '@', 'Z', 'a', 'w', '^', '2', 't', 'G', 'Q', '6', (byte) 0xA5, (byte) 0xBC};

            for (int i = 0; i < bytes.length; i++) {
                byte ccc = bytes[i];
                byte f = (byte) (ccc ^ keys[i % 16]);
                bytes[i] = f;
            }

            return new String(bytes, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(bufferedSource);
        }

        return null;
    }

    /**
     * 分隔每个歌词
     *
     * @param lineLyricsTemp
     * @return
     */
    private String[] getLyricsWords(String[] lineLyricsTemp) throws Exception {
        String temp[] = null;
        if (lineLyricsTemp.length < 2) {
            return new String[lineLyricsTemp.length];
        }
        //
        temp = new String[lineLyricsTemp.length - 1];
        for (int i = 1; i < lineLyricsTemp.length; i++) {
            temp[i - 1] = lineLyricsTemp[i];
        }
        return temp;
    }

    @Override
    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("zrce");
    }

    @Override
    public String getSupportFileExt() {
        return "zrce";
    }
}
