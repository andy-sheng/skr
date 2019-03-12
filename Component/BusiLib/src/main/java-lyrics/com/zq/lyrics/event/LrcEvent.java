package com.zq.lyrics.event;

public class LrcEvent {

    public static class FinishLoadLrcEvent {
        public String hash = "";

        public FinishLoadLrcEvent(String hash) {
            this.hash = hash;
        }
    }

    /**
     * 行开始
     */
    public static class LyricLineStartEvent {
        public int lineNum;
        public Object extra;
        public LyricLineStartEvent() {
        }
    }

    /**
     * 行结束
     */
    public static class LineLineEndEvent {
        public int lineNum;
        public Object extra;
        public LineLineEndEvent() {
        }

    }

    /**
     * 歌词开始
     */
    public static class LyricStartEvent {
        public int lineNum;
        public Object extra;
        public LyricStartEvent() {
        }

    }
}
