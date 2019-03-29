cmake_minimum_required(VERSION 3.4.1)

set(PATH_TO_MEDIACORE2 ${CMAKE_SOURCE_DIR}/src/main/cpp/two)
set(PATH_TO_JNI_LAYER2 ${CMAKE_SOURCE_DIR}/src/main/jni/two)

#set(PATH_TO_THIRDPARTY ${PATH_TO_MEDIACORE}/thirdparty)
#set(PATH_TO_PRE_BUILT ${PATH_TO_THIRDPARTY}/prebuilt/${ANDROID_ABI})

add_definitions("
-DGL_GLEXT_PROTOTYPES
-DEGL_EGLEXT_PROTOTYPES
")

include_directories(${PATH_TO_MEDIACORE2}/)
include_directories(${PATH_TO_MEDIACORE2}/*/)
include_directories(${PATH_TO_MEDIACORE2}/*/*/)
include_directories(${PATH_TO_MEDIACORE2}/common/)
include_directories(${PATH_TO_MEDIACORE2}/Delay/)
include_directories(${PATH_TO_MEDIACORE2}/Delay/Delay_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/effect/)
include_directories(${PATH_TO_MEDIACORE2}/effect/effect_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/effect/Plugin_conv_reverb/)
include_directories(${PATH_TO_MEDIACORE2}/filter/)
include_directories(${PATH_TO_MEDIACORE2}/filter/Filter_LIB/)


file(GLOB FILES_JNI_LAYER2 "${PATH_TO_JNI_LAYER2}/*.cpp")

file(GLOB FILES_LIB_ALL1 "${PATH_TO_MEDIACORE2}/*/*.c")
file(GLOB FILES_LIB_ALL2 "${PATH_TO_MEDIACORE2}/*/*/*.c")

file(GLOB FILES_LIB_COMMON2 "${PATH_TO_MEDIACORE2}/common/*.c")
file(GLOB FILES_LIB_DELAY2 "${PATH_TO_MEDIACORE2}/Delay/Delay_LIB/*.c")
file(GLOB FILES_LIB_EFFECT12 "${PATH_TO_MEDIACORE2}/effect/effect_LIB/*.c")
file(GLOB FILES_LIB_EFFECT22 "${PATH_TO_MEDIACORE2}/effect/Plugin_conv_reverb/*.c")
file(GLOB FILES_LIB_FILTER2 "${PATH_TO_MEDIACORE2}/filter/Filter_LIB/*.c")


#该指令的主要作用就是将指定的源文件生成链接文件，然后添加到工程中去。该指令常用的语法如下：
add_library(native-lib2 SHARED
        ${FILES_JNI_LAYER2}

        ${FILES_LIB_ALL1}
        ${FILES_LIB_ALL2}

        ${FILES_LIB_COMMON2}
        ${FILES_LIB_DELAY2}
        ${FILES_LIB_EFFECT12}
        ${FILES_LIB_EFFECT22}
        ${FILES_LIB_FILTER2}
        )

target_link_libraries( # Specifies the target library.
        native-lib2
        android
        #${PATH_TO_PRE_BUILT}/libsox.a
        z
        log )