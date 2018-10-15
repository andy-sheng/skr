/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.base.pinyin;

import android.text.TextUtils;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * An object to convert Chinese character to its corresponding pinyin string. For characters with multiple possible
 * pinyin string, only one is selected according to collator. Polyphone is not supported in this implementation. This
 * class is implemented to achieve the best runtime performance and minimum runtime resources with tolerable sacrifice
 * of accuracy. This implementation highly depends on zh_CN ICU collation data and must be always synchronized with ICU.
 */
public class HanziToPinyinICS {
    private static final String TAG = "HanziToPinyin";

    /**
     * Unihans array. Each unihans is the first one within same pinyin. Use it to determine pinyin for all ~20k unihans.
     */
    public static final char[] UNIHANS = {
            '\u5475', '\u54ce', '\u5b89', '\u80ae', '\u51f9',
            '\u516b', '\u6300', '\u6273', '\u90a6', '\u5305', '\u5351', '\u5954', '\u4f3b',
            '\u5c44', '\u8fb9', '\u6807', '\u618b', '\u90a0', '\u69df', '\u7676', '\u5cec',
            '\u5693', '\u5a47', '\u98e1', '\u4ed3', '\u64cd', '\u518a', '\u5d7e', '\u564c',
            '\u53c9', '\u9497', '\u8fbf', '\u4f25', '\u6284', '\u8f66', '\u62bb', '\u67fd',
            '\u5403', '\u5145', '\u62bd', '\u51fa', '\u6b3b', '\u63e3', '\u5ddd', '\u75ae',
            '\u5439', '\u6776', '\u9034', '\u75b5', '\u5306', '\u51d1', '\u7c97', '\u6c46',
            '\u5d14', '\u90a8', '\u6413', '\u5491', '\u5927', '\u75b8', '\u5f53', '\u5200',
            '\u6dc2', '\u5f97', '\u6265', '\u706f', '\u6c10', '\u55f2', '\u7538', '\u5201',
            '\u7239', '\u4ec3', '\u4e1f', '\u4e1c', '\u5517', '\u561f', '\u5073', '\u5806',
            '\u9413', '\u591a', '\u5a40', '\u8bf6', '\u5940', '\u97a5', '\u800c', '\u53d1',
            '\u5e06', '\u65b9', '\u98de', '\u5206', '\u4e30', '\u8985', '\u4ecf', '\u7d11',
            '\u4f15', '\u65ee', '\u8be5', '\u7518', '\u5188', '\u768b', '\u6208', '\u7d66',
            '\u6839', '\u5e9a', '\u5de5', '\u52fe', '\u4f30', '\u74dc', '\u7f6b', '\u5173',
            '\u5149', '\u5f52', '\u886e', '\u5459', '\u54c8', '\u54b3', '\u9878', '\u82c0',
            '\u84bf', '\u8bc3', '\u9ed2', '\u62eb', '\u4ea8', '\u5677', '\u543d', '\u9f41',
            '\u5322', '\u82b1', '\u6000', '\u72bf', '\u5ddf', '\u7070', '\u660f', '\u5419',
            '\u4e0c', '\u52a0', '\u620b', '\u6c5f', '\u827d', '\u9636', '\u5dfe', '\u52a4',
            '\u5182', '\u52fc', '\u530a', '\u5a1f', '\u5658', '\u519b', '\u5494', '\u5f00',
            '\u520a', '\u95f6', '\u5c3b', '\u533c', '\u524b', '\u80af', '\u962c', '\u7a7a',
            '\u62a0', '\u5233', '\u5938', '\u84af', '\u5bbd', '\u5321', '\u4e8f', '\u5764',
            '\u6269', '\u5783', '\u6765', '\u5170', '\u5577', '\u635e', '\u4ec2', '\u52d2',
            '\u5844', '\u5215', '\u5006', '\u5941', '\u826f', '\u64a9', '\u5217', '\u62ce',
            '\u3007', '\u6e9c', '\u9f99', '\u779c', '\u565c', '\u5a08', '\u7567', '\u62a1',
            '\u7f57', '\u5463', '\u5988', '\u973e', '\u5ada', '\u9099', '\u732b', '\u9ebc',
            '\u6c92', '\u95e8', '\u753f', '\u54aa', '\u7720', '\u55b5', '\u54a9', '\u6c11',
            '\u540d', '\u8c2c', '\u6478', '\u54de', '\u6bea', '\u62cf', '\u5b7b', '\u56e1',
            '\u56ca', '\u5b6c', '\u8bb7', '\u9981', '\u6041', '\u80fd', '\u59ae', '\u62c8',
            '\u5b22', '\u9e1f', '\u634f', '\u60a8', '\u5b81', '\u599e', '\u519c', '\u7fba',
            '\u5974', '\u597b', '\u8650', '\u632a', '\u5594', '\u8bb4', '\u8db4', '\u62cd',
            '\u7705', '\u4e53', '\u629b', '\u5478', '\u55b7', '\u5309', '\u4e15', '\u504f',
            '\u527d', '\u6c15', '\u59d8', '\u4e52', '\u948b', '\u5256', '\u4ec6', '\u4e03',
            '\u6390', '\u5343', '\u545b', '\u6084', '\u767f', '\u4fb5', '\u9751', '\u909b',
            '\u4e18', '\u66f2', '\u5f2e', '\u7f3a', '\u590b', '\u5465', '\u7a63', '\u5a06',
            '\u60f9', '\u4eba', '\u6254', '\u65e5', '\u8338', '\u53b9', '\u5982', '\u5827',
            '\u6875', '\u95f0', '\u82e5', '\u4ee8', '\u6be2', '\u4e09', '\u6852', '\u63bb',
            '\u8272', '\u68ee', '\u50e7', '\u6740', '\u7b5b', '\u5c71', '\u4f24', '\u5f30',
            '\u5962', '\u7533', '\u5347', '\u5c38', '\u53ce', '\u4e66', '\u5237', '\u6454',
            '\u95e9', '\u53cc', '\u8c01', '\u542e', '\u5981', '\u53b6', '\u5fea', '\u635c',
            '\u82cf', '\u72fb', '\u590a', '\u5b59', '\u5506', '\u4ed6', '\u82d4', '\u574d',
            '\u94f4', '\u5932', '\u5fd1', '\u71a5', '\u5254', '\u5929', '\u4f7b', '\u5e16',
            '\u5385', '\u56f2', '\u5077', '\u92c0', '\u6e4d', '\u63a8', '\u541e', '\u6258',
            '\u6316', '\u6b6a', '\u5f2f', '\u5c2a', '\u5371', '\u586d', '\u7fc1', '\u631d',
            '\u5140', '\u5915', '\u867e', '\u4eda', '\u4e61', '\u7071', '\u4e9b', '\u5fc3',
            '\u661f', '\u51f6', '\u4f11', '\u65f4', '\u8f69', '\u75b6', '\u52cb', '\u4e2b',
            '\u6079', '\u592e', '\u5e7a', '\u8036', '\u4e00', '\u6b2d', '\u5e94', '\u54df',
            '\u4f63', '\u4f18', '\u625c', '\u9e22', '\u66f0', '\u6655', '\u531d', '\u707d',
            '\u7ccc', '\u7242', '\u50ae', '\u5219', '\u8d3c', '\u600e', '\u5897', '\u5412',
            '\u635a', '\u6cbe', '\u5f20', '\u948a', '\u8707', '\u8d1e', '\u4e89', '\u4e4b',
            '\u4e2d', '\u5dde', '\u6731', '\u6293', '\u8de9', '\u4e13', '\u5986', '\u96b9',
            '\u5b92', '\u5353', '\u5b5c', '\u5b97', '\u90b9', '\u79df', '\u94bb', '\u539c',
            '\u5c0a', '\u6628',
    };

    /**
     * Pinyin array. Each pinyin is corresponding to unihans of same offset in the unihans array.
     */
    public static final byte[][] PINYINS = {
            {
                    65, 0, 0, 0, 0, 0
            }, {
                    65, 73, 0, 0, 0, 0
            }, {
                    65, 78, 0, 0, 0, 0
            },
            {
                    65, 78, 71, 0, 0, 0
            }, {
                    65, 79, 0, 0, 0, 0
            }, {
                    66, 65, 0, 0, 0, 0
            },
            {
                    66, 65, 73, 0, 0, 0
            }, {
                    66, 65, 78, 0, 0, 0
            }, {
                    66, 65, 78, 71, 0, 0
            },
            {
                    66, 65, 79, 0, 0, 0
            }, {
                    66, 69, 73, 0, 0, 0
            }, {
                    66, 69, 78, 0, 0, 0
            },
            {
                    66, 69, 78, 71, 0, 0
            }, {
                    66, 73, 0, 0, 0, 0
            }, {
                    66, 73, 65, 78, 0, 0
            },
            {
                    66, 73, 65, 79, 0, 0
            }, {
                    66, 73, 69, 0, 0, 0
            }, {
                    66, 73, 78, 0, 0, 0
            },
            {
                    66, 73, 78, 71, 0, 0
            }, {
                    66, 79, 0, 0, 0, 0
            }, {
                    66, 85, 0, 0, 0, 0
            },
            {
                    67, 65, 0, 0, 0, 0
            }, {
                    67, 65, 73, 0, 0, 0
            },
            {
                    67, 65, 78, 0, 0, 0
            }, {
                    67, 65, 78, 71, 0, 0
            }, {
                    67, 65, 79, 0, 0, 0
            },
            {
                    67, 69, 0, 0, 0, 0
            }, {
                    67, 69, 78, 0, 0, 0
            }, {
                    67, 69, 78, 71, 0, 0
            },
            {
                    67, 72, 65, 0, 0, 0
            }, {
                    67, 72, 65, 73, 0, 0
            }, {
                    67, 72, 65, 78, 0, 0
            },
            {
                    67, 72, 65, 78, 71, 0
            }, {
                    67, 72, 65, 79, 0, 0
            }, {
                    67, 72, 69, 0, 0, 0
            },
            {
                    67, 72, 69, 78, 0, 0
            }, {
                    67, 72, 69, 78, 71, 0
            }, {
                    67, 72, 73, 0, 0, 0
            },
            {
                    67, 72, 79, 78, 71, 0
            }, {
                    67, 72, 79, 85, 0, 0
            }, {
                    67, 72, 85, 0, 0, 0
            },
            {
                    67, 72, 85, 65, 0, 0
            }, {
                    67, 72, 85, 65, 73, 0
            }, {
                    67, 72, 85, 65, 78, 0
            },
            {
                    67, 72, 85, 65, 78, 71
            }, {
                    67, 72, 85, 73, 0, 0
            }, {
                    67, 72, 85, 78, 0, 0
            },
            {
                    67, 72, 85, 79, 0, 0
            }, {
                    67, 73, 0, 0, 0, 0
            }, {
                    67, 79, 78, 71, 0, 0
            },
            {
                    67, 79, 85, 0, 0, 0
            }, {
                    67, 85, 0, 0, 0, 0
            }, {
                    67, 85, 65, 78, 0, 0
            },
            {
                    67, 85, 73, 0, 0, 0
            }, {
                    67, 85, 78, 0, 0, 0
            }, {
                    67, 85, 79, 0, 0, 0
            },
            {
                    68, 65, 0, 0, 0, 0
            }, {
                    68, 65, 73, 0, 0, 0
            }, {
                    68, 65, 78, 0, 0, 0
            },
            {
                    68, 65, 78, 71, 0, 0
            }, {
                    68, 65, 79, 0, 0, 0
            }, {
                    68, 69, 0, 0, 0, 0
            },
            {
                    68, 69, 73, 0, 0, 0
            }, {
                    68, 69, 78, 0, 0, 0
            }, {
                    68, 69, 78, 71, 0, 0
            },
            {
                    68, 73, 0, 0, 0, 0
            }, {
                    68, 73, 65, 0, 0, 0
            }, {
                    68, 73, 65, 78, 0, 0
            },
            {
                    68, 73, 65, 79, 0, 0
            }, {
                    68, 73, 69, 0, 0, 0
            }, {
                    68, 73, 78, 71, 0, 0
            },
            {
                    68, 73, 85, 0, 0, 0
            }, {
                    68, 79, 78, 71, 0, 0
            }, {
                    68, 79, 85, 0, 0, 0
            },
            {
                    68, 85, 0, 0, 0, 0
            }, {
                    68, 85, 65, 78, 0, 0
            }, {
                    68, 85, 73, 0, 0, 0
            },
            {
                    68, 85, 78, 0, 0, 0
            }, {
                    68, 85, 79, 0, 0, 0
            }, {
                    69, 0, 0, 0, 0, 0
            },
            {
                    69, 73, 0, 0, 0, 0
            }, {
                    69, 78, 0, 0, 0, 0
            }, {
                    69, 78, 71, 0, 0, 0
            },
            {
                    69, 82, 0, 0, 0, 0
            }, {
                    70, 65, 0, 0, 0, 0
            }, {
                    70, 65, 78, 0, 0, 0
            },
            {
                    70, 65, 78, 71, 0, 0
            }, {
                    70, 69, 73, 0, 0, 0
            }, {
                    70, 69, 78, 0, 0, 0
            },
            {
                    70, 69, 78, 71, 0, 0
            }, {
                    70, 73, 65, 79, 0, 0
            }, {
                    70, 79, 0, 0, 0, 0
            },
            {
                    70, 79, 85, 0, 0, 0
            }, {
                    70, 85, 0, 0, 0, 0
            }, {
                    71, 65, 0, 0, 0, 0
            },
            {
                    71, 65, 73, 0, 0, 0
            }, {
                    71, 65, 78, 0, 0, 0
            }, {
                    71, 65, 78, 71, 0, 0
            },
            {
                    71, 65, 79, 0, 0, 0
            }, {
                    71, 69, 0, 0, 0, 0
            }, {
                    71, 69, 73, 0, 0, 0
            },
            {
                    71, 69, 78, 0, 0, 0
            }, {
                    71, 69, 78, 71, 0, 0
            }, {
                    71, 79, 78, 71, 0, 0
            },
            {
                    71, 79, 85, 0, 0, 0
            }, {
                    71, 85, 0, 0, 0, 0
            }, {
                    71, 85, 65, 0, 0, 0
            },
            {
                    71, 85, 65, 73, 0, 0
            }, {
                    71, 85, 65, 78, 0, 0
            }, {
                    71, 85, 65, 78, 71, 0
            },
            {
                    71, 85, 73, 0, 0, 0
            }, {
                    71, 85, 78, 0, 0, 0
            }, {
                    71, 85, 79, 0, 0, 0
            },
            {
                    72, 65, 0, 0, 0, 0
            }, {
                    72, 65, 73, 0, 0, 0
            }, {
                    72, 65, 78, 0, 0, 0
            },
            {
                    72, 65, 78, 71, 0, 0
            }, {
                    72, 65, 79, 0, 0, 0
            }, {
                    72, 69, 0, 0, 0, 0
            },
            {
                    72, 69, 73, 0, 0, 0
            }, {
                    72, 69, 78, 0, 0, 0
            }, {
                    72, 69, 78, 71, 0, 0
            },
            {
                    72, 77, 0, 0, 0, 0
            }, {
                    72, 79, 78, 71, 0, 0
            }, {
                    72, 79, 85, 0, 0, 0
            },
            {
                    72, 85, 0, 0, 0, 0
            }, {
                    72, 85, 65, 0, 0, 0
            }, {
                    72, 85, 65, 73, 0, 0
            },
            {
                    72, 85, 65, 78, 0, 0
            }, {
                    72, 85, 65, 78, 71, 0
            }, {
                    72, 85, 73, 0, 0, 0
            },
            {
                    72, 85, 78, 0, 0, 0
            }, {
                    72, 85, 79, 0, 0, 0
            }, {
                    74, 73, 0, 0, 0, 0
            },
            {
                    74, 73, 65, 0, 0, 0
            }, {
                    74, 73, 65, 78, 0, 0
            }, {
                    74, 73, 65, 78, 71, 0
            },
            {
                    74, 73, 65, 79, 0, 0
            }, {
                    74, 73, 69, 0, 0, 0
            }, {
                    74, 73, 78, 0, 0, 0
            },
            {
                    74, 73, 78, 71, 0, 0
            }, {
                    74, 73, 79, 78, 71, 0
            }, {
                    74, 73, 85, 0, 0, 0
            },
            {
                    74, 85, 0, 0, 0, 0
            }, {
                    74, 85, 65, 78, 0, 0
            }, {
                    74, 85, 69, 0, 0, 0
            },
            {
                    74, 85, 78, 0, 0, 0
            }, {
                    75, 65, 0, 0, 0, 0
            }, {
                    75, 65, 73, 0, 0, 0
            },
            {
                    75, 65, 78, 0, 0, 0
            }, {
                    75, 65, 78, 71, 0, 0
            }, {
                    75, 65, 79, 0, 0, 0
            },
            {
                    75, 69, 0, 0, 0, 0
            }, {
                    75, 69, 73, 0, 0, 0
            }, {
                    75, 69, 78, 0, 0, 0
            },
            {
                    75, 69, 78, 71, 0, 0
            }, {
                    75, 79, 78, 71, 0, 0
            }, {
                    75, 79, 85, 0, 0, 0
            },
            {
                    75, 85, 0, 0, 0, 0
            }, {
                    75, 85, 65, 0, 0, 0
            }, {
                    75, 85, 65, 73, 0, 0
            },
            {
                    75, 85, 65, 78, 0, 0
            }, {
                    75, 85, 65, 78, 71, 0
            }, {
                    75, 85, 73, 0, 0, 0
            },
            {
                    75, 85, 78, 0, 0, 0
            }, {
                    75, 85, 79, 0, 0, 0
            }, {
                    76, 65, 0, 0, 0, 0
            },
            {
                    76, 65, 73, 0, 0, 0
            }, {
                    76, 65, 78, 0, 0, 0
            }, {
                    76, 65, 78, 71, 0, 0
            },
            {
                    76, 65, 79, 0, 0, 0
            }, {
                    76, 69, 0, 0, 0, 0
            }, {
                    76, 69, 73, 0, 0, 0
            },
            {
                    76, 69, 78, 71, 0, 0
            }, {
                    76, 73, 0, 0, 0, 0
            }, {
                    76, 73, 65, 0, 0, 0
            },
            {
                    76, 73, 65, 78, 0, 0
            }, {
                    76, 73, 65, 78, 71, 0
            }, {
                    76, 73, 65, 79, 0, 0
            },
            {
                    76, 73, 69, 0, 0, 0
            }, {
                    76, 73, 78, 0, 0, 0
            }, {
                    76, 73, 78, 71, 0, 0
            },
            {
                    76, 73, 85, 0, 0, 0
            }, {
                    76, 79, 78, 71, 0, 0
            }, {
                    76, 79, 85, 0, 0, 0
            },
            {
                    76, 85, 0, 0, 0, 0
            }, {
                    76, 85, 65, 78, 0, 0
            }, {
                    76, 85, 69, 0, 0, 0
            },
            {
                    76, 85, 78, 0, 0, 0
            }, {
                    76, 85, 79, 0, 0, 0
            }, {
                    77, 0, 0, 0, 0, 0
            },
            {
                    77, 65, 0, 0, 0, 0
            }, {
                    77, 65, 73, 0, 0, 0
            }, {
                    77, 65, 78, 0, 0, 0
            },
            {
                    77, 65, 78, 71, 0, 0
            }, {
                    77, 65, 79, 0, 0, 0
            }, {
                    77, 69, 0, 0, 0, 0
            },
            {
                    77, 69, 73, 0, 0, 0
            }, {
                    77, 69, 78, 0, 0, 0
            }, {
                    77, 69, 78, 71, 0, 0
            },
            {
                    77, 73, 0, 0, 0, 0
            }, {
                    77, 73, 65, 78, 0, 0
            }, {
                    77, 73, 65, 79, 0, 0
            },
            {
                    77, 73, 69, 0, 0, 0
            }, {
                    77, 73, 78, 0, 0, 0
            }, {
                    77, 73, 78, 71, 0, 0
            },
            {
                    77, 73, 85, 0, 0, 0
            }, {
                    77, 79, 0, 0, 0, 0
            }, {
                    77, 79, 85, 0, 0, 0
            },
            {
                    77, 85, 0, 0, 0, 0
            }, {
                    78, 65, 0, 0, 0, 0
            }, {
                    78, 65, 73, 0, 0, 0
            },
            {
                    78, 65, 78, 0, 0, 0
            }, {
                    78, 65, 78, 71, 0, 0
            }, {
                    78, 65, 79, 0, 0, 0
            },
            {
                    78, 69, 0, 0, 0, 0
            }, {
                    78, 69, 73, 0, 0, 0
            }, {
                    78, 69, 78, 0, 0, 0
            },
            {
                    78, 69, 78, 71, 0, 0
            }, {
                    78, 73, 0, 0, 0, 0
            }, {
                    78, 73, 65, 78, 0, 0
            },
            {
                    78, 73, 65, 78, 71, 0
            }, {
                    78, 73, 65, 79, 0, 0
            }, {
                    78, 73, 69, 0, 0, 0
            },
            {
                    78, 73, 78, 0, 0, 0
            }, {
                    78, 73, 78, 71, 0, 0
            }, {
                    78, 73, 85, 0, 0, 0
            },
            {
                    78, 79, 78, 71, 0, 0
            }, {
                    78, 79, 85, 0, 0, 0
            }, {
                    78, 85, 0, 0, 0, 0
            },
            {
                    78, 85, 65, 78, 0, 0
            }, {
                    78, 85, 69, 0, 0, 0
            }, {
                    78, 85, 79, 0, 0, 0
            },
            {
                    79, 0, 0, 0, 0, 0
            }, {
                    79, 85, 0, 0, 0, 0
            }, {
                    80, 65, 0, 0, 0, 0
            },
            {
                    80, 65, 73, 0, 0, 0
            }, {
                    80, 65, 78, 0, 0, 0
            }, {
                    80, 65, 78, 71, 0, 0
            },
            {
                    80, 65, 79, 0, 0, 0
            }, {
                    80, 69, 73, 0, 0, 0
            }, {
                    80, 69, 78, 0, 0, 0
            },
            {
                    80, 69, 78, 71, 0, 0
            }, {
                    80, 73, 0, 0, 0, 0
            }, {
                    80, 73, 65, 78, 0, 0
            },
            {
                    80, 73, 65, 79, 0, 0
            }, {
                    80, 73, 69, 0, 0, 0
            }, {
                    80, 73, 78, 0, 0, 0
            },
            {
                    80, 73, 78, 71, 0, 0
            }, {
                    80, 79, 0, 0, 0, 0
            }, {
                    80, 79, 85, 0, 0, 0
            },
            {
                    80, 85, 0, 0, 0, 0
            }, {
                    81, 73, 0, 0, 0, 0
            }, {
                    81, 73, 65, 0, 0, 0
            },
            {
                    81, 73, 65, 78, 0, 0
            }, {
                    81, 73, 65, 78, 71, 0
            }, {
                    81, 73, 65, 79, 0, 0
            },
            {
                    81, 73, 69, 0, 0, 0
            }, {
                    81, 73, 78, 0, 0, 0
            }, {
                    81, 73, 78, 71, 0, 0
            },
            {
                    81, 73, 79, 78, 71, 0
            }, {
                    81, 73, 85, 0, 0, 0
            }, {
                    81, 85, 0, 0, 0, 0
            },
            {
                    81, 85, 65, 78, 0, 0
            }, {
                    81, 85, 69, 0, 0, 0
            }, {
                    81, 85, 78, 0, 0, 0
            },
            {
                    82, 65, 78, 0, 0, 0
            }, {
                    82, 65, 78, 71, 0, 0
            }, {
                    82, 65, 79, 0, 0, 0
            },
            {
                    82, 69, 0, 0, 0, 0
            }, {
                    82, 69, 78, 0, 0, 0
            }, {
                    82, 69, 78, 71, 0, 0
            },
            {
                    82, 73, 0, 0, 0, 0
            }, {
                    82, 79, 78, 71, 0, 0
            }, {
                    82, 79, 85, 0, 0, 0
            },
            {
                    82, 85, 0, 0, 0, 0
            }, {
                    82, 85, 65, 78, 0, 0
            }, {
                    82, 85, 73, 0, 0, 0
            },
            {
                    82, 85, 78, 0, 0, 0
            }, {
                    82, 85, 79, 0, 0, 0
            }, {
                    83, 65, 0, 0, 0, 0
            },
            {
                    83, 65, 73, 0, 0, 0
            }, {
                    83, 65, 78, 0, 0, 0
            }, {
                    83, 65, 78, 71, 0, 0
            },
            {
                    83, 65, 79, 0, 0, 0
            }, {
                    83, 69, 0, 0, 0, 0
            }, {
                    83, 69, 78, 0, 0, 0
            },
            {
                    83, 69, 78, 71, 0, 0
            }, {
                    83, 72, 65, 0, 0, 0
            }, {
                    83, 72, 65, 73, 0, 0
            },
            {
                    83, 72, 65, 78, 0, 0
            }, {
                    83, 72, 65, 78, 71, 0
            }, {
                    83, 72, 65, 79, 0, 0
            },
            {
                    83, 72, 69, 0, 0, 0
            }, {
                    83, 72, 69, 78, 0, 0
            }, {
                    83, 72, 69, 78, 71, 0
            },
            {
                    83, 72, 73, 0, 0, 0
            }, {
                    83, 72, 79, 85, 0, 0
            }, {
                    83, 72, 85, 0, 0, 0
            },
            {
                    83, 72, 85, 65, 0, 0
            }, {
                    83, 72, 85, 65, 73, 0
            }, {
                    83, 72, 85, 65, 78, 0
            },
            {
                    83, 72, 85, 65, 78, 71
            }, {
                    83, 72, 85, 73, 0, 0
            }, {
                    83, 72, 85, 78, 0, 0
            },
            {
                    83, 72, 85, 79, 0, 0
            }, {
                    83, 73, 0, 0, 0, 0
            }, {
                    83, 79, 78, 71, 0, 0
            },
            {
                    83, 79, 85, 0, 0, 0
            }, {
                    83, 85, 0, 0, 0, 0
            }, {
                    83, 85, 65, 78, 0, 0
            },
            {
                    83, 85, 73, 0, 0, 0
            }, {
                    83, 85, 78, 0, 0, 0
            }, {
                    83, 85, 79, 0, 0, 0
            },
            {
                    84, 65, 0, 0, 0, 0
            }, {
                    84, 65, 73, 0, 0, 0
            }, {
                    84, 65, 78, 0, 0, 0
            },
            {
                    84, 65, 78, 71, 0, 0
            }, {
                    84, 65, 79, 0, 0, 0
            }, {
                    84, 69, 0, 0, 0, 0
            },
            {
                    84, 69, 78, 71, 0, 0
            }, {
                    84, 73, 0, 0, 0, 0
            }, {
                    84, 73, 65, 78, 0, 0
            },
            {
                    84, 73, 65, 79, 0, 0
            }, {
                    84, 73, 69, 0, 0, 0
            }, {
                    84, 73, 78, 71, 0, 0
            },
            {
                    84, 79, 78, 71, 0, 0
            }, {
                    84, 79, 85, 0, 0, 0
            }, {
                    84, 85, 0, 0, 0, 0
            },
            {
                    84, 85, 65, 78, 0, 0
            }, {
                    84, 85, 73, 0, 0, 0
            }, {
                    84, 85, 78, 0, 0, 0
            },
            {
                    84, 85, 79, 0, 0, 0
            }, {
                    87, 65, 0, 0, 0, 0
            }, {
                    87, 65, 73, 0, 0, 0
            },
            {
                    87, 65, 78, 0, 0, 0
            }, {
                    87, 65, 78, 71, 0, 0
            }, {
                    87, 69, 73, 0, 0, 0
            },
            {
                    87, 69, 78, 0, 0, 0
            }, {
                    87, 69, 78, 71, 0, 0
            }, {
                    87, 79, 0, 0, 0, 0
            },
            {
                    87, 85, 0, 0, 0, 0
            }, {
                    88, 73, 0, 0, 0, 0
            }, {
                    88, 73, 65, 0, 0, 0
            },
            {
                    88, 73, 65, 78, 0, 0
            }, {
                    88, 73, 65, 78, 71, 0
            }, {
                    88, 73, 65, 79, 0, 0
            },
            {
                    88, 73, 69, 0, 0, 0
            }, {
                    88, 73, 78, 0, 0, 0
            }, {
                    88, 73, 78, 71, 0, 0
            },
            {
                    88, 73, 79, 78, 71, 0
            }, {
                    88, 73, 85, 0, 0, 0
            }, {
                    88, 85, 0, 0, 0, 0
            },
            {
                    88, 85, 65, 78, 0, 0
            }, {
                    88, 85, 69, 0, 0, 0
            }, {
                    88, 85, 78, 0, 0, 0
            },
            {
                    89, 65, 0, 0, 0, 0
            }, {
                    89, 65, 78, 0, 0, 0
            }, {
                    89, 65, 78, 71, 0, 0
            },
            {
                    89, 65, 79, 0, 0, 0
            }, {
                    89, 69, 0, 0, 0, 0
            }, {
                    89, 73, 0, 0, 0, 0
            },
            {
                    89, 73, 78, 0, 0, 0
            }, {
                    89, 73, 78, 71, 0, 0
            }, {
                    89, 79, 0, 0, 0, 0
            },
            {
                    89, 79, 78, 71, 0, 0
            }, {
                    89, 79, 85, 0, 0, 0
            }, {
                    89, 85, 0, 0, 0, 0
            },
            {
                    89, 85, 65, 78, 0, 0
            }, {
                    89, 85, 69, 0, 0, 0
            }, {
                    89, 85, 78, 0, 0, 0
            },
            {
                    90, 65, 0, 0, 0, 0
            }, {
                    90, 65, 73, 0, 0, 0
            }, {
                    90, 65, 78, 0, 0, 0
            },
            {
                    90, 65, 78, 71, 0, 0
            }, {
                    90, 65, 79, 0, 0, 0
            }, {
                    90, 69, 0, 0, 0, 0
            },
            {
                    90, 69, 73, 0, 0, 0
            }, {
                    90, 69, 78, 0, 0, 0
            }, {
                    90, 69, 78, 71, 0, 0
            },
            {
                    90, 72, 65, 0, 0, 0
            }, {
                    90, 72, 65, 73, 0, 0
            }, {
                    90, 72, 65, 78, 0, 0
            },
            {
                    90, 72, 65, 78, 71, 0
            }, {
                    90, 72, 65, 79, 0, 0
            }, {
                    90, 72, 69, 0, 0, 0
            },
            {
                    90, 72, 69, 78, 0, 0
            }, {
                    90, 72, 69, 78, 71, 0
            }, {
                    90, 72, 73, 0, 0, 0
            },
            {
                    90, 72, 79, 78, 71, 0
            }, {
                    90, 72, 79, 85, 0, 0
            }, {
                    90, 72, 85, 0, 0, 0
            },
            {
                    90, 72, 85, 65, 0, 0
            }, {
                    90, 72, 85, 65, 73, 0
            }, {
                    90, 72, 85, 65, 78, 0
            },
            {
                    90, 72, 85, 65, 78, 71
            }, {
                    90, 72, 85, 73, 0, 0
            }, {
                    90, 72, 85, 78, 0, 0
            },
            {
                    90, 72, 85, 79, 0, 0
            }, {
                    90, 73, 0, 0, 0, 0
            }, {
                    90, 79, 78, 71, 0, 0
            },
            {
                    90, 79, 85, 0, 0, 0
            }, {
                    90, 85, 0, 0, 0, 0
            }, {
                    90, 85, 65, 78, 0, 0
            },
            {
                    90, 85, 73, 0, 0, 0
            }, {
                    90, 85, 78, 0, 0, 0
            }, {
                    90, 85, 79, 0, 0, 0
            },
    };

    private static HashMap<Character, String[]> sPolyPhoneMap = new HashMap<Character, String[]>();
    private static HashMap<String, String[]> sHyphenatedNamePolyPhoneMap = new HashMap<String, String[]>();
    private static HashMap<Character, String> sLastNamePolyPhoneMap = new HashMap<Character, String>();

    /**
     * First and last Chinese character with known Pinyin according to zh collation
     */
    private static final String FIRST_PINYIN_UNIHAN = "\u963F";
    private static final String LAST_PINYIN_UNIHAN = "\u84D9";
    /** The first Chinese character in Unicode block */
    private static final char FIRST_UNIHAN = '\u3400';
    private static final Collator COLLATOR = Collator.getInstance(Locale.CHINA);

    private static HanziToPinyinICS sInstance;
    private final boolean mHasChinaCollator;

    static {
        /**
         * The below command can generate the codes according the the comments below. awk '{print
         * "sPolyPhoneMap.put(\047\\u"tolower($2)"\047, new String[] { \""
         * $3"\", \""$4"\", \""$5"\", \""$6"\" }); // "$0}' | sed -e "s/, \"\"//g" | sed -e "s/\s*$//g"
         */
        sPolyPhoneMap.put('\u963f', new String[] {
                "A", "E"
        }); // 阿 963F a e
        sPolyPhoneMap.put('\u814c', new String[] {
                "YAN", "A"
        }); // 腌 814C yan a
        sPolyPhoneMap.put('\u62d7', new String[] {
                "AO", "O", "NIU"
        }); // 拗 62D7 ao o niu
        sPolyPhoneMap.put('\u6252', new String[] {
                "PA", "BA"
        }); // 扒 6252 pa ba
        sPolyPhoneMap.put('\u868c', new String[] {
                "BANG", "BENG"
        }); // 蚌 868C bang beng
        sPolyPhoneMap.put('\u8584', new String[] {
                "BAO", "BO"
        }); // 薄 8584 bao bo
        sPolyPhoneMap.put('\u5821', new String[] {
                "BAO", "BU", "PU"
        }); // 堡 5821 bao bu pu
        sPolyPhoneMap.put('\u66b4', new String[] {
                "BAO", "PU"
        }); // 暴 66B4 bao pu
        sPolyPhoneMap.put('\u8d32', new String[] {
                "BEN", "FEI", "BI"
        }); // 贲 8D32 ben fei bi
        sPolyPhoneMap.put('\u8d39', new String[] {
                "FEI", "BI"
        }); // 费 8D39 fei bi
        sPolyPhoneMap.put('\u81c2', new String[] {
                "BI", "BEI"
        }); // 臂 81C2 bi bei
        sPolyPhoneMap.put('\u8f9f', new String[] {
                "PI", "BI"
        }); // 辟 8F9F pi bi
        sPolyPhoneMap.put('\u8300', new String[] {
                "FU", "BI"
        }); // 茀 8300 fu bi
        sPolyPhoneMap.put('\u6241', new String[] {
                "BIAN", "PIAN"
        }); // 扁 6241 bian pian
        sPolyPhoneMap.put('\u4fbf', new String[] {
                "BIAN", "PIAN"
        }); // 便 4FBF bian pian
        sPolyPhoneMap.put('\u8180', new String[] {
                "PANG", "BANG"
        }); // 膀 8180 pang bang
        sPolyPhoneMap.put('\u78c5', new String[] {
                "PANG", "BANG"
        }); // 磅 78C5 pang bang
        sPolyPhoneMap.put('\u9aa0', new String[] {
                "BIAO", "PIAO"
        }); // 骠 9AA0 biao piao
        sPolyPhoneMap.put('\u756a', new String[] {
                "FAN", "PAN", "BO"
        }); // 番 756A fan pan bo
        sPolyPhoneMap.put('\u5b5b', new String[] {
                "BEI", "BO"
        }); // 孛 5B5B bei bo
        sPolyPhoneMap.put('\u5e9f', new String[] {
                "FEI", "BO"
        }); // 废 5E9F fei bo
        sPolyPhoneMap.put('\u5265', new String[] {
                "BO", "BAO", "XUE"
        }); // 剥 5265 bo bao xue
        sPolyPhoneMap.put('\u6cca', new String[] {
                "BO", "PO", "BAN"
        }); // 泊 6CCA bo po ban
        sPolyPhoneMap.put('\u4f2f', new String[] {
                "BO", "BAI"
        }); // 伯 4F2F bo bai
        sPolyPhoneMap.put('\u535c', new String[] {
                "BO", "BU"
        }); // 卜 535C bo bu
        sPolyPhoneMap.put('\u4f27', new String[] {
                "CANG", "CHEN"
        }); // 伧 4F27 cang chen
        sPolyPhoneMap.put('\u85cf', new String[] {
                "CANG", "ZANG"
        }); // 藏 85CF cang zang
        sPolyPhoneMap.put('\u53c2', new String[] {
                "CAN", "SHEN", "CEN"
        }); // 参 53C2 can shen cen
        sPolyPhoneMap.put('\u66fe', new String[] {
                "CENG", "ZENG"
        }); // 曾 66FE ceng zeng
        sPolyPhoneMap.put('\u564c', new String[] {
                "CENG", "CHENG"
        }); // 噌 564C ceng cheng
        sPolyPhoneMap.put('\u5dee', new String[] {
                "CHA", "CHAI"
        }); // 差 5DEE cha chai
        sPolyPhoneMap.put('\u67e5', new String[] {
                "CHA", "ZHA"
        }); // 查 67E5 cha zha
        sPolyPhoneMap.put('\u7985', new String[] {
                "CHAN", "SHAN"
        }); // 禅 7985 chan shan
        sPolyPhoneMap.put('\u98a4', new String[] {
                "CHAN", "ZHAN"
        }); // 颤 98A4 chan zhan
        sPolyPhoneMap.put('\u5b71', new String[] {
                "CHAN", "CAN"
        }); // 孱 5B71 chan can
        sPolyPhoneMap.put('\u88f3', new String[] {
                "SHANG", "CHANG"
        }); // 裳 88F3 shang chang
        sPolyPhoneMap.put('\u573a', new String[] {
                "CHANG", "CHANG"
        }); // 场 573A chang chang
        sPolyPhoneMap.put('\u6668', new String[] {
                "CHEN", "CHANG", "ZE"
        }); // 晨 6668 chen chang ze
        sPolyPhoneMap.put('\u957f', new String[] {
                "CHANG", "ZHANG"
        }); // 长 957F chang zhang
        sPolyPhoneMap.put('\u5382', new String[] {
                "CHANG", "AN", "HAN"
        }); // 厂 5382 chang an han
        sPolyPhoneMap.put('\u5632', new String[] {
                "CHAO", "ZHAO", "ZHA"
        }); // 嘲 5632 chao zhao zha
        sPolyPhoneMap.put('\u8f66', new String[] {
                "CHE", "JU"
        }); // 车 8F66 che ju
        sPolyPhoneMap.put('\u79f0', new String[] {
                "CHENG", "CHEN"
        }); // 称 79F0 cheng chen
        sPolyPhoneMap.put('\u6f84', new String[] {
                "CHENG", "DENG"
        }); // 澄 6F84 cheng deng
        sPolyPhoneMap.put('\u94db', new String[] {
                "DANG", "CHENG"
        }); // 铛 94DB dang cheng
        sPolyPhoneMap.put('\u4e58', new String[] {
                "CHENG", "SHENG"
        }); // 乘 4E58 cheng sheng
        sPolyPhoneMap.put('\u671d', new String[] {
                "CHAO", "ZHAO"
        }); // 朝 671D chao zhao
        sPolyPhoneMap.put('\u9561', new String[] {
                "XIN", "CHAN", "TAN"
        }); // 镡 9561 xin chan tan
        sPolyPhoneMap.put('\u5319', new String[] {
                "SHI", "CHI"
        }); // 匙 5319 shi chi
        sPolyPhoneMap.put('\u90d7', new String[] {
                "XI", "CHI"
        }); // 郗 90D7 xi chi
        sPolyPhoneMap.put('\u6cbb', new String[] {
                "ZHI", "CHI"
        }); // 治 6CBB zhi chi
        sPolyPhoneMap.put('\u7633', new String[] {
                "CHOU", "LU"
        }); // 瘳 7633 chou lu
        sPolyPhoneMap.put('\u4e11', new String[] {
                "CHOU", "NIU"
        }); // 丑 4E11 chou niu
        sPolyPhoneMap.put('\u81ed', new String[] {
                "CHOU", "XIU"
        }); // 臭 81ED chou xiu
        sPolyPhoneMap.put('\u91cd', new String[] {
                "ZHONG", "CHONG"
        }); // 重 91CD zhong chong
        sPolyPhoneMap.put('\u79cd', new String[] {
                "ZHONG", "CHONG"
        }); // 种 79CD zhong chong
        sPolyPhoneMap.put('\u755c', new String[] {
                "CHU", "XU"
        }); // 畜 755C chu xu
        sPolyPhoneMap.put('\u9664', new String[] {
                "CHU", "XU"
        }); // 除 9664 chu xu
        sPolyPhoneMap.put('\u4f20', new String[] {
                "CHUAN", "ZHUAN"
        }); // 传 4F20 chuan zhuan
        sPolyPhoneMap.put('\u555c', new String[] {
                "CHUO", "CHUAI"
        }); // 啜 555C chuo chuai
        sPolyPhoneMap.put('\u7ef0', new String[] {
                "CHUO", "CHAO"
        }); // 绰 7EF0 chuo chao
        sPolyPhoneMap.put('\u891a', new String[] {
                "ZHU", "CHU", "ZHE"
        }); // 褚 891A zhu chu zhe
        sPolyPhoneMap.put('\u690e', new String[] {
                "ZHUI", "CHUI"
        }); // 椎 690E zhui chui
        sPolyPhoneMap.put('\u6b21', new String[] {
                "CI", "CHI", "QI"
        }); // 次 6B21 ci chi qi
        sPolyPhoneMap.put('\u4f3a', new String[] {
                "CI", "SI"
        }); // 伺 4F3A ci si
        sPolyPhoneMap.put('\u5179', new String[] {
                "ZI", "CI"
        }); // 兹 5179 zi ci
        sPolyPhoneMap.put('\u679e', new String[] {
                "CONG", "ZONG"
        }); // 枞 679E cong zong
        sPolyPhoneMap.put('\u6512', new String[] {
                "CUAN", "ZAN"
        }); // 攒 6512 cuan zan
        sPolyPhoneMap.put('\u5352', new String[] {
                "ZU", "CU"
        }); // 卒 5352 zu cu
        sPolyPhoneMap.put('\u8870', new String[] {
                "SHUAI", "CUI"
        }); // 衰 8870 shuai cui
        sPolyPhoneMap.put('\u64ae', new String[] {
                "CUO", "ZUO"
        }); // 撮 64AE cuo zuo
        sPolyPhoneMap.put('\u5927', new String[] {
                "DA", "DAI"
        }); // 大 5927 da dai
        sPolyPhoneMap.put('\u6c93', new String[] {
                "TA", "DA"
        }); // 沓 6C93 ta da
        sPolyPhoneMap.put('\u5355', new String[] {
                "DAN", "CHAN", "SHAN"
        }); // 单 5355 dan chan shan
        sPolyPhoneMap.put('\u53e8', new String[] {
                "DAO", "TAO"
        }); // 叨 53E8 dao tao
        sPolyPhoneMap.put('\u63d0', new String[] {
                "TI", "DI"
        }); // 提 63D0 ti di
        sPolyPhoneMap.put('\u9046', new String[] {
                "DI", "TI"
        }); // 遆 9046 di ti
        sPolyPhoneMap.put('\u7fdf', new String[] {
                "DI", "ZHAI"
        }); // 翟 7FDF di zhai
        sPolyPhoneMap.put('\u5f97', new String[] {
                "DE", "DEI"
        }); // 得 5F97 de dei
        sPolyPhoneMap.put('\u94bf', new String[] {
                "DIAN", "TIAN"
        }); // 钿 94BF dian tian
        sPolyPhoneMap.put('\u4f43', new String[] {
                "DIAN", "TIAN"
        }); // 佃 4F43 dian tian
        sPolyPhoneMap.put('\u5200', new String[] {
                "DAO", "DIAO"
        }); // 刀 5200 dao diao
        sPolyPhoneMap.put('\u8c03', new String[] {
                "DIAO", "TIAO"
        }); // 调 8C03 diao tiao
        sPolyPhoneMap.put('\u90fd', new String[] {
                "DOU", "DU"
        }); // 都 90FD dou du
        sPolyPhoneMap.put('\u5ea6', new String[] {
                "DU", "DUO"
        }); // 度 5EA6 du duo
        sPolyPhoneMap.put('\u56e4', new String[] {
                "TUN", "DUN"
        }); // 囤 56E4 tun dun
        sPolyPhoneMap.put('\u5426', new String[] {
                "FOU", "PI"
        }); // 否 5426 fou pi
        sPolyPhoneMap.put('\u812f', new String[] {
                "PU", "FU"
        }); // 脯 812F pu fu
        sPolyPhoneMap.put('\u8f67', new String[] {
                "YA", "ZHA", "GA"
        }); // 轧 8F67 ya zha ga
        sPolyPhoneMap.put('\u625b', new String[] {
                "KANG", "GANG"
        }); // 扛 625B kang gang
        sPolyPhoneMap.put('\u76d6', new String[] {
                "GAI", "GE"
        }); // 盖 76D6 gai ge
        sPolyPhoneMap.put('\u54af', new String[] {
                "GE", "KA", "LO"
        }); // 咯 54AF ge ka lo
        sPolyPhoneMap.put('\u9769', new String[] {
                "GE", "JI"
        }); // 革 9769 ge ji
        sPolyPhoneMap.put('\u5408', new String[] {
                "HE", "GE"
        }); // 合 5408 he ge
        sPolyPhoneMap.put('\u7ed9', new String[] {
                "GEI", "JI"
        }); // 给 7ED9 gei ji
        sPolyPhoneMap.put('\u9888', new String[] {
                "JING", "GENG"
        }); // 颈 9888 jing geng
        sPolyPhoneMap.put('\u7ea2', new String[] {
                "HONG", "GONG"
        }); // 红 7EA2 hong gong
        sPolyPhoneMap.put('\u67b8', new String[] {
                "GOU", "JU"
        }); // 枸 67B8 gou ju
        sPolyPhoneMap.put('\u5471', new String[] {
                "GUA", "GU"
        }); // 呱 5471 gua gu
        sPolyPhoneMap.put('\u8c37', new String[] {
                "GU", "YU"
        }); // 谷 8C37 gu yu
        sPolyPhoneMap.put('\u866b', new String[] {
                "CHONG", "GU"
        }); // 虫 866B chong gu
        sPolyPhoneMap.put('\u9e44', new String[] {
                "HU", "GU"
        }); // 鹄 9E44 hu gu
        sPolyPhoneMap.put('\u62ec', new String[] {
                "KUO", "GUA"
        }); // 括 62EC kuo gua
        sPolyPhoneMap.put('\u839e', new String[] {
                "GUAN", "WAN"
        }); // 莞 839E guan wan
        sPolyPhoneMap.put('\u7eb6', new String[] {
                "LUN", "GUAN"
        }); // 纶 7EB6 lun guan
        sPolyPhoneMap.put('\u7085', new String[] {
                "JIONG", "GUI"
        }); // 炅 7085 jiong gui
        sPolyPhoneMap.put('\u6867', new String[] {
                "GUI", "HUI"
        }); // 桧 6867 gui hui
        sPolyPhoneMap.put('\u7094', new String[] {
                "QUE", "GUI"
        }); // 炔 7094 que gui
        sPolyPhoneMap.put('\u660b', new String[] {
                "GUI", "JIONG"
        }); // 昋 660B gui jiong
        sPolyPhoneMap.put('\u4f1a', new String[] {
                "HUI", "KUAI", "GUI"
        }); // 会 4F1A hui kuai gui
        sPolyPhoneMap.put('\u82a5', new String[] {
                "JIE", "GAI"
        }); // 芥 82A5 jie gai
        sPolyPhoneMap.put('\u867e', new String[] {
                "XIA", "HA"
        }); // 虾 867E xia ha
        sPolyPhoneMap.put('\u8f69', new String[] {
                "XUAN", "HAN"
        }); // 轩 8F69 xuan han
        sPolyPhoneMap.put('\u6496', new String[] {
                "KAN", "HAN"
        }); // 撖 6496 kan han
        sPolyPhoneMap.put('\u54b3', new String[] {
                "KE", "HAI"
        }); // 咳 54B3 ke hai
        sPolyPhoneMap.put('\u5df7', new String[] {
                "XIANG", "HANG"
        }); // 巷 5DF7 xiang hang
        sPolyPhoneMap.put('\u542d', new String[] {
                "KENG", "HANG"
        }); // 吭 542D keng hang
        sPolyPhoneMap.put('\u9ed8', new String[] {
                "MO", "HE", "MEI"
        }); // 默 9ED8 mo he mei
        sPolyPhoneMap.put('\u548c', new String[] {
                "HE", "HU", "HUO"
        }); // 和 548C he hu huo
        sPolyPhoneMap.put('\u8c89', new String[] {
                "HE", "HAO"
        }); // 貉 8C89 he hao
        sPolyPhoneMap.put('\u9ed1', new String[] {
                "HEI", "HE"
        }); // 黑 9ED1 hei he
        sPolyPhoneMap.put('\u8679', new String[] {
                "HONG", "JIANG"
        }); // 虹 8679 hong jiang
        sPolyPhoneMap.put('\u90c7', new String[] {
                "XUN", "HUAN"
        }); // 郇 90C7 xun huan
        sPolyPhoneMap.put('\u5bf0', new String[] {
                "HUAN", "XIAN"
        }); // 寰 5BF0 huan xian
        sPolyPhoneMap.put('\u5947', new String[] {
                "QI", "JI"
        }); // 奇 5947 qi ji
        sPolyPhoneMap.put('\u7f09', new String[] {
                "JI", "QI"
        }); // 缉 7F09 ji qi
        sPolyPhoneMap.put('\u5048', new String[] {
                "JIE", "JI"
        }); // 偈 5048 jie ji
        sPolyPhoneMap.put('\u7cfb', new String[] {
                "XI", "JI"
        }); // 系 7CFB xi ji
        sPolyPhoneMap.put('\u7a3d', new String[] {
                "JI", "QI"
        }); // 稽 7A3D ji qi
        sPolyPhoneMap.put('\u4e9f', new String[] {
                "JI", "QI"
        }); // 亟 4E9F ji qi
        sPolyPhoneMap.put('\u8bd8', new String[] {
                "JIE", "JI"
        }); // 诘 8BD8 jie ji
        sPolyPhoneMap.put('\u8bb0', new String[] {
                "JI", "JIE"
        }); // 记 8BB0 ji jie
        sPolyPhoneMap.put('\u5267', new String[] {
                "JU", "JI"
        }); // 剧 5267 ju ji
        sPolyPhoneMap.put('\u796d', new String[] {
                "JI", "ZHA", "ZHAI"
        }); // 祭 796D ji zha zhai
        sPolyPhoneMap.put('\u8304', new String[] {
                "QIE", "JIA"
        }); // 茄 8304 qie jia
        sPolyPhoneMap.put('\u56bc', new String[] {
                "JIAO", "JUE"
        }); // 嚼 56BC jiao jue
        sPolyPhoneMap.put('\u4fa5', new String[] {
                "JIAO", "YAO"
        }); // 侥 4FA5 jiao yao
        sPolyPhoneMap.put('\u89d2', new String[] {
                "JIAO", "JUE"
        }); // 角 89D2 jiao jue
        sPolyPhoneMap.put('\u811a', new String[] {
                "JIAO", "JUE"
        }); // 脚 811A jiao jue
        sPolyPhoneMap.put('\u527f', new String[] {
                "JIAO", "CHAO"
        }); // 剿 527F jiao chao
        sPolyPhoneMap.put('\u6821', new String[] {
                "XIAO", "JIAO"
        }); // 校 6821 xiao jiao
        sPolyPhoneMap.put('\u7f34', new String[] {
                "JIAO", "ZHUO"
        }); // 缴 7F34 jiao zhuo
        sPolyPhoneMap.put('\u89c1', new String[] {
                "JIAN", "XIAN"
        }); // 见 89C1 jian xian
        sPolyPhoneMap.put('\u964d', new String[] {
                "XIANG", "LONG", "JIANG"
        }); // 降 964D xiang long jiang
        sPolyPhoneMap.put('\u89e3', new String[] {
                "JIE", "XIE"
        }); // 解 89E3 jie xie
        sPolyPhoneMap.put('\u85c9', new String[] {
                "JIE", "JI"
        }); // 藉 85C9 jie ji
        sPolyPhoneMap.put('\u77dc', new String[] {
                "JIN", "QIN"
        }); // 矜 77DC jin qin
        sPolyPhoneMap.put('\u52b2', new String[] {
                "JIN", "JING"
        }); // 劲 52B2 jin jing
        sPolyPhoneMap.put('\u9f9f', new String[] {
                "GUI", "QIU", "CI", "JUN"
        }); // 龟 9F9F gui qiu ci jun
        sPolyPhoneMap.put('\u5480', new String[] {
                "JU", "ZUI"
        }); // 咀 5480 ju zui
        sPolyPhoneMap.put('\u741a', new String[] {
                "JU", "QU"
        }); // 琚 741A ju qu
        sPolyPhoneMap.put('\u83cc', new String[] {
                "JUN", "XUN"
        }); // 菌 83CC jun xun
        sPolyPhoneMap.put('\u96bd', new String[] {
                "JUN", "JUAN"
        }); // 隽 96BD jun juan
        sPolyPhoneMap.put('\u5361', new String[] {
                "KA", "QIA"
        }); // 卡 5361 ka qia
        sPolyPhoneMap.put('\u770b', new String[] {
                "KAN", "KAN"
        }); // 看 770B kan kan
        sPolyPhoneMap.put('\u61a8', new String[] {
                "HAN", "KAN"
        }); // 憨 61A8 han kan
        sPolyPhoneMap.put('\u5777', new String[] {
                "KE", "KE"
        }); // 坷 5777 ke ke
        sPolyPhoneMap.put('\u58f3', new String[] {
                "KE", "QIA"
        }); // 壳 58F3 ke qia
        sPolyPhoneMap.put('\u514b', new String[] {
                "KE", "KEI"
        }); // 克 514B ke kei
        sPolyPhoneMap.put('\u9760', new String[] {
                "KAO", "KU"
        }); // 靠 9760 kao ku
        sPolyPhoneMap.put('\u9697', new String[] {
                "WEI", "KUI"
        }); // 隗 9697 wei kui
        sPolyPhoneMap.put('\u9b3c', new String[] {
                "GUI", "WEI", "KUI"
        }); // 鬼 9B3C gui wei kui
        sPolyPhoneMap.put('\u8312', new String[] {
                "KUANG", "GUAN", "YUAN"
        }); // 茒 8312 kuang guan yuan
        sPolyPhoneMap.put('\u5587', new String[] {
                "LA", "YAO"
        }); // 喇 5587 la yao
        sPolyPhoneMap.put('\u84dd', new String[] {
                "LAN", "PIE"
        }); // 蓝 84DD lan pie
        sPolyPhoneMap.put('\u70d9', new String[] {
                "LAO", "LUO", "PAO"
        }); // 烙 70D9 lao luo pao
        sPolyPhoneMap.put('\u96d2', new String[] {
                "LUO", "LAO"
        }); // 雒 96D2 luo lao
        sPolyPhoneMap.put('\u808b', new String[] {
                "LE", "LEI"
        }); // 肋 808B le lei
        sPolyPhoneMap.put('\u4e50', new String[] {
                "LE", "YUE"
        }); // 乐 4E50 le yue
        sPolyPhoneMap.put('\u4e86', new String[] {
                "LE", "LIAO"
        }); // 了 4E86 le liao
        sPolyPhoneMap.put('\u4fe9', new String[] {
                "LIANG", "LIA"
        }); // 俩 4FE9 liang lia
        sPolyPhoneMap.put('\u6f66', new String[] {
                "LIAO", "LAO"
        }); // 潦 6F66 liao lao
        sPolyPhoneMap.put('\u788c', new String[] {
                "LU", "ZHOU", "LIU"
        }); // 碌 788C lu zhou liu
        sPolyPhoneMap.put('\u507b', new String[] {
                "LOU", "LU"
        }); // 偻 507B lou lu
        sPolyPhoneMap.put('\u9732', new String[] {
                "LU", "LOU"
        }); // 露 9732 lu lou
        sPolyPhoneMap.put('\u634b', new String[] {
                "LU", "LUO"
        }); // 捋 634B lu luo
        sPolyPhoneMap.put('\u7eff', new String[] {
                "LV", "LU"
        }); // 绿 7EFF lv lu
        sPolyPhoneMap.put('\u516d', new String[] {
                "LIU", "LU"
        }); // 六 516D liu lu
        sPolyPhoneMap.put('\u7edc', new String[] {
                "LUO", "LAO"
        }); // 络 7EDC luo lao
        sPolyPhoneMap.put('\u843d', new String[] {
                "LUO", "LAO", "LA"
        }); // 落 843D luo lao la
        sPolyPhoneMap.put('\u62b9', new String[] {
                "MA", "MO"
        }); // 抹 62B9 ma mo
        sPolyPhoneMap.put('\u8109', new String[] {
                "MAI", "MO"
        }); // 脉 8109 mai mo
        sPolyPhoneMap.put('\u57cb', new String[] {
                "MAI", "MAN"
        }); // 埋 57CB mai man
        sPolyPhoneMap.put('\u8513', new String[] {
                "MAN", "WAN"
        }); // 蔓 8513 man wan
        sPolyPhoneMap.put('\u6c13', new String[] {
                "MANG", "MENG"
        }); // 氓 6C13 mang meng
        sPolyPhoneMap.put('\u6ca1', new String[] {
                "MEI", "MO"
        }); // 没 6CA1 mei mo
        sPolyPhoneMap.put('\u79d8', new String[] {
                "MI", "BI"
        }); // 秘 79D8 mi bi
        sPolyPhoneMap.put('\u6ccc', new String[] {
                "MI", "BI"
        }); // 泌 6CCC mi bi
        sPolyPhoneMap.put('\u4f74', new String[] {
                "MI", "NAI", "NI"
        }); // 佴 4F74 mi nai ni
        sPolyPhoneMap.put('\u8c2c', new String[] {
                "MIAO", "MIU"
        }); // 谬 8C2C miao miu
        sPolyPhoneMap.put('\u6a21', new String[] {
                "MO", "MU"
        }); // 模 6A21 mo mu
        sPolyPhoneMap.put('\u6469', new String[] {
                "MO", "MA", "SA"
        }); // 摩 6469 mo ma sa
        sPolyPhoneMap.put('\u6bcd', new String[] {
                "MU", "WU"
        }); // 母 6BCD mu wu
        sPolyPhoneMap.put('\u7f2a', new String[] {
                "MIU", "MIAO", "MOU"
        }); // 缪 7F2A miu miao mou
        sPolyPhoneMap.put('\u5f04', new String[] {
                "NONG", "LONG"
        }); // 弄 5F04 nong long
        sPolyPhoneMap.put('\u96be', new String[] {
                "NAN", "NING"
        }); // 难 96BE nan ning
        sPolyPhoneMap.put('\u759f', new String[] {
                "NUE", "YAO"
        }); // 疟 759F nue yao
        sPolyPhoneMap.put('\u4e5c', new String[] {
                "MIE", "NIE"
        }); // 乜 4E5C mie nie
        sPolyPhoneMap.put('\u5a1c', new String[] {
                "NA", "NUO"
        }); // 娜 5A1C na nuo
        sPolyPhoneMap.put('\u533a', new String[] {
                "QU", "OU"
        }); // 区 533A qu ou
        sPolyPhoneMap.put('\u7e41', new String[] {
                "FAN", "PO"
        }); // 繁 7E41 fan po
        sPolyPhoneMap.put('\u8feb', new String[] {
                "PO", "PAI"
        }); // 迫 8FEB po pai
        sPolyPhoneMap.put('\u80d6', new String[] {
                "PANG", "PAN"
        }); // 胖 80D6 pang pan
        sPolyPhoneMap.put('\u5228', new String[] {
                "PAO", "BAO"
        }); // 刨 5228 pao bao
        sPolyPhoneMap.put('\u70ae', new String[] {
                "PAO", "BAO"
        }); // 炮 70AE pao bao
        sPolyPhoneMap.put('\u9022', new String[] {
                "FENG", "PANG"
        }); // 逢 9022 feng pang
        sPolyPhoneMap.put('\u84ec', new String[] {
                "PENG", "PANG"
        }); // 蓬 84EC peng pang
        sPolyPhoneMap.put('\u6734', new String[] {
                "PU", "PO", "PIAO"
        }); // 朴 6734 pu po piao
        sPolyPhoneMap.put('\u7011', new String[] {
                "PU", "BAO"
        }); // 瀑 7011 pu bao
        sPolyPhoneMap.put('\u66dd', new String[] {
                "BAO", "PU"
        }); // 曝 66DD bao pu
        sPolyPhoneMap.put('\u6816', new String[] {
                "XI", "QI"
        }); // 栖 6816 xi qi
        sPolyPhoneMap.put('\u8e4a', new String[] {
                "XI", "QI"
        }); // 蹊 8E4A xi qi
        sPolyPhoneMap.put('\u7a3d', new String[] {
                "JI", "QI"
        }); // 稽 7A3D ji qi
        sPolyPhoneMap.put('\u8368', new String[] {
                "XUN", "QIAN"
        }); // 荨 8368 xun qian
        sPolyPhoneMap.put('\u79a4', new String[] {
                "QIAN", "XUAN"
        }); // 禤 79A4 qian xuan
        sPolyPhoneMap.put('\u5f3a', new String[] {
                "QIANG", "JIANG"
        }); // 强 5F3A qiang jiang
        sPolyPhoneMap.put('\u8d84', new String[] {
                "QIE", "JU"
        }); // 趄 8D84 qie ju
        sPolyPhoneMap.put('\u4eb2', new String[] {
                "QIN", "QING"
        }); // 亲 4EB2 qin qing
        sPolyPhoneMap.put('\u96c0', new String[] {
                "QUE", "QIAO"
        }); // 雀 96C0 que qiao
        sPolyPhoneMap.put('\u4ec7', new String[] {
                "CHOU", "QIU"
        }); // 仇 4EC7 chou qiu
        sPolyPhoneMap.put('\u5708', new String[] {
                "QUAN", "JUAN"
        }); // 圈 5708 quan juan
        sPolyPhoneMap.put('\u8272', new String[] {
                "SE", "SHAI"
        }); // 色 8272 se shai
        sPolyPhoneMap.put('\u585e', new String[] {
                "SAI", "SE"
        }); // 塞 585E sai se
        sPolyPhoneMap.put('\u53a6', new String[] {
                "XIA", "SHA"
        }); // 厦 53A6 xia sha
        sPolyPhoneMap.put('\u53ec', new String[] {
                "ZHAO", "SHAO"
        }); // 召 53EC zhao shao
        sPolyPhoneMap.put('\u6749', new String[] {
                "SHAN", "SHA"
        }); // 杉 6749 shan sha
        sPolyPhoneMap.put('\u6c64', new String[] {
                "TANG", "SHANG"
        }); // 汤 6C64 tang shang
        sPolyPhoneMap.put('\u62fe', new String[] {
                "SHI", "SHE"
        }); // 拾 62FE shi she
        sPolyPhoneMap.put('\u6298', new String[] {
                "ZHE", "SHE"
        }); // 折 6298 zhe she
        sPolyPhoneMap.put('\u4ec0', new String[] {
                "SHEN", "SHI"
        }); // 什 4EC0 shen shi
        sPolyPhoneMap.put('\u845a', new String[] {
                "SHEN", "REN"
        }); // 葚 845A shen ren
        sPolyPhoneMap.put('\u8bc6', new String[] {
                "SHI", "ZHI"
        }); // 识 8BC6 shi zhi
        sPolyPhoneMap.put('\u4f3c', new String[] {
                "SI", "SHI"
        }); // 似 4F3C si shi
        sPolyPhoneMap.put('\u5c5e', new String[] {
                "SHU", "ZHU"
        }); // 属 5C5E shu zhu
        sPolyPhoneMap.put('\u719f', new String[] {
                "SHU", "SHOU"
        }); // 熟 719F shu shou
        sPolyPhoneMap.put('\u8bf4', new String[] {
                "SHUO", "SHUI"
        }); // 说 8BF4 shuo shui
        sPolyPhoneMap.put('\u6570', new String[] {
                "SHU", "SHUO"
        }); // 数 6570 shu shuo
        sPolyPhoneMap.put('\u5fea', new String[] {
                "SONG", "ZHONG"
        }); // 忪 5FEA song zhong
        sPolyPhoneMap.put('\u5bbf', new String[] {
                "SU", "XIU"
        }); // 宿 5BBF su xiu
        sPolyPhoneMap.put('\u772d', new String[] {
                "GUI", "XU", "SUI"
        }); // 眭 772D gui xu sui
        sPolyPhoneMap.put('\u6fb9', new String[] {
                "DAN", "TAN"
        }); // 澹 6FB9 dan tan
        sPolyPhoneMap.put('\u6c93', new String[] {
                "TA", "DA"
        }); // 沓 6C93 ta da
        sPolyPhoneMap.put('\u8983', new String[] {
                "TAN", "QIN"
        }); // 覃 8983 tan qin
        sPolyPhoneMap.put('\u8c03', new String[] {
                "DIAO", "TIAO"
        }); // 调 8C03 diao tiao
        sPolyPhoneMap.put('\u892a', new String[] {
                "TUI", "TUN"
        }); // 褪 892A tui tun
        sPolyPhoneMap.put('\u62d3', new String[] {
                "TUO", "TA"
        }); // 拓 62D3 tuo ta
        sPolyPhoneMap.put('\u5729', new String[] {
                "WEI", "XU"
        }); // 圩 5729 wei xu
        sPolyPhoneMap.put('\u59d4', new String[] {
                "WEI", "QU"
        }); // 委 59D4 wei qu
        sPolyPhoneMap.put('\u5c3e', new String[] {
                "WEI", "YI"
        }); // 尾 5C3E wei yi
        sPolyPhoneMap.put('\u5c09', new String[] {
                "WEI", "YU"
        }); // 尉 5C09 wei yu
        sPolyPhoneMap.put('\u9057', new String[] {
                "YI", "WEI"
        }); // 遗 9057 yi wei
        sPolyPhoneMap.put('\u4e4c', new String[] {
                "WU", "LA"
        }); // 乌 4E4C wu la
        sPolyPhoneMap.put('\u5413', new String[] {
                "XIA", "HE"
        }); // 吓 5413 xia he
        sPolyPhoneMap.put('\u7ea4', new String[] {
                "XIAN", "QIAN"
        }); // 纤 7EA4 xian qian
        sPolyPhoneMap.put('\u884c', new String[] {
                "XING", "HANG", "HENG"
        }); // 行 884C xing hang heng
        sPolyPhoneMap.put('\u7701', new String[] {
                "SHENG", "XING"
        }); // 省 7701 sheng xing
        sPolyPhoneMap.put('\u524a', new String[] {
                "XIAO", "XUE"
        }); // 削 524A xiao xue
        sPolyPhoneMap.put('\u8840', new String[] {
                "XUE", "XIE"
        }); // 血 8840 xue xie
        sPolyPhoneMap.put('\u6bb7', new String[] {
                "YIN", "YAN"
        }); // 殷 6BB7 yin yan
        sPolyPhoneMap.put('\u54bd', new String[] {
                "YAN", "YE"
        }); // 咽 54BD yan ye
        sPolyPhoneMap.put('\u7ea6', new String[] {
                "YUE", "YAO"
        }); // 约 7EA6 yue yao
        sPolyPhoneMap.put('\u94a5', new String[] {
                "YAO", "YUE"
        }); // 钥 94A5 yao yue
        sPolyPhoneMap.put('\u53f6', new String[] {
                "YE", "XIE"
        }); // 叶 53F6 ye xie
        sPolyPhoneMap.put('\u827e', new String[] {
                "AI", "YI"
        }); // 艾 827E ai yi
        sPolyPhoneMap.put('\u71a8', new String[] {
                "YUN", "YU"
        }); // 熨 71A8 yun yu
        sPolyPhoneMap.put('\u5401', new String[] {
                "YU", "XU"
        }); // 吁 5401 yu xu
        sPolyPhoneMap.put('\u5458', new String[] {
                "YUAN", "YUN"
        }); // 员 5458 yuan yun
        sPolyPhoneMap.put('\u8d20', new String[] {
                "YUAN", "YUN"
        }); // 贠 8D20 yuan yun
        sPolyPhoneMap.put('\u548b', new String[] {
                "ZA", "ZE", "ZHA"
        }); // 咋 548B za ze zha
        sPolyPhoneMap.put('\u62e9', new String[] {
                "ZE", "ZHAI"
        }); // 择 62E9 ze zhai
        sPolyPhoneMap.put('\u624e', new String[] {
                "ZHA", "ZA"
        }); // 扎 624E zha za
        sPolyPhoneMap.put('\u8f67', new String[] {
                "YA", "ZHA"
        }); // 轧 8F67 ya zha
        sPolyPhoneMap.put('\u7c98', new String[] {
                "NIAN", "ZHAN"
        }); // 粘 7C98 nian zhan
        sPolyPhoneMap.put('\u722a', new String[] {
                "ZHUA", "ZHAO"
        }); // 爪 722A zhua zhao
        sPolyPhoneMap.put('\u7740', new String[] {
                "ZHAO", "ZHUO"
        }); // 着 7740 zhao zhuo
        sPolyPhoneMap.put('\u6b96', new String[] {
                "ZHI", "SHI"
        }); // 殖 6B96 zhi shi
        sPolyPhoneMap.put('\u8457', new String[] {
                "ZHU", "ZHE", "ZHUO"
        }); // 著 8457 zhu zhe zhuo
        sPolyPhoneMap.put('\u5e62', new String[] {
                "ZHUANG", "CHUANG"
        }); // 幢 5E62 zhuang chuang
        sPolyPhoneMap.put('\u7efc', new String[] {
                "ZONG", "ZENG"
        }); // 综 7EFC zong zeng
        sPolyPhoneMap.put('\u67de', new String[] {
                "ZUO", "ZHA"
        }); // 柞 67DE zuo zha
        sPolyPhoneMap.put('\u4ed4', new String[] {
                "ZI", "ZAI"
        }); // 仔 4ED4 zi zai

        // Below is some exceptions.
        sPolyPhoneMap.put('\u5185', new String[] {
                "NEI"
        }); // 内
        sPolyPhoneMap.put('\u77BF', new String[] {
                "QU"
        }); // 瞿
        sPolyPhoneMap.put('\u6765', new String[] {
                "LAI"
        }); // 来
        sPolyPhoneMap.put('\u53c9', new String[] {
                "CHA"
        }); // 叉
        sPolyPhoneMap.put('\u5979', new String[] {
                "TA"
        }); // 她
        sPolyPhoneMap.put('\u513f', new String[] {
                "ER"
        }); // 儿
        sPolyPhoneMap.put('\u6c88', new String[] {
                "SHEN"
        }); // 沈
        sPolyPhoneMap.put('\u8d3e', new String[] {
                "JIA"
        }); // 贾

        // 以下是为了解决有些拼音搜不到的问题，比如wu
        sPolyPhoneMap.put('\u6b66', new String[] {
                "WU"
        }); // 武
        sPolyPhoneMap.put('\u5434', new String[] {
                "WU"
        }); // 吴
        sPolyPhoneMap.put('\u4e4c', new String[] {
                "WU"
        }); // 乌
        sPolyPhoneMap.put('\u4f0d', new String[] {
                "WU"
        }); // 伍
        sPolyPhoneMap.put('\u5348', new String[] {
                "WU"
        }); // 午
        sPolyPhoneMap.put('\u5deb', new String[] {
                "WU"
        }); // 巫
    }

    static {
        sHyphenatedNamePolyPhoneMap.put("\u5355\u4e8e", new String[] {
                "CHAN", "YU"
        }); // 单于
        sHyphenatedNamePolyPhoneMap.put("\u957f\u5b59", new String[] {
                "ZHANG", "SUN"
        }); // 长孙
        sHyphenatedNamePolyPhoneMap.put("\u5b50\u8f66", new String[] {
                "ZI", "JU"
        }); // 子车
        sHyphenatedNamePolyPhoneMap.put("\u4e07\u4fdf", new String[] {
                "MO", "QI"
        }); // 万俟
        sHyphenatedNamePolyPhoneMap.put("\u6fb9\u53f0", new String[] {
                "TAN", "TAI"
        }); // 澹台
        sHyphenatedNamePolyPhoneMap.put("\u5c09\u8fdf", new String[] {
                "YU", "CHI"
        }); // 尉迟
    }

    static {
        sLastNamePolyPhoneMap.put('\u8d3e', "JIA"); // 贾 8d3e jia
        sLastNamePolyPhoneMap.put('\u6c88', "SHEN"); // 沈 6c88 shen
        sLastNamePolyPhoneMap.put('\u535c', "BU"); // 卜 535C bu
        sLastNamePolyPhoneMap.put('\u8584', "BO"); // 薄 8584 bo
        sLastNamePolyPhoneMap.put('\u5b5b', "BO"); // 孛 5B5B bo
        sLastNamePolyPhoneMap.put('\u8d32', "BEN"); // 贲 8D32 ben
        sLastNamePolyPhoneMap.put('\u8d39', "FEI"); // 费 8D39 fei
        sLastNamePolyPhoneMap.put('\u6cca', "BAN"); // 泊 6CCA ban
        sLastNamePolyPhoneMap.put('\u8300', "BI"); // 茀 8300 bi
        sLastNamePolyPhoneMap.put('\u5e9f', "BO"); // 废 5E9F bo
        sLastNamePolyPhoneMap.put('\u756a', "BO"); // 番 756A bo
        sLastNamePolyPhoneMap.put('\u891a', "CHU"); // 褚 891A chu
        sLastNamePolyPhoneMap.put('\u91cd', "CHONG"); // 重 91CD chong
        sLastNamePolyPhoneMap.put('\u5382', "HAN"); // 厂 5382 han
        sLastNamePolyPhoneMap.put('\u4f20', "CHUAN"); // 传 4F20 chuan
        sLastNamePolyPhoneMap.put('\u53c2', "CAN"); // 参 53C2 can
        sLastNamePolyPhoneMap.put('\u79cd', "CHONG"); // 种 79CD chong
        sLastNamePolyPhoneMap.put('\u90d7', "CHI"); // 郗 90D7 chi
        sLastNamePolyPhoneMap.put('\u9561', "CHAN"); // 镡 9561 chan
        sLastNamePolyPhoneMap.put('\u671d', "CHAO"); // 朝 671D chao
        sLastNamePolyPhoneMap.put('\u6cbb', "CHI"); // 治 6CBB chi
        sLastNamePolyPhoneMap.put('\u555c', "CHUAI"); // 啜 555C chuai
        sLastNamePolyPhoneMap.put('\u8870', "CUI"); // 衰 8870 cui
        sLastNamePolyPhoneMap.put('\u6668', "CHANG"); // 晨 6668 chang
        sLastNamePolyPhoneMap.put('\u4e11', "CHOU"); // 丑 4E11 chou
        sLastNamePolyPhoneMap.put('\u7633', "CHOU"); // 瘳 7633 chou
        sLastNamePolyPhoneMap.put('\u957f', "CHANG"); // 长 957F chang
        sLastNamePolyPhoneMap.put('\u6b21', "QI"); // 次 6B21 qi
        sLastNamePolyPhoneMap.put('\u8f66', "CHE"); // 车 8F66 che
        sLastNamePolyPhoneMap.put('\u7fdf', "ZHAI"); // 翟 7FDF zhai
        sLastNamePolyPhoneMap.put('\u4f43', "DIAN"); // 佃 4F43 dian
        sLastNamePolyPhoneMap.put('\u5200', "DIAO"); // 刀 5200 diao
        sLastNamePolyPhoneMap.put('\u8c03', "DIAO"); // 调 8C03 diao
        sLastNamePolyPhoneMap.put('\u9046', "DI"); // 遆 9046 di
        sLastNamePolyPhoneMap.put('\u76d6', "GE"); // 盖 76D6 ge
        sLastNamePolyPhoneMap.put('\u7085', "GUI"); // 炅 7085 gui
        sLastNamePolyPhoneMap.put('\u866b', "GU"); // 虫 866B gu
        sLastNamePolyPhoneMap.put('\u7094', "GUI"); // 炔 7094 gui
        sLastNamePolyPhoneMap.put('\u660b', "GUI"); // 昋 660B gui
        sLastNamePolyPhoneMap.put('\u4f1a', "GUI"); // 会 4F1A gui
        sLastNamePolyPhoneMap.put('\u82a5', "GAI"); // 芥 82A5 gai
        sLastNamePolyPhoneMap.put('\u8312', "KUANG"); // 茒 8312 kuang
        sLastNamePolyPhoneMap.put('\u90c7', "HUAN"); // 郇 90C7 huan
        sLastNamePolyPhoneMap.put('\u5df7', "XIANG"); // 巷 5DF7 xiang
        sLastNamePolyPhoneMap.put('\u9ed1', "HE"); // 黑 9ED1 he
        sLastNamePolyPhoneMap.put('\u8f69', "HAN"); // 轩 8F69 han
        sLastNamePolyPhoneMap.put('\u6496', "HAN"); // 撖 6496 han
        sLastNamePolyPhoneMap.put('\u9ed8', "HE"); // 默 9ED8 he
        sLastNamePolyPhoneMap.put('\u89c1', "JIAN"); // 见 89C1 jian
        sLastNamePolyPhoneMap.put('\u964d', "JIANG"); // 降 964D jiang
        sLastNamePolyPhoneMap.put('\u89d2', "JIAO"); // 角 89D2 jiao
        sLastNamePolyPhoneMap.put('\u7f34', "JIAO"); // 缴 7F34 jiao
        sLastNamePolyPhoneMap.put('\u8bb0', "JI"); // 记 8BB0 ji
        sLastNamePolyPhoneMap.put('\u741a', "JU"); // 琚 741A ju
        sLastNamePolyPhoneMap.put('\u5267', "JI"); // 剧 5267 ji
        sLastNamePolyPhoneMap.put('\u96bd', "JUAN"); // 隽 96BD juan
        sLastNamePolyPhoneMap.put('\u9697', "KUI"); // 隗 9697 kui
        sLastNamePolyPhoneMap.put('\u9b3c', "KUI"); // 鬼 9B3C kui
        sLastNamePolyPhoneMap.put('\u61a8', "KAN"); // 憨 61A8 kan
        sLastNamePolyPhoneMap.put('\u9760', "KU"); // 靠 9760 ku
        sLastNamePolyPhoneMap.put('\u4e50', "YUE"); // 乐 4E50 yue
        sLastNamePolyPhoneMap.put('\u516d', "LU"); // 六 516D lu
        sLastNamePolyPhoneMap.put('\u5587', "LA"); // 喇 5587 la
        sLastNamePolyPhoneMap.put('\u96d2', "LUO"); // 雒 96D2 luo
        sLastNamePolyPhoneMap.put('\u4e86', "LIAO"); // 了 4E86 liao
        sLastNamePolyPhoneMap.put('\u7f2a', "MIAO"); // 缪 7F2A miao
        sLastNamePolyPhoneMap.put('\u4f74', "MI"); // 佴 4F74 mi
        sLastNamePolyPhoneMap.put('\u8c2c', "MIAO"); // 谬 8C2C miao
        sLastNamePolyPhoneMap.put('\u4e5c', "NIE"); // 乜 4E5C nie
        sLastNamePolyPhoneMap.put('\u96be', "NING"); // 难 96BE ning
        sLastNamePolyPhoneMap.put('\u533a', "OU"); // 区 533A ou
        sLastNamePolyPhoneMap.put('\u9022', "PANG"); // 逢 9022 pang
        sLastNamePolyPhoneMap.put('\u84ec', "PENG"); // 蓬 84EC peng
        sLastNamePolyPhoneMap.put('\u6734', "PIAO"); // 朴 6734 piao
        sLastNamePolyPhoneMap.put('\u7e41', "PO"); // 繁 7E41 po
        sLastNamePolyPhoneMap.put('\u4fbf', "PIAN"); // 便 4FBF pian
        sLastNamePolyPhoneMap.put('\u4ec7', "QIU"); // 仇 4EC7 qiu
        sLastNamePolyPhoneMap.put('\u5361', "QIA"); // 卡 5361 qia
        sLastNamePolyPhoneMap.put('\u8983', "TAN"); // 覃 8983 tan
        sLastNamePolyPhoneMap.put('\u79a4', "QIAN"); // 禤 79A4 qian
        sLastNamePolyPhoneMap.put('\u53ec', "SHAO"); // 召 53EC shao
        sLastNamePolyPhoneMap.put('\u4ec0', "SHI"); // 什 4EC0 shi
        sLastNamePolyPhoneMap.put('\u6298', "SHE"); // 折 6298 she
        sLastNamePolyPhoneMap.put('\u772d', "SUI"); // 眭 772D sui
        sLastNamePolyPhoneMap.put('\u89e3', "XIE"); // 解 89E3 xie
        sLastNamePolyPhoneMap.put('\u7cfb', "XI"); // 系 7CFB xi
        sLastNamePolyPhoneMap.put('\u5df7', "XIANG"); // 巷 5DF7 xiang
        sLastNamePolyPhoneMap.put('\u9664', "XU"); // 除 9664 xu
        sLastNamePolyPhoneMap.put('\u5bf0', "XIAN"); // 寰 5BF0 xian
        sLastNamePolyPhoneMap.put('\u5458', "YUAN"); // 员 5458 yuan
        sLastNamePolyPhoneMap.put('\u8d20', "YUAN"); // 贠 8D20 yuan
        sLastNamePolyPhoneMap.put('\u66fe', "ZENG"); // 曾 66FE zeng
        sLastNamePolyPhoneMap.put('\u67e5', "ZHA"); // 查 67E5 zha
        sLastNamePolyPhoneMap.put('\u4f20', "CHUAN"); // 传 4F20 chuan
        sLastNamePolyPhoneMap.put('\u53ec', "SHAO"); // 召 53EC shao
        sLastNamePolyPhoneMap.put('\u796d', "ZHAI"); // 祭 796D zhai
    }

    protected HanziToPinyinICS(boolean hasChinaCollator) {
        mHasChinaCollator = hasChinaCollator;
    }

    public static HanziToPinyinICS getInstance() {
        synchronized (HanziToPinyinICS.class) {
            if (sInstance != null) {
                return sInstance;
            }
            // Check if zh_CN collation data is available
            final Locale locale[] = Collator.getAvailableLocales();
            for (int i = 0; i < locale.length; i++) {
                if (locale[i].equals(Locale.CHINESE)) {
                    sInstance = new HanziToPinyinICS(true);
                    return sInstance;
                }
            }
            Log.w(TAG, "There is no Chinese collator, HanziToPinyin is disabled");
            sInstance = new HanziToPinyinICS(false);
            return sInstance;
        }
    }

    private HanziToPinyin.Token getToken(char character) {
        HanziToPinyin.Token token = new HanziToPinyin.Token();
        String[] polyPhones;
        final String letter = Character.toString(character);
        token.source = letter;
        int offset = -1;
        int cmp;
        if (character < 256) {
            token.type = HanziToPinyin.Token.LATIN;
            token.target = letter;
            return token;
        } else if (character < FIRST_UNIHAN) {
            token.type = HanziToPinyin.Token.UNKNOWN;
            token.target = letter;
            return token;
        } else if ((polyPhones = sPolyPhoneMap.get(character)) != null) {
            token.type = HanziToPinyin.Token.PINYIN;
            token.polyPhones = polyPhones;
            token.target = token.polyPhones[0];
            return token;
        } else {
            cmp = COLLATOR.compare(letter, FIRST_PINYIN_UNIHAN);
            if (cmp < 0) {
                token.type = HanziToPinyin.Token.UNKNOWN;
                token.target = letter;
                return token;
            } else if (cmp == 0) {
                token.type = HanziToPinyin.Token.PINYIN;
                offset = 0;
            } else {
                cmp = COLLATOR.compare(letter, LAST_PINYIN_UNIHAN);
                if (cmp > 0) {
                    token.type = HanziToPinyin.Token.UNKNOWN;
                    token.target = letter;
                    return token;
                } else if (cmp == 0) {
                    token.type = HanziToPinyin.Token.PINYIN;
                    offset = UNIHANS.length - 1;
                }
            }
        }

        token.type = HanziToPinyin.Token.PINYIN;
        if (offset < 0) {
            int begin = 0;
            int end = UNIHANS.length - 1;
            while (begin <= end) {
                offset = (begin + end) / 2;
                final String unihan = Character.toString(UNIHANS[offset]);
                cmp = COLLATOR.compare(letter, unihan);
                if (cmp == 0) {
                    break;
                } else if (cmp > 0) {
                    begin = offset + 1;
                } else {
                    end = offset - 1;
                }
            }
        }
        if (cmp < 0) {
            offset--;
        }
        StringBuilder pinyin = new StringBuilder();
        for (int j = 0; (j < PINYINS[offset].length) && (PINYINS[offset][j] != 0); j++) {
            pinyin.append((char) PINYINS[offset][j]);
        }
        token.target = pinyin.toString();
        return token;
    }

    private ArrayList<HanziToPinyin.Token> getPolyPhoneLastNameTokens(final String name) {

        if (TextUtils.isEmpty(name)) {
            return null;
        }

        ArrayList<HanziToPinyin.Token> tokens = new ArrayList<HanziToPinyin.Token>();

        // 如果是复姓
        if (name.length() >= 2) {
            final String hyphenatedName = name.substring(0, 2);
            String[] polyPhones = sHyphenatedNamePolyPhoneMap.get(hyphenatedName);
            if (polyPhones != null) {
                for (int i = 0; i < polyPhones.length; i++) {
                    HanziToPinyin.Token token = new HanziToPinyin.Token();
                    token.type = HanziToPinyin.Token.PINYIN;
                    token.source = String.valueOf(hyphenatedName.charAt(i));
                    token.target = polyPhones[i];
                    tokens.add(token);
                }

                return tokens;
            }
        }

        final Character lastName = name.charAt(0);
        String polyPhone = sLastNamePolyPhoneMap.get(lastName);
        if (polyPhone != null) {
            HanziToPinyin.Token token = new HanziToPinyin.Token();
            token.type = HanziToPinyin.Token.PINYIN;
            token.source = lastName.toString();
            token.target = polyPhone;
            tokens.add(token);
            return tokens;
        }

        return null;
    }

    /**
     * Convert the input to a array of tokens. The sequence of ASCII or Unknown characters without space will be put
     * into a Token, One Hanzi character which has pinyin will be treated as a Token. If these is no China collator, the
     * empty token array is returned.
     */
    public ArrayList<HanziToPinyin.Token> get(final String input) {
        return get(input, true, true);
    }

    /**
     * Convert the input to a array of tokens. The sequence of ASCII or Unknown characters without space will be put
     * into a Token, One Hanzi character which has pinyin will be treated as a Token. If these is no China collator, the
     * empty token array is returned.
     * 
     * @param filterInvalidChar set True to filter out white space those invalid chars. set False to keep them.
     * @param ignoreLastName set True is default set False to sort last name by polyPhone
     * @hide for libra only
     */
    public ArrayList<HanziToPinyin.Token> get(final String input, boolean filterInvalidChar, boolean ignoreLastName) {
        ArrayList<HanziToPinyin.Token> tokens = new ArrayList<HanziToPinyin.Token>();
        if (!mHasChinaCollator || TextUtils.isEmpty(input)) {
            // return empty tokens.
            return tokens;
        }

        int startIndex = 0;
        if (!ignoreLastName) {
            final ArrayList<HanziToPinyin.Token> polyPhoneLastNameTokens = getPolyPhoneLastNameTokens(input);
            if ((polyPhoneLastNameTokens != null) && (polyPhoneLastNameTokens.size() > 0)) {
                tokens.addAll(polyPhoneLastNameTokens);
                startIndex = polyPhoneLastNameTokens.size();
            }
        }

        final int inputLength = input.length();
        final StringBuilder sb = new StringBuilder();
        int tokenType = HanziToPinyin.Token.LATIN;
        // Go through the input, create a new token when
        // a. Token type changed
        // b. Get the Pinyin of current charater.
        // c. current character is space.
        for (int i = startIndex; i < inputLength; i++) {
            final char character = input.charAt(i);
            if (character == HanziToPinyin.Token.SEPARATOR) {
                if (sb.length() > 0) {
                    addToken(sb, tokens, tokenType);
                }
                if (!filterInvalidChar) {
                    String separator = String.valueOf(HanziToPinyin.Token.SEPARATOR);
                    tokens.add(new HanziToPinyin.Token(HanziToPinyin.Token.UNKNOWN, separator, separator));
                }
                tokenType = HanziToPinyin.Token.UNKNOWN;
            } else if (character < 256) {
                if ((tokenType != HanziToPinyin.Token.LATIN) && (sb.length() > 0)) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = HanziToPinyin.Token.LATIN;
                sb.append(character);
            } else if (character < FIRST_UNIHAN) {
                if ((tokenType != HanziToPinyin.Token.UNKNOWN) && (sb.length() > 0)) {
                    addToken(sb, tokens, tokenType);
                }
                tokenType = HanziToPinyin.Token.UNKNOWN;
                sb.append(character);
            } else {
                    HanziToPinyin.Token t = getToken(character);
                if (t.type == HanziToPinyin.Token.PINYIN) {
                    if (sb.length() > 0) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokens.add(t);
                    tokenType = HanziToPinyin.Token.PINYIN;
                } else {
                    if ((tokenType != t.type) && (sb.length() > 0)) {
                        addToken(sb, tokens, tokenType);
                    }
                    tokenType = t.type;
                    sb.append(character);
                }
            }
        }
        if (sb.length() > 0) {
            addToken(sb, tokens, tokenType);
        }
        return tokens;
    }

    private void addToken(final StringBuilder sb, final ArrayList<HanziToPinyin.Token> tokens,
            final int tokenType) {
        String str = sb.toString();
        tokens.add(new HanziToPinyin.Token(tokenType, str, str));
        sb.setLength(0);
    }

    private static final char[] sT9Map = new char[] {
            '2', '2', '2', '3', '3', '3', '4', '4', '4', '5', '5', '5', '6', '6', '6', '7', '7',
            '7', '7', '8', '8', '8', '9', '9', '9', '9'
    };

    /**
     * @param s
     * @return String T9 numbers for input s. return null if input s is empty string.
     * @hide for libra only
     */
    public static String formatCharToT9(String s) {
        if (TextUtils.isEmpty(s)) {
            return null;
        }

        StringBuilder builder = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if ((c >= 'A') && (c <= 'Z')) {
                builder.append(sT9Map[c - 'A']);
            } else if ((c >= 'a') && (c <= 'z')) {
                builder.append(sT9Map[c - 'a']);
            } else if ((c >= '0') && (c <= '9')) {
                builder.append((char) c);
            } else {
                continue;
            }
        }

        return builder.toString();
    }

    /**
     * @param c
     * @return format char type T9 numbers for input c. return 0 if char is invalid.
     * @hide for libra only
     */
    public static char formatCharToT9(char c) {
        if ((c >= 'A') && (c <= 'Z')) {
            return sT9Map[c - 'A'];
        } else if ((c >= 'a') && (c <= 'z')) {
            return sT9Map[c - 'a'];
        } else if ((c >= '0') && (c <= '9')) {
            return c;
        } else {
            return 0;
        }
    }
}
