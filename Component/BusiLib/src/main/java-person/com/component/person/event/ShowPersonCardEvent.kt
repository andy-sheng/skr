package com.component.person.event

class ShowPersonCardEvent(val uid: Int, val showKick: Boolean?) {
    constructor(uid: Int) : this(uid, null)
}
