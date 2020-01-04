package com.example.ppt_controller;

import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityService;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.os.Bundle;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.provider.Settings;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Scanner;


public class MainActivity extends AppCompatActivity {
    ImageButton connect;
    private static final String TAG = "MainActivity";
    TextView Info;
    TextView fileinfo;
    Socket socket=null;
    TextView connect_info;
    ImageButton next;
    ImageButton previous;
    public static boolean ConnectOn=false;
    public static String sendStr;
    public static boolean send = false;
    String getwayIpS=null;
    public Thread thread = null;
    NetThread nt=new NetThread();
    private Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Info=(TextView)findViewById(R.id.Info);
        connect_info=(TextView)findViewById(R.id.connect_info);
        previous = (ImageButton)findViewById(R.id.previous);
        next = (ImageButton)findViewById(R.id.next);
        connect=(ImageButton)findViewById(R.id.start);
        WifiManager wm = (WifiManager)getApplicationContext().getSystemService(WIFI_SERVICE);

        DhcpInfo di = wm.getDhcpInfo();
        long getewayIpL=di.gateway;
        getwayIpS=long2ip(getewayIpL);//网关地址！！！！
        connect_info.setText(getwayIpS);
        long netmaskIpL=di.netmask;
        String netmaskIpS=long2ip(netmaskIpL);//子网掩码地址


        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sendStr = "PPT;previous;0;";
                MainActivity.send = true;


            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.sendStr="PPT;next;0;";
                MainActivity.send = true;

            }
        });
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStr = "enter";
                send=true;
                ConnectOn=true;
                thread = new Thread(nt,"thread");
                thread.start();
                connect_info.setText("连接成功!");
                 //   Intent intent1 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                  //  startActivity(intent1);




            }
        });


    }
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            MainActivity.sendStr="PPT;next;0;";
            MainActivity.send = true;
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            MainActivity.sendStr = "PPT;previous;0;";
            MainActivity.send = true;
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    String long2ip(long ip){
        StringBuffer sb=new StringBuffer();
        sb.append(String.valueOf((int)(ip&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>8)&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>16)&0xff)));
        sb.append('.');
        sb.append(String.valueOf((int)((ip>>24)&0xff)));
        return sb.toString();
    }
    class SendThread implements Runnable {
           @Override
           public void run() {
               try {
                   PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"GBK")), true);
   //                int counts=0;
                   while(ConnectOn){
   //
                       if(send){
                           Log.e("---","send! "+sendStr);
                           Info.post(new ChangeText("Sending:" + sendStr));
                           out.print(sendStr);
                           out.flush();
                           MainActivity.this.runOnUiThread(new ChangeText("...finished!\n"));
                           send = false;

                       }

                   }
                   out.close();


               } catch (Exception EE) {
                   connect_info.setText("发送失败");
                   EE.printStackTrace();
               }
           }

       }
    class NetThread implements Runnable{
        public void run() {
            // TODO Auto-generated method stub
            try {
                socket = new Socket(getwayIpS,8080);
               // connect_info.setText("连接成功!");
                Thread Sthread = new Thread(new SendThread(), "thread_s");
                Sthread.start();
                if (!isAccessibilitySettingsOn(MainActivity.this, ppt_control.class.getCanonicalName())) {
                    Intent intent1 = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                    startActivity(intent1);
                } else {
                    intent = new Intent(MainActivity.this, ppt_control.class);
                    startService(intent);
                }

                if(!ConnectOn){
                    socket.close();
                }
            } catch (Exception EE) {
                connect_info.setText("连接失败!");
                EE.printStackTrace();
            }
        }

    }
    class RecvThread implements Runnable{
        @Override
        public void run() {
         /*   try {
                //BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream(),"GBK"));
                while (ConnectOn) {
                    byte[] recivebuff = new byte[100];
                    //boolean recivef = false;
                    if (socket.getInputStream().available() >= 100) {
                        int count1=0;
                        while(count1<100){
                            count1+=socket.getInputStream().read(recivebuff,count1,100-count1);
                        }
                        String ret = new String(recivebuff, "GBK");
                        if(ret.contains("enter")){
                            connect_info.post(new ChangeInfoText("连接成功！"));
                        }
                        if (ret.contains("2557893640")) {
                            //Info.post(new ChangeText("进入接收文件模式\n"));
                            while(socket.getInputStream().available()<100)
                                continue;
                            count1=0;
                            while(count1<100){
                                count1+=socket.getInputStream().read(recivebuff,count1,100-count1);
                            }
                            String filename2 = new String(recivebuff,"GBK");
                            String filename = new String();
                            filename = "";
                            for(int i=0;i<100;i++){
                                if(filename2.charAt(i)!=0){
                                    //Info.post(new ChangeText(""+i+":"+filename2.charAt(i)));
                                    filename+=filename2.charAt(i);
                                }
                                else
                                    break;
                            }
                            //Info.post(new ChangeText("filename:" + filename +"\n"));
                            String filepath2 = Environment.getExternalStorageDirectory().getPath();
                            filename = filepath2 +"/controller/"+ filename;
                            //Info.post(new ChangeText(filepath2+"\n"));
                            File dir = new File(filepath2+"/controller/");
                            if (!dir.exists()) {
                                try {
                                    dir.mkdirs();
                                    Info.post(new ChangeText("新建文件夹\n"));
                                    fileinfo.post(new ShowInfoText("新建文件夹" ));
                                    //Toast.makeText(MainActivity.this,"新建文件夹\n/storage/emulated/0/contoral/",Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    //Info.post(new ChangeText("失败\n"));25535
                                }
                            }
                            File dir2 = new File(filename);
                            if (!dir2.exists()) {
                                try {
                                    dir2.createNewFile();
                                } catch (Exception e) {
                                    Info.post(new ChangeText("失败2\n"));
                                }
                            } else {
                                Info.post(new ChangeText("文件已存在，将进行覆盖\n"));
                                fileinfo.post(new ShowInfoText("文件已存在，将进行覆盖" ));
                                //Toast.makeText(MainActivity.this,"文件已存在，将进行覆盖",Toast.LENGTH_SHORT).show();
                            }
                            FileOutputStream out = new FileOutputStream(filename);
                            //Info.post(new ChangeText("buffsize:" + (socket.getInputStream().available() + "\n")));
                            count1=0;
                            //Info.post(new ChangeText("filename:" + filename +"3\n"));
                            while(count1<100){
                                count1+=socket.getInputStream().read(recivebuff,count1,100-count1);
                            }

                            int filesize = 0;
                            for(int i=0;i<100;i++){
                                if(recivebuff[i]!=0)
                                    filesize=filesize*10+recivebuff[i]-'0';
                                else
                                    break;
                            }

                            int count = 0, count2 = 0;
                            byte[] filebuff = new byte[filesize];
                            int recivesize = 0;
                            int recivesize2 = 0;
                            while (count < filesize) {
                                count2 = count;
                                count += socket.getInputStream().read(filebuff, count, filesize - count);
                                recivesize2 = count*100/filesize;
                                out.write(filebuff, count2, count - count2);
                                if((recivesize2-recivesize)>3){
                                    Info.post(new ChangeText("已接收" + recivesize2+"% "));
                                    recivesize=recivesize2;
                                }
                            }
                            out.close();
                            Info.post(new ChangeText("接收文件完成\n存放在："+filename + "\n"));
                            fileinfo.post(new ShowInfoText("接收文件完成\n存放在："+filename ));
                            Toast.makeText(MainActivity.this,"接收文件完成\n存放在："+filename,Toast.LENGTH_SHORT).show();
                        } else {
                            Info.post(new ChangeText("return:" + ret + "\n"));
                            MainActivity.this.runOnUiThread(new ChangeText("...finished!\n"));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }*/
        }
    }
    class ChangeInfoText implements Runnable {
        String text;
        ChangeInfoText(String text) {
            this.text = text;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            connect_info.setText(text);
        }
    }
    class ChangeText implements Runnable {
        String text;
        ChangeText(String text) {
            this.text = text;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Info.append(text);
        }
    }
    class ShowInfoText implements Runnable {
        String text;
        ShowInfoText(String text) {
            this.text = text;
        }
        @Override
        public void run() {
            // TODO Auto-generated method stub
            fileinfo.setText(text);

        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (!ppt_control.isStart())
        {
            try {
                String enabledServicesSetting = Settings.Secure.getString(
                        getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

                ComponentName selfComponentName = new ComponentName(getPackageName(),
                        "ppt_control");
                String flattenToString = selfComponentName.flattenToString();
                if (enabledServicesSetting==null||
                        !enabledServicesSetting.contains(flattenToString)) {
                    enabledServicesSetting += flattenToString;
                }
//                  Settings.Secure.putString(getContentResolver(),Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,enabledServicesSetting);
//                  Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
            }catch (Exception e)
            {
              //  this.startActivity(new Intent(Settings.ACTION_SETTINGS));
                e.printStackTrace();
            }

        }
    }
    private boolean isAccessibilitySettingsOn(Context mContext, String serviceName) {
        int accessibilityEnabled = 0;
        // 对应的服务
        final String service = getPackageName() + "/" + serviceName;
        //Log.i(TAG, "service:" + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt(mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: " + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + "     " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }
        return false;
    }

}

