package com.example.aaa1.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class TestActivity extends AppCompatActivity {


    Button TestBtn,backHomeBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        TestBtn = (Button)findViewById(R.id.TestStartBtn);

        // 테스트 시작 버튼 누르면
        TestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),TestingActivity_L.class);
                startActivity(intent);
            }
        });


    }
}

