import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyLogFilter {

    String packageName = "";
    boolean featchAll = false;
    List<String> excludeParams = new ArrayList<>();
    List<String> pidList = new ArrayList<>();
    String device = "";
    FileWriter writer;
    String pidRegexStr;
    String tagRegexStr;
    long lastRecvTs;
    int died = 10000;
    boolean restart = false;

    void init(String args[]) {
//        args = new String[]{"com.wali.live", "all", "aaa", "bbb"};
        // 初始化参数
        initInput(args);
        while (true) {
            pidList.clear();
            died = 10000;
            lastRecvTs = 0;
            restart = false;
            // 根据包名获得进程
            List<String> rl = exec("adb devices");
            // 分析有几台设备
            List<String> devices = new ArrayList<>();
            for (int i = 0; i < rl.size(); i++) {
                String ss = rl.get(i);
                if (ss.contains("device") && !ss.contains("List")) {
                    String[] ss2 = ss.split("\t");
                    devices.add(ss2[0]);
                }
            }
            System.out.println("设备有:" + devices);
            if (devices.size() > 0 && device.equals("")) {
                device = devices.get(0);
            }
            String cmd;
            System.out.println("packageName:" + packageName);
            if (!device.equals("")) {
                cmd = String.format("adb -s %s shell ps | grep %s ", device, packageName);
            } else {
                cmd = String.format("adb  shell ps | grep %s ", packageName);
            }
            System.out.println("cmd:" + cmd);
            rl = exec(cmd);

            System.out.println(rl);
            for (String s : rl) {
                System.out.println("进程号:" + s);
                String s1[] = s.split("[ ]+");
                if (s1[s1.length - 1].contains(":")) {
                    pidList.add(s1[1]);
                } else {
                    pidList.add(0, s1[1]);
                }
            }
            MyThread thread = new MyThread() {
                @Override
                public void run() {
                    while (!killThread) {
                        // 检测中
                        System.out.println("died:"+died+" lastRecvTs:"+lastRecvTs);
                        if (died < 10 && System.currentTimeMillis() - lastRecvTs > 10 * 1000) {
                            System.out.println("检测到需要重启");
                            restart = true;
                            break;
                        }
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();
            run(thread);
            System.out.println("重新检测");
        }
    }

    class MyThread extends Thread{
        boolean killThread = false;
    }
    void initInput(String args[]) {
        int i=0;
        if(args[i].equals("-s")){
            i++;
            device = args[i];
            i++;
        }
        packageName = args[i];
        i++;
        if (args.length > i && args[i].equals("all")) {
            featchAll = true;
        }
        for (int j = args.length - 1; j >= i; j--) {
            if (!args[j].equals("all")) {
                excludeParams.add(args[j]);
            }else{
                break;
            }
        }
        try {
            writer = new FileWriter(args[args.length-1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void createPattern(List<String> tracePids, List<String> tags) {
        {
            StringBuilder pidRegex = new StringBuilder("[\\s|\\S]*(");
            for (int i = 0; i < tracePids.size(); i++) {
                String pid = tracePids.get(i);
                if (i != 0)
                    pidRegex.append("|");
                pidRegex.append(pid);
            }
            pidRegex.append(")").append("[\\s|\\S]*");
            pidRegexStr = pidRegex.toString();
        }
        {
            StringBuilder tagRegex = new StringBuilder("[\\s|\\S]*(");
            for (int i = 0; i < tags.size(); i++) {
                if (i != 0) {
                    tagRegex.append("|");
                }
                tagRegex.append(tags.get(i));
            }
            tagRegex.append(")").append("[\\s|\\S]*");
            tagRegexStr = tagRegex.toString();
        }
    }

    void run(MyThread thread) {
        try {
            String cmd = String.format("adb -s %s logcat -v time", device);
            List<String> fetchPidList = new ArrayList<>();

            for (int i = 0; i < pidList.size(); i++) {
                String pid = pidList.get(i);
                if (i == 0) {
                    fetchPidList.add(pid);
                } else {
                    if (featchAll) {
                        fetchPidList.add(pid);
                    }
                }
            }
            if(fetchPidList.size()==0){
                try {
                    thread.killThread = true;
                    Thread.sleep(5100);

                    return;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println(cmd);
            System.out.println(fetchPidList);

            createPattern(fetchPidList, excludeParams);
            System.out.println(pidRegexStr);
            System.out.println(tagRegexStr);
            Process exec = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = exec.getInputStream();
            Scanner sc = new Scanner(inputStream);
            while (!restart && sc.hasNextLine()) {
                String line = sc.nextLine();

                if (line.matches(pidRegexStr)) {
                    if (!line.matches(tagRegexStr)) {
                        print(line);
//                        line = "10-24 11:43:54.504 V/UidProcStateHelper(18810): process died:[12677,10562]";
                        if (line.contains("process died") || line.contains("Killing")) {
                            died = 0;
                        } else {
                            died++;
                        }
                    }
                    lastRecvTs = System.currentTimeMillis();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void print(String str) {
        System.out.println(str);
        try {
            writer.write(str);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    static List<String> exec(String cmd) {
        //获取执行命令后的输入流
        try {
            Process exec = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = exec.getInputStream();
            InputStreamReader buInputStreamReader = new InputStreamReader(inputStream);//装饰器模式
            BufferedReader bufferedReader = new BufferedReader(buInputStreamReader);//直接读字符串
            String str = null;
            List<String> r = new ArrayList<>();
            while ((str = bufferedReader.readLine()) != null) {
                r.add(str);//每读一行拼接到sb里面去
            }
            return r;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }


    public static void main(String args[]) {
        MyLogFilter logFilter = new MyLogFilter();
        logFilter.init(args);
    }


}

