package com.example.aaa1.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {

    static String changedName = "";
    static String changedAge="";

    Button testBtn,myRecord,introduceBtn;
    static TextView userNameText;
    static TextView userAgeText;
    ImageView backBtn;


    LinearLayout profile_edit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        setTitle("설정");
        testBtn = (Button)findViewById(R.id.testBtn);
        myRecord = (Button)findViewById(R.id.myRecord);
        backBtn = (ImageView)findViewById(R.id.backBtn);
        introduceBtn = (Button)findViewById(R.id.introduceBtn);

        userNameText = (TextView)findViewById(R.id.userNameText);
        userAgeText = (TextView)findViewById(R.id.userAgeText);

        profile_edit = (LinearLayout)findViewById(R.id.profile_edit);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        introduceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),IntroduceActivity.class);
                startActivity(intent);

            }
        });
        myRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),RecordActivity.class);
                startActivity(intent);
            }
        });

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TestActivity.class);
                startActivity(intent);
            }
        });


        profile_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(),EditActivity.class);
                startActivity(intent);

            }
        });


        // 정보 초기화
        init();

    }




    public void init()
    {

        // 실제로는 db에서 불러와야 함
        String name = "박진숙";
        String age = "68";
        userNameText.setText(name);
        userAgeText.setText(age+"세");
    }
}
