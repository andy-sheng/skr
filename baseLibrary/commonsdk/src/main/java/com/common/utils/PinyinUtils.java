package com.common.utils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.pinyinhelper.PinyinMapDict;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by YoKey on 16/3/20.
 */
public class PinyinUtils {
    private static final String PATTERN_POLYPHONE = "^#[a-zA-Z]+#.+";
    private static final String PATTERN_LETTER = "^[a-zA-Z].*+";

    PinyinUtils() {
    }

    public void changeConfig() {
        // 添加中文城市词典
//        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance());

// 添加自定义词典
//        Pinyin.init(Pinyin.newConfig()
//                .with(new PinyinMapDict() {
//                    @Override
//                    public Map<String, String[]> mapping() {
//                        HashMap<String, String[]> map = new HashMap<String, String[]>();
//                        map.put("重庆", new String[]{"CHONG", "QING"});
//                        return map;
//                    }
//                }));
    }


    /**
     * 程思敏->CHNEGSIMIN
     * Chinese character -> Pinyin
     */
    public String getPingYin(String inputString) {
        if (inputString == null) return "";
        return Pinyin.toPinyin(inputString, "").toLowerCase();
    }

    /**
     * Are start with a letter
     *
     * @return if return false, index should be #
     */
    public boolean matchingLetter(String inputString) {
        return Pattern.matches(PATTERN_LETTER, inputString);
    }


    /**
     * 是否是多音字
     *
     * @param inputString
     * @return
     */
    public boolean matchingPolyphone(String inputString) {
        return Pattern.matches(PATTERN_POLYPHONE, inputString);
    }


    public String gePolyphoneInitial(String inputString) {
        return inputString.substring(1, 2);
    }

    public String getPolyphoneRealPinyin(String inputString) {
        String[] splits = inputString.split("#");
        return splits[1];
    }

    /**
     * 如果是多音字
     *
     * @param inputString
     * @return
     */
    public String getPolyphoneRealHanzi(String inputString) {
        String[] splits = inputString.split("#");
        return splits[2];
    }
}
