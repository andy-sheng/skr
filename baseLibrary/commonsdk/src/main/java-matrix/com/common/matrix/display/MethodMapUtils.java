package com.common.matrix.display;

import android.util.SparseArray;

import com.common.log.MyLog;
import com.common.utils.U;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MethodMapUtils {
    static SparseArray<MethodInfo> sparseArray = new SparseArray();

    public static void loadMethod() {
        if(sparseArray.size()>10){
            return;
        }
        sparseArray.clear();
        try {
            FileInputStream fin = new FileInputStream(new File(U.getAppInfoUtils().getMainDir(), "matrix_method.txt"));
            if (fin != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(fin);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String args[] = line.split(",");
                    int no = Integer.parseInt(args[0]);
                    int type = Integer.parseInt(args[1]);
                    String method = args[2];
                    MethodInfo methodInfo = new MethodInfo(no, type, method);
                    sparseArray.append(no, methodInfo);
                }
                inputStreamReader.close();
            }
        } catch (Exception e) {
            MyLog.e(e);
        }
        return ;
    }

    public static String get(int method) {
        MethodInfo methodInfo = sparseArray.get(method);
        if(methodInfo!=null){
            return methodInfo.getMethod();
        }
        return method+"";
    }
}
