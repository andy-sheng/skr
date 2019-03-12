cmake_minimum_required(VERSION 3.4.1)

#添加子目录,将会调用子目录中的CMakeLists.txt

SET(ONE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/one)
SET(TWO_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/two)

MESSAGE(STATUS “src  dir: ${CMAKE_CURRENT_SOURCE_DIR}”)

MESSAGE(STATUS “one  dir: ${ONE_PATH}”)
MESSAGE(STATUS “two  dir: ${TWO_PATH}”)

ADD_SUBDIRECTORY(${TWO_PATH})
ADD_SUBDIRECTORY(${ONE_PATH})

