package com.component.lyrics.exception

import java.lang.RuntimeException

class LyricLoadFailedException : RuntimeException{
    constructor() : super()
    constructor(message: String?) : super(message)
}