#ifndef RECORD_LEVEL_H_
#define RECORD_LEVEL_H_

#include "CommonTools.h"

class RecordLevel {
private:
	/** common的变量 **/
	long timeMills;
	short* samples;
	/** 音高打分用到的变量 **/
	float level;
	float conf;
	/** 节奏打分用到的变量 **/
	int volume;
	bool hasMutation;
public:
	RecordLevel();
	virtual ~RecordLevel();

	long getTimeMills() {
		return timeMills;
	};
	void setTimeMills(long timeMillsParam) {
		timeMills = timeMillsParam;
	};
	short* getSamples() {
		return samples;
	};
	void setSamples(short* samplesParam) {
		samples = samplesParam;
	};

	float getLevel() {
		return level;
	};
	void setLevel(float levelParam) {
		level = levelParam;
	};
	float getConf(){
			return conf;
		};
	void setConf(float confParam){
		conf = confParam;
	};
	int getVol() {
			return volume;
	};
	void setVol(int volumeParam) {
		volume = volumeParam;
	};
	bool isMutation() {
		return hasMutation;
	};
	void setMutation(bool hasMutationParam) {
		hasMutation = hasMutationParam;
	};
};

#endif /* RECORD_LEVEL_H_ */
