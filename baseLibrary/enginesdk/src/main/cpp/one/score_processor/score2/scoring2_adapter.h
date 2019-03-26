
#include <pty.h>
#include <string>
#include "calc_score.hpp"

class Scoring2Adapter {
    typedef struct {
        short *data;
        int len;
    } PushData;
private:
    pthread_t scoringThread;
    CalcScore *calcScore;
    bool isRunning;
    std::vector<PushData> queue;
protected:
public:
    Scoring2Adapter(int sampleRate);

    ~Scoring2Adapter();

    int LoadMelp(std::string filename, int startStamp);

    void Flow(short *data, int len);

    int GetScore(int curTimeStamp);

    void *startScoringThread(void *ptr);

    void destroy();
};

#endif /* BASE_SCORING_H_ */
