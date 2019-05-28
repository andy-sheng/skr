//
// Created by 昝晓飞 on 16/7/28.
//

#ifndef KSYSTREAMERANDROID_IMAGEMIXERCONFIG_H
#define KSYSTREAMERANDROID_IMAGEMIXERCONFIG_H
class  ImageMixerConfig {
public:
    int x;
    int y;
    int w;
    int h;
    int alpha;

    ImageMixerConfig() {}
    ImageMixerConfig(int x, int y, int w, int h, int alpha){
        this->x = x;
        this->y = y;
        this->w = w;
        this->h = h;
        this->alpha = alpha;
    }
};
#endif //KSYSTREAMERANDROID_IMAGEMIXERCONFIG_H
