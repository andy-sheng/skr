#ifndef MEL_CHORD_ANA_H
#define MEL_CHORD_ANA_H

#include <string>
#include <vector>
#include <set>
#include <stdlib.h>
#include "mel_types.h"
//#include <inttypes.h>
#define _MELP_VERSION 10

class MelChordAna
{

public:
    static const int SupportKey = 0x1;
    static const int SupportMelody = 0x2;
    static const int SupportChord = 0x4;
    static const int SupportSongFrame = 0x8;
    static const int SupportARBeat = 0x10;
    static const int SupportARPitch = 0x20;
    static const int SupportARSection = 0x40;

public:
    MelChordAna(int sampleRate);
    void SetVocalSampleRate(int sampleRate);
    ~MelChordAna();

    void InitByMelFile(std::string filename, int tag);
    void InitByMelFileMock(std::string filename, int tag);
    void InitByPlainMelFile(const std::string &filename, int tag);

    int GetKeyIdx(int sampleCount);
    int GetChordIdx(int sampleCount);
    bool IsInChorus(int sampleCount);
    int GetKeyScale();

    bool IsNoteInChord(int note);
    bool IsNoteInChordKey(int note);
    bool IsNoteInKey(int note);
    void PrintChord();

    int GetSupportFlag();
    void PitchShift(int pitchDiff);

    int GetToneScoreTempl(std::vector<MelodyNote>& ret);

    int GetCurARPitchIdx(int sampleCount);
    int GetCurARSectionIdx(int sampleCount);

    ARBeat GetARBeat();
    ARPitch GetCurARPitch();
    ARSection GetCurARSection();
    std::vector<KeyScale> GetAllKeyScale() { return m_keyScale;}
    std::vector<ARPitch> GetAllARPitch() { return m_arPitch; };
    std::vector<ARSection> GetAllARSection() { return m_arSection;};
    std::vector<MelodyNote> GetAllMelodyNote() { return m_melNote; };
private:

    void _InitByOldMelFile(std::string filename);
    void _Init(int tag);
    void _AnalyzeMelString(const std::string &contents);
    void _UpdateSampleCount(int sampleCount);
    int _NoteDistance(int a, int b);
    void _FormatTime();
    std::string _TrimContents(const std::string &contents);
    void _ComputeKeyScale();
    void _ComputeChordKeyScale();
    int _CheckARSectionData(vector<ARSection>& _arSectionVector);

    std::vector<int > m_note;
    int m_tag;

    int m_supportFlag;

    //std::vector<std::vector<int> > m_scale;

    int32_t m_diffMax;

    //std::vector<int> m_scalePartIdxList;

    //std::vector<std::set<NoteType> > m_scaleSetList;

    //int m_scaleIdx;
    int m_sampleRate;
    int m_sampleCount;

    int m_vocalSampleRate;

    int m_chordIdx;
    int m_keyIdx;
    //int m_melIdx;
    int m_songFrameIdx;
    int m_melMaxPos;
    int m_pitchDiff;
    std::vector<MelodyNote> m_melNote;
    std::vector<KeyScale > m_keyScale;
    std::vector<KeyScale > m_chordKeyScale;
    std::vector<KeyScale > m_chordScale;
    std::vector<SongFrame > m_songFrame;

    int m_arPitchIdx;
    int m_arSectionIdx;
    ARBeat m_arBeat;
    std::vector<ARPitch> m_arPitch;
    std::vector<ARSection> m_arSection;
};

#endif
