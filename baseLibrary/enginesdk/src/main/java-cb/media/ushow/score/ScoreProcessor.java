package media.ushow.score;


public class ScoreProcessor {

    private ScoreProcessorService mScoreProcessorService;

    public ScoreProcessor(int sampleRate, int channels, int sampleFormat, int bufferSizeInShorts, String melPath){
        mScoreProcessorService = new ScoreProcessorService();
        mScoreProcessorService.init(sampleRate, channels, sampleFormat, bufferSizeInShorts, melPath);
    }

    /** 每行歌词结束的时候调用 **/
    public int getLineScore() {
        int lineScore = -1;
        if(null != mScoreProcessorService) {
            lineScore = mScoreProcessorService.getScore();
        }
        return lineScore;
    }

}
