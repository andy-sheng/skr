//
//  autorap_log_node.cpp
//  AutoRap
//
//  Created by apple on 2016/12/21.
//  Copyright © 2016年 allenyang. All rights reserved.
//

#include "autorap_log_node.hpp"

AutoRapLogNode::AutoRapLogNode(const char fileName[], const char methodName[]) {
#ifdef DEBUG
    this->fileName = fileName;
    this->methodName = methodName;
    beginProcessTimeMills = getCurrentTimeMills();
#endif
}
AutoRapLogNode::~AutoRapLogNode() {
#ifdef DEBUG
    AutoRapLogger::GetInstance()->put(fileName, methodName, (double)(getCurrentTimeMills() - beginProcessTimeMills));
#endif
}
