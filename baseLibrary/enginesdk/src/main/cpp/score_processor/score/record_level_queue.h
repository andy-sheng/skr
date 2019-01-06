#ifndef RECORD_LEVEL_QUEUE_H
#define RECORD_LEVEL_QUEUE_H

#include <pthread.h>
#include "CommonTools.h"
#include "record_level.h"

typedef struct RecordLevelNode {
	RecordLevel *recordLevel;
	struct RecordLevelNode *next;
	RecordLevelNode(){
		recordLevel = NULL;
		next = NULL;
	}
} RecordLevelNode;

class RecordLevelQueue {
private:
	RecordLevelNode* mFirst;
	RecordLevelNode* mLast;
	int mNbPackets;
	bool mAbortRequest;
	pthread_mutex_t mLock;
	pthread_cond_t mCondition;
	const char* queueName;

public:
	RecordLevelQueue();
	RecordLevelQueue(const char* queueNameParam);
	~RecordLevelQueue();

	void init();
	int push(RecordLevel* recordLevel);
	/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
	int peek(RecordLevel **recordLevel, bool block);
	int poll(RecordLevel **recordLevel, bool block);
	/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
	int pop(bool block);

	int getLatestRhythmRecordLevel(long currentTimeMills);

	RecordLevel* getRear();
	int size();
	void flush();
	void abort();
};

#endif // RECORD_LEVEL_QUEUE_H
