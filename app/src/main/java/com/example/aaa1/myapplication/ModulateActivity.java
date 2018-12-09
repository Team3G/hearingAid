package com.example.aaa1.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.MultichannelToMono;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.WaveformSimilarityBasedOverlapAdd;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.resample.RateTransposer;


//FFT(Fast Fourier Transform) DFT 알고리즘 : 데이터를 시간 기준(time base)에서 주파수 기준(frequency base)으로 바꾸는데 사용.
public class ModulateActivity extends Activity {

    ImageView backBtn;
    private long time = 0;

    public static Modulation modulationTask;
    public static AudioRecord audioInput;

    /*변조 변수*/
    private AudioReader  audioReader2;
    private int sampleRate2 = 8000;
    private int inputBlockSize2 = 256;
    private int sampleDecimate2 = 1;
    private int db2 = 0;
    Button btn12, btn22;
    TextView txtView2;
    View newView2;
    int during2=0;
    boolean isPlaying2=true;
    String mFilePath2= Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm";
    int frequency2 = 8000;
    int channelConfiguration2 = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding2 = AudioFormat.ENCODING_PCM_16BIT;
    private RealDoubleFFT transformer2;
    int blockSize2 = 256;
    public static boolean started2 = false;
    short[] buffer2 = new short[blockSize2];

    LinearLayout warning;

    private AudioReader  audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int sampleDecimate = 1;
    private int db = 0;
    Button btn1, btn2;
    TextView txtView;
    View newView;
    String TAG = "hello";
    String mJsonString;

    private static final String TAG_JSON="testapi";
    private static final String TAG_LF_DECIBEL = "lf_decibel";
    private static final String TAG_LF_HERTZ ="lf_hertz";
    private static final String TAG_RT_DECIBEL = "rt_decibel";
    private static final String TAG_RT_HERTZ ="rt_hertz";


    //추가한 것


    public static boolean Tested = false;
    FrameLayout frameLayout;


    boolean isPlaying=true;
    String mFilePath= Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm";;



    // AudioRecord 객체에서 주파수는 8kHz, 오디오 채널은 하나, 샘플은 16비트를 사용
    int frequency = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    // 우리의 FFT 객체는 transformer고, 이 FFT 객체를 통해 AudioRecord 객체에서 한 번에 256가지 샘플을
    // 다룬다. 사용하는 샘플의 수는 FFT 객체를 통해
    // 샘플들을 실행하고 가져올 주파수의 수와 일치한다. 다른 크기를 마음대로 지정해도 되지만, 메모리와 성능 측면을 반드시 고려해야
    // 한다.
    // 적용될 수학적 계산이 프로세서의 성능과 밀접한 관계를 보이기 때문이다.
    private RealDoubleFFT transformer;
    int blockSize = 256;
    Button startStopButton;
    //   boolean started = false;


    // Bitmap 이미지를 표시하기 위해 ImageView를 사용한다. 이 이미지는 현재 오디오 스트림에서 주파수들의 레벨을 나타낸다.
    // 이 레벨들을 그리려면 Bitmap에서 구성한 Canvas 객체와 Paint객체가 필요하다.
    ImageView imageView;
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;
    public static AudioTrack mAudioTrack;
    static Button mBtPlay;
    Thread mPlayThread;

    String user_name="admin";

    // 1027추가
    double rate = 1.0;
    double factor;
    RateTransposer rateTransposer;
    public static AudioDispatcher dispatcher;
    WaveformSimilarityBasedOverlapAdd wsola;

    // 1030 추가
    int[][] not = new int[MainActivity.cnt][2];
    int[] cents = new int[MainActivity.cnt];
    AndroidAudioPlayer player;

    LocalBroadcastManager broadcaster;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modulate);

        make_cents();

        // GetData task = new GetData();
        // task.execute(user_name);

        broadcaster = LocalBroadcastManager.getInstance(this);

        Log.d("주파수","입니다");
        backBtn = (ImageView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                off();
                finish();
            }
        });



        warning=(LinearLayout)findViewById(R.id.warning) ;

        txtView = (TextView)findViewById(R.id.txtView);
        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        mBtPlay=(Button)findViewById(R.id.play);



        mBtPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                /*if(RealtimeTalkActivity.started){
                    // 다른 페이지에서 하는 거 멈추려고
                    RealtimeTalkActivity.started = false;
                    RealtimeTalkActivity.startStopButton.setText("Start");
                    //doStop(arg0);
                    RealtimeTalkActivity.recordTask.cancel(true);
                }*/
                // 지금 하고 있는 거 멈출려고
                if (started2) {
                    started2 = false;
                    mBtPlay.setText("Start");
                    //doStop(arg0);
                    modulationTask.cancel(true);
                    dispatcher.stop();
                } else {
                    // 시작하려고
                    started2 = true;
                    mBtPlay.setText("Stop");

                    modulationTask = new Modulation();
                    modulationTask.execute();

                }

            }
        });

        // RealDoubleFFT 클래스 컨스트럭터는 한번에 처리할 샘플들의 수를 받는다. 그리고 출력될 주파수 범위들의 수를
        // 나타낸다.
        transformer = new RealDoubleFFT(blockSize);

        // ImageView 및 관련 객체 설정 부분
        imageView = (ImageView) findViewById(R.id.ImageView01);
        bitmap = Bitmap.createBitmap(256, 100, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
        paint.setColor(Color.GREEN);
        imageView.setImageBitmap(bitmap);

    }

    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ModulateActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

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

    public static void off(){

        if(ModulateActivity.started2){
            if(ModulateActivity.modulationTask!=null){
                ModulateActivity.modulationTask.cancel(true);
                //ModulateActivity.audioInput.stop();
                //ModulateActivity.audioInput.release();
                // ModulateActivity.mAudioTrack.stop();
                //ModulateActivity.mAudioTrack.release();
                ModulateActivity.started2=false;
                dispatcher.stop();
            }
        }
    }

    private void make_cents() {
        for(int i = 0; i<not.length; i++) {
            not[i][0] = MainActivity.codes[MainActivity.result_final[i]][0];
            not[i][1] = MainActivity.codes[MainActivity.result_final[i]][1];
            Log.d("하희", not[i][0]+"/"+not[i][1]);
        }

        int count = 0;
        for(int i = not.length-1; i>=0;) {
            try{
                while(not[i-count][0] - not[i-1-count][1] == 1)
                {
                    count++;
                    Log.d("하희123",not[i][0]+","+not[i-1][1]+" "+count);
                    if(count>100)break;
                }
            }catch (Exception e){//마지막까지 안들리는 경우
                for(int k =0; k<count; k++)
                    cents[i-k]=-(count+1)*100;
            }
            count+=1;
            for(int k =0; k<count; k++)
                cents[i-k]=-(count-k)*100;
            i=i-count;
            count=0;
        }

        for(int i = 0; i<not.length; i++) {
            Log.d("하희", cents[i]+"");
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

                HashMap<String,Integer> hashMap = new HashMap<>();


                hashMap.put(TAG_LF_DECIBEL, lf_decibel);
                hashMap.put(TAG_LF_HERTZ,lf_hertz);
                hashMap.put(TAG_RT_DECIBEL, rt_decibel);
                hashMap.put(TAG_RT_HERTZ,rt_hertz);

                // Log.d("hello", lf_decibel + ", " + lf_hertz + ", " +rt_decibel + ", " +rt_hertz + ", " );

                //mArrayList.add(hashMap);
            }

            /*
            ListAdapter adapter = new SimpleAdapter(
                    MainActivity.this, mArrayList, R.layout.item_list,
                    new String[]{TAG_NAME, TAG_TEL},
                    new int[]{R.id.textView_list_name, R.id.textView_list_tel}
            );*/

            // mListViewList.setAdapter(adapter);

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        off();
        finish();
    }

    class Modulation extends AsyncTask<Void, double[], Void> {

        // Our audio input buffer, and the index of the next item to go in.

        @Override
        synchronized protected Void doInBackground(Void... params) {

            Log.d("fatal", "하하");
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            blockSize2=256;
            final int sampleRate=44100;

            final int bufferSize = AudioRecord.getMinBufferSize(sampleRate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate,bufferSize,bufferSize/2);
            player = new AndroidAudioPlayer(dispatcher.getFormat(), bufferSize);
            double factor = centToFactor(0);
            PitchDetectionHandler pdh = new PitchDetectionHandler() {
                @Override
                public void handlePitch(final PitchDetectionResult result, AudioEvent e) {
                    final float pitchInHz = result.getPitch();
                    Log.d("fatal-pdh", "하하");

                    for(int i = 0; i<cents.length; i++)
                        if(not[i][0]<pitchInHz && pitchInHz<not[i][1])
                            modulate(cents[i]);
                }
            };
            AudioProcessor p = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate, bufferSize, pdh);




            rateTransposer = new RateTransposer(factor);
            dispatcher.addAudioProcessor(p);
            //dispatcher.addAudioProcessor(new MultichannelToMono(1, true));
            dispatcher.addAudioProcessor(rateTransposer);
            dispatcher.addAudioProcessor(player);
            //(new Thread(dispatcher)).start();
            dispatcher.run();
            return null;
        }
    }

    public void modulate(int cents) {
        double factor = centToFactor(cents*2);
        rateTransposer.setFactor(factor);
        //rateTransposer = new RateTransposer(factor);
        // dispatcher.addAudioProcessor(rateTransposer);
        //dispatcher.run();
    }
    public static double centToFactor(double cents){
        return 1 / Math.pow(Math.E,cents*Math.log(2)/1200/Math.log(Math.E));
    }
    public void sendResult(String message) {
        // Intent intent = new Intent("com.example.tarsosaudioproject.RecordingMfccService.REQUEST_PROCESSED");

        //if(message != null)
        //     intent.putExtra("UINotification", message);
//
        //broadcaster.sendBroadcast(intent);
    }
    public static int calculate(int sampleRate, short [] audioData){

        int numSamples = audioData.length;
        int numCrossing = 0;
        for (int p = 0; p < numSamples-1; p++)
        {
            if ((audioData[p] > 0 && audioData[p + 1] <= 0) ||
                    (audioData[p] < 0 && audioData[p + 1] >= 0))
            {
                numCrossing++;
            }
        }

        float numSecondsRecorded = (float)numSamples/(float)sampleRate;
        float numCycles = numCrossing/2;
        float frequency = numCycles/numSecondsRecorded;

        return (int)frequency;
    }

}