package com.zq.lyrics.event;

public class LrcEvent {

    public static class FinishLoadLrcEvent{
        public  String hash = "";
        public FinishLoadLrcEvent(String hash){
            this.hash = hash;
        }
    }

    public static class LineEndEvent{

    }
}
