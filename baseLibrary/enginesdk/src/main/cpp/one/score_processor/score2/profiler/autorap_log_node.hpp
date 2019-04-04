//
//  autorap_log_node.hpp
//  AutoRap
//
//  Created by apple on 2016/12/21.
//  Copyright © 2016年 allenyang. All rights reserved.
//

#ifndef autorap_log_node_hpp
#define autorap_log_node_hpp

#include <stdio.h>
#include <string>
#include <sys/time.h>
#include "autorap_logger.hpp"
using namespace std;

static inline double getCurrentTimeMills() {
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return (double)(tv.tv_sec * 1000 + (double)tv.tv_usec / 1000);
}

#ifdef DEBUG
#define LOG4DEBUG 1
#endif

//把LOG4ARProfiling放在一个函数开始定义的地方即可，用于统计该函数的累计执行时间
#ifdef LOG4DEBUG
#define LOG4ARProfiling AutoRapLogNode node(__FILE__, __FUNCTION__);
#else
#define LOG4ARProfiling
#endif

//LOG4ARProfilingBegin ,LOG4ARProfilingEnd 必须成对调用，用于统计某一段代码的执行时间；
//因为实现时会把这段代码用{}括起来，因此需要注意代码段内变量作用域的问题，可能导致编译错误
#ifdef LOG4DEBUG
#define LOG4ARProfilingBegin(__SubProcName__) { AutoRapLogNode node(__FILE__, __SubProcName__);
#define LOG4ARProfilingEnd() }
#else
#define LOG4ARProfilingBegin(__SubProcName__)
#define LOG4ARProfilingEnd
#endif

class AutoRapLogNode
{
private:
    const char* fileName;
    const char* methodName;
    double beginProcessTimeMills;

public:
    AutoRapLogNode(const char fileName[], const char methodName[]);
    ~AutoRapLogNode();
};

#endif /* autorap_log_node_hpp */
