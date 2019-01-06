#include "record_level.h"

#define LOG_TAG "RecordLevel"

RecordLevel::RecordLevel() {
	samples = NULL;
}
RecordLevel::~RecordLevel() {
	if(NULL != samples){
		delete[] samples;
	}
}
