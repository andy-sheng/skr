package com.component.person.model

import com.alibaba.fastjson.annotation.JSONField
import java.io.Serializable

class RelationNumModel : Serializable {
    /**
     * relation : 1
     * cnt : 4
     */

    @JSONField(name = "relation")
    var relation: Int = 0
    @JSONField(name = "cnt")
    var cnt: Int = 0
}
