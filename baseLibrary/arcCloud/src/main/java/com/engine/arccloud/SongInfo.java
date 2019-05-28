package com.engine.arccloud;

import java.io.Serializable;

/**
 * "external_ids": {},
 * "play_offset_ms": 1410,
 * "external_metadata": {},
 * "title": "两只老虎",
 * "score": 0.69,
 * "album": {
 * "name": "两只老虎",
 * "image": ""
 * },
 * "acrid": "a8c2abbc21f3050f5eb97c2d8ac906da",
 * "result_from": 2,
 * "artists": [{
 * "name": "白目宝宝"
 * }]
 */
public class SongInfo implements Serializable {
    String title;
    int play_offset_ms;
    float score;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getPlay_offset_ms() {
        return play_offset_ms;
    }

    public void setPlay_offset_ms(int play_offset_ms) {
        this.play_offset_ms = play_offset_ms;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "SongInfo{" +
                "title='" + title + '\'' +
                ", play_offset_ms=" + play_offset_ms +
                ", score=" + score +
                '}';
    }
}
