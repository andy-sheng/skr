cmake_minimum_required(VERSION 3.4.1)

add_definitions(-DPERFORMANCE_TEST)


set(PATH_TO_MEDIACORE ${CMAKE_SOURCE_DIR}/src/main/cpp/one)
set(PATH_TO_JNI_LAYER ${CMAKE_SOURCE_DIR}/src/main/jni/one)
set(PATH_TO_THIRDPARTY ${PATH_TO_MEDIACORE}/thirdparty)
set(PATH_TO_PRE_BUILT ${PATH_TO_THIRDPARTY}/prebuilt/${ANDROID_ABI})

MESSAGE( STATUS "this var key = ${PATH_TO_MEDIACORE}.")

add_definitions("
-DGL_GLEXT_PROTOTYPES
-DEGL_EGLEXT_PROTOTYPES
")

include_directories(${PATH_TO_MEDIACORE}/)
include_directories(${PATH_TO_MEDIACORE}/common/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/score/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/score2/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/score2/math/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/score2/profiler/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/score2/ne10/inc)
include_directories(${PATH_TO_MEDIACORE}/score_processor/f0_cal/baseband/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/f0_cal/PYin/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/fft_wrapper/)
include_directories(${PATH_TO_MEDIACORE}/score_processor/mel/)
include_directories(${PATH_TO_MEDIACORE}/audio_effect/)
include_directories(${PATH_TO_MEDIACORE}/audio_effect/sox_effect_param)
include_directories(${PATH_TO_MEDIACORE}/audio_effect_filter/)
include_directories(${PATH_TO_MEDIACORE}/audio_effect_filter_impl/)
include_directories(${PATH_TO_MEDIACORE}/audio_effect_impl/)
include_directories(${PATH_TO_MEDIACORE}/audio_effect_processor/)
include_directories(${PATH_TO_MEDIACORE}/audio_effect_processor/live/)
# External AudioEffect File
include_directories(${PATH_TO_MEDIACORE}/external_audio_effect/)
include_directories(${PATH_TO_MEDIACORE}/external_audio_effect/reverb_inc/)


file(GLOB FILES_JNI_LAYER "${PATH_TO_JNI_LAYER}/*.cpp")
file(GLOB FILES_LIB_COMMON "${PATH_TO_MEDIACORE}/common/*.cpp")
file(GLOB FILES_SCORE_PROCESSOR "${PATH_TO_MEDIACORE}/score_processor/score/*.cpp")
file(GLOB FILES_SCORE_PROCESSOR2 "${PATH_TO_MEDIACORE}/score_processor/score2/*.cpp")
file(GLOB FILES_SCORE_PROCESSOR2_MATH "${PATH_TO_MEDIACORE}/score_processor/score2/math/*.cpp")
file(GLOB FILES_SCORE_PROCESSOR2_PROFILER "${PATH_TO_MEDIACORE}/score_processor/score2/profiler/*.cpp")
file(GLOB FILES_SCORE_F0_CAL_BASEBAND "${PATH_TO_MEDIACORE}/score_processor/f0_cal/baseband/*.cpp")
file(GLOB FILES_SCORE_F0_CAL_PYIN "${PATH_TO_MEDIACORE}/score_processor/f0_cal/PYin/*.cpp")
file(GLOB FILES_SCORE_FFT_WRAPPER "${PATH_TO_MEDIACORE}/score_processor/fft_wrapper/*.cpp")
file(GLOB FILES_SCORE_MEL "${PATH_TO_MEDIACORE}/score_processor/mel/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_MODEL "${PATH_TO_MEDIACORE}/audio_effect/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_SOX_PARAM_MODEL "${PATH_TO_MEDIACORE}/audio_effect/sox_effect_param/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_FILTER "${PATH_TO_MEDIACORE}/audio_effect_filter/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_FILTER_FADE_IMPL "${PATH_TO_MEDIACORE}/audio_effect_filter_impl/fade/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_FILTER_SOX_EFFECT_IMPL "${PATH_TO_MEDIACORE}/audio_effect_filter_impl/sox_effect/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_FILTER_SPECIFIC_IMPL "${PATH_TO_MEDIACORE}/audio_effect_filter_impl/specific/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_FILTER_VOLUME_ADJUST_IMPL "${PATH_TO_MEDIACORE}/audio_effect_filter_impl/volume_adjust/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_PROCESSOR "${PATH_TO_MEDIACORE}/audio_effect_processor/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_PROCESSOR_LIVE "${PATH_TO_MEDIACORE}/audio_effect_processor/live/*.cpp")
file(GLOB FILES_AUDIO_EFFECT_PROCESSOR_POST "${PATH_TO_MEDIACORE}/audio_effect_processor/post/*.cpp")
# External AudioEffect File
file(GLOB CPP_FILES_IN_REVERB        "${PATH_TO_MEDIACORE}/external_audio_effect/*.cpp")
file(GLOB CPP_FILES_IN_REVERB_SRC        "${PATH_TO_MEDIACORE}/external_audio_effect/reverb_src/*.cpp")
file(GLOB CPP_FILES_IN_BIQUAD_FILTERS    "${PATH_TO_MEDIACORE}/external_audio_effect/reverb_src/BiquadFilters/*.cpp")
file(GLOB CPP_FILES_OPENAL_IN_OPENAL     "${PATH_TO_MEDIACORE}/external_audio_effect/reverb_src/openal/*.cpp")
file(GLOB CPP_FILES_IN_VERB              "${PATH_TO_MEDIACORE}/external_audio_effect/reverb_src/verb/*.cpp")

add_library(native-lib1 SHARED
        ${FILES_JNI_LAYER}
        ${FILES_LIB_COMMON}
        ${FILES_SCORE_PROCESSOR}
        ${FILES_SCORE_PROCESSOR2}
        ${FILES_SCORE_PROCESSOR2_MATH}
        ${FILES_SCORE_PROCESSOR2_PROFILER}
        ${FILES_SCORE_F0_CAL_BASEBAND}
        ${FILES_SCORE_F0_CAL_PYIN}
        ${FILES_SCORE_FFT_WRAPPER}
        ${FILES_SCORE_MEL}
        ${FILES_AUDIO_EFFECT_MODEL}
        ${FILES_AUDIO_EFFECT_SOX_PARAM_MODEL}
        ${FILES_AUDIO_EFFECT_FILTER}
        ${FILES_AUDIO_EFFECT_FILTER_FADE_IMPL}
        ${FILES_AUDIO_EFFECT_FILTER_SOX_EFFECT_IMPL}
        ${FILES_AUDIO_EFFECT_FILTER_SPECIFIC_IMPL}
        ${FILES_AUDIO_EFFECT_FILTER_VOLUME_ADJUST_IMPL}
        ${FILES_AUDIO_EFFECT_PROCESSOR}
        ${FILES_AUDIO_EFFECT_PROCESSOR_POST}
        ${FILES_AUDIO_EFFECT_PROCESSOR_LIVE}
        ${CPP_FILES_IN_REVERB}
        ${CPP_FILES_IN_REVERB_SRC}
        ${CPP_FILES_IN_BIQUAD_FILTERS}
        ${CPP_FILES_OPENAL_IN_OPENAL}
        ${CPP_FILES_IN_VERB}
        )

target_link_libraries( # Specifies the target library.
        native-lib1
        android
        ${PATH_TO_PRE_BUILT}/libsox.a
        log
        NE10
        m
        z
        )




