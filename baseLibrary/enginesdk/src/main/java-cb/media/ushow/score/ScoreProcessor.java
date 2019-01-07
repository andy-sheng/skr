package media.ushow.score;


import android.text.TextUtils;

public class ScoreProcessor {

    private ScoreProcessorService mScoreProcessorService;


    public ScoreProcessor() {

    }

    public void init(int sampleRate, int channels, int sampleFormat, int bufferSizeInShorts, String melPath) {
        if (mScoreProcessorService == null) {
            mScoreProcessorService = new ScoreProcessorService();
        }
        if (!TextUtils.isEmpty(melPath)) {
            mScoreProcessorService.init(sampleRate, channels, sampleFormat, bufferSizeInShorts, melPath);
        }
    }

    /**
     * 每行歌词结束的时候调用
     **/
    public int getLineScore() {
        int lineScore = -1;
        if (null != mScoreProcessorService) {
            lineScore = mScoreProcessorService.getScore();
        }
        return lineScore;
    }

    public void destroy() {
        mScoreProcessorService.destroy();
        mScoreProcessorService = null;
    }
}
