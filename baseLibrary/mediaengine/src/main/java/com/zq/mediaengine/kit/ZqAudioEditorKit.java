package com.zq.mediaengine.kit;

import android.content.Context;

/**
 * 音频编辑、合成实现类。
 *
 * 实现人声和伴奏的对齐，以及音效、音量的调整。
 */

public class ZqAudioEditorKit {
    public static final String TAG = "ZqAudioEditorKit";

    private Context mContext;

    private OnPreviewStartedListener mOnPreviewStartedListener;
    private OnPreviewCompletionListener mOnPreviewCompletionListener;
    private OnComposeCompletionListener mOnComposeCompletionListener;
    private OnErrorListener mOnErrorListener;

    /**
     * 音频合成预览开始的回调接口
     */
    public interface OnPreviewStartedListener {
        void onPreviewStarted();
    }

    /**
     * 音频合成预览结束的回调接口
     */
    public interface OnPreviewCompletionListener {
        void onPreviewCompletion();
    }

    /**
     * 音频合成已完成的回调接口。
     */
    public interface OnComposeCompletionListener {
        void onComposeCompletion();
    }

    /**
     * 错误回调接口。
     */
    public interface OnErrorListener {
        void onError(int what, int msg1, int msg2);
    }

    public ZqAudioEditorKit(Context context) {
        mContext = context;
    }

    public void setOnPreviewStartedListener(OnPreviewStartedListener listener) {
        mOnPreviewStartedListener = listener;
    }

    public void setOnPreviewCompletionListener(OnPreviewCompletionListener listener) {
        mOnPreviewCompletionListener = listener;
    }

    public void setOnComposeCompletionListener(OnComposeCompletionListener listener) {
        mOnComposeCompletionListener = listener;
    }

    public void setOnErrorListener(OnErrorListener listener) {
        mOnErrorListener = listener;
    }

    /**
     * 重置当前实例，调用后当前实例的状态恢复到刚创建时的样子。
     *
     * 如果在合成过程中，则合成操作立即中断，中间文件均被删除。
     */
    public void reset() {

    }

    /**
     * 设置需要编辑合成的音频文件路径。
     *
     * idx为0的音频文件会作为基准，一般设置为伴奏的地址。
     * 注意均需要设置为本地文件地址，网络地址目前不能很好的支持。
     *
     * @param idx   音频文件索引
     * @param path  音频文件的绝对路径
     */
    public void setDataSource(int idx, String path) {

    }

    /**
     * 开始或恢复音频合成预览。
     *
     * 调用该接口前，需要设置好合成预览的各路音频文件路径。
     * idx为0的路径必须设置，不然该接口不会工作。
     *
     * @param loopCount 循环次数，<0表示无限循环。
     */
    public void startPreview(int loopCount) {

    }

    /**
     * 暂停音频合成预览。
     */
    public void pausePreview() {

    }

    /**
     * 停止音频合成预览。
     */
    public void stopPreview() {

    }

    /**
     * 获取主音轨的时长信息。
     *
     * 需要startPreview后，收到onPreviewStarted回调后获取方才有效。
     * 可以在onPreviewStarted回调中获取并保存该值。
     *
     * @return  音频时长或0
     */
    public long getDuration() {
        return 0;
    }

    /**
     * 获取主音轨在预览时的位置信息。
     *
     * @return  当前播放的位置，单位为ms
     */
    public long getPosition() {
        return 0;
    }

    /**
     * 对主音轨进行seek操作，其他音轨也会自动seek。
     *
     * @param pos 需要seek的目标位置，单位ms
     */
    public void seekTo(long pos) {

    }

    /**
     * 设置各路音轨的音量。
     *
     * 可以在预览开始前后设置，预览开始后设置会实时生效，合成开始后设置无效。
     *
     * @param idx   待设置的音轨索引
     * @param vol   音量大小，一般在[0, 1]之间，大于1可以放大声音，但可能会出现爆音。
     */
    public void setInputVolume(int idx, float vol) {

    }

    /**
     * 获取当前指定音轨的音量。
     *
     * @param idx   音轨索引
     * @return  音量大小。
     */
    public float getInputVolume(int idx) {
        return 1.0f;
    }

    /**
     * 设置最终合成输出的音频音量大小。
     *
     * @param vol   音量大小，一般在[0, 1]之间，大于1可以放大声音，但可能会出现爆音。
     */
    public void setOutputVolume(float vol) {

    }

    /**
     * 获取最终合成输出的音量大小。
     *
     * @return  当前的输出音量大小。
     */
    public float getOutputVolume() {
        return 1.0f;
    }

    /**
     * 设置指定音轨的延迟信息。
     *
     * 可以在预览开始前后设置，预览开始后设置会实时生效，合成开始后设置无效。
     *
     * @param idx       待设置的音轨序号
     * @param delayInMs 延迟大小，单位为毫秒。小于0表示前移，大于0表示后移。
     */
    public void setDelay(int idx, long delayInMs) {

    }

    /**
     * 获取指定音轨的延迟信息。
     *
     * @param idx   音轨索引
     * @return  延迟大小，单位为毫秒。
     */
    public long getDelay(int idx) {
        return 0;
    }

    /**
     * 设置指定音轨的音效。
     *
     * @param idx           音轨索引
     * @param effectType    音效类型
     */
    public void setAudioEffect(int idx, int effectType) {

    }

    /**
     * 获取指定音轨的音效。
     *
     * @param idx   音轨索引
     * @return  音效类型。
     */
    public int getAudioEffect(int idx) {
        return 0;
    }

    /**
     * 设置合成后的输出目标文件路径。
     *
     * @param path  输出路径。
     */
    public void setOutputPath(String path) {

    }

    /**
     * 获取合成后的输出目标文件路径。
     *
     * @return  输出路径。
     */
    public String getOutputPath() {
        return null;
    }

    /**
     * 开始合成。
     */
    public void startCompose() {

    }

    /**
     * 释放当前实例，释放后不能再访问该实例。
     */
    public void release() {

    }
}
