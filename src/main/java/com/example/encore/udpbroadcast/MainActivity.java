package com.example.encore.udpbroadcast;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText NameText;
    private Button btnLogin;
    private Button btnPic0;
    private Button btnPic1;
    private Button btnPic2;
    private Button btnPic3;
    private Button btnPic4;
    private Button btnPic5;
    private Button btnPic6;
    private Button btnPic7;

    private int iconID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        NameText = (EditText) findViewById(R.id.NameText);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnPic0 = (Button) findViewById(R.id.pic0);
        btnPic1 = (Button) findViewById(R.id.pic1);
        btnPic2 = (Button) findViewById(R.id.pic2);
        btnPic3 = (Button) findViewById(R.id.pic3);
        btnPic4 = (Button) findViewById(R.id.pic4);
        btnPic5 = (Button) findViewById(R.id.pic5);
        btnPic6 = (Button) findViewById(R.id.pic6);
        btnPic7 = (Button) findViewById(R.id.pic7);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this , TalkieRoom.class);
                String name = NameText.getText().toString();
                if (name != null && name.length() > 1){
                    if (iconID != -1){
                        intent.putExtra("name" , name);
                        intent.putExtra("icon" , iconID);
                        startActivity(intent);
                    }
                    else Toast.makeText(MainActivity.this, "Please choose your icon!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Please input your name!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnPic0.setOnClickListener(this);
        btnPic1.setOnClickListener(this);
        btnPic2.setOnClickListener(this);
        btnPic3.setOnClickListener(this);
        btnPic4.setOnClickListener(this);
        btnPic5.setOnClickListener(this);
        btnPic6.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pic0:
                iconID = 0;
                break;
            case R.id.pic1:
                iconID = 1;
                break;
            case R.id.pic2:
                iconID = 2;
                break;
            case R.id.pic3:
                iconID = 3;
                break;
            case R.id.pic4:
                iconID = 4;
                break;
            case R.id.pic5:
                iconID = 5;
                break;
            case R.id.pic6:
                iconID = 6;
                break;
            case R.id.pic7:
                iconID = 7;
                break;
            default:
                break;
        }
    }
}
