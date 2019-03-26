#include "scoring2_adapter.h"
#include <sys/time.h>

#define LOG_TAG "Scoring2Adapter"

#define LOGOPEN 0

Scoring2Adapter::Scoring2Adapter(int sampleRate) {
    calcScore = new CalcScore(sampleRate);
}

Scoring2Adapter::~Scoring2Adapter() {
}

int Scoring2Adapter::LoadMelp(std::string filename, int startStamp) {
    calcScore - LoadMelp(filename, startStamp);
    /**
      2.     中止线程可以有三种方式：
a.     在线程函数中return
b.     被同一进程中的另外的线程Cancel掉
c.     线程调用pthread_exit函数
     */
    isRunning = true;
    pthread_create(&scoringThread, NULL, startScoringThread, this);
}

void Scoring2Adapter::Flow(short *data, int len) {
    PushData pushData;
    pushData.data =data;
    pushData.len = len;
    queue.push_back(pushData);
}

int Scoring2Adapter::GetScore(int curTimeStamp) {

}


void *Scoring2Adapter::startScoringThread(void *ptr) {
    while (isRunning) {
//        int resultCode = recordRawQueue->poll(&recordLevel, true);
//        if (resultCode > 0) {
//            if (NULL != recordLevel) {
//                //LOGI("running in process ");
//                mPushTime = getRealTime();
//                mRearLevelMills = recordLevel->getTimeMills();
//                processRecordLevel(recordLevel);
//                if (NULL != recordLevel) {
//                    delete recordLevel;
//                    recordLevel = NULL;
//                }
//            }
//        }
    }
}

void Scoring2Adapter::destroy() {
    isRunning  =false;
    void *status;
    pthread_join(scoringThread, &status);
}
