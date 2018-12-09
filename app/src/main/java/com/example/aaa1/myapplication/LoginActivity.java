package com.example.aaa1.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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


public class LoginActivity extends AppCompatActivity {

    EditText id_EditText, pwd_EditText;
    Button login_Btn;
    TextView join_Text, find_Text;
    String TAG = "hello";
    String mJsonString;
    private static final String TAG_LF_DECIBEL = "lf_decibel";
    private static final String TAG_LF_HERTZ ="lf_hertz";
    private static final String TAG_RT_DECIBEL = "rt_decibel";
    private static final String TAG_RT_HERTZ ="rt_hertz";
    boolean tested=false;

    public static double hearingHz, headingDb;

    //오프라인 모드를 위한 디비 구현부
    public static SQLiteDatabase database;
    public final static String DB_NAME = "my_db";
    public static String table_name = "my_table";
    public static String table_step = "my_step";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        startActivity(new Intent(this,SplashActivity.class));

        //DB 생성
        createDB(DB_NAME);
        createTable(table_name);

        id_EditText = (EditText)findViewById(R.id.id_EditText);
        pwd_EditText = (EditText)findViewById(R.id.pwd_EditText);

        login_Btn = (Button)findViewById(R.id.login_Btn);
        join_Text = (TextView)findViewById(R.id.join_Text);
        find_Text = (TextView)findViewById(R.id.find_Text);


        // 로그인버튼 눌렀을 때
        login_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                check_login();
            }
        });


        // 회원가입 버튼 눌렀을 때
        join_Text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,JoinActivity.class);
                startActivity(intent);
            }
        });


    }


    public void createDB(String name) {
        try {
            database = openOrCreateDatabase(name, Activity.MODE_PRIVATE, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTable(String name) {
        database.execSQL("create table if not exists " + name + " ( _id integer PRIMARY KEY autoincrement, name text, result text);");
    }

    public static void insert(String result) {
        String[] where={"admin"};
        Cursor s1 = LoginActivity.database.rawQuery("select * from " + LoginActivity.table_name+" where name=?",where);

        if(s1.getCount()==0)//없을 경우 새로 입력
            database.execSQL("insert into " + table_name + " (name, result) values ('"+"admin"+"','" + result + "');");
        else //기존 이력이 있을 경우 업데이트
            update(result);
        Log.d("안녕","업데이트"+getNum());
    }
    public static void update(String result) {
        String[] where = {"admin"};
        ContentValues recoreView = new ContentValues();
        recoreView.put("result", result);
        database.update(table_name, recoreView, "name=?", where);
    }
    public static int getNum(){
        String[] where={"admin"};
        Cursor c= database.rawQuery("select * from "+table_name+" where name=?",where);
        c.moveToNext();

        return c.getCount();
    }
    public static String getResult(){
        String[] where={"admin"};
        Cursor c= database.rawQuery("select result from "+table_name+" where name=?",where);
        c.moveToNext();

        return c.getString(0);
    }
    // 로그인 검사하는 함수랄까
    public void check_login()
    {
        //로그인 성공 id_EditText.getText().toString().equals("admin") && pwd_EditText.getText().toString().equals("1234")
        if(true) {

            //서버에서 해당 사용자의 검사 결과 유무를 확인한다.
            GetData getTask = new GetData();
            getTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,id_EditText.getText().toString());
        }
        //로그인 실패
        else{
            Toast.makeText(getApplicationContext(),"로그인 실패",Toast.LENGTH_SHORT).show();
        }
    }

    class GetData extends AsyncTask<String, Void, String>{

        // ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "안녕response - " + result);

            if (result == null){//인터넷 접속 오류일 경우(오프라인 모드)
                Intent intent;
                Log.d("안녕",getNum()+"");
                if(getNum()<1){//검사 이력이 없을 경우
                    intent = new Intent(getApplicationContext(),TestActivity.class);
                }else {
                    intent=new Intent(getApplicationContext(),MainActivity.class);//기존 저장된 결과가 있을 경우
                }
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "인터넷 접속이 원활하지 않아 로그인 없이 진행합니다.", Toast.LENGTH_SHORT).show();
            }
            else {

                mJsonString = result;
                Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_SHORT).show();

                // 청력 검사 안했을 경우(처음 로그인 하는 경우)
                if(result.contains("찾을 수 없습니다")){
                    Main2Activity.Tested=false;
                    Intent intent = new Intent(getApplicationContext(),TestActivity.class);
                    startActivity(intent);

                }
                // 청력 검사 했을 경우
                else {
                    Main2Activity.Tested=true;
                    //showResult();
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
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
                Log.d(TAG, "response code - " + responseStatusCode);

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

                Log.d(TAG, "Query: Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("webnautes");

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);


                int lf_decibel = item.getInt(TAG_LF_DECIBEL);
                int lf_hertz= item.getInt(TAG_LF_HERTZ);
                int rt_decibel = item.getInt(TAG_RT_DECIBEL);
                int rt_hertz= item.getInt(TAG_RT_HERTZ);

                hearingHz=(lf_hertz+rt_hertz)/2;
                headingDb=(lf_decibel+rt_hertz)/2;

                HashMap<String,Integer> hashMap = new HashMap<>();


                hashMap.put(TAG_LF_DECIBEL, lf_decibel);
                hashMap.put(TAG_LF_HERTZ,lf_hertz);
                hashMap.put(TAG_RT_DECIBEL, rt_decibel);
                hashMap.put(TAG_RT_HERTZ,rt_hertz);

                Log.d("hello", lf_decibel + ", " + lf_hertz + ", " +rt_decibel + ", " +rt_hertz + ", " );

            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }


}

