//
//  autorap_log.hpp
//  AutoRap
//
//  Created by apple on 2016/12/21.
//  Copyright © 2016年 allenyang. All rights reserved.
//

#ifndef autorap_log_hpp
#define autorap_log_hpp

#include <stdio.h>
#include <vector>
#include <map>
#include <string>
#include <utility>
#include "autorap_log_node.hpp"

class AutoRapLogger
{
private:
    AutoRapLogger();
    std::map<std::string, std::pair<double,int>> profileStatistics;

public:
    static AutoRapLogger* GetInstance();
    //程序结束时调用该函数删除autoraplogger对象，并输出log信息
    static void DeleteInstance();
    virtual ~AutoRapLogger();
    void put(const char* fileName, const char* methodName, float wasteTimeMills);
    std::string getLogInfo();
    void printLogInfo();
};

#endif /* autorap_log_hpp */
