package com.example.aaa1.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

public class RecordActivity extends AppCompatActivity {


    ImageView backBtn;
    TabHost tabHost;
    FrameLayout frameLayout;
    TextView userNameText,a_left,b_left,c_left,d_left,e_left,f_left,g_left,b2_left,c2_left,e2_left,f2_left,g2_left;
    TextView a_right,b_right,c_right,d_right,e_right,f_right,g_right,b2_right,c2_right,e2_right,f2_right,g2_right;

    TextView[] left = new TextView[12];
    TextView[] right = new TextView[12];



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        userNameText = (TextView)findViewById(R.id.userNameText);
        backBtn = (ImageView)findViewById(R.id.backBtn);
        tabHost = (TabHost) findViewById(R.id.tabhost1);
        tabHost.setup();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        frameLayout = (FrameLayout) findViewById(android.R.id.tabcontent);
        TabHost.TabSpec tab1 = tabHost.newTabSpec("1").setContent(R.id.tab1).setIndicator("왼쪽 귀 검사결과");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("2").setContent(R.id.tab2).setIndicator("오른쪽 귀 검사결과");

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);

        init();


    }

    void init(){
        //사용자이름으로 바꿔야 한다.
        userNameText.setText("박진숙님의 결과");

        a_left = (TextView)findViewById(R.id.text_a_left);
        b_left = (TextView)findViewById(R.id.text_b_left);
        c_left = (TextView)findViewById(R.id.text_c_left);
        d_left = (TextView)findViewById(R.id.text_d_left);
        e_left = (TextView)findViewById(R.id.text_e_left);
        f_left = (TextView)findViewById(R.id.text_f_left);
        g_left = (TextView)findViewById(R.id.text_g_left);
        b2_left = (TextView)findViewById(R.id.text_b2_left);
        c2_left = (TextView)findViewById(R.id.text_c2_left);
        e2_left = (TextView)findViewById(R.id.text_e2_left);
        f2_left = (TextView)findViewById(R.id.text_f2_left);
        g2_left = (TextView)findViewById(R.id.text_g2_left);

        a_right = (TextView)findViewById(R.id.text_a_right);
        b_right = (TextView)findViewById(R.id.text_b_right);
        c_right = (TextView)findViewById(R.id.text_c_right);
        d_right = (TextView)findViewById(R.id.text_d_right);
        e_right = (TextView)findViewById(R.id.text_e_right);
        f_right = (TextView)findViewById(R.id.text_f_right);
        g_right = (TextView)findViewById(R.id.text_g_right);
        b2_right = (TextView)findViewById(R.id.text_b2_right);
        c2_right = (TextView)findViewById(R.id.text_c2_right);
        e2_right = (TextView)findViewById(R.id.text_e2_right);
        f2_right = (TextView)findViewById(R.id.text_f2_right);
        g2_right = (TextView)findViewById(R.id.text_g_right2);

        left[0] = a_left;
        left[1] = b2_left;
        left[2]= b_left;
        left[3] = c_left;
        left[4] = c2_left;
        left[5] = d_left;
        left[6] = e2_left;
        left[7] = e_left;
        left[8] = f_left;
        left[9] = f2_left;
        left[10] = g_left;
        left[11] = g2_left;

        right[0] = a_right;
        right[1] = b2_right;
        right[2]= b_right;
        right[3] = c_right;
        right[4] = c2_right;
        right[5] = d_right;
        right[6] = e2_right;
        right[7] = e_right;
        right[8] = f_right;
        right[9] = f2_right;
        right[10] = g_right;
        right[11] = g2_right;

        changeText_left();
        changeText_right();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
        startActivity(intent);
        finish();

    }
    public void changeText_left(){
        //db에서 불러온 걸로 바꾸면 될 것 같다.

        for(int i = 0; i<left.length; i++) {
            char left_rst = TestingActivity_L.left_result.charAt(i);
            if(left_rst == '1')
                left[i].setText("O");
            else
                left[i].setText("X");
        }
    }

    public void changeText_right(){
        //db에서 불러온 걸로 바꾸면 될 것 같다.
        for(int i = 0; i<left.length; i++) {
            char right_rst = TestingActivity_R.right_result.charAt(i);
            if(right_rst == '1')
                right[i].setText("O");
            else
                right[i].setText("X");
        }
    }
}