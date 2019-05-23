package com.common.utils;

import android.media.MediaPlayer;

import com.common.log.MyLog;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by linjinbin on 15/2/10.
 */
public class MediaUtils {
    public final static String TAG = "MediaUtils";


    public void rawToWave(final File rawFile, final File waveFile, int channels, int sampleRate, int byteRate) throws IOException {
        MyLog.d(TAG, "rawToWave" + " rawFile=" + rawFile + " waveFile=" + waveFile + " channels=" + channels + " sampleRate=" + sampleRate + " byteRate=" + byteRate);

        long len = rawFile.length();
        DataOutputStream output = null;
        try {
            output = new DataOutputStream(new FileOutputStream(waveFile));

            long totalDataLen = len + 36;

            byte[] header = new byte[44];
            header[0] = 'R';
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';

            header[16] = (byte) 16;
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;

            header[20] = 1;
            header[21] = 0;

            header[22] = (byte) channels;
            header[23] = 0;

            header[24] = (byte) (sampleRate & 0xff);
            header[25] = (byte) ((sampleRate >> 8) & 0xff);
            header[26] = (byte) ((sampleRate >> 16) & 0xff);
            header[27] = (byte) ((sampleRate >> 24) & 0xff);

            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);

            header[32] = (byte) 2;
            header[33] = 0;
            header[34] = 16;
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (len & 0xff);
            header[41] = (byte) ((len >> 8) & 0xff);
            header[42] = (byte) ((len >> 16) & 0xff);
            header[43] = (byte) ((len >> 24) & 0xff);

            output.write(header, 0, header.length);
            writeSlow(rawFile, output);
        } catch (Exception e) {
            MyLog.e(TAG, e);
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

    void writeSlow(File f, DataOutputStream outputStream) throws IOException {
        InputStream is = new FileInputStream(f);
        try {
            //创建一个字节输入流对象
            //指定每次读取的大小--可根据性能字节修改
            byte bytes[] = new byte[1024 * 2];
            int len = -1;//每次读取的实际长度
            while ((len = is.read(bytes)) != -1) {
                outputStream.write(bytes, 0, len);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            is.close();//关闭流
        }
    }

    private void writeInt(final DataOutputStream output, final int value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
        output.write(value >> 16);
        output.write(value >> 24);
    }

    private void writeShort(final DataOutputStream output, final short value) throws IOException {
        output.write(value >> 0);
        output.write(value >> 8);
    }

    private void writeString(final DataOutputStream output, final String value) throws IOException {
        for (int i = 0; i < value.length(); i++) {
            output.write(value.charAt(i));
        }
    }

    public int getDuration(String filePath) {
        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(filePath);  //recordingFilePath（）为音频文件的路径
            player.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int duration = player.getDuration();//获取音频的时间
        player.release();//记得释放资源
        return duration;
    }

}

