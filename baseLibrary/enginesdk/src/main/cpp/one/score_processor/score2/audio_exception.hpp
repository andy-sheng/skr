//
//  autorap_exception.hpp
//  AutoRap
//
//  Created by wangguoteng on 16/9/6.
//  Copyright © 2016年 wangguoteng. All rights reserved.
//
#ifndef AUTORAP_EXCEPTION_HPP
#define AUTORAP_EXCEPTION_HPP

#include <string>
#include <exception>

class CParamException: public std::exception
{
public:
    explicit CParamException(const std::string& desc):
        m_desc("AutoRap CParamExcption: " + desc)
    {

    }
    ~CParamException() throw()
    {
    }
    const char* what() const throw()
    {
        return m_desc.c_str();
    }

private:
    std::string m_desc;
};

#endif
