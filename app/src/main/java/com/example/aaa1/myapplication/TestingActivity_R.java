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
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.aaa1.myapplication.TestingActivity_L.lf_decibel;
import static com.example.aaa1.myapplication.TestingActivity_L.lf_hertz;

public class TestingActivity_R extends AppCompatActivity {

    String user_name ="admin";

    Button nextBtn, beforeBtn;
    String TAG = "hello";
    int i=0;
    float volume=(float)0.2;
    String p;
    String[] sound = new String[12];
    MediaPlayer sound1, soundDecibel;
    int decibelTest;
    boolean decibel=true;
    public static String right_result="";

    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testing_r);

        right_result="";

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
        i=0;

        decibelTest= getResources().getIdentifier("@raw/winter", "raw", p);
        soundDecibel = MediaPlayer.create(getApplicationContext(), decibelTest);

        nextBtn = (Button) findViewById(R.id.nextBtn);//들랴요
        beforeBtn = (Button) findViewById(R.id.beforeBtn);//안들려요

        decibel();

        nextBtn.setOnClickListener(new View.OnClickListener() {//들려ㅕ요
            @Override
            public void onClick(View view) {//들릴경우. decibel 시 들릴 경우 decibel 검사 종료 후 주파수 검사 시작
                if(decibel){
                    showMessageDe();
                }else {
                    right_result+="1";
                    play();

                }
            }
        });

        beforeBtn.setOnClickListener(new View.OnClickListener() {//안들려요
            @Override
            public void onClick(View view) {
                if(decibel)decibel();//데시벨일 떄 안들릴 경우 다음 데시벨
                else {//헤르츠 검사는 끝까지함.
                    right_result+="0";
                    play();
                }
            }
        });

    }
    public void decibel(){
        soundDecibel.stop();
        soundDecibel = MediaPlayer.create(getApplicationContext(), decibelTest);
        if(volume>=1)showMessageDe();
        soundDecibel.setVolume(0,volume);
        volume=volume+(float)0.2;
        soundDecibel.start();
    }
    public void play(){
        if(i>=12){
            decibel=true;
            Log.d("안녕",right_result);

            showMessage();
        }
        Log.d("안녕1",i+"");
        try{
            int resId = getResources().getIdentifier(sound[i], "raw", p);
            Log.d("안녕",resId+"");
            sound1 = MediaPlayer.create(getApplicationContext(), resId);
            sound1.setVolume(0,volume);
            sound1.start();
        }catch (Exception e){

        }
        i++;
    }
    public void showMessage(){
        sound1.stop();
        Toast.makeText(getApplicationContext(),"검사가 끝났습니다. \n수고하셧습니다.",Toast.LENGTH_LONG).show();
        InsertData task = new InsertData();
        task.execute(user_name,TestingActivity_L.left_result, right_result);
        Log.d("안녕123",TestingActivity_L.left_result+"   "+right_result);

        Intent intent = new Intent(getApplicationContext(),RecordActivity.class);
        startActivity(intent);

        Main2Activity.Tested = true;
    }
    public void showMessageDe() {
        decibel=false;
        soundDecibel.stop();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(TestingActivity_R.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            finish();
            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String user_name = (String)params[0];
            String left_hearing = (String)params[1];
            String right_hearing = (String)params[2];


            String serverURL = "helloWorld";
            String postParameters = "user_name=" + user_name + "&left_hearing=" + left_hearing + "&right_hearing=" + right_hearing;

            Log.d("안녕123",TestingActivity_L.left_result+"   "+right_result+user_name);
            Log.d("안녕1234",left_hearing+"   "+right_hearing);
            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();

                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

}


