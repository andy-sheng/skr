//
//define java class method felied
//ArrayList
//Integer
//CredtpModel
//ImgBufFrame
//ImgBufFormat
//ImgPreProcessWrap.ImgBufMixerConfig
//AudioBufFormat
// Created by 昝晓飞 on 16/7/26.

#ifndef KSYSTREAMERANDROID_JNI_CLASS_DEF_H
#define KSYSTREAMERANDROID_JNI_CLASS_DEF_H

#ifndef JAVA_PACKAGE_PATH
#define JAVA_PACKAGE_PATH                          "org/opencdnunion/media/streamer/"
#endif

///////////// ArrayList Define ////////////////////////////////////////
// Class name
#define JAVA_CLASS_ARRRAYLIST "java/util/ArrayList"

// Method name
#define JAVA_ARRRAYLIST_METHOD_CONSTRUCT    "<init>"
#define JAVA_ARRRAYLIST_METHOD_SIZE    "size"
#define JAVA_ARRRAYLIST_METHOD_GET     "get"
#define JAVA_ARRRAYLIST_METHOD_ADD     "add"
#define JAVA_ARRRAYLIST_METHOD_CLEAR   "clear"
///////////// end ArrayList Define ////////////////////////////////////

///////////////////Integer Define/////////////////////////////////////
//Class name
#define JAVA_CLASS_INTEGER "java/lang/Integer"

//Method name
#define JAVA_INTEGER_METHOD_INIT     "<init>"
#define JAVA_INTEGER_METHOD_INTVALUE    "intValue"
//////////////////end Integer Define/////////////////////////////////

///////////// java class Define ////////////////////////////////////
// Class name
#ifndef JAVA_CLASS_PATH_CREDPTMODEL
#define JAVA_CLASS_PATH_CREDPTMODEL               JAVA_PACKAGE_PATH"framework/CredtpModel"
#endif

// Method name
#define JAVA_CREDPTMODEL_METHOD_CONSTRUCT      "<init>"
#define JAVA_CREDPTMODEL_METHOD_GETKEY         "getKey" //0
#define JAVA_CREDPTMODEL_METHOD_SETKEY         "setKey"
#define JAVA_CREDPTMODEL_METHOD_GETBODY        "getBody"
#define JAVA_CREDPTMODEL_METHOD_SETBODY        "setBody"
#define JAVA_CREDPTMODEL_METHOD_GETTYPE        "getType"
#define JAVA_CREDPTMODEL_METHOD_SETTYPE        "setType"


// Field name
#define JAVA_CREDPTMODEL_FIELD_KEY                 "key"
#define JAVA_CREDPTMODEL_FIELD_BODY                "body"
#define JAVA_CREDPTMODEL_FIELD_TYPE                "type"


// Class name
#ifndef JAVA_CLASS_PATH_IMGBUFFRAME
#define JAVA_CLASS_PATH_IMGBUFFRAME               JAVA_PACKAGE_PATH"framework/ImgBufFrame"
#endif

// Method name
#define JAVA_IMGBUFFRAME_METHOD_CONSTRUCT      "<init>"


// Field name
#define JAVA_IMGBUFFRAME_FIELD_FORMAT                 "format"
#define JAVA_IMGBUFFRAME_FIELD_BUF                "buf"
//#define JAVA_IMGBUFFRAME_FIELD_BUFSIZE                "buf_size"
#define JAVA_IMGBUFFRAME_FIELD_DTS                "dts"
#define JAVA_IMGBUFFRAME_FIELD_PTS                "pts"
#define JAVA_IMGBUFFRAME_FIELD_FLAGS                "flags"

// Class name
#ifndef JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT
#define JAVA_CLASS_PATH_IMGBUFFRAME_FORMAT             JAVA_PACKAGE_PATH"framework/ImgBufFormat"
#endif

// Method name
#define JAVA_IMGBUFFRAME_FORMAT_METHOD_CONSTRUCT      "<init>"


// Field name
#define JAVA_IMGBUFFRAME_FORMAT_FIELD_FORMAT                 "format"
#define JAVA_IMGBUFFRAME_FORMAT_FIELD_ORIENTATION                "orientation"
#define JAVA_IMGBUFFRAME_FORMAT_FIELD_WIDTH                "width"
#define JAVA_IMGBUFFRAME_FORMAT_FIELD_HEIGHT                "height"
#define JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDE                "stride"
#define JAVA_IMGBUFFRAME_FORMAT_FIELD_STRIDENUMER                "strideNum"

// Class name
#ifndef JAVA_CLASS_PATH_IMGBUFMIXERCONFIG
#define JAVA_CLASS_PATH_IMGBUFMIXERCONFIG             JAVA_PACKAGE_PATH"filter/imgbuf/ImgPreProcessWrap$ImgBufMixerConfig"
#endif

// Method name
#define JAVA_IMGBUFMIXERCONFIG_METHOD_CONSTRUCT      "<init>"


// Field name
#define JAVA_IMGBUFMIXERCONFIG_FIELD_X                 "x"
#define JAVA_IMGBUFMIXERCONFIG_FIELD_Y                "y"
#define JAVA_IMGBUFMIXERCONFIG_FIELD_W                "w"
#define JAVA_IMGBUFMIXERCONFIG_FIELD_H                "h"
#define JAVA_IMGBUFMIXERCONFIG_FIELD_ALPHA                "alpha"

// Class name
#ifndef JAVA_CLASS_PATH_AUDIOBUFFORMAT
#define JAVA_CLASS_PATH_AUDIOBUFFORMAT            JAVA_PACKAGE_PATH"framework/AudioBufFormat"
#endif

// Method name
#define JAVA_AUDIOBUFFORMAT_METHOD_CONSTRUCT      "<init>"


// Field name
#define JAVA_AUDIOBUFFORMAT_FIELD_FORMAT                 "sampleFormat"
#define JAVA_AUDIOBUFFORMAT_FIELD_RATE                "sampleRate"
#define JAVA_AUDIOBUFFORMAT_FIELD_CHANNELS                "channels"
#define JAVA_AUDIOBUFFORMAT_FIELD_CODECID             "codecId"

//calss name
#ifndef JAVA_CLASS_PATH_AVFILTERWRAPPER
#define JAVA_CLASS_PATH_AVFILTERWRAPPER            JAVA_PACKAGE_PATH"filter/audio/AVFilterWrapper"
#endif

//Method name
#define JAVA_AVFILTERWRAPPER_METHOD_CONSTRUCT      "<init>"
#define JAVA_AVFILTERWRAPPER_METHOD_ONAUDIOFRAME "onAudioFrame"

#endif //KSYSTREAMERANDROID_JNI_CLASS_DEF_H
