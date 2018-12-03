package com.zq.lyrics.formats.zrce;

public class LrcWord
{
   public int start;
   public int stop;
   public String word;
   public int level;
   public LrcWord(){}
   public LrcWord(int start, int stop, String word, int level){
	   this.start = start;
	   this.stop = stop;
	   this.word = word;
	   this.level = level;
   }
   
}
