#include "record_level_queue.h"
#define LOG_TAG "RecordLevelQueue"

RecordLevelQueue::RecordLevelQueue() {
	init();
}

RecordLevelQueue::RecordLevelQueue(const char* queueNameParam) {
	init();
	queueName = queueNameParam;
}

void RecordLevelQueue::init() {
	int initLockCode = pthread_mutex_init(&mLock, NULL);
	int initConditionCode = pthread_cond_init(&mCondition, NULL);
	mNbPackets = 0;
	mFirst = NULL;
	mLast = NULL;
	mAbortRequest = false;
}

RecordLevelQueue::~RecordLevelQueue() {
	LOGI("%s ~RecordLevelQueue ....", queueName);
	flush();
	pthread_mutex_destroy(&mLock);
	pthread_cond_destroy(&mCondition);
}

int RecordLevelQueue::size() {
	pthread_mutex_lock(&mLock);
	int size = mNbPackets;
	pthread_mutex_unlock(&mLock);
	return size;
}

void RecordLevelQueue::flush() {
	LOGI("%s flush .... and this time the queue size is %d", queueName, size());
	RecordLevelNode *node, *node1;

	RecordLevel *recordLevel;
	pthread_mutex_lock(&mLock);

	for (node = mFirst; node != NULL; node = node1) {
		node1 = node->next;
		recordLevel = node->recordLevel;
		if (NULL != recordLevel) {
			delete recordLevel;
		}
		delete node;
		node = NULL;
	}
	mLast = NULL;
	mFirst = NULL;
	mNbPackets = 0;

	pthread_mutex_unlock(&mLock);
}
RecordLevel* RecordLevelQueue::getRear() {
	RecordLevel* result = NULL;
	if (!mAbortRequest) {
		pthread_mutex_lock(&mLock);
		RecordLevelNode *node = mLast;
		if (node) {
			result = node->recordLevel;
		}
		pthread_mutex_unlock(&mLock);
	}
	return result;
}
int RecordLevelQueue::push(RecordLevel* recordLevel) {
	if (mAbortRequest) {
		delete recordLevel;
		return -1;
	}
	RecordLevelNode *node = new RecordLevelNode();
	if (!node)
		return -1;
	node->recordLevel = recordLevel;
	node->next = NULL;
	int getLockCode = pthread_mutex_lock(&mLock);
	if (mLast == NULL) {
		mFirst = node;
	} else {
		mLast->next = node;
	}
	mLast = node;
	mNbPackets++;
	pthread_cond_signal(&mCondition);
	pthread_mutex_unlock(&mLock);
	return 0;

}

/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
int RecordLevelQueue::pop(bool block) {
	RecordLevelNode *node;
	int ret = -1;
	int getLockCode = pthread_mutex_lock(&mLock);
	for (;;) {
		if (mAbortRequest) {
			ret = -1;
			break;
		}
		node = mFirst;
		if (node) {
			mFirst = node->next;
			if (!mFirst)
				mLast = NULL;
			mNbPackets--;
			delete node;
			node = NULL;
			ret = 1;
			break;
		} else if (!block) {
			ret = 0;
			break;
		} else {
			pthread_cond_wait(&mCondition, &mLock);
		}
	}
	pthread_mutex_unlock(&mLock);
	return ret;

}

int RecordLevelQueue::poll(RecordLevel **recordLevel, bool block) {
	RecordLevelNode *node;
	int ret = -1;
	int getLockCode = pthread_mutex_lock(&mLock);
	for (;;) {
		if (mAbortRequest) {
			ret = -1;
			break;
		}
		node = mFirst;
		if (node) {
			mFirst = node->next;
			if (!mFirst)
				mLast = NULL;
			mNbPackets--;
//			LOGI("name is %s queue's mNbPackets : %d", queueName, mNbPackets);
			*recordLevel = node->recordLevel;
			delete node;
			node = NULL;
			ret = 1;
			break;
		} else if (!block) {
			ret = 0;
			break;
		} else {
			pthread_cond_wait(&mCondition, &mLock);
		}
	}
	pthread_mutex_unlock(&mLock);
	return ret;

}

/* return < 0 if aborted, 0 if no packet and > 0 if packet.  */
int RecordLevelQueue::peek(RecordLevel **recordLevel, bool block) {
	RecordLevelNode *node;
	int ret = -1;
	int getLockCode = pthread_mutex_lock(&mLock);
	for (;;) {
		if (mAbortRequest) {
			ret = -1;
			break;
		}
		node = mFirst;
		if (node) {
			*recordLevel = node->recordLevel;
			ret = 1;
			break;
		} else if (!block) {
			ret = 0;
			break;
		} else {
			pthread_cond_wait(&mCondition, &mLock);
		}
	}
	pthread_mutex_unlock(&mLock);
	return ret;
}

int RecordLevelQueue::getLatestRhythmRecordLevel(long currentTimeMills) {
	RecordLevelNode *node;
	int offset = -1;
	if (!mAbortRequest) {
		int getLockCode = pthread_mutex_lock(&mLock);
		node = mFirst;
		int index = 0;
		int currentCursor = 0;
		RecordLevel** recordLevelTmps = new RecordLevel*[10];
		RecordLevel** recordLevels = new RecordLevel*[10];
		//通过一个循环，找出离这个时间最近的10个来并且记录下从front到这个时间一共有多少个元素
		while ((NULL != node)
				&& (node->recordLevel->getTimeMills() < currentTimeMills)) {
			index++;
			recordLevelTmps[currentCursor] = (node->recordLevel);
			currentCursor = (currentCursor + 1) % 10;
			node = node->next;
		}
		if (index > 0) {
			//将存下来的最近10个元素颠倒排列成正确的顺序
			int length = index;
			if (index >= 10) {
				length = 10;
				for (int i = 0; i < 10; i++) {
					recordLevels[i] = recordLevelTmps[currentCursor];
					currentCursor = (currentCursor + 1) % 10;
				}
			} else {
				for (int i = 0; i < length; i++) {
					recordLevels[i] = recordLevelTmps[i];
				}
			}
			//删除recordLevelTmps
//			for(int deleteIndex = 0;deleteIndex<10;deleteIndex++){
//				delete[] recordLevelTmps[deleteIndex];
//			}
//			delete[] recordLevelTmps;
			//如果前面多余10个元素，全部删除掉
			while (index > 10) {
				node = mFirst;
				mFirst = node->next;
				if (!mFirst)
					mLast = NULL;
				mNbPackets--;
				RecordLevel* recordLevel = node->recordLevel;
				delete recordLevel;
				delete node;
				node = NULL;
				index--;
			}
			//拿到最近的10个元素，进行计算渲染界面的值
			bool isFlag = false;
			for (int i = length - 1; i > 0; i--) {
				RecordLevel* current = recordLevels[i];
				RecordLevel* pre = NULL;
				if (i >= 1) {
					pre = recordLevels[i - 1];
				}
				if (current->isMutation() && NULL != pre
						&& !pre->isMutation()) {
					isFlag = true;
					current->setMutation(false);
					break;
				}
				offset++;
			}
			//删除recordLevels
//			for(int deleteIndex = 0;deleteIndex<10;deleteIndex++){
//				delete[] recordLevels[deleteIndex];
//			}
//			delete[] recordLevels;
			if (!isFlag) {
				offset = -1;
			}
		} else {
			offset = -1;
		}
		//注意这里不是要删除里面的对象 只是删除指针就可以了因为 下一次循环的时候会删除掉queue里面的实际内存
		delete[] recordLevelTmps;
		//注意这里不是要删除里面的对象 只是删除指针就可以了因为 下一次循环的时候会删除掉queue里面的实际内存
		delete[] recordLevels;
		pthread_mutex_unlock(&mLock);
	}
	return offset;
}

void RecordLevelQueue::abort() {
	pthread_mutex_lock(&mLock);
	mAbortRequest = true;
	pthread_cond_signal(&mCondition);
	pthread_mutex_unlock(&mLock);
}
