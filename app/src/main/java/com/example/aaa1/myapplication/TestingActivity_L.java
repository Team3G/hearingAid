package com.example.aaa1.myapplication;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestingActivity_L extends AppCompatActivity {

    Button nextBtn, beforeBtn;
    String TAG = "hello";
    int i=0;
    float volume=(float)0.2;
    String p;
    String[] sound = new String[12];
    MediaPlayer sound1, soundDecibel;
    int decibelTest;
    boolean decibel=true;
    static int lf_hertz=330, lf_decibel=44;
    public static String left_result="";

    int flag  = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        left_result = "";
        setContentView(R.layout.activity_testing_l);
        i=0;

        setContentView(R.layout.activity_testing_l);
        p= this.getPackageName();
        sound[0] = "@raw/a_1";
        sound[1] = "@raw/a_2";
        sound[2] = "@raw/a_3";
        sound[3] = "@raw/a_4";
        sound[4] = "@raw/a_5";
        sound[5] = "@raw/a_6";
        sound[6] = "@raw/a_7";
        sound[7] = "@raw/a_8";
        sound[8] = "@raw/a_9";
        sound[9] = "@raw/a_10";
        sound[10] = "@raw/a_11";
        sound[11] = "@raw/a_12";
        decibelTest= getResources().getIdentifier("@raw/winter", "raw", p);
        soundDecibel = MediaPlayer.create(getApplicationContext(), decibelTest);

        nextBtn = (Button) findViewById(R.id.nextBtn);//들랴요
        beforeBtn = (Button) findViewById(R.id.beforeBtn);//안들려요

        //play();
        decibel();

        nextBtn.setOnClickListener(new View.OnClickListener() {//들려ㅕ요
            @Override
            public void onClick(View view) {//들릴경우. decibel 시 들릴 경우 decibel 검사 종료 후 주파수 검사 시작
                if(decibel){
                    showMessageDe();
                }else {
                    left_result+="1";
                    play();
                }
                Log.d("안녕",left_result+"");
            }
        });

        beforeBtn.setOnClickListener(new View.OnClickListener() {//안들려요
            @Override
            public void onClick(View view) {
                if(decibel)decibel();//데시벨일 떄 안들릴 경우 다음 데시벨
                else {//헤르츠 검사는 끝까지함.
                    left_result+="0";
                    play();
                }
                Log.d("안녕",left_result+"");
            }
        });

    }


    public void decibel(){
        soundDecibel.stop();
        soundDecibel = MediaPlayer.create(getApplicationContext(), decibelTest);
        if(volume>=1)showMessageDe();
        soundDecibel.setVolume(volume,0);
        volume=volume+(float)0.2;
        soundDecibel.start();
    }
    public void play(){
        if(i>=12){
            decibel=true;
            //decibel();
            Log.d("안녕",left_result);
            showMessage();
        }

        try{
            int resId = getResources().getIdentifier(sound[i], "raw", p);
            Log.d("안녕",resId+"");
            sound1 = MediaPlayer.create(getApplicationContext(), resId);
            sound1.setVolume(volume,0);
            sound1.start();
        }catch (Exception e){

        }

        i++;
    }


    public void showMessage() {
        sound1.stop();

        lf_decibel=(int)(volume*10);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("알림");
        builder.setMessage("오른쪽 검사를 시작하겠습니까?");

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent(getApplicationContext(), TestingActivity_R.class);
                finish();
                startActivity(intent);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //lf_hertz = 440;
                //lf_decibel= 30;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
    public void showMessageDe() {
        decibel=false;
        soundDecibel.stop();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Log.d("안녕","연습");
        builder.setTitle("알림");
        builder.setMessage("주파수 검사를 시작합니다.");

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                decibel=false;
                play();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }
}