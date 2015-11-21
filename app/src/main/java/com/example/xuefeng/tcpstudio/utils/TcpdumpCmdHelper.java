package com.example.xuefeng.tcpstudio.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class TcpdumpCmdHelper {
    private static final String NAME = "tcpdump";
    private static final String TAG = "CommandsHelper";
    private static boolean retval = false;
    public static String DEST_FILE = Environment.getExternalStorageDirectory() + "/capture.pcap";


    public static boolean startCapture(Context context,String storagePath) {
        InputStream is = null;
        OutputStream os = null;
        if(!TextUtils.isEmpty(storagePath)){
            DEST_FILE = storagePath;
        }
        try {
            AssetManager am = context.getAssets();
            is = am.open(NAME);
            File sdcardFile = Environment.getExternalStorageDirectory();
            File dstFile = new File(sdcardFile, NAME);
            os = new FileOutputStream(dstFile);

            copyStream(is, os);

            String[] commands = new String[7];
            commands[0] = "adb shell";
            commands[1] = "su";
            commands[2] = "cp -rf " + dstFile.toString() + " /data/local/tcpdump";
            commands[3] = "rm -r " + dstFile.toString();
            commands[4] = "chmod 777 /data/local/tcpdump";
            commands[5] = "cd /data/local";
            commands[6] = "tcpdump -p -vv -s 0 -w " + DEST_FILE;
            Log.e("dstFile", dstFile.toString());
            execCmd(commands);
            retval = true;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i(TAG, "    error: " + e.getMessage());
        } finally {
            closeSafely(is);
            closeSafely(os);
        }
        return retval;
    }
    public static void stopCapture(Context context) {
        // 找出所有的带有tcpdump的进程
        String[] commands = new String[2];
        commands[0] = "adb shell";
        commands[1] = "ps|grep tcpdump";
        Process process = execCmd(commands);
        String result = parseInputStream(process.getInputStream());
        String[] killCmds;
        if (!TextUtils.isEmpty(result)) {

            String[] cmds = result.split("\n");
            killCmds = new String[cmds.length];
            Log.e("length:", String.valueOf(cmds.length));
            for(int i = 0;i < cmds.length;i++){
                Log.e("result", cmds[i]);
                String[] params = cmds[i].split(" ");
                for(int j = 0;j < params.length;j++){
                    Log.e("params", String.valueOf(j)+" : "+params[j]);
                }

                killCmds[i] = "kill -9 "+params[5];

            }
            execCmd(killCmds);
        }
    }

    public static Process execCmd(String command) {
        return execCmd(new String[] { command }, true);
    }

    public static Process execCmd(String[] commands) {
        return execCmd(commands, true);
    }

    public static Process execCmd(String[] commands, boolean waitFor) {

        Process suProcess = null;
        try {
            suProcess = Runtime.getRuntime().exec("su");
            OutputStream os = suProcess.getOutputStream();
            DataOutputStream dos = new DataOutputStream(os);
            for (String cmd : commands) {
                if (!TextUtils.isEmpty(cmd)) {
                    Log.e("cmd", cmd);
                    dos.writeBytes(cmd + "\n");
                    dos.flush();
                }

            }

            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (waitFor) {
            try {
                int suProcessRetval = suProcess.waitFor();
                if (255 != suProcessRetval) {
                    retval = true;
                } else {
                    retval = false;
                }
            } catch (Exception ex) {
                Log.w("Error ejecutando el comando Root", ex);
            }
        }

        return suProcess;
    }

    private static void copyStream(InputStream is, OutputStream os) {
        final int BUFFER_SIZE = 1024;
        try {
            byte[] bytes = new byte[BUFFER_SIZE];
            for (;;) {
                int count = is.read(bytes, 0, BUFFER_SIZE);
                if (count == -1) {
                    break;
                }

                os.write(bytes, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void closeSafely(Closeable is) {
        try {
            if (null != is) {
                is.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String parseInputStream(InputStream is) {
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        String line = null;
        StringBuilder sb = new StringBuilder();
        try {
            while ( (line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }
}

