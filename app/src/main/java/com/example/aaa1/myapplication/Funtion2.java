package com.example.aaa1.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class Funtion2 extends AppCompatActivity {

    private AudioReader  audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;

    Button btn1, btn2;
    TextView txtView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funtion2);

        btn1 = (Button)findViewById(R.id.btn1);
        btn2 = (Button)findViewById(R.id.btn2);
        txtView = (TextView)findViewById(R.id.txtView);

        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStart(v);
                Log.d("###", "hehe");
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doStop(v);
            }
        });
    }

    public void doStart(View v)
    {
        audioReader.startReader(sampleRate, inputBlockSize * sampleDecimate, new AudioReader.Listener()
        {
            @Override
            public final void onReadComplete(int dB)
            {
                receiveDecibel(dB);
            }

            @Override
            public void onReadError(int error)
            {

                Log.d("###", "error");
            }
        });
    }

    private void receiveDecibel(final int dB)
    {
        Log.e("###", dB+" dB");
        //txtView.setText(dB);
    }

    public void doStop(View v)
    {
        audioReader.stopReader();
    }

}