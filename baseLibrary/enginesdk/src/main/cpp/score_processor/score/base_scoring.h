#ifndef BASE_SCORING_H_
#define BASE_SCORING_H_

#include "CommonTools.h"
#include "record_level_queue.h"

#define PITCH_SCORING_TYPE 1
#define RHYTHM_SCORING_TYPE 2

class BaseScoring {
private:
	RecordLevelQueue* recordRawQueue;
	short* samples;
	int sampleCursor;
	long currentTimeMills;
	long mPushTime;//levelbuffer计算完成后push进去的时间
	long mMaxT1;//最大延迟时间
	long mRearLevelMills;//level队列中队尾buffer对应的时间
	bool isRunning;
	/** 根据声音原始数据计算声音对应的recordLevel **/
	pthread_t scoringThread;
	static void* startScoringThread(void* ptr);
	/** 开启计算线程的回调方法 **/
	void consumeRecordRawQueue();
	long getRealTime();
	void pushSamplesToRecordRawQueue();
protected:
	RecordLevelQueue* recordLevelQueue;
	int UNIT_LEN_IN_MS;
	float mShortCntPerMs;
	int AUDIO_DATA_SAMPLE_FOR_SCORE;
	bool isNeedDestroy;
public:
	BaseScoring();
	virtual ~BaseScoring();
	int getMinBufferSize(int sampleRate, int channelNum, int bit,
			int bufferSizeInShorts);
	void init(int sampleRate, int channelNum, int bit, char *melFilePath);
	void mergeRecordRaw(short* buffer, int bufferSize, long currentTime);
	virtual int destroy();
	virtual void setNeedDestroy(bool isNeedDestroy){
		this->isNeedDestroy = isNeedDestroy;
	};
	/** 第一次画图的时候，计算出当前计算的录音数据的得分和当前播放器的播放时间的延迟 **/
	int getLatency(long currentTimeMills);
	virtual void getRenderData(long currentTimeMills, float* meta){

	};
	virtual void onInit(int sampleRate, int channelNum, int dB, char *melFilePath){

	};
	virtual void doScoring(short* buffer, int bufferSize, long currentTime){

	};
	virtual void processRecordLevel(RecordLevel* recordLevel){

	};

	virtual int getScore() = 0;
	void reset();
	long getCurTime();
};

#endif /* BASE_SCORING_H_ */
