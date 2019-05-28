cmake_minimum_required(VERSION 3.4.1)

set(PATH_TO_MEDIACORE2 ${CMAKE_SOURCE_DIR}/src/main/cpp/two)
set(PATH_TO_JNI_LAYER2 ${CMAKE_SOURCE_DIR}/src/main/jni/two)

#set(PATH_TO_THIRDPARTY ${PATH_TO_MEDIACORE}/thirdparty)
#set(PATH_TO_PRE_BUILT ${PATH_TO_THIRDPARTY}/prebuilt/${ANDROID_ABI})

add_definitions("
-DGL_GLEXT_PROTOTYPES
-DEGL_EGLEXT_PROTOTYPES
")

include_directories(${PATH_TO_MEDIACORE2}/amplitude/AGC_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/amplitude/DCC_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/amplitude/Dynamic_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/amplitude/Level_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/amplitude/libvoln/)
include_directories(${PATH_TO_MEDIACORE2}/amplitude/)
include_directories(${PATH_TO_MEDIACORE2}/common/)
include_directories(${PATH_TO_MEDIACORE2}/Delay/Delay_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/Delay/)
include_directories(${PATH_TO_MEDIACORE2}/effect/effect_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/effect/Plugin_conv_reverb/)
include_directories(${PATH_TO_MEDIACORE2}/effect/)
include_directories(${PATH_TO_MEDIACORE2}/filter/Filter_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/filter/)
include_directories(${PATH_TO_MEDIACORE2}/naturemix/NatureMix_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/naturemix/)
include_directories(${PATH_TO_MEDIACORE2}/pitch/pitch_Normalized_CrossCorrelation_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/pitch/)
include_directories(${PATH_TO_MEDIACORE2}/resample/Resample_LIB/)
include_directories(${PATH_TO_MEDIACORE2}/resample/)
include_directories(${PATH_TO_MEDIACORE2}/)


file(GLOB FILES_JNI_LAYER2 "${PATH_TO_JNI_LAYER2}/*.cpp")

file(GLOB FILES_LIB_AGC_LIB "${PATH_TO_MEDIACORE2}/amplitude/AGC_LIB/*.c")
file(GLOB FILES_LIB_DCC_LIB "${PATH_TO_MEDIACORE2}/amplitude/DCC_LIB/*.c")
file(GLOB FILES_LIB_Dynamic_LIB "${PATH_TO_MEDIACORE2}/amplitude/Dynamic_LIB/*.c")
file(GLOB FILES_LIB_Level_LIB "${PATH_TO_MEDIACORE2}/amplitude/Level_LIB/*.c")
file(GLOB FILES_LIB_libvoln "${PATH_TO_MEDIACORE2}/amplitude/libvoln/*.c")
file(GLOB FILES_LIB_amplitude "${PATH_TO_MEDIACORE2}/amplitude/*.c")
file(GLOB FILES_LIB_common "${PATH_TO_MEDIACORE2}/common/*.c")
file(GLOB FILES_LIB_Delay_LIB "${PATH_TO_MEDIACORE2}/Delay/Delay_LIB/*.c")
file(GLOB FILES_LIB_Delay "${PATH_TO_MEDIACORE2}/Delay/*.c")
file(GLOB FILES_LIB_effect_LIB "${PATH_TO_MEDIACORE2}/effect/effect_LIB/*.c")
file(GLOB FILES_LIB_Plugin_conv_reverb "${PATH_TO_MEDIACORE2}/effect/Plugin_conv_reverb/*.c")
file(GLOB FILES_LIB_effect "${PATH_TO_MEDIACORE2}/effect/*.c")
file(GLOB FILES_LIB_Filter_LIB "${PATH_TO_MEDIACORE2}/filter/Filter_LIB/*.c")
file(GLOB FILES_LIB_filter "${PATH_TO_MEDIACORE2}/filter/*.c")
file(GLOB FILES_LIB_NatureMix_LIB "${PATH_TO_MEDIACORE2}/naturemix/NatureMix_LIB/*.c")
file(GLOB FILES_LIB_naturemix "${PATH_TO_MEDIACORE2}/naturemix/*.c")
file(GLOB FILES_LIB_pitch_Normalized_CrossCorrelation_LIB "${PATH_TO_MEDIACORE2}/pitch/pitch_Normalized_CrossCorrelation_LIB/*.c")
file(GLOB FILES_LIB_pitch "${PATH_TO_MEDIACORE2}/pitch/*.c")
file(GLOB FILES_LIB_Resample_LIB "${PATH_TO_MEDIACORE2}/resample/Resample_LIB/*.c")
file(GLOB FILES_LIB_resample "${PATH_TO_MEDIACORE2}/resample/*.c")


#该指令的主要作用就是将指定的源文件生成链接文件，然后添加到工程中去。该指令常用的语法如下：
add_library(native-lib2 SHARED
        ${FILES_JNI_LAYER2}

        ${FILES_LIB_AGC_LIB}
        ${FILES_LIB_DCC_LIB}
        ${FILES_LIB_Dynamic_LIB}
        ${FILES_LIB_Level_LIB}
        ${FILES_LIB_libvoln}
        ${FILES_LIB_amplitude}
        ${FILES_LIB_common}
        ${FILES_LIB_Delay_LIB}
        ${FILES_LIB_Delay}
        ${FILES_LIB_effect_LIB}
        ${FILES_LIB_Plugin_conv_reverb}
        ${FILES_LIB_effect}
        ${FILES_LIB_Filter_LIB}
        ${FILES_LIB_filter}
        ${FILES_LIB_NatureMix_LIB}
        ${FILES_LIB_naturemix}
        ${FILES_LIB_pitch_Normalized_CrossCorrelation_LIB}
        ${FILES_LIB_pitch}
        ${FILES_LIB_Resample_LIB}
        ${FILES_LIB_resample}
        )

target_link_libraries( # Specifies the target library.
        native-lib2
        android
        #${PATH_TO_PRE_BUILT}/libsox.a
        z
        log )