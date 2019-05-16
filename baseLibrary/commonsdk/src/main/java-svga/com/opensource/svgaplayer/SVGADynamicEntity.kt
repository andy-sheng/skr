package com.opensource.svgaplayer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.text.StaticLayout
import android.text.TextPaint
import com.glidebitmappool.GlideBitmapFactory
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * Created by cuiminghui on 2017/3/30.
 */
class SVGADynamicEntity {

    internal var dynamicHidden: HashMap<String, Boolean> = hashMapOf()

    internal var dynamicImage: HashMap<String, Bitmap> = hashMapOf()

    internal var dynamicText: HashMap<String, String> = hashMapOf()

    internal var dynamicTextPaint: HashMap<String, TextPaint> = hashMapOf()

    internal var dynamicLayoutText: HashMap<String, StaticLayout> = hashMapOf()

    internal var dynamicDrawer: HashMap<String, (canvas: Canvas, frameIndex: Int) -> Boolean> = hashMapOf()

    internal var isTextDirty = false

    fun setHidden(value: Boolean, forKey: String) {
        this.dynamicHidden.put(forKey, value)
    }

    fun setDynamicImage(bitmap: Bitmap, forKey: String) {
        this.dynamicImage.put(forKey, bitmap)
    }

    fun setDynamicImage(url: String, forKey: String) {
        val handler = android.os.Handler()
        Observable.create<Bitmap> { emitter ->
            (URL(url).openConnection() as? HttpURLConnection)?.let {
                it.connectTimeout = 20 * 1000
                it.requestMethod = "GET"
                it.connect()
                // 这里不能用 GlideBitmapFactory ，可能会导致inputStream 被读掉了
                var bitmap = BitmapFactory.decodeStream(it.inputStream);
                emitter.onNext(bitmap);
                it.inputStream.close()
                emitter.onComplete()
            }
            emitter.onComplete()
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setDynamicImage(it, forKey)
                })
//        thread {
//            try {
//                (URL(url).openConnection() as? HttpURLConnection)?.let {
//                    it.connectTimeout = 20 * 1000
//                    it.requestMethod = "GET"
//                    it.connect()
//                    // 这里不能用 GlideBitmapFactory ，可能会导致inputStream 被读掉了
//                    BitmapFactory.decodeStream(it.inputStream)?.let {
//                        handler.post {
//                            setDynamicImage(it, forKey)
//                        }
//                    }
//                    it.inputStream.close()
//                }
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
    }

    fun setDynamicText(text: String, textPaint: TextPaint, forKey: String) {
        this.isTextDirty = true
        this.dynamicText.put(forKey, text)
        this.dynamicTextPaint.put(forKey, textPaint)
    }

    fun setDynamicText(layoutText: StaticLayout, forKey: String) {
        this.isTextDirty = true
        this.dynamicLayoutText.put(forKey, layoutText)
    }

    fun setDynamicDrawer(drawer: (canvas: Canvas, frameIndex: Int) -> Boolean, forKey: String) {
        this.dynamicDrawer.put(forKey, drawer)
    }

    fun clearDynamicObjects() {
        this.isTextDirty = true
        this.dynamicHidden.clear()
        this.dynamicImage.clear()
        this.dynamicText.clear()
        this.dynamicTextPaint.clear()
        this.dynamicLayoutText.clear()
        this.dynamicDrawer.clear()
    }

}