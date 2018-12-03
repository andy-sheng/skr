//package com.zq.lyrics.formats.zrce;
//
//import java.io.File;
//import java.util.List;
//
//import android.content.Context;
//import android.content.res.TypedArray;
//import android.graphics.Bitmap;
//import android.graphics.Canvas;
//import android.graphics.Color;
//import android.graphics.Paint;
//import android.graphics.Paint.FontMetricsInt;
//import android.graphics.Rect;
//import android.graphics.Typeface;
//import android.graphics.drawable.BitmapDrawable;
//import android.os.CountDownTimer;
//import android.os.Handler;
//import android.os.Looper;
//import android.text.TextPaint;
//import android.text.TextUtils;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.view.View;
//import android.view.animation.AccelerateDecelerateInterpolator;
//import android.widget.Scroller;
//
//import com.changba.studio.R;
//import com.changba.context.KTVApplication;
//import com.nineoldandroids.animation.ValueAnimator;
//import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;
//
///**
// * 歌词渲染（整合逐字/逐行）
// *
// * @author jz
// *
// */
//public class VerbatimLrcView extends View {
//
//	public static final String TAG = "VerbatimLrcView";
//
//	private static final int LINES_COUNT_DEFAULT = 3;
//
//	// 逐行滚动动画间隔
//	private static final int DURATION_FOR_LRC_SCROLL = 1000;
//	// 进唱倒计时每个点一秒钟
//	private static final int POINT_TIME = 1000;
//
//	// 非高亮歌词的文字大小
//	private static final int COMMON_FONT_SIZE = KTVApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.lrc_text_size);
//	// 高亮歌词的文字大小
//	private static final int HIGHLIGHT_FONT_SIZE = KTVApplication.getInstance().getResources().getDimensionPixelSize(R.dimen.lrc_highlight_text_size);
//
//	private static final int UNHIGH_LIGHT_FONT_COLOR = Color.argb(255, 255, 255, 255);
//	private static final int HIGH_LIGHT_FONT_COLOR = Color.argb(255, 38, 243, 168);
//
//	private static final int WHITE_FONT_COLOR_1 = Color.argb(150, 255, 255, 255);
//	private static final int WHITE_FONT_COLOR_2 = Color.argb(57, 255, 255, 255);
//	private static final int WHITE_FONT_COLOR_3 = Color.argb(31, 255, 255, 255);
//
//	private static final int GREEN_FONT_COLOR_1 = Color.argb(255, 38, 243, 168);
//	private static final int GREEN_FONT_COLOR_2 = Color.argb(57, 38, 243, 168); //57
//	private static final int GREEN_FONT_COLOR_3 = Color.argb(31, 38, 243, 168); //31
//
//	// 上/下方显示几行歌词
//	private int mLinesCount = LINES_COUNT_DEFAULT;
//	// 歌词间距
//	private int mLineSpace = 0;
//	// 当前渲染的歌词行
//	private int mCurrentLineIndex = 0;
//	// 上次渲染的歌词行
//	private int mLastLineIndex = 0;
//	// 当前播放的时间
//	private int mCurrenttime = 0;
//	// 歌曲开始的时间
//	private int mStartSingTime = 0;
//	// 歌曲进唱延时(如果进唱时间小于4s)
//	private int mStartSingTimeDelay = 0;
//
//	private int mWidth = 0;
//	private int mHeight = 0;
//	private Scroller mScroller;
//	// 歌曲开始前倒计时Drawable
//	private BitmapDrawable mCountDownDrawable;
//	// 控制歌词水平滚动的属性动画
//	private ValueAnimator mHorizontalScrollAnimator;
//	// 歌词水平滚动的offset
//	private int mSentenceLineScrollX = 0;
//	private int mStartPointOffset = 0;
//	// 根据整个View空间计算可以绘制的行数
//	private int mMaxRows = 0;
//	// 当前歌词scale参数
//	private float mHighLightFontScaleFactor = 1F;
//	// 当前渲染的画笔
//	private TextPaint mCurrentPaint;
//	// 解析的逐字歌词
//	private List<LrcSentence> mVerbatimSentences = null;
//	// 解析的逐行歌词
//	private List<Sentence> mVerticalSentences = null;
//	// 标识是逐行/字歌词
//	private volatile boolean isVerbatim = true;
//	// 标识歌词是否解析完
//	private volatile boolean hasParse = false;
//	// 歌词文件
//	private File mFile = null;
//	// 歌曲名字
//	private String mSongName = "";
//
//	//方便计算进唱倒计时
//	private CountDownTimer mStartCountDownTimer;
//
//	private Handler mHandler = new Handler();
//
//	// 标志是否是视频合唱邀请
//	private boolean mIsMVDuteInvited = false;
//
//	public VerbatimLrcView(Context context) {
//		super(context);
//	}
//
//	public VerbatimLrcView(Context context, AttributeSet attr) {
//		super(context, attr);
//
//		TypedArray a = context.obtainStyledAttributes(attr, R.styleable.VerbatimLrcView);
//
//		mLinesCount = a.getInt(R.styleable.VerbatimLrcView_lyrics_line_num, LINES_COUNT_DEFAULT);
//		mLineSpace = a.getDimensionPixelSize(
//				R.styleable.VerbatimLrcView_lyrics_line_space,
//				getResources().getDimensionPixelSize(R.dimen.lrc_margin_top));
//
//		a.recycle();
//
//		mScroller = new Scroller(context);
//
//		mCountDownDrawable = ((BitmapDrawable) getResources().getDrawable(R.drawable.countdown_icon));
//
//	}
//
//	public void setIsMVDuteInvited(boolean isDute) {
//		this.mIsMVDuteInvited = isDute;
//	}
//
//	private Paint getCurrentPaint() {
//		if (mCurrentPaint == null) {
//			mCurrentPaint = new TextPaint();
//			mCurrentPaint.setAntiAlias(true);
//			mCurrentPaint.setTypeface(Typeface.DEFAULT);
//		}
//		return mCurrentPaint;
//	}
//
//	/**
//	 * 是否支持打分
//	 * @return
//	 */
//	public boolean isSupportScore() {
//		if (isVerbatim && mVerbatimSentences != null
//				&& !mVerbatimSentences.isEmpty()) {
//			return true;
//		}
//		return false;
//	}
//
//	public List<LrcSentence> getVerbatimSentences() {
//		return this.mVerbatimSentences;
//	}
//
//	private LrcOwnerDetector detector;
//	/**
//	 * 初始化歌词View
//	 *
//	 * @param file
//	 */
//	public void dataInit(File file, String songName, ILyricParserCallback cb, LrcOwnerDetector detector) {
//		this.detector = detector;
//		this.dataInit(file, songName, cb);
//	}
//	public void dataInit(File file, String songName, ILyricParserCallback cb) {
////		android.util.Log.d("jz", "VerbatimLrcView dataInit() enter.....");
//
//		if (file == null
//				|| !file.exists()) {
//			return;
//		}
//
//		this.mSongName = songName;
//
//		reset();
//
//		if (mFile == null || !mFile.equals(file)) { //开始解析歌词
//			mFile = file;
//			hasParse = false;
//
//			mScroller.forceFinished(true);
//			mScroller.setFinalY(0);
//
//			doParse(cb);
////			android.util.Log.d("jz", "VerbatimLrcView dataInit() step1..");
//
//		} else {
//			mScroller.forceFinished(true);
//			mScroller.setFinalY(0);
//
//			if (!startSingTimeDelay(mStartSingTime, cb)) {
//				if (cb != null) {
//					cb.onParseComplete(file, hasParse);
//				}
//			}
//
//			hasParse = true;
//
//			postInvalidate();
//
////			android.util.Log.d("jz", "VerbatimLrcView dataInit() step2 mStartSingTime="+mStartSingTime+"  mStartSingTimeDelay="+mStartSingTimeDelay);
//		}
//
//	}
//
//	public void stop() {
//		if (mStartCountDownTimer != null) {
//			mStartCountDownTimer.cancel();
//		}
////		android.util.Log.d("jz", "VerbatimLrcView stop().......");
//	}
//
//	private void doParse(ILyricParserCallback cb) {
//		new Thread(new DoParseRunnable(cb)).start();
//	}
//
//	private class DoParseRunnable implements Runnable {
//
//		private ILyricParserCallback mParseCb;
//
//		public DoParseRunnable(ILyricParserCallback cb) {
//			this.mParseCb = cb;
//		}
//
//		@Override
//		public void run() {
//
//			boolean needSingTimeDelay = false;
//
//			if (mFile != null && mFile.exists()) {
//
//				final SongFileParser parser = new SongFileParser();
//				parser.formatLrc(mFile);
//
//				if (parser.isLineMode()) { //逐行歌词
//					isVerbatim = false;
//					Lyric lyric = new Lyric(mFile.getPath(), mSongName);
//					mVerticalSentences = lyric.list;
//					if (mVerticalSentences == null
//							|| mVerticalSentences.isEmpty()) {
//						hasParse = false;
//					} else {
//						hasParse = true;
//					}
//				} else { //逐字歌词
//					isVerbatim = true;
//					mVerbatimSentences = parser.getSentences();
//					if (mVerbatimSentences == null
//							|| mVerbatimSentences.isEmpty()) {
//						hasParse = false;
//					} else {
//						hasParse = true;
//					}
//				}
//
//				if (hasParse) {
//					mStartSingTime = parser.getStartTime();
//					needSingTimeDelay = startSingTimeDelay(mStartSingTime, mParseCb);
//				}
//
//			} else {
//				hasParse = false;
//			}
//
//
//			if (!needSingTimeDelay) {
//				if (mParseCb != null) {
//					mParseCb.onParseComplete(mFile, hasParse);
//				}
//			}
//
//			postInvalidate();
//
////			android.util.Log.d("jz", "DoParseRunnable run() mStartSingTime="+mStartSingTime+"  mStartSingTimeDelay="+mStartSingTimeDelay);
//
//		}
//
//	}
//
//	private boolean startSingTimeDelay(int startSingTime, final ILyricParserCallback cb) {
//		boolean ret = false;
//
//		if (startSingTime < 4000) {
//			mStartSingTimeDelay = 4000-startSingTime;
//			ret = true;
//		} else {
//			mStartSingTimeDelay = 0;
//		}
//
////		android.util.Log.d("jz", "startSingTimeDelay() mStartSingTimeDelay="+mStartSingTimeDelay);
//
//		startCountDownTimer(cb);
//
//		return ret;
//	}
//
//	private void startCountDownTimer(final ILyricParserCallback cb) {
//		boolean isMain = (Looper.myLooper() == Looper.getMainLooper());
//		if (isMain) {
//			if (mStartCountDownTimer != null) {
//				mStartCountDownTimer.cancel();
//			}
//
//			if (mStartSingTimeDelay != 0) {
//
//				mStartCountDownTimer = new CountDownTimer(mStartSingTimeDelay, 10) {
//					@Override
//					public void onTick(long millisUntilFinished) {
////						android.util.Log.d("jz", "startSingTimeDelay() CountDownTimer onTick() millisUntilFinished="+millisUntilFinished);
//						mStartSingTimeDelay = (int) millisUntilFinished;
//
//						invalidate();
//					}
//					@Override
//					public void onFinish() {
////						android.util.Log.d("jz", "startSingTimeDelay() CountDownTimer onFinish()....");
//						if (cb != null) {
//							cb.onParseComplete(mFile, hasParse);
//						}
//					}
//
//				}.start();
//			}
//
//		} else {
//			mHandler.post(new Runnable() {
//				@Override
//				public void run() {
//					startCountDownTimer(cb);
//				}
//			});
//		}
//
//	}
//
//	private void reset() {
////		android.util.Log.d("jz", "VerbatimLrcView reset()....");
//		mCurrentLineIndex = mLastLineIndex = 0;
//		mSentenceLineScrollX = mStartPointOffset = 0;
//		mHighLightFontScaleFactor = 1F;
//		mCurrenttime = 0;
//		started = false;
//	}
//
//	public void restart(int seekLineStartTime) {
//		mStartSingTime = seekLineStartTime;
//		mStartSingTimeDelay = 0;
//		started = false;
//		Log.i(TAG, "restart() seekLineStartTime="+seekLineStartTime);
//	}
//
//	/**
//	 * 逐字渲染当前行
//	 *
//	 * @param canvas
//	 * @param curSentence
//	 * @param textRect
//	 */
//	private void drawLrcWordsOneLine(Canvas canvas, LrcSentence curSentence, Rect textRect, Paint paint, boolean needScrll) {
//		String fulltext = Null2String(curSentence.fulltxt);
//		if (TextUtils.isEmpty(fulltext)) {
//			return;
//		}
//
//		List<LrcWord> currentWords = curSentence.words;
//		if (curSentence.words.size() > 0) {
//			int currentWordsLastWordIndex = currentWords.size() - 1;
//			long sentenceStartTime = currentWords.get(0).start;
//			long sentenceEndTime = currentWords.get(currentWordsLastWordIndex).stop;
//
//			if (mCurrenttime >= currentWords.get(currentWordsLastWordIndex).stop) {
//				drawHighLightWords(canvas, fulltext, textRect, textRect.width(), paint, sentenceStartTime, sentenceEndTime);
//
//			} else {
//				int wordIndex = -1;
//				for (int i = 0; i < currentWords.size(); i++) {
//					int stopTime = currentWords.get(i).stop;
//					int startTime = currentWords.get(i).start;
//					if (mCurrenttime >= startTime
//							&& mCurrenttime <= stopTime) {
//						wordIndex = i;
//						break;
//					}
//				}
//
//				if (wordIndex >= 0) {
//					int totalHighLightLetter = 0;
//					for (int i=0;i<wordIndex;i++) {
//						totalHighLightLetter += Null2String(currentWords.get(i).word).length();
//					}
//
//					int cutLen = 0;
//					if (totalHighLightLetter > 0) {
//						cutLen += paint.measureText(fulltext, 0, totalHighLightLetter);
//					}
//
//					LrcWord currentWord = currentWords.get(wordIndex);
//					String wordText = Null2String(currentWord.word);
//
//					float rate = (float) (mCurrenttime - currentWord.start) / (float) (currentWord.stop - currentWord.start);
//					float cutW = paint.measureText(wordText);
//					cutLen += Float2Int(rate * cutW);
//
//					if (needScrll) {
//						int scrollX = textRect.left;
//						int remain = textRect.width()-Math.abs(scrollX)-mWidth;
//						if (remain > 0) {
//							int toLeft = cutLen-Math.abs(scrollX);
//							int distance = Math.min(toLeft, remain);
//							//计算滚动的时间
//							int total = 0;
//							int i=wordIndex+1;
//							for (;i<currentWords.size();i++) {
//								total += paint.measureText(Null2String(currentWords.get(i).word));
//								if (total>distance) {
//									break;
//								}
//							}
//
//							int duration = 0;
//							if (i >= currentWords.size()) {
//								duration = currentWord.stop - currentWord.start;
//							} else {
//								LrcWord tmp = currentWords.get(i);
//								duration = (tmp.start+tmp.stop)/2 - (currentWord.start+currentWord.stop)/2;
//							}
//							duration = Math.max(duration, 0);
//							startHorizontalScroll(scrollX, scrollX-distance, duration);
//
//						}
//
//					}
//
//					drawHighLightWords(canvas, fulltext, textRect, cutLen, paint, sentenceStartTime, sentenceEndTime);
//					drawUnHighLightWords(canvas, fulltext, textRect, cutLen, paint, sentenceStartTime, sentenceEndTime);
//
//				} else {
//					drawUnHighLightWords(canvas, fulltext, textRect, 0, paint, sentenceStartTime, sentenceEndTime);
//				}
//
//			}
//		}
//	}
//
//
//	private void drawUnHighLightWords(Canvas canvas,
//			String fulltext, Rect textRect, int cutLen, Paint paint,
//			long sentenceStartTime, long sentenceEndTime) {
//		paint.setColor(getCurrentSingUnHighLightWordColor(sentenceStartTime, sentenceEndTime));
//		canvas.save();
//
//		//扩充高度
//		int top = textRect.top-mLineSpace/2;
//		int bottom = textRect.bottom+mLineSpace/2;
//		canvas.clipRect(textRect.left+cutLen, top, textRect.right, bottom);
//		drawText(canvas, fulltext, textRect, paint);
//		canvas.restore();
//
//	}
//
//
//	private void drawHighLightWords(Canvas canvas,
//			String fulltext, Rect textRect, int cutLen, Paint paint,
//			long sentenceStartTime, long sentenceEndTime) {
//		paint.setColor(getCurrentSingHighLightWordColor(sentenceStartTime, sentenceEndTime));
//		canvas.save();
//
//		//扩充高度
//		int top = textRect.top-mLineSpace/2;
//		int bottom = textRect.bottom+mLineSpace/2;
//		canvas.clipRect(textRect.left, top, textRect.left+cutLen, bottom);
//		drawText(canvas, fulltext, textRect, paint);
//		canvas.restore();
//
//	}
//
//	/**
//	 * 是否解析完成
//	 * @return
//	 */
//	private boolean hasParse() {
//		if(!hasParse){
//			return false;
//		}
//		return true;
//	}
//
//
//	private boolean started = false;
//	@Override
//	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
//		if(!hasParse()){
//			return;
//		}
//
////		android.util.Log.d("jz", "VerbatimLrcView onDraw  mCurrenttime="+mCurrenttime
////				+"  height="+mHeight+"  width="+mWidth
////				+"  started="+started+"  isVerbatim="+isVerbatim+"  mStartSingTime="+mStartSingTime
////				+"  mStartSingTimeDelay="+mStartSingTimeDelay);
//
//		if (isVerbatim) {
//			drawVerbatimLrc(canvas);
//
//		} else {
//			drawVerticalLrc(canvas);
//
//		}
//
//		int time = (mStartSingTime+mStartSingTimeDelay)-mCurrenttime;
//		if (time < 0 && !started) {
//			started = true;
//		}
//		if (time > 0 && time < 4*POINT_TIME && !started) {
//			drawStartPoint(canvas, mCurrenttime);
//		}
//
//	}
//
//	private int Float2Int(float f) {
//		return Math.round(f);
//	}
//
//	private String Null2String(String s) {
//		if (s == null) {
//			return "";
//		}
//		return s;
//	}
//
//
//	/**
//	 * 渲染逐行歌词
//	 *
//	 * @param canvas
//	 */
//	private void drawVerticalLrc(Canvas canvas) {
//		if (mVerticalSentences == null || mVerticalSentences.isEmpty()) {
//			return;
//		}
//
//		if (mMaxRows <= 0) {
//			mMaxRows = (getHeight() - HIGHLIGHT_FONT_SIZE)/(COMMON_FONT_SIZE+mLineSpace)+2+1;
//		}
//
//		int upMinRow = mCurrentLineIndex - (mMaxRows-1)/2;
//		int downMaxRaw = mCurrentLineIndex + (mMaxRows-1)/2;
//
//		upMinRow = Math.max(upMinRow, 0);
//		downMaxRaw = Math.min(downMaxRaw, mVerticalSentences.size()-1);
//
//		int rowY = mHeight/2+upMinRow*(COMMON_FONT_SIZE+mLineSpace);
//
//		for (int i = upMinRow; i <= downMaxRaw; i++) {
//
//			Sentence currentSentence = mVerticalSentences.get(i);
//			String text = Null2String(currentSentence.getContent());
//
//			long sentenceStartTime = currentSentence.getFromTime();
//			long sentenceEndTime = currentSentence.getToTime();
//
//			if(i == mCurrentLineIndex){//画高亮歌词
//				Paint paint = getCurrentPaint();
//				int textSize = Float2Int(
//						COMMON_FONT_SIZE+(HIGHLIGHT_FONT_SIZE-COMMON_FONT_SIZE)*mHighLightFontScaleFactor);
//				paint.setTextSize(textSize);
//				paint.setColor(getCurrentSingHighLightWordColor(sentenceStartTime, sentenceEndTime));
//
//				int textSpace = mWidth;
//				int textWidth = Float2Int(paint.measureText(text));
//
//				if (textWidth <= textSpace) {
//					mStartPointOffset =  (textSpace-textWidth)/2;
//					drawText(canvas, text,
//							getTextRect(textWidth, mStartPointOffset, rowY, paint),
//							paint);
//
//				} else {
//					mStartPointOffset = 0;
//					drawText(canvas, text,
//							getTextRect(textWidth, mSentenceLineScrollX, rowY, paint),
//							paint);
//
//				}
//
//			} else {
//				Paint paint = getCurrentPaint();
//
//				float textSize;
//				if(i == mLastLineIndex) {
//					textSize = HIGHLIGHT_FONT_SIZE-(
//							HIGHLIGHT_FONT_SIZE-COMMON_FONT_SIZE)*mHighLightFontScaleFactor;
//				}else {
//					textSize = COMMON_FONT_SIZE;
//				}
//
//				int distance = Math.abs(i-mCurrentLineIndex);
//				int textColor = getOthersWordColor(distance, sentenceStartTime, sentenceEndTime);
//
//				paint.setTextSize(textSize);
//				paint.setColor(textColor);
//
//				int textSpace = mWidth;
//				int textWidth = Float2Int(paint.measureText(text));
//				int textX = Math.max((textSpace-textWidth)/2, 0);
//
//				drawText(canvas, text,
//						getTextRect(textWidth, textX, rowY, paint), paint);
//			}
//
//			rowY += COMMON_FONT_SIZE + mLineSpace;
//
//		}
//
//	}
//
//
//	/**
//	 * 渲染逐字歌词
//	 *
//	 * @param canvas
//	 */
//	private void drawVerbatimLrc(Canvas canvas) {
//		if(mVerbatimSentences == null || mVerbatimSentences.isEmpty()){
//			return;
//		}
//
//		if (mMaxRows <= 0) {
//			mMaxRows = (getHeight() - HIGHLIGHT_FONT_SIZE)/(COMMON_FONT_SIZE+mLineSpace)+2+1;
//		}
//
//		int upMinRow = mCurrentLineIndex - (mMaxRows-1)/2;
//		int downMaxRaw = mCurrentLineIndex + (mMaxRows-1)/2;
//
//		upMinRow = Math.max(upMinRow, 0);
//		downMaxRaw = Math.min(downMaxRaw, mVerbatimSentences.size()-1);
//
//		int rowY = mHeight/2+upMinRow*(COMMON_FONT_SIZE+mLineSpace);
//
////		android.util.Log.d("jz", "VerbatimLrcView drawVerbatimLrc() mMaxRows="+mMaxRows+"  mCurrentLineIndex="+mCurrentLineIndex
////				+"  mLastLineIndex="+mLastLineIndex+"  upMinRow="+upMinRow+"  downMaxRaw="+downMaxRaw+"  rowY="+rowY);
//
//		for (int i = upMinRow; i <= downMaxRaw; i++) {
//
//			LrcSentence currentSentence = mVerbatimSentences.get(i);
//			String text = Null2String(currentSentence.fulltxt);
//
//			long sentenceStartTime = 0;
//			long sentenceEndTime = 0;
//			if (currentSentence.words != null
//					&& !currentSentence.words.isEmpty()) {
//				sentenceStartTime = currentSentence.words.get(0).start;
//				sentenceEndTime = currentSentence.words.get(currentSentence.words.size()-1).stop;
//			}
////			android.util.Log.d("jz", getClass().getName()+"  drawVerbatimLrc() current text="+text);
//
//			if(i == mCurrentLineIndex){//画高亮歌词
//				Paint paint = getCurrentPaint();
//				int textSize = Float2Int(
//						COMMON_FONT_SIZE+(HIGHLIGHT_FONT_SIZE-COMMON_FONT_SIZE)*mHighLightFontScaleFactor);
//				paint.setTextSize(textSize);
//
//				int textSpace = mWidth;
//				int textWidth = Float2Int(paint.measureText(text));
//
//				if (textWidth <= textSpace) {
//					mStartPointOffset =  (textSpace-textWidth)/2;
//					drawLrcWordsOneLine(canvas, currentSentence,
//							getTextRect(textWidth, mStartPointOffset, rowY, paint), paint, false);
//
//				} else {
//					mStartPointOffset = 0;
//					boolean needAnim = true;
//					if (mHorizontalScrollAnimator != null) {
//						needAnim = !mHorizontalScrollAnimator.isRunning();
//					}
//					drawLrcWordsOneLine(canvas, currentSentence,
//							getTextRect(textWidth, mSentenceLineScrollX, rowY, paint),
//							paint, needAnim);
//				}
//
//			} else {
//				Paint paint = getCurrentPaint();
//
//				float textSize;
//				if(i == mLastLineIndex) {
//					textSize = HIGHLIGHT_FONT_SIZE-(
//							HIGHLIGHT_FONT_SIZE-COMMON_FONT_SIZE)*mHighLightFontScaleFactor;
//				}else {
//					textSize = COMMON_FONT_SIZE;
//				}
//
//				int distance = Math.abs(i-mCurrentLineIndex);
//				int textColor = getOthersWordColor(distance, sentenceStartTime, sentenceEndTime);
//
//				paint.setTextSize(textSize);
//				paint.setColor(textColor);
//
//				int textSpace = mWidth;
//				int textWidth = Float2Int(paint.measureText(text));
//				int textX = Math.max((textSpace-textWidth)/2, 0);
//
//				if (sentenceStartTime<mStartSingTime) {
////					drawText(canvas, "",
////							getTextRect(textWidth, textX, rowY, paint), paint);
//				} else {
//					drawText(canvas, text,
//							getTextRect(textWidth, textX, rowY, paint), paint);
//				}
//
//			}
//
//			rowY += COMMON_FONT_SIZE + mLineSpace;
//
//		}
//
//	}
//
//	/**
//	 * 受邀请视频合唱，要区分演唱者的歌词颜色
//	 * @param starTime
//	 * @param endTime
//	 * @return
//	 */
//	private int getCurrentSingHighLightWordColor(long starTime, long endTime) {
//		if (mIsMVDuteInvited) {
//			return UNHIGH_LIGHT_FONT_COLOR;
//		} else {
//			return HIGH_LIGHT_FONT_COLOR;
//		}
//	}
//
//	private int getCurrentSingUnHighLightWordColor(long starTime, long endTime) {
//		boolean isMeSing = true;
//		if(null != detector){
//			isMeSing = !detector.detectOwner(starTime, endTime);
//		}
//		if (mIsMVDuteInvited) {
//			if (isMeSing) {
//				return HIGH_LIGHT_FONT_COLOR;
//			} else {
//				return WHITE_FONT_COLOR_1;
//			}
//		} else {
//			return UNHIGH_LIGHT_FONT_COLOR;
//		}
//	}
//
//	private int getOthersWordColor(int distance, long starTime, long endTime) {
//		boolean isMeSing = true;
//		if(null != detector){
//			isMeSing = !detector.detectOwner(starTime, endTime);
//		}
//		int textColor;
//		if (mIsMVDuteInvited && isMeSing) {
//			if (distance == 1) {
//				textColor = GREEN_FONT_COLOR_1;
//			} else if (distance == 2) {
//				textColor = GREEN_FONT_COLOR_2;
//			} else {
//				textColor = GREEN_FONT_COLOR_3;
//			}
//		} else {
//			if (distance == 1) {
//				textColor = WHITE_FONT_COLOR_1;
//			} else if (distance == 2) {
//				textColor = WHITE_FONT_COLOR_2;
//			} else {
//				textColor = WHITE_FONT_COLOR_3;
//			}
//		}
//		return textColor;
//	}
//
//	/**
//	 * 文字绘制区域
//	 *
//	 * @param textWidth
//	 * @param textX
//	 * @param textMidY
//	 * @param paint
//	 * @return
//	 */
//	private Rect getTextRect(int textWidth, int textX, int textMidY, Paint paint) {
////		android.util.Log.d("jz", getClass().getName()+"  getTextRect() textWidth="+textWidth+"  textWidth="+textWidth+"  textMidY="+textMidY);
//
//		Rect outRect = new Rect();
//
//		float halfFontSize = paint.getTextSize()/2;
//		int top = (int) Math.floor(textMidY - halfFontSize);
//		int bottom = (int) Math.ceil(textMidY + halfFontSize);
//
//	    outRect.left = textX;
//	    outRect.top = top;
//	    outRect.right = textX + textWidth;
//	    outRect.bottom = bottom;
//
//		return outRect;
//	}
//
//
//	/**
//	 * 绘制歌词
//	 * @param canvas
//	 * @param text
//	 * @param paint
//	 */
//	private void drawText(Canvas canvas, String text, Rect textRect, Paint paint) {
//		FontMetricsInt fontMetrics = paint.getFontMetricsInt();
//		int top = textRect.top;
//		int bottom = textRect.bottom;
//	    int baseline = top+(bottom-top-fontMetrics.bottom+fontMetrics.top)/2-fontMetrics.top;
//	    canvas.drawText(text, textRect.left, baseline, paint);
//	}
//
//
//
//	/**
//	 * 绘制录制起始点倒计时
//	 * @param canvas
//	 * @param currenttime
//	 */
//	private void drawStartPoint(Canvas canvas, int currenttime) {
//
//		int num = ((mStartSingTime+mStartSingTimeDelay)-currenttime)/POINT_TIME + 1;
//
//		Bitmap bmp = mCountDownDrawable.getBitmap();
//
//		for (int i = 0; i < num; i++) {
//			float left = mStartPointOffset + (int)(i*bmp.getWidth()*1.5);
//			float top = mHeight/2-bmp.getHeight()*3-15 + getScrollY();
//
//			canvas.drawBitmap(bmp, left, top, null);
//		}
//		Log.i(TAG, "drawStartPoint() currenttime="+currenttime+"  num="+num
//				+"  mStartPointOffset="+mStartPointOffset+"  mHeight="+mHeight+"  bmp.isRecycled()="+bmp.isRecycled()
//				+"  bmp.getWidth()="+bmp.getWidth()
//				+"  bmp.getHeight()="+bmp.getHeight()
//				+"  canvas.getWidth()="+canvas.getWidth()
//				+"  canvas.getHeight()="+canvas.getHeight()
//				+"  getScrollY()="+getScrollY());
//	}
//
//
//	/**
//	 * 开始水平滚动歌词
//	 * @param endX 歌词第一个字的最终的x坐标
//	 * @param duration 滚动的持续时间
//	 */
//	private void startHorizontalScroll(float startX, float endX, int duration, int delay){
//		if(mHorizontalScrollAnimator == null){
//			mHorizontalScrollAnimator = ValueAnimator.ofFloat(startX, endX);
//			mHorizontalScrollAnimator.addUpdateListener(updateListener);
//			mHorizontalScrollAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
//		}else{
//			mHorizontalScrollAnimator.cancel();
//			mHorizontalScrollAnimator.setFloatValues(startX, endX);
//		}
//		mHorizontalScrollAnimator.setDuration(duration);
//		mHorizontalScrollAnimator.setStartDelay(delay);
//		mHorizontalScrollAnimator.start();
//	}
//
//	private void startHorizontalScroll(float startX, float endX, int duration){
//		startHorizontalScroll(startX, endX, duration, 0);
//	}
//
//	/**
//	 * 停止歌词的滚动
//	 */
//	private void stopHorizontalScroll(){
//		if(mHorizontalScrollAnimator != null){
//			mHorizontalScrollAnimator.cancel();
//		}
//		mSentenceLineScrollX = 0;
//	}
//
//	/***
//	 * 监听属性动画的数值值的改变
//	 */
//	AnimatorUpdateListener updateListener = new AnimatorUpdateListener() {
//
//		@Override
//		public void onAnimationUpdate(ValueAnimator animation) {
//			float offsetValue = (Float) animation.getAnimatedValue();
//			mSentenceLineScrollX = Float2Int(offsetValue);
//			invalidate();
//		}
//	};
//
//
//	private int calcWantHeightSize() {
//		int size = 2*mLinesCount*(COMMON_FONT_SIZE+mLineSpace);
//		size += HIGHLIGHT_FONT_SIZE;
//		return size;
//	}
//
//
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int heightSize = 0;
//
//        if (heightMode == MeasureSpec.EXACTLY) {
//        	int wantHeightSize = calcWantHeightSize();
//        	heightSize = Math.min(MeasureSpec.getSize(heightMeasureSpec), wantHeightSize);
////        	android.util.Log.d("jz", "VerbatimLrcView onMeasure step1 wantHeightSize="+wantHeightSize+"  heightSize="+heightSize);
//        } else {
//        	int wantHeightSize = calcWantHeightSize();
//        	heightSize = wantHeightSize;
////        	android.util.Log.d("jz", "VerbatimLrcView onMeasure step2 wantHeightSize="+wantHeightSize+"  heightSize="+heightSize);
//        }
//
//        int calcHeightMeasureSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
//		setMeasuredDimension(
//				getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
//				getDefaultSize(getSuggestedMinimumHeight(), calcHeightMeasureSpec)
//				);
//
//	}
//
//
//	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
//		super.onSizeChanged(width, height, oldWidth, oldHeight);
////		android.util.Log.d("jz", getClass().getName()+"  onSizeChanged() width="+width+"  height="+height
////				+"  oldWidth="+oldWidth+"  oldHeight="+oldHeight);
//		this.mWidth = width;
//		this.mHeight = height;
//	}
//
//	public int getSingSentence() {
//		int hasSingSentence = 0;
//		if (mVerbatimSentences != null) {
//			for (int i = mVerbatimSentences.size() - 1; i >= 0; i--) {
//				if (mVerbatimSentences.get(i).start < mCurrenttime) {
//					hasSingSentence = i;
//					break;
//				}
//			}
//		}
//		return hasSingSentence;
//	}
//
//	public int getCurrentLineIndex() {
//		return mCurrentLineIndex;
//	}
//
//	/**
//	 * 播放器更新当前时间
//	 *
//	 * @param time
//	 */
//	public void updateCurrentTime(int time) {
//		if (!hasParse()) {
//			return;
//		}
//		if (!isAttach) {
//			return;
//		}
//		if (isVerbatim) {
//			updateVerbatimCurrentTime(time);
//		} else {
//			updateVerticalCurrentTime(time);
//		}
//	}
//
//	/**
//     * 计算第一句歌词的演唱时间
//     * @return
//     */
//    public int getLineStartTime(int index) {
//        if (isVerbatim) {
//            return mVerbatimSentences.get(index).words.get(0).start;
//        } else {
//            return (int)mVerticalSentences.get(index).getFromTime();
//        }
//    }
//
//    public int getLineEndTime(int index) {
//    	if (isVerbatim) {
//            return mVerbatimSentences.get(index).words.get(0).stop;
//        } else {
//            return (int)mVerticalSentences.get(index).getToTime();
//        }
//    }
//
//	private void updateVerticalCurrentTime(int time) {
//		mCurrenttime = time;
//
//		int index = 0;
//
//		//处理逐行歌词
//		if(mVerticalSentences == null || mVerticalSentences.isEmpty()){
//			return;
//		}
//		for (; index<mVerticalSentences.size(); index++) {
//			Sentence line = mVerticalSentences.get(index);
//			if (time < line.getToTime()) {
//				break;
//			}
//		}
//
//		// 需要滚动
//		if (index != mCurrentLineIndex) {
//
//			mLastLineIndex = mCurrentLineIndex;
//
//			mCurrentLineIndex = index;
//
//			if (!mScroller.isFinished()) {
//				mScroller.forceFinished(true);
//			}
//
//			Sentence currentSentence = mVerticalSentences.get(index);
//			String text = Null2String(currentSentence.getContent());
//			int duration = (int) currentSentence.getDuring();
//
//			Paint paint = getCurrentPaint();
//			paint.setTextSize(HIGHLIGHT_FONT_SIZE);
//			float textWidth = paint.measureText(text);
//			if (textWidth > mWidth) {
//				mSentenceLineScrollX = 0;
//				startHorizontalScroll(0F, mWidth-textWidth, (int)(duration*0.5), (int)(duration*0.4));
//			}
//
//			smoothScrollTo(mCurrentLineIndex * (COMMON_FONT_SIZE + mLineSpace), DURATION_FOR_LRC_SCROLL);
//
//		}
//
//		invalidate();
//	}
//
//	private void updateVerbatimCurrentTime(int time) {
//		mCurrenttime = time;
//
//		int index = 0;
//
//		//处理逐字歌词
//		if(mVerbatimSentences == null || mVerbatimSentences.isEmpty()){
//			return;
//		}
//
//		time = Math.max(time, mStartSingTime);
//
//		for (; index<mVerbatimSentences.size(); index++) {
//			LrcSentence line = mVerbatimSentences.get(index);
//			if (time < line.words.get(line.words.size()-1).stop) {
//				break;
//			}
//		}
//
//		Log.i(TAG, "updateVerbatimCurrentTime()  time="+time+"  index="+index+"  mCurrentLineIndex="+mCurrentLineIndex);
//
//		// 需要滚动
//		if (index != mCurrentLineIndex) {
//
//			mLastLineIndex = mCurrentLineIndex;
//
//			mCurrentLineIndex = index;
//
//			if (!mScroller.isFinished()) {
//				mScroller.forceFinished(true);
//			}
//
//			stopHorizontalScroll();
//
//			smoothScrollTo(mCurrentLineIndex * (COMMON_FONT_SIZE + mLineSpace), DURATION_FOR_LRC_SCROLL);
//
//		}
//
//		invalidate();
//
////		android.util.Log.d("jz", "VerbatimLrcView updateVerbatimCurrentTime time="+time+"  mCurrentLineIndex="+mCurrentLineIndex+"  mLastLineIndex="+mLastLineIndex);
//	}
//
//
//	/**
//	 * 平滑移动
//	 */
//	private void smoothScrollTo(int dstY,int duration){
//		int oldScrollX = getScrollX();
//		int oldScrollY = getScrollY();
//		int offset = dstY - oldScrollY;
//
////		android.util.Log.d("jz", getClass().getName()+" smoothScrollTo() dstY="+dstY+"  oldScrollX="+oldScrollX+"  oldScrollY="+oldScrollY+"  offset="+offset);
//
//		mScroller.startScroll(oldScrollX, oldScrollY, oldScrollX, offset, duration);
//	}
//
//
//	@Override
//	public void computeScroll() {
//		if (!mScroller.isFinished()) {
//			if (mScroller.computeScrollOffset()) {
//				int oldY = getScrollY();
//				int y = mScroller.getCurrY();
//				if (oldY != y) {
//					scrollTo(getScrollX(), y);
//				}
//				int timePassed = mScroller.timePassed();
//				mHighLightFontScaleFactor = Math.min(timePassed*3f/DURATION_FOR_LRC_SCROLL, 1F);
//				invalidate();
//
////				android.util.Log.d("jz", getClass().getName()+"  computeScroll() getScrollY()="+oldY+"  getCurrY()="+y);
//			}
//		}
//	}
//
//	private boolean isAttach = false;
//	@Override
//	protected void onAttachedToWindow() {
//		super.onAttachedToWindow();
//		isAttach = true;
//	}
//
//
//	@Override
//	protected void onDetachedFromWindow() {
//		super.onDetachedFromWindow();
////		android.util.Log.d("jz", "onDetachedFromWindow().........");
//		if (mStartCountDownTimer != null) {
//			mStartCountDownTimer.cancel();
//		}
//		isAttach = false;
//	}
//
//	/**
//	 * 歌曲解析接口
//	 *
//	 */
//	public static interface ILyricParserCallback {
//		public void onParseComplete(File lrcFile, boolean parseSucc);
//	}
//
//	public interface LrcOwnerDetector{
//		public boolean detectOwner(long startTimeMills, long endTimeMills);
//	};
//
//}
