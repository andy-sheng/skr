#ifndef PITCH_SCORING_H_
#define PITCH_SCORING_H_

#include "base_scoring.h"
#include "f0_cal/CCalcBaseband.h"
#include "mel_chord_ana.h"

class PitchScoring: public BaseScoring {
private:
	int sampleRate;
	int channelNum;
	int bitDepth;

	CCalcBaseband *calBasebandUtil;
	bool isInitialization;
	pthread_t initBasebandThread;
	static void* 			startInitBasebandThread(void* ptr);

//	FILE* pcmFile;

	//打分相关
	std::vector<MelodyNote> mMelodyNotes;
	int 					mNotesMaxLen;
	int 					mIndex;
    int 					mIndexMaxScore;
	int 					mPcmTotal;
	int 					mPcmBufferSize;
	int 					mLastScore;
	int 					mCurrentLineLevelSum;
	int 					mCurrentLineSampleCount;
	float 					noteDiff(float curNote, short targetNote);
public:
	PitchScoring();
	virtual ~PitchScoring();
	void onInit(int sampleRate, int channelNum, int bitDepth, char *melFilePath);
	void doScoring(short* buffer, int bufferSize, long currentTime);
	void processRecordLevel(RecordLevel* recordLevel);
	int getScore();
	int destroy();

	/** 弃用 **/
	void getRenderData(long currentTimeMills, float* meta);

	int computePcmDB(short *buffer, int bufferSize);

	MelodyNote *getSingingIndex(long currentTimeMills, int *flag, int *index);
};

#endif /* PITCH_SCORING_H_ */
