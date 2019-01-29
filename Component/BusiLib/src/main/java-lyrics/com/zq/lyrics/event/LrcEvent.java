package com.zq.lyrics.event;

public class LrcEvent {

    public static class FinishLoadLrcEvent{
        public  String hash = "";
        public FinishLoadLrcEvent(String hash){
            this.hash = hash;
        }
    }

    public static class LineEndEvent{
        int lineNum;

        public LineEndEvent(int lineNum) {
            this.lineNum = lineNum;
        }

        public int getLineNum() {
            return lineNum;
        }
    }

    public static class LineStartEvent{
        int lineNum;

        public LineStartEvent(int lineNum) {
            this.lineNum = lineNum;
        }

        public int getLineNum() {
            return lineNum;
        }
    }
}
