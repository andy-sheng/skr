package com.zq.lyrics.event;

public class EventClass {
    public static class FinishLoadLrcEvent{
        public  String hash = "";
        public FinishLoadLrcEvent(String hash){
            this.hash = hash;
        }
    }
}
