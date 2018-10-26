package com.common.core.config.event;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/9/12.
 *
 * @moudle 域名预解析
 */
public class DomainListUpdateEvent {
    public List<String> domainPortList = new ArrayList<>();

    public DomainListUpdateEvent(List<String> domainPortList) {
        this.domainPortList = domainPortList;
    }
}
