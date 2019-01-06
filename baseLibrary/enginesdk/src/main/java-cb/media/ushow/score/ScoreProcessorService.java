package media.ushow.score;

public class ScoreProcessorService {
	/** 主要是初始化scoring **/
	public native int init(int sampleRate, int channels, int sampleFormat, int bufferSizeInShorts, String melPath);

	//这个是因为pitch scoring由于需要重采样初始化时间比较长，就有可能导致用户退出，一旦退出就调用这个方法，当初始化完毕之后，自己销毁
	public native void setDestroyScoreProcessorFlag(boolean destroyScoreProcessorFlag);

	/** 每次画图的时候需要调用这个方法计算数据 **/
	public native int getScore();

	/** 销毁整个打分处理器 **/
	public native void destroy();

}
