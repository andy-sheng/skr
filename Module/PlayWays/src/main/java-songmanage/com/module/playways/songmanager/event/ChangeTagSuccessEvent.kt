package com.module.playways.songmanager.event

import com.component.busilib.friends.SpecialModel

class ChangeTagSuccessEvent(specialModel: SpecialModel) {
    var specialModel: SpecialModel
        internal set

    init {
        this.specialModel = specialModel
    }
}
