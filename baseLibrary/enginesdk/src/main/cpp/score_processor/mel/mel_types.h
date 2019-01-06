#ifndef MEL_TYPES_H
#define MEL_TYPES_H
#include <iostream>
#include <stdlib.h>
using namespace std;

typedef  int16_t t_Scale;
class MelodyNote
{
public:
    int beginTime;
    int endTime;
    int16_t note;
    int exhibitionPos;
    int beginTimeMs;
    int endTimeMs;
    int16_t note_org;

    static int TransNote(int note)
    {
        return (note + 3) % 12;
    }
};

class KeyScale
{
public:
    int beginTime;
    int endTime;
    t_Scale scale;
    int rootNote; // 0 ~ 11 default -1
    KeyScale()
        : beginTime(0), endTime(0), scale(0), rootNote(-1)
    {}
    static t_Scale TransScale(t_Scale scale)
    {
        int bitDiff = 3; // C - A
        return ShiftScale(scale, bitDiff);
    }
    static t_Scale ShiftScale(t_Scale scale, int bitDiff)
    {
        int t1 = scale & 0xfff;
        t1 = t1 << bitDiff;
        int tmpBit = t1 >> 12;
        t1 |= tmpBit;
        scale = t1 & 0xfff;
        return scale;
    }
};

class SongFrame
{
public:
    int beginTime;
    int endTime;
    t_Scale code;

};
/**
 * mel文件中各个轨道类型
 */
enum MelTrackType
{
    MTT_KEYSCALE = 0, //记录某段时间内出现的的音阶序列
    MTT_MELODY = 1, //记录某个时间内的用0-11表示的旋律
    MTT_CHORD = 2, //记录某段时间内的和弦音阶值
    MTT_SONGFRAME = 3, //记录歌曲主副歌结构
    MTT_ARBEAT = 4, //记录歌曲节拍信息,如歌曲谱号、bpm
    MTT_ARPITCH = 5, //记录歌曲某个时间内弹奏的midi值
    MTT_ARSECTION = 6 //记录段落划分信息
};
/**
 * 记录和弦中出现的音阶
 */
typedef KeyScale ChordKeyScale;
/**
 * ARBeat 轨道中数据定义
 * bpm,beatNumPerSection,notetimePerBeat，totalBeatNum，loopStartBeatIndex，loopEndBeatIndex 是在melp文件中标注的，其他需要计算得到
 */
class ARBeat {
public:
    int bpm; //每分钟节拍数
    float bpmfloat;//浮点类型的每分钟节拍数
    int beatNumPerSection;//每一小节的拍数
    int notetimePerBeat;//每拍音符的时值，如4分音符，8分音符，16分音符...

    int timePerBeat;//每个beat的时间
    int timePerSection;//每个section的时间

    int accFrameLenPerBeat;//acc里一个beat的frame长度
    int accFrameLenPerSection;//acc里一个section的frame长度
    int vocalFrameLenPerBeat;//vocal里一个beat的frame长度,在melmanger里计算
    int vocalFrameLenPerSection;//vocal里一个section的frame长度，在melmanger里计算
    ARBeat()
        : bpm(0), bpmfloat(0), beatNumPerSection(0), notetimePerBeat(0), timePerBeat(0), timePerSection(0),
          accFrameLenPerBeat(0), accFrameLenPerSection(0), vocalFrameLenPerBeat(0), vocalFrameLenPerSection(0)
    {}
};

/**
 * ARPitch 轨道中数据定义
 * midiKey , beginTimeMs，endTimeMs 是在melp文件中标注的，其他需要计算得到
 */
class ARPitch {
public:
    int beginTimeMs;
    int endTimeMs;
    int midiKey;

    int beginFrameIndex;//frameindex是根据mel中的时间换算出来的值
    int endFrameIndex;
    ARPitch()
        : beginTimeMs(0), endTimeMs(0), midiKey(0), beginFrameIndex(-1), endFrameIndex(-1)
    {}
};

/**
 * ARSection轨道中数据定义
 * beginTimeMs，endTimeMs，nums 是在melp文件中标注的，其他需要计算得到
 */
class ARSection {
public:
    //原始数据
    int dottedNum;//标记的音符值个数，符点数的多少用于标记大段落或小段落
    vector<int> dottedNotes;//标记的音符值
    int beginTimeMs;//起始时间ms

    //计算得到的数据
    int endTimeMs;//结束时间
    int beginFrameIndex;//开始frameindex是根据mel中的时间换算出来的值
    int endFrameIndex;//结束frameindex
    int beatIndex;//根据开始时间对齐到整数beat后计算得到
    int sectionCnt;//所占的section数

    ARSection()
        : dottedNum(0), beginTimeMs(0), endTimeMs(0), beginFrameIndex(-1), endFrameIndex(-1), beatIndex(-1), sectionCnt(0)
    {}
};
#endif
