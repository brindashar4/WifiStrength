package com.example.wifistrength;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String name = "wifistrength.txt";
    //public static File temp = new File(Environment.getExternalStorageDirectory().getPath() + name);
    //static File textFile = new File(Environment.getExternalStorageDirectory(),name);

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView t = findViewById(R.id.display);
        File temp = new File(getFilesDir() + "/"+name);
        //t.setMovementMethod(new ScrollingMovementMethod());
        //File textFile = new File(Environment.getExternalStorageDirectory(),name);

            RandomAccessFile raf;
            try {
                raf = new RandomAccessFile(temp, "rw");
                raf.setLength(0);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        Button turnOnW = findViewById(R.id.turnOnW);

        WifiManager wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()) {
            turnOnW.setVisibility(View.VISIBLE);
        }
    }


    public void on(View view) {
        WifiManager wifiManager = (WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        Button t = findViewById(R.id.turnOnW);
        t.setVisibility(View.INVISIBLE);
    }

    private boolean isExternalStorageWritable() {
        if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.i("State","Yes, it is writable");
            return true;
        }
        else {
            return false;
        }
    }

    public void display(View view) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        if(!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        StringBuilder s = new StringBuilder();
        s.append("Link name: "+wifiInfo.getSSID()+"\n");
        s.append("Link Speed: "+wifiInfo.getLinkSpeed()+" Mbps\n");
        s.append("MAC address: "+wifiInfo.getMacAddress()+"\n");
        int ip = wifiInfo.getIpAddress();
        String ipAddress = Formatter.formatIpAddress(ip);
        s.append("IP address: "+ipAddress+"\n");
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(),5);
        s.append("WiFi Strength level: "+level+"\n");

        TextView ans = findViewById(R.id.speed);
//        Log.v("Wifi", s.toString());
        ans.setText(s.toString());

        if(isExternalStorageWritable() && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            File file = new File(Environment.getExternalStorageDirectory(),name);
            Log.v("External file", file.toString());

            try {
                FileOutputStream fos = new FileOutputStream(file);

                //level =0;
                StringBuilder log = new StringBuilder();

                for(int i=0;i<60;i++) {
                    wifiInfo = wifiManager.getConnectionInfo();
                    level = wifiInfo.getRssi();
                    log.append(String.valueOf(level)+" dbm\n");
                    Thread.sleep(100);
                }
                fos.write(log.toString().getBytes());
                fos.close();
                Toast.makeText(this,"File Saved to: "+file.toString(),Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
        else {
            Toast.makeText(this,"Cannot perform write operation, Permission denied",Toast.LENGTH_LONG).show();
        }

        File file = new File(Environment.getExternalStorageDirectory(),name);

        try {
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            ArrayList<String> disp = new ArrayList<String>();
            String text;

            while((text = br.readLine())!=null) {
                disp.add(text);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1, disp);
            ListView listView = findViewById(R.id.display);
            listView.setAdapter(adapter);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disp(View view) {
        File file = new File(Environment.getExternalStorageDirectory(),name);

        Uri selectedUri = Uri.parse(file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, "text/csv");

        if (intent.resolveActivityInfo(getPackageManager(), 0) != null)
        {
            startActivity(intent);
        }
    }

    public boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this,permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }
}
