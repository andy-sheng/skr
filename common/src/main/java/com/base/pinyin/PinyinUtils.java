package com.base.pinyin;

import android.text.TextUtils;

import com.base.log.MyLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by MK on 15-4-9.
 */
public abstract class PinyinUtils {

    private static final String[] VALID_PINYINS = {
            // "a", // 5
            "a",
            "ai",
            "an",
            "ang",
            "ao",
            // "b", //16
            "ba",
            "bai",
            "ban",
            "bang",
            "bao",
            "bei",
            "ben",
            "beng",
            "bi",
            "bian",
            "biao",
            "bie",
            "bin",
            "bing",
            "bo",
            "bu",
            // "c", //36
            "ca",
            "cai",
            "can",
            "cang",
            "cao",
            "ce",
            "cen",
            "ceng",
            "ch",
            "cha",
            "chai",
            "chan",
            "chang",
            "chao",
            "che",
            "chen",
            "cheng",
            "chi",
            "chong",
            "chou",
            "chu",
            "chua",
            "chuai",
            "chuan",
            "chuang",
            "chui",
            "chun",
            "chuo",
            "ci",
            "cong",
            "cou",
            "cu",
            "cuan",
            "cui",
            "cun",
            "cuo",
            // "d", //23
            "da",
            "dai",
            "dan",
            "dang",
            "com/wali/live/dao",
            "de",
            "den",
            "dei",
            "deng",
            "di",
            "dia",
            "dian",
            "diao",
            "die",
            "ding",
            "diu",
            "dong",
            "dou",
            "du",
            "duan",
            "dui",
            "dun",
            "duo",
            // "e", // 5
            "e",
            "ei",
            "en",
            "eng",
            "er",
            // "f", //9
            "fa",
            "fan",
            "fang",
            "fei",
            "fen",
            "feng",
            "fo",
            "fou",
            "fu",
            // "g", //19
            "ga",
            "gai",
            "gan",
            "gang",
            "gao",
            "ge",
            "gei",
            "gen",
            "geng",
            "gong",
            "gou",
            "gu",
            "gua",
            "guai",
            "guan",
            "guang",
            "gui",
            "gun",
            "guo",
            // "h", //19
            "ha",
            "hai",
            "han",
            "hang",
            "hao",
            "he",
            "hei",
            "hen",
            "heng",
            "hong",
            "hou",
            "hu",
            "hua",
            "huai",
            "huan",
            "huang",
            "hui",
            "hun",
            "huo",
            // "j", //14
            "ji",
            "jia",
            "jian",
            "jiang",
            "jiao",
            "jie",
            "jin",
            "jing",
            "jiong",
            "jiu",
            "ju",
            "juan",
            "jue",
            "jun",
            // "k", //18
            "ka",
            "kai",
            "kan",
            "kang",
            "kao",
            "ke",
            "ken",
            "keng",
            "kong",
            "kou",
            "ku",
            "kua",
            "kuai",
            "kuan",
            "kuang",
            "kui",
            "kun",
            "kuo",
            // "l", //26
            "la", "lai", "lan", "lang", "lao", "le", "lei", "leng", "li", "lia",
            "lian",
            "liang",
            "liao",
            "lie",
            "lin",
            "ling",
            "liu",
            "long",
            "lou",
            "lu",
            "lv",
            "luan",
            "lue",
            "lve",
            "lun",
            "luo",
            // "m", //19
            "ma", "mai", "man", "mang", "mao",
            "me",
            "mei",
            "men",
            "meng",
            "mi",
            "mian",
            "miao",
            "mie",
            "min",
            "ming",
            "miu",
            "mo",
            "mou",
            "mu",
            // "n", //25
            "na", "nai",
            "nan",
            "nang",
            "nao",
            "ne",
            "nei",
            "nen",
            "neng",
            "ni",
            "nian",
            "niang",
            "niao",
            "nie",
            "nin",
            "ning",
            "niu",
            "nong",
            "nou",
            "nu",
            "nv",
            "nuan",
            "nve",
            "nuo",
            "nun",
            // "o", // 2
            "o",
            "ou",
            // "p", //17
            "pa", "pai", "pan", "pang", "pao", "pei",
            "pen",
            "peng",
            "pi",
            "pian",
            "piao",
            "pie",
            "pin",
            "ping",
            "po",
            "pou",
            "pu",
            // "q", //14
            "qi", "qia", "qian", "qiang",
            "qiao",
            "qie",
            "qin",
            "qing",
            "qiong",
            "qiu",
            "qu",
            "quan",
            "que",
            "qun",
            // "r", //14
            "ran", "rang", "rao", "re", "ren",
            "reng",
            "ri",
            "rong",
            "rou",
            "ru",
            "ruan",
            "rui",
            "run",
            "ruo",
            // "s", //36
            "sa", "sai", "san", "sang", "sao", "se", "sen", "seng", "sh", "sha", "shai", "shan",
            "shang", "shao", "she", "shei", "shen", "sheng", "shi", "shou", "shu", "shua", "shuai",
            "shuan", "shuang", "shui", "shun", "shuo", "si", "song",
            "sou",
            "su",
            "suan",
            "sui",
            "sun",
            "suo",
            // "t", //19
            "ta", "tai", "tan", "tang", "tao", "te", "teng", "ti", "tian", "tiao", "tie", "ting",
            "tong", "tou", "tu",
            "tuan",
            "tui",
            "tun",
            "tuo",
            // "w", //9
            "wa", "wai", "wan", "wang", "wei",
            "wen",
            "weng",
            "wo",
            "wu",
            // "x", //14
            "xi", "xia", "xian", "xiang", "xiao", "xie", "xin", "xing", "xiong", "xiu",
            "xu",
            "xuan",
            "xue",
            "xun",
            // "y", //15
            "ya", "yan", "yang", "yao", "ye", "yi", "yin", "ying", "yo", "yong", "you", "yu",
            "yuan",
            "yue",
            "yun",
            // "z", //38
            "za", "zai", "zan", "zang", "zao", "ze", "zei", "zen", "zeng", "zh", "zha", "zhai",
            "zhan", "zhang", "zhao", "zhe", "zhei", "zhen", "zheng", "zhi", "zhong", "zhou", "zhu",
            "zhua", "zhuai", "zhuan", "zhuang", "zhui", "zhun", "zhuo", "zi", "zong", "zou", "zu",
            "zuan", "zui", "zun", "zuo"
    };

    private static HashMap<String, String> sHanziToPinyinCache = new HashMap<String, String>();

    private static ConcurrentHashMap<String, Boolean> sValidFuzzyPinyins = null;

    private static Object sLock = new Object();

    public static void initFuzzyPinyinMap() {
        synchronized (sLock) {
            if (sValidFuzzyPinyins == null) {
                sValidFuzzyPinyins = new ConcurrentHashMap<String, Boolean>();
                for (String pinyin : VALID_PINYINS) {
                    for (int i = 0; i < pinyin.length(); ++i) {
                        String s = pinyin.substring(0, i + 1);
                        sValidFuzzyPinyins.put(s, true);
                    }
                }
            }
        }
        return;
    }

    public static boolean isValidFuzzyPinyin(String s) {
        if (sValidFuzzyPinyins == null) {
            initFuzzyPinyinMap();
        }
        if (TextUtils.isEmpty(s)) {
            return false;
        }
        Boolean result = sValidFuzzyPinyins.get(s);
        return null != result;
    }

    private static void pick(ArrayList<ArrayList<String>> splits, ArrayList<String> split,
                             String keyword, int cursor) {
        if (cursor < keyword.length()) {
            int maxPickLength = keyword.length() - cursor;
            for (int pickLength = 1; pickLength <= maxPickLength; ++pickLength) {
                String pickString = keyword.substring(cursor, cursor + pickLength);
                cursor += pickLength;
                if (isValidFuzzyPinyin(pickString)) {
                    split.add(pickString);
                    pick(splits, new ArrayList<String>(split), keyword, cursor);
                    split.remove(split.size() - 1);
                }
                cursor -= pickLength;
            }
        } else {
            if (split.size() > 0) {
                splits.add(split);
            }
        }
    }

    public static ArrayList<ArrayList<String>> splitToValidPinyinTokens(String s) {
        ArrayList<ArrayList<String>> tokens = new ArrayList<ArrayList<String>>();
        pick(tokens, new ArrayList<String>(), s, 0);
        return tokens;
    }

    private static char getFirstLetterFromPinyin(final String pinyin) {
        char rv = '#';
        if (!TextUtils.isEmpty(pinyin)) {
            final char firstLetter = pinyin.toUpperCase().charAt(0);
            if ((firstLetter >= 'A') && (firstLetter <= 'Z')) {
                rv = firstLetter;
            }
        }
        return rv;
    }

    /**
     * 返回小写拼音
     *
     * @param source
     * @return
     */
    public static String hanziToPinyin(final String source) {
        final StringBuilder sbFullPinyin = new StringBuilder();
        if (!TextUtils.isEmpty(source)) {
            StringBuilder hanzi = new StringBuilder();
            for (int i = 0; i < source.length(); i++) {
                String c = String.valueOf(source.charAt(i));
                if (!sHanziToPinyinCache.containsKey(c)) {
                    hanzi.append(c);
                } else {
                    if (sbFullPinyin.length() > 0) {
                        sbFullPinyin.append(" ");
                    }
                    sbFullPinyin.append(sHanziToPinyinCache.get(c));
                }
            }
            if (hanzi.length() > 0) {
                sbFullPinyin.delete(0, sbFullPinyin.length());
                final ArrayList<HanziToPinyin.Token> pinyins = HanziToPinyin.getInstance().get(hanzi.toString());
                if ((pinyins != null) && (pinyins.size() > 0)) {
                    for (final HanziToPinyin.Token aToken : pinyins) {
                        String lowerTokenSource = aToken.source != null ? aToken.source.toLowerCase() : "";
                        String upperTokenTarget = aToken.target != null ? aToken.target.toUpperCase() : "";
                        if (!TextUtils.isEmpty(upperTokenTarget)) {
                            sHanziToPinyinCache.put(lowerTokenSource,
                                    upperTokenTarget);
                        }
                    }
                }
                for (int i = 0; i < source.length(); i++) {
                    if (sbFullPinyin.length() > 0) {
                        sbFullPinyin.append(" ");
                    }
                    sbFullPinyin.append(sHanziToPinyinCache.get(String.valueOf(source.charAt(i))));
                }
            }
            if (TextUtils.isEmpty(sbFullPinyin)) {
                return source.toLowerCase(Locale.ENGLISH);
            }
        }
        return sbFullPinyin.toString().toLowerCase(Locale.ENGLISH);
    }


    /**
     * 返回名字的第一个字的对应的pinyin（大写的格式）
     *
     * @param name
     * @return
     */
    public static String getFirstHanziPinyinByName(final String name) {
        if (name == null) {
            return "#";
        }
        final String noSpace = name.trim();
        if (TextUtils.isEmpty(noSpace)) {
            return "#";
        }
        final String firstChar = String.valueOf(noSpace.charAt(0)).toLowerCase();
        try {
            if (sHanziToPinyinCache.containsKey(firstChar)) {
                return sHanziToPinyinCache.get(firstChar);
            }
        } catch (final Exception e) {
            MyLog.e(e);
        }
        String pinYin = hanziToPinyin(firstChar).toUpperCase();
        if (TextUtils.isEmpty(pinYin)) {
            pinYin = "#";
        } else {
            final char firstLetter = pinYin.charAt(0);
            if ((firstLetter < 'A') || (firstLetter > 'Z')) {
                pinYin = "#";
            }
        }
        sHanziToPinyinCache.put(firstChar, pinYin);
        return pinYin;
    }

    /**
     * 返回名字的第一个汉子对应的大写字母
     *
     * @param name
     * @return
     */
    public static char getFirstLetterByName(final String name) {
        final String pinYin = getFirstHanziPinyinByName(name);
        return getFirstLetterFromPinyin(pinYin);
    }

}
