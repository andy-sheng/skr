package com.mi.liveassistant.dns;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/9/12.
 *
 * @moudle 域名预解析
 */
public class DomainListUpdateEvent {
    public List<String> domainList = new ArrayList();
    public List<String> domainPortList = new ArrayList();

    public DomainListUpdateEvent(List<String> domainList, List<String> domainPortList) {
        this.domainList = domainList;
        this.domainPortList = domainPortList;
    }
}
