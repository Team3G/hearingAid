package com.example.aaa1.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;


//FFT(Fast Fourier Transform) DFT 알고리즘 : 데이터를 시간 기준(time base)에서 주파수 기준(frequency base)으로 바꾸는데 사용.
public class MainActivity extends Activity {


    LinearLayout settingLinear,readingLinear,modulateLinear,talkLinear;
    TextView welcomeText;
    public static int[][] codes = new int[12][2];

    public static String result="";
    public static int cnt=0;

    public static int[] result_final;

    String mJsonString;

    String user_name="admin";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        talkLinear = (LinearLayout)findViewById(R.id.talkLinear);
        modulateLinear = (LinearLayout)findViewById(R.id.modulateLinear);
        readingLinear = (LinearLayout)findViewById(R.id.readingLinear);
        settingLinear = (LinearLayout)findViewById(R.id.settingLinear);
        welcomeText = (TextView)findViewById(R.id.welcomeText);


        codes[0][0] = 479; // 시
        codes[0][1] = 508;
        codes[1][0] = 509; // 도
        codes[1][1] = 538;
        codes[2][0] = 539; // 도#
        codes[2][1] = 570;
        codes[3][0] = 571; // 레
        codes[3][1] = 604;
        codes[4][0] = 605; // 레#
        codes[4][1] = 640;
        codes[5][0] = 641; // 미
        codes[5][1] = 678;
        codes[6][0] = 679; //파
        codes[6][1] = 718;
        codes[7][0] = 719; // 파#
        codes[7][1] = 761;
        codes[8][0] = 762; // 솔
        codes[8][1] = 806;
        codes[9][0] = 807; // 솔#
        codes[9][1] = 855;
        codes[10][0] =856; // 라
        codes[10][1] = 906;
        codes[11][0] = 907; // 라#
        codes[11][1] = 959;


        // 로그인하면 바꿔야 한다
        welcomeText.setText("환영합니다!");

        talkLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RealtimeTalkActivity.class);
                startActivity(intent);
            }
        });
        modulateLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ModulateActivity.class);
                startActivity(intent);
            }
        });

        readingLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),ReadingActivity.class);
                startActivity(intent);
            }
        });

        settingLinear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });

        if(TestingActivity_L.left_result.length()<12 || TestingActivity_R.right_result.length()<12){//검사 안하고 넘어온 경우
            GetData g=new GetData();
            g.execute(user_name);
        }
        else answer();


    }
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "정보를 가져오는 중입니다.", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d("안녕", "response - " + result);

            if (result == null){

                //mTextViewResult.setText(errorString);
            }
            else {

                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {

            String searchKeyword = params[0];

            String serverURL = "http://18.188.180.156/3g_project/aws/query.php";
            String postParameters = "user_name=" + searchKeyword;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d("", "response code - " + responseStatusCode);

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
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();


            } catch (Exception e) {

                Log.d("", "Query: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }
    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("webnautes");

            JSONObject item = jsonArray.getJSONObject(jsonArray.length()-1);//가장 최근 검사 결과

            TestingActivity_L.left_result = item.getString("left_hearing");
            TestingActivity_R.right_result=item.getString("right_hearing");
            Log.d("결과입니다.",TestingActivity_L.left_result+"   "+ TestingActivity_R.right_result);

            answer();


        } catch (JSONException e) {//없을 경우 기본값

            TestingActivity_L.left_result="+111100001111";
            TestingActivity_R.right_result="111100001111";
            Log.d("ㄴ", "showResult : ", e);
            answer();
        }


    }
    public void answer() {

        createResult();
        makeResultFinal();

    }
    public void createResult(){
        //result 생성.
        try{//검사 완료한 경우
            for(int i = 0; i<12; i++) {
                char left_rst = TestingActivity_L.left_result.charAt(i);
                char right_rst = TestingActivity_R.right_result.charAt(i);

                if(left_rst == right_rst){//같은 결과일 경우
                    result+=left_rst;
                    if(left_rst=='0')cnt++;//둘다 안들리는 경우에도 cnt는 증가해야함
                }
                else {//다른 결과일 경우 > 둘 중 하나라도 안들리므로 무조건 안들림
                    result += "0";
                    cnt++;
                }
            }
            LoginActivity.insert(result);
            Log.d("안녕", "업데이"+"    "+TestingActivity_L.left_result+"    "+TestingActivity_R.right_result);
        }catch (Exception e){//검사하지 않은 경우
            result=LoginActivity.getResult();
            Log.d("안녕", "업데이오류");
        }
    }
    public void makeResultFinal(){
        Log.d("하희", result+"   "+cnt);
        result_final = new int[cnt];
        int j =0;

        for(int i =0; i<12; i++) {
            char r = result.charAt(i);

            if(r=='0'){
                result_final[j] = i;
                j++;
            }
        }
    }
}