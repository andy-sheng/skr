package com.common.image.debug

import android.net.Uri
import java.io.Serializable

class ImageDebugModel : Serializable {
    var vw = 0
    var vh = 0
    var bw = 0
    var bh = 0
    var uri: String

    constructor(vw: Int, vh: Int, bw: Int, bh: Int, uri: String) {
        this.vw = vw
        this.vh = vh
        this.bw = bw
        this.bh = bh
        this.uri = uri
    }

    override fun toString(): String {
        return "ImageDebugModel(vw=$vw, vh=$vh, bw=$bw, bh=$bh, uri='$uri')"
    }

}