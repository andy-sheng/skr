package media;

import android.app.Service;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.base.log.MyLog;

public class ContestMediaHelper {
    private static final String TAG = ContestMediaHelper.class.getSimpleName();

    private Context mContext;

    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;     //声明一个振动器对象

    public ContestMediaHelper(Context context) {
        mContext = context;
        mMediaPlayer = new MediaPlayer();
        mVibrator = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
    }

    public void vibrate() {
        mVibrator.vibrate(500);
    }

    public void playRawSource(int rawId) {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.reset();
            }

            AssetFileDescriptor fileDesc = mContext.getResources().openRawResourceFd(rawId);
            if (fileDesc == null) {
                MyLog.e(TAG, "playRawSource fileDesc is null");
                return;
            }

            mMediaPlayer.setDataSource(fileDesc.getFileDescriptor(), fileDesc.getStartOffset(), fileDesc.getLength());
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }

    public void destroy() {
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
                mMediaPlayer.release();
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }
    }
}