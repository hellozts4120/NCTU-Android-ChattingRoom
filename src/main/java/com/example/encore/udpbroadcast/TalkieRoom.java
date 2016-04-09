package com.example.encore.udpbroadcast;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class TalkieRoom extends AppCompatActivity {

    private MulticastSocket ds;
    String multicastHost;   //="192.168.43.255";
    String localIP;
    private String myName = "default";
    InetAddress receiveAddress;
    private TextView tv  ;
    private Button btnSendBroadcast;
    private EditText message;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        myName = intent.getStringExtra("name");

        tv = (TextView) findViewById(R.id.ReceiveText);
        message = (EditText) findViewById(R.id.Message);
        message.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(keyCode==KeyEvent.KEYCODE_ENTER){
                    String text = message.getText().toString();
                    if(text != null){
                        String ip = getLocalMacAddressFromWifiInfo(TalkieRoom.this);
                        text = myName + ":"  + text;
                        SendBroadcastMessage sbm = new SendBroadcastMessage(ip ,text ,TalkieRoom.this);
                        sbm.start();
                        message.setText("");
                    }
                    return true;
                }
                else return false;
            }
        });

        btnSendBroadcast = (Button) findViewById(R.id.btnSendBroadcast);
        btnSendBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = message.getText().toString();
                if(text != null && text !=""){
                    String ip = getLocalMacAddressFromWifiInfo(TalkieRoom.this);
                    text = myName +":" +text;
                    SendBroadcastMessage sbm = new SendBroadcastMessage(ip ,text ,TalkieRoom.this);
                    sbm.start();
                    message.setText("");
                }
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        AsyncTask<Void , String , Void> read = new AsyncTask<Void, String, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ds = new MulticastSocket(8003);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    receiveAddress=InetAddress.getByName(multicastHost);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                try {
                    ds.joinGroup(receiveAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println("Connected!");
                byte buf[] = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buf, 1024);
                while (true){
                    boolean flag = true;
                    try {
                        ds.receive(dp);
                        flag = dp.getAddress().getHostAddress().equals(localIP);
                    } catch (IOException e) {
                        System.out.println("Listen error!");
                        e.printStackTrace();
                    }
                    String message = new String(buf, 0, dp.getLength());
//                    System.out.println("client ip : " + message);
                    if(flag){
                        String[] send = message.split(":");
                        publishProgress("Me:"+send[1]+"\n");
                    }
                    else {
                        publishProgress(message+"\n");
                    }

                }

            }

            @Override
            protected void onProgressUpdate(String... values) {
                tv.append(values[0].toString());
                super.onProgressUpdate(values);

            }
        };
        read.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public String getLocalMacAddressFromWifiInfo(Context context)
    {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifi.isWifiEnabled()){
            Toast.makeText(TalkieRoom.this, "Please open Wifi connection!", Toast.LENGTH_SHORT).show();
        }
        WifiInfo info = wifi.getConnectionInfo();

//        String macAdress = info.getMacAddress(); //获取mac地址
        int ipAddress = info.getIpAddress();  //获取ip地址
        if (ipAddress == 0){
            Toast.makeText(TalkieRoom.this, "Wifi is not connected!", Toast.LENGTH_SHORT).show();
        }
        localIP = intToIp(ipAddress);
        System.out.println("Local IP is: "+localIP);
        String broadcastip =getBroadcastIP(ipAddress);
//        System.out.println("Broadcast IP is: "+broadcastip);

        return broadcastip;
    }

    public String intToIp(int ipAdress)
    {
        return (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + "." +
                ( ipAdress >> 24 & 0xFF) ;
    }

    private String getBroadcastIP(int ipAdress){
        multicastHost = (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + ".255" ;
        return multicastHost;
    }
}
