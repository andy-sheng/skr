package com.component.lyrics;

import android.util.Base64;

import com.common.log.MyLog;
import com.component.lyrics.formats.LyricsFileReader;
import com.component.lyrics.model.LyricsInfo;
import com.component.lyrics.model.LyricsLineInfo;
import com.component.lyrics.model.LyricsTag;
import com.component.lyrics.utils.LyricsIOUtils;
import com.component.lyrics.utils.LyricsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 歌词读管理器
 * Created by zhangliangming on 2018-02-25.
 */

public class LyricsReader implements Cloneable {

    public final String TAG = "LyricsReader";

    /**
     * 时间补偿值,其单位是毫秒，正值表示整体提前，负值相反。这是用于总体调整显示快慢的。
     */
    private long mDefOffset = 0;
    /**
     * 增量
     */
    private long mOffset = 0;

    /**
     * 歌词类型
     */
    private int mLyricsType = LyricsInfo.DYNAMIC;

    /**
     * 歌词文件路径
     */
    private String mLrcFilePath;

    /**
     * 文件hash
     */
    private String mHash;

    /**
     * 原始歌词列表
     */
    private TreeMap<Integer, LyricsLineInfo> mLrcLineInfos;
    /**
     * 原始翻译行歌词列表
     */
    private List<LyricsLineInfo> mTranslateLrcLineInfos;
    /**
     * 原始音译歌词行
     */
    private List<LyricsLineInfo> mTransliterationLrcLineInfos;

    private LyricsInfo mLyricsInfo;

    public LyricsReader() {

    }

    @Override
    protected LyricsReader clone() {
        try {
            LyricsReader lyricsReader = (LyricsReader) super.clone();
            lyricsReader.mLyricsInfo = mLyricsInfo;
            return lyricsReader;
        } catch (CloneNotSupportedException e) {
            MyLog.e(TAG, e);
        }

        return null;
    }

    /**
     * 加载歌词数据
     *
     * @param lyricsFile
     */
    public void loadLrc(File lyricsFile) throws Exception {
        this.mLrcFilePath = lyricsFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(lyricsFile);
        LyricsInfo lyricsInfo = lyricsFileReader.readFile(lyricsFile);
        parser(lyricsInfo);
    }

    /**
     * @param base64FileContentString 歌词base64文件
     * @param saveLrcFile             要保存的的lrc文件
     * @param fileName                含后缀名的文件名称
     */
    public void loadLrc(String base64FileContentString, File saveLrcFile, String fileName) throws Exception {
        loadLrc(Base64.decode(base64FileContentString, Base64.NO_WRAP), saveLrcFile, fileName);
    }

    /**
     * @param base64ByteArray 歌词base64数组
     * @param saveLrcFile
     * @param fileName
     */
    public void loadLrc(byte[] base64ByteArray, File saveLrcFile, String fileName) throws Exception {
        if (saveLrcFile != null)
            mLrcFilePath = saveLrcFile.getPath();
        LyricsFileReader lyricsFileReader = LyricsIOUtils.getLyricsFileReader(fileName);
        LyricsInfo lyricsInfo = lyricsFileReader.readLrcText(base64ByteArray, saveLrcFile);
        parser(lyricsInfo);

    }


    /**
     * 解析
     *
     * @param lyricsInfo
     */
    private void parser(LyricsInfo lyricsInfo) {
        mLyricsInfo = lyricsInfo;
        mLyricsType = lyricsInfo.getLyricsType();
        Map<String, Object> tags = lyricsInfo.getLyricsTags();
        if (tags.containsKey(LyricsTag.TAG_OFFSET)) {
            mDefOffset = 0;
            try {
                mDefOffset = Long.parseLong((String) tags.get(LyricsTag.TAG_OFFSET));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mDefOffset = 0;
        }
        //默认歌词行
        mLrcLineInfos = lyricsInfo.getLyricsLineInfoTreeMap();
        if (mLrcLineInfos == null) {
            MyLog.e(TAG, "mLrcLineInfos 为null");
        }
        //翻译歌词集合
        if (lyricsInfo.getTranslateLrcLineInfos() != null)
            mTranslateLrcLineInfos = LyricsUtils.getTranslateLrc(mLyricsType, mLrcLineInfos, lyricsInfo.getTranslateLrcLineInfos());
        //音译歌词集合
        if (lyricsInfo.getTransliterationLrcLineInfos() != null)
            mTransliterationLrcLineInfos = LyricsUtils.getTransliterationLrc(mLyricsType, mLrcLineInfos, lyricsInfo.getTransliterationLrcLineInfos());

    }

    public void cut(long startTs, long endTs) {
        if (startTs >= endTs) {
            MyLog.d(TAG, "歌词开始时间大于结束时间，不截取:" + "cut" + " startTs=" + startTs + " endTs=" + endTs);
            return;
        }

        if (mLrcLineInfos == null) {
            return;
        }

        Iterator<Map.Entry<Integer, LyricsLineInfo>> it = mLrcLineInfos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, LyricsLineInfo> entry = it.next();

            if (entry.getValue().getEndTime() == 0 && entry.getValue().getStartTime() > 0) {
                if (entry.getValue().getStartTime() < startTs) {
                    continue;
                }
            } else {
                if (entry.getValue().getEndTime() >= endTs && entry.getValue().getStartTime() < endTs) {

                    continue;
                }

                if (entry.getValue().getEndTime() > endTs) {
                    it.remove();
                    continue;
                }

                if (entry.getValue().getEndTime() <= startTs) {
                    it.remove();
                    continue;
                }
            }
        }

        /**
         * 生成新的歌词
         */
        Iterator<Map.Entry<Integer, LyricsLineInfo>> newIt = mLrcLineInfos.entrySet().iterator();
        mLrcLineInfos = new TreeMap<>();

        int index = 0;
        while (newIt.hasNext()) {
            Map.Entry<Integer, LyricsLineInfo> entry = newIt.next();
            mLrcLineInfos.put(index++, entry.getValue());
        }
    }

    public String getTwoLineGuideLyric(long guideStart) {
        List<LyricsLineInfo> lyricsLineInfoList = getLyricsLineInfoList();
        if (lyricsLineInfoList == null || lyricsLineInfoList.size() == 0) {
            MyLog.d(TAG, "getTwoLineGuideLyric lyricsLineInfoList error");
            return "";
        }

        int lastGuideLyricLineNum = 0;
        for (int i = 0; i < lyricsLineInfoList.size(); i++) {
            LyricsLineInfo info = lyricsLineInfoList.get(i);
            if (info.getStartTime() == guideStart || (guideStart > info.getStartTime() && guideStart < info.getEndTime())) {
                lastGuideLyricLineNum = i - 1;
                break;
            }
        }

        if (lastGuideLyricLineNum > 0) {
            String lyric = lyricsLineInfoList.get(lastGuideLyricLineNum - 1).getLineLyrics() + "\n"
                    + lyricsLineInfoList.get(lastGuideLyricLineNum).getLineLyrics();
            return lyric;
        }

        return "";
    }

    public List<LyricsLineInfo> getLyricsLineInfoList() {
        ArrayList<LyricsLineInfo> mLyricsLineInfoList = new ArrayList<>();

        if (mLrcLineInfos != null) {
            Iterator<Map.Entry<Integer, LyricsLineInfo>> newIt = mLrcLineInfos.entrySet().iterator();

            while (newIt.hasNext()) {
                Map.Entry<Integer, LyricsLineInfo> entry = newIt.next();
                mLyricsLineInfoList.add(entry.getValue());
            }
        }

        return mLyricsLineInfoList;
    }

    public int getLineInfoIdByStartTs(long startTs) {
        if (mLrcLineInfos != null) {
            Iterator<Map.Entry<Integer, LyricsLineInfo>> it = mLrcLineInfos.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, LyricsLineInfo> entry = it.next();
                if (entry.getValue().getEndTime() > startTs) {
                    return entry.getKey();
                }
            }
        }
        return -1;
    }

    ////////////////////////////////////////////////////////////////////////////////


    public int getLyricsType() {
        return mLyricsType;
    }

    public TreeMap<Integer, LyricsLineInfo> getLrcLineInfos() {
        return mLrcLineInfos;
    }

    public List<LyricsLineInfo> getTranslateLrcLineInfos() {
        return mTranslateLrcLineInfos;
    }

    public List<LyricsLineInfo> getTransliterationLrcLineInfos() {
        return mTransliterationLrcLineInfos;
    }

    public String getHash() {
        return mHash;
    }

    public void setHash(String mHash) {
        this.mHash = mHash;
    }

    public String getLrcFilePath() {
        return mLrcFilePath;
    }

    public void setLrcFilePath(String mLrcFilePath) {
        this.mLrcFilePath = mLrcFilePath;
    }

    public long getOffset() {
        return mOffset;
    }

    public void setOffset(long offset) {
        this.mOffset = offset;
    }

    public LyricsInfo getLyricsInfo() {
        return mLyricsInfo;
    }

    public void setLyricsType(int mLyricsType) {
        this.mLyricsType = mLyricsType;
    }

    public void setLrcLineInfos(TreeMap<Integer, LyricsLineInfo> mLrcLineInfos) {
        this.mLrcLineInfos = mLrcLineInfos;
    }

    public void setTranslateLrcLineInfos(List<LyricsLineInfo> mTranslateLrcLineInfos) {
        this.mTranslateLrcLineInfos = mTranslateLrcLineInfos;
    }

    public void setTransliterationLrcLineInfos(List<LyricsLineInfo> mTransliterationLrcLineInfos) {
        this.mTransliterationLrcLineInfos = mTransliterationLrcLineInfos;
    }

    public void setLyricsInfo(LyricsInfo mLyricsInfo) {
        this.mLyricsInfo = mLyricsInfo;
    }

    ////////////////////////////////////////////////////////////////////////////////

    /**
     * 播放的时间补偿值
     *
     * @return
     */
    public long getPlayOffset() {
        return mDefOffset + mOffset;
    }
}
