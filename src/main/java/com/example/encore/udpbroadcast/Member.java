package com.example.encore.udpbroadcast;

/**
 * Created by encore on 2016/4/10.
 */
public class Member {

    public String name = null;
    public String ip = null;
    public int icon = -1;

    public Member(String name , String ip , int icon){
        this.name = name;
        this.ip = ip;
        this.icon = icon;
    }
}
