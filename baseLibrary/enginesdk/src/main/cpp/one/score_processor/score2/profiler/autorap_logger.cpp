//
//  autorap_log.cpp
//  AutoRap
//
//  Created by apple on 2016/12/21.
//  Copyright © 2016年 allenyang. All rights reserved.
//
#include <algorithm>
#include "autorap_logger.hpp"
#include "autorap_util.hpp"
#include "audio_engine_common.h"

#define LOG_TAG "autorap_logger"

using namespace std;

static AutoRapLogger* g_instance = NULL;

typedef pair<string, pair<double,int>> LOGPAIR;

static bool cmp_by_value(const LOGPAIR& lhs, const LOGPAIR& rhs) {  
    return lhs.second.first < rhs.second.first;  
}

AutoRapLogger::AutoRapLogger() {
}
AutoRapLogger::~AutoRapLogger() {
}

AutoRapLogger* AutoRapLogger::GetInstance() {
    if (g_instance==NULL) {
        g_instance = new AutoRapLogger();
    }
    return g_instance;
}

void AutoRapLogger::DeleteInstance(){
    if (g_instance) {
        delete g_instance;
        g_instance = NULL;
    }
}
void AutoRapLogger::put(const char* fileName, const char* methodName, float wasteTimeTimeMills) {
#ifdef DEBUG
    string name = string(fileName).append("::").append(string(methodName));
    map<string, std::pair<double,int>>::iterator iter = profileStatistics.find(string(name));
    if (iter != profileStatistics.end()) {
        iter->second.first += wasteTimeTimeMills;
        iter->second.second+=1;
    } else {
        //profileStatistics.insert(pair<string, int64_t>(string(name), wasteTimeTimeMills));
        std::pair<double,int> value(wasteTimeTimeMills,1);
        profileStatistics.insert(pair<string,pair<double,int>>(string(name),value));
    }
#endif
}

string AutoRapLogger::getLogInfo() {
#ifdef DEBUG
    vector<LOGPAIR> logcontent(profileStatistics.begin(), profileStatistics.end()); 
    std::sort(logcontent.begin(), logcontent.end(), cmp_by_value);
    string logInfo;
    vector<LOGPAIR> ::iterator iter;
    for (iter = logcontent.begin(); iter != logcontent.end(); iter++) {
        string fpath = iter->first;
        size_t pos = fpath.find_last_of("/");
        string shortpath=fpath;
        if (pos != string::npos) {
            shortpath = fpath.substr(pos+1,fpath.size());
        }
        if (shortpath.size()<53) {//文件名和函数名按53字节对齐
            int len = 53-shortpath.size();
            while(len-->0)shortpath.append(" ");
        }
        logInfo.append(shortpath).append(string("\t: "));
        string rcnt = CAutorapUtil::double2string(iter->second.second, false);
        if (rcnt.length()<6) {//执行次数按6字节对齐
            int len = 6-rcnt.size();
            while (len-->0) {
                rcnt.append(" ");
            }
        }
        logInfo.append(rcnt);
        logInfo.append(string("\t: "));
        logInfo.append(CAutorapUtil::double2string(iter->second.first, false)).append(string("ms \n\n"));
    }
    logInfo.insert(0, "\n");
    return logInfo;
#else
    return "";
#endif
}

void AutoRapLogger::printLogInfo(){
#ifdef DEBUG
    LOGI("\n%s", getLogInfo().c_str());    
#endif
}
