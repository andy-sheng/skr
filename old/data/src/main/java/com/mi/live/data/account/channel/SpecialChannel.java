package com.mi.live.data.account.channel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lan on 2017/7/20.
 */

public class SpecialChannel {
    public static Set<Integer> sChannelSet = new HashSet<>();

    static {
        sChannelSet.add(50001);
        sChannelSet.add(50014);
    }
}
