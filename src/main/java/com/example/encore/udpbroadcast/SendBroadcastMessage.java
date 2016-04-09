package com.example.encore.udpbroadcast;

import android.content.Context;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

/**
 * Created by encore on 2016/4/9.
 */
public class SendBroadcastMessage extends Thread {

    private MulticastSocket ms;
    private String ip;
    private Context context;
    private String mymsg;
    public SendBroadcastMessage(String ip ,String mymsg ,Context context){
        try {
            ms = new MulticastSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.context = context;
        this.ip = ip;
        this.mymsg = mymsg;
    }

    @Override
    public void run() {

        super.run();
        DatagramPacket datapacket = null;


        byte[] data = mymsg.getBytes();
        //224.0.0.1为广播地址
        InetAddress address = null;
        try {
            address = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
//            Toast.makeText(context, "Wrong IP!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
        //这个地方可以输出判断该地址是不是广播类型的地址
//                System.out.println(address.isMulticastAddress());
        datapacket = new DatagramPacket(data, data.length, address,
                8003);
        try {
            ms.send(datapacket);
        } catch (IOException e) {
//            Toast.makeText(context, "Failed to Send Message!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
                ms.close();
    }
}
