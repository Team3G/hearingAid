package com.example.aaa1.myapplication;


import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;


public class Main2Activity extends AppCompatActivity {


    public static boolean Tested = false;
    TabHost tabHost;
    FrameLayout frameLayout;

    TextView menu_text;
    ImageView setting_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("메인페이지");


        menu_text = (TextView)findViewById(R.id.menu_text);
        setting_img = (ImageView)findViewById(R.id.setting_img);

        tabHost = (TabHost) findViewById(R.id.tabhost1);
        tabHost.setup();

        TabHost.TabSpec tab1 = tabHost.newTabSpec("1").setContent(R.id.tab1).setIndicator("대화하기");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("2").setContent(R.id.tab2).setIndicator("변조하기");
        TabHost.TabSpec tab3 = tabHost.newTabSpec("3").setContent(R.id.tab3).setIndicator("읽어주기");

        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        tabHost.addTab(tab3);

        init();

        setting_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent);
            }
        });

    }

    void init(){
        frameLayout = (FrameLayout) findViewById(android.R.id.tabcontent);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String number) {
                switch (number) {

                    case "1":
                        frameLayout.setBackgroundColor(Color.parseColor("#ede4fd"));
                        menu_text.setText("대화하기");
                        break;
                    case "2":
                        frameLayout.setBackgroundColor(Color.parseColor("#cfbfea"));
                        menu_text.setText("변조하기");
                        break;

                    case "3":
                        frameLayout.setBackgroundColor(Color.parseColor("#FFD2D4FC"));
                        menu_text.setText("읽어주기");
                        break;
                }

            }
        });
    }

    public boolean getTested(){
        return this.Tested;
    }

}