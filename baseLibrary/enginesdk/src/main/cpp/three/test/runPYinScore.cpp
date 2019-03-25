//
//  runPYinScore.cpp
//  DetectSingSkill
//
//  Created by WangYi on 2019/3/24.
//  Copyright © 2019 zhouyu. All rights reserved.
//

#include <iostream>
#include "calc_score.hpp"
#include "LTIPitchDetection.hpp"
#include "WavFile.h"

#define BUFFER_LEN 2048

int main(int argc, const char * argv[]) {
    
    const char* vocalFilePath = argv[1];
    
    //16153ms
    string melpFilePath = string(argv[2]);
    
    WavInFile* vocalFile = new WavInFile(vocalFilePath);

    CalcScore *calcScore = new CalcScore(vocalFile->getSampleRate());
    calcScore->LoadMelp(melpFilePath, 16153);
    
    //*************
    LTIPitchDetection* pitchDetetor = new LTIPitchDetection();
    pitchDetetor->Init(vocalFile->getSampleRate(), vocalFile->getNumChannels());
    pitchDetetor->LoadMelp(melpFilePath, 0, 16153+49000);
    //*************
    
    short sInputData[BUFFER_LEN];
    
    int totSamp = 0;
    int score = 0;
    int score2 = 0;
    while(!vocalFile->eof()) {
        int actualLen = vocalFile->read(sInputData, BUFFER_LEN);
        if (actualLen % 2 == 1) {
            actualLen -= 1;
        }
        totSamp += actualLen;
        calcScore->Flow(sInputData, actualLen);
        pitchDetetor->Flow(sInputData, actualLen);
        if (totSamp % (BUFFER_LEN * 100) == 0) {
            //cout <<totSamp << endl;
            score = calcScore->GetScore(16153+totSamp/44.1);
            
            score2 = pitchDetetor->LyricLineDidEndWithCurrentLineIndex(1);
            cout << score << "  "  << score2 << endl;
        }
    }
    score = calcScore->GetScore(16153+46718);
    score2 = pitchDetetor->LyricLineDidEndWithCurrentLineIndex(1);
    cout << score2 << endl;
    
    delete vocalFile;
    delete calcScore;
    return 0;
}
