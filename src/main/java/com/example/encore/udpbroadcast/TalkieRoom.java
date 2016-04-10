package com.example.encore.udpbroadcast;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class TalkieRoom extends AppCompatActivity {

    private MulticastSocket ds;
    String multicastHost;
    String localIP;
    private int myIcon;
    private String myName = "default name";
    InetAddress receiveAddress;
    private Vector<Member> memberList = null;

    private TextView tv  ;
    private Button btnSendBroadcast;
    private EditText message;
    private TextView username;
    private ImageButton usericon;
    private ListView chatListView = null;
    protected MyChatAdapter adapter=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        memberList = new Vector<Member>();

        Intent intent = getIntent();
        myName = intent.getStringExtra("name");
        myIcon = intent.getIntExtra("icon" , 0);

        //initialize user part
        username = (TextView) findViewById(R.id.user_name);
        username.setText(myName);
        usericon = (ImageButton) findViewById(R.id.user_image);
        setIconID(myIcon);
        chatListView = (ListView) findViewById(R.id.msg_list);

        tv = (TextView) findViewById(R.id.user_name);
        message = (EditText) findViewById(R.id.chat_bottom_edittext);
        message.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    String text = message.getText().toString();
                    if (text != null) {
                        String ip = getLocalMacAddressFromWifiInfo(TalkieRoom.this);
                        text = myName + ":" + text;
                        SendBroadcastMessage sbm = new SendBroadcastMessage(ip, text, TalkieRoom.this);
                        sbm.start();
                        message.setText("");
                    }
                    return true;
                } else return false;
            }
        });

        btnSendBroadcast = (Button) findViewById(R.id.chat_bottom_sendbutton);
        btnSendBroadcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = message.getText().toString();
                if (text != null && text.length() != 0) {
                    String ip = getLocalMacAddressFromWifiInfo(TalkieRoom.this);
                    text = myName + "@@" + myIcon + "@@"+text;
                    SendBroadcastMessage sbm = new SendBroadcastMessage(ip, text, TalkieRoom.this);
                    sbm.start();
                    message.setText("");
                }
            }
        });

        chatList=new ArrayList<HashMap<String,Object>>();
//        addTextToList("三个代表", ME);
//        addTextToList("大新闻\n  ^_^", OTHER);
//        addTextToList("蛤蛤蛤蛤蛤蛤蛤蛤", ME);
//        addTextToList("excited", OTHER);
        adapter = new MyChatAdapter(this,chatList,layout,from,to);

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
                    String ipAddress = null;
                    try {
                        ds.receive(dp);
                        ipAddress = dp.getAddress().getHostAddress();
                    } catch (IOException e) {
                        System.out.println("Listen error!");
                        e.printStackTrace();
                    }
                    String message = new String(buf, 0, dp.getLength());
//                    System.out.println("client ip : " + message);
                    if(ipAddress != null){
                        String[] send = message.split("@@");
                        if(send[2].length() != 0){
                            if(ipAddress.equals(localIP)){
                                publishProgress(localIP , String.valueOf(myIcon) , send[2]);
                            }
                            else {
                                if (!isMember(ipAddress)){
                                    int ic = Integer.parseInt(send[1]);
                                    addMember(send[0] , ipAddress , ic);
                                    System.out.println("add member :ip " + ipAddress + "\tname:" + send[0]);
                                }
                                publishProgress(ipAddress , send[1] , send[2]);
                            }
                        }

                    }
                }

            }

            @Override
            protected void onProgressUpdate(String... values) {
//                tv.append(values[0].toString());
                addTextToList(values[2], values[0], values[1]);
                adapter.notifyDataSetChanged();
                chatListView.setSelection(chatList.size() - 1);
                super.onProgressUpdate(values);

            }
        };
        read.execute();
        chatListView.setAdapter(adapter);
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
//        System.out.println("Local IP is: "+localIP);
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

    private void setIconID(int id){

        switch (id){
            case 0:
                usericon.setImageResource(R.drawable.pic0);

                break;
            case 1:
                usericon.setImageResource(R.drawable.pic1);
                break;
            case 2:
                usericon.setImageResource(R.drawable.pic2);
                break;
            case 3:
                usericon.setImageResource(R.drawable.pic3);
                break;
            case 4:
                usericon.setImageResource(R.drawable.pic4);
                break;
            case 5:
                usericon.setImageResource(R.drawable.pic5);
                break;
            case 6:
                usericon.setImageResource(R.drawable.pic6);
                break;
            case 7:
                usericon.setImageResource(R.drawable.pic7);
                break;
        }
    }

    private boolean isMember(String ip){
        int i = 0;
        for(i = 0 ; i < memberList.size() ; i++){
            if(memberList.elementAt(i).ip.equals(ip))
                return true;
        }
        return false;
    }

    private void addMember(String name , String ip , int icon){
        memberList.add(new Member(name , ip ,icon));
    }


    ArrayList<HashMap<String,Object>> chatList = null;
    String[] from={"image","text"};
    int[] to={R.id.chatlist_image_me, R.id.chatlist_text_me, R.id.chatlist_image_other, R.id.chatlist_text_other};
    int[] layout={R.layout.my_msg, R.layout.others_msg};
    String userId=null;


    public final static int OTHER = 1;
    public final static int ME = 0;

    protected void addTextToList(String text, String who , String icon){
        HashMap<String,Object> map=new HashMap<String,Object>();
        map.put("person",who );
        int iconid = Integer.parseInt(icon);

        System.out.println(iconid);
        switch (iconid){
            case 0:
                map.put("image", R.drawable.pic0);
                break;
            case 1:
                map.put("image", R.drawable.pic1);
                break;
            case 2:
                map.put("image", R.drawable.pic2);
                break;
            case 3:
                map.put("image", R.drawable.pic3);
                break;
            case 4:
                map.put("image", R.drawable.pic4);
                break;
            case 5:
                map.put("image", R.drawable.pic5);
                break;
            case 6:
                map.put("image", R.drawable.pic6);
                break;
            case 7:
                map.put("image", R.drawable.pic7);
                break;
            default:
                map.put("image", R.drawable.pic0);
                break;
        }

        map.put("text", text);
        chatList.add(map);
    }

    private class MyChatAdapter extends BaseAdapter {

        Context context = null;
        ArrayList<HashMap<String, Object>> chatList = null;
        int[] layout;
        String[] from;
        int[] to;


        public MyChatAdapter(Context context,
                             ArrayList<HashMap<String, Object>> chatList, int[] layout,
                             String[] from, int[] to) {
            super();
            this.context = context;
            this.chatList = chatList;
            this.layout = layout;
            this.from = from;
            this.to = to;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return chatList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        class ViewHolder {
            public ImageView imageView = null;
            public TextView textView = null;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder holder = null;
            int who;
            if(chatList.get(position).get("person").equals(localIP)){
                who = ME;
            }
            else who = OTHER;


            convertView = LayoutInflater.from(context).inflate(
                    layout[who == ME ? 0 : 1], null);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(to[who * 2 + 0]);
            holder.textView = (TextView) convertView.findViewById(to[who * 2 + 1]);

            holder.imageView.setBackgroundResource((Integer) chatList.get(position).get(from[0]));
            holder.textView.setText(chatList.get(position).get(from[1]).toString());
            return convertView;
        }
    }
}
