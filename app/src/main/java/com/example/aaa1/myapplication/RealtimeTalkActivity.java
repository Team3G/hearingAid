package com.example.aaa1.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.icu.text.AlphabeticIndex;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.HashMap;


//FFT(Fast Fourier Transform) DFT 알고리즘 : 데이터를 시간 기준(time base)에서 주파수 기준(frequency base)으로 바꾸는데 사용.
public class RealtimeTalkActivity extends AppCompatActivity {

    ImageView backBtn;
    int blockSize2 = 256;

    LinearLayout warning;

    private AudioReader  audioReader;
    private int sampleRate = 8000;
    private int inputBlockSize = 256;
    private int db = 0;
    Button btn1, btn2;
    TextView txtView;
    String TAG = "hello";
    String mJsonString;

    private static final String TAG_JSON="testapi";
    private static final String TAG_LF_DECIBEL = "lf_decibel";
    private static final String TAG_LF_HERTZ ="lf_hertz";
    private static final String TAG_RT_DECIBEL = "rt_decibel";
    private static final String TAG_RT_HERTZ ="rt_hertz";


    boolean isPlaying=true;
    String mFilePath= Environment.getExternalStorageDirectory().getAbsolutePath() +"/record.pcm";;

    private RealDoubleFFT transformer;
    int blockSize = 256;
    Button startStopButton;
    boolean started = false;

    // RecordAudio는 여기에서 정의되는 내부 클래스로서 AsyncTask를 확장한다.
    RecordAudio recordTask=new RecordAudio();

    // Bitmap 이미지를 표시하기 위해 ImageView를 사용한다. 이 이미지는 현재 오디오 스트림에서 주파수들의 레벨을 나타낸다.
    // 이 레벨들을 그리려면 Bitmap에서 구성한 Canvas 객체와 Paint객체가 필요하다.
    Bitmap bitmap;
    Canvas canvas;
    Paint paint;

    String user_name="admin";
    AppCompatActivity this_a=this;

    int[][] not = new int[MainActivity.cnt][2];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_talk);

        //GetData task = new GetData();
        //task.execute(user_name);

        for(int i = 0; i<not.length; i++) {
            not[i][0] = MainActivity.codes[MainActivity.result_final[i]][0];
            not[i][1] = MainActivity.codes[MainActivity.result_final[i]][1];
            Log.d("하희", not[i][0]+"/"+not[i][1]);


        }

        Log.d("실행",ModulateActivity.started2+"");
        //변조 스레드 실행중인 경우 변조 종료
        if(false){


            if(ModulateActivity.modulationTask!=null){
                ModulateActivity.modulationTask.cancel(true);
                ModulateActivity.modulationTask=null;
            }
            if(ModulateActivity.mAudioTrack!=null){
                ModulateActivity.mAudioTrack.stop();
                ModulateActivity.mAudioTrack.release();
                ModulateActivity.mAudioTrack = null;
            }

        }

        setTitle("메인페이지");
        Log.d("안녕안녕안녕안녕","시작");
        warning=(LinearLayout) findViewById(R.id.warning) ;

        txtView = (TextView)findViewById(R.id.txtView);
        audioReader = new AudioReader();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        startStopButton = (Button) findViewById(R.id.StartStopButton);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnClick();
            }
        });
        // RealDoubleFFT 클래스 컨스트럭터는 한번에 처리할 샘플들의 수를 받는다. 그리고 출력될 주파수 범위들의 수를
        // 나타낸다.
        transformer = new RealDoubleFFT(blockSize);

        // ImageView 및 관련 객체 설정 부분
        bitmap = Bitmap.createBitmap(256, 100, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint = new Paint();
//        paint.setColor(Color.GREEN);

        backBtn = (ImageView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                if(recordTask!=null){
                    recordTask.audioInput.release();
                    recordTask.cancel(true);
                    started = false;
                }
                finish();
            }
        });
    }

    // 이 액티비티의 작업들은 대부분 RecordAudio라는 클래스에서 진행된다. 이 클래스는 AsyncTask를 확장한다.
    // AsyncTask를 사용하면 사용자 인터페이스를 멍하니 있게 하는 메소드들을 별도의 스레드로 실행한다.
    // doInBackground 메소드에 둘 수 있는 것이면 뭐든지 이런 식으로 실행할 수 있다.
    class RecordAudio extends AsyncTask<Void, double[], Void> {

        AudioRecord audioInput;
        // Our audio input buffer, and the index of the next item to go in.
        private short[][] inputBuffer = new short[2][inputBlockSize];
        private int inputBufferWhich = 0;
        private int inputBufferIndex = 0;
        private boolean running = false;
        private short[] audioData;
        private long audioSequence = 0;
        private long audioProcessed = 0;

        @Override
        synchronized protected Void doInBackground(Void... params) {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            int bufferSize = AudioRecord.getMinBufferSize(8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            audioInput= new AudioRecord(MediaRecorder.AudioSource.MIC,8000,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    MediaRecorder.AudioEncoder.AMR_NB, bufferSize);

            short[] buffer_;
            int index, readSize;

            ;
            // short로 이뤄진 배열인 buffer는 원시 PCM 샘플을 AudioRecord 객체에서 받는다.
            // double로 이뤄진 배열인 toTransform은 같은 데이터를 담지만 double 타입인데, FFT
            // 클래스에서는 double타입이 필요해서이다.
            short[] buffer = new short[blockSize]; //blockSize = 256
            double[] toTransform = new double[blockSize]; //blockSize = 256
            //audioRecord.startRecording();
            audioInput.startRecording();

            while (started) {
                int bufferReadResult = audioInput.read(buffer, 0, blockSize); //blockSize = 256
                Log.i("bufferReadResult", Integer.toString(bufferReadResult));

                // AudioRecord 객체에서 데이터를 읽은 다음에는 short 타입의 변수들을 double 타입으로
                // 바꾸는 루프를 처리한다.
                // 직접 타입 변환(casting)으로 이 작업을 처리할 수 없다. 값들이 전체 범위가 아니라 -1.0에서
                // 1.0 사이라서 그렇다
                // short를 32,767(Short.MAX_VALUE) 으로 나누면 double로 타입이 바뀌는데,
                // 이 값이 short의 최대값이기 때문이다.
                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    toTransform[i] = (double) buffer[i] / Short.MAX_VALUE; // 부호 있는 16비트
                }


                // 이제 double값들의 배열을 FFT 객체로 넘겨준다. FFT 객체는 이 배열을 재사용하여 출력 값을
                // 담는다. 포함된 데이터는 시간 도메인이 아니라
                // 주파수 도메인에 존재한다. 이 말은 배열의 첫 번째 요소가 시간상으로 첫 번째 샘플이 아니라는 얘기다.
                // 배열의 첫 번째 요소는 첫 번째 주파수 집합의 레벨을 나타낸다.

                // 256가지 값(범위)을 사용하고 있고 샘플 비율이 8,000 이므로 배열의 각 요소가 대략
                // 15.625Hz를 담당하게 된다. 15.625라는 숫자는 샘플 비율을 반으로 나누고(캡쳐할 수 있는
                // 최대 주파수는 샘플 비율의 반이다. <- 누가 그랬는데...), 다시 256으로 나누어 나온 것이다.
                // 따라서 배열의 첫 번째 요소로 나타난 데이터는 영(0)과 15.625Hz 사이에
                // 해당하는 오디오 레벨을 의미한다.

                transformer.ft(toTransform);
                // publishProgress를 호출하면 onProgressUpdate가 호출된다.
                publishProgress(toTransform);

                readSize = inputBlockSize;
                int space = inputBlockSize - inputBufferIndex;
                if (readSize > space)
                    readSize = space;
                buffer_ = inputBuffer[inputBufferWhich];
                index = inputBufferIndex;
                synchronized (buffer_)
                {
                    int nread = audioInput.read(buffer_, index, readSize);
                    boolean done = false;
                    int end = inputBufferIndex + nread;
                    if (end >= inputBlockSize)
                    {
                        inputBufferWhich = (inputBufferWhich + 1) % 2;
                        inputBufferIndex = 0;
                        done = true;
                    }
                    else
                        inputBufferIndex = end;

                    if (done){readDone(buffer_); }
                }
            }
            audioInput.stop();

            return null;
        }
        private void readDone(short[] buffer)
        {
            synchronized (this)
            {
                audioData = buffer;
                ++audioSequence;

                short[] buffer2 = null;
                if (audioData != null && audioSequence > audioProcessed)
                {
                    audioProcessed = audioSequence;
                    buffer2 = audioData;
                }

                if (buffer2 != null)
                {
                    final int len = buffer2.length;
                    Log.d("안녕123",calculatePowerDb(buffer2, 0, len)+"");
                    buffer2.notify();
                }
            }
        }
        public int calculatePowerDb(short[] sdata, int off, int samples)
        {
            double sum = 0;
            double sqsum = 0;
            for (int i = 0; i < samples; i++)
            {
                final long v = sdata[off + i];
                sum += v;
                sqsum += v * v;
            }

            double power = (sqsum - sum * sum / samples) / samples;

            double result = Math.log10(power) * 10f + 0.6F;
            db=(int)result;
            return (int)result;
        }

        // onProgressUpdate는 우리 엑티비티의 메인 스레드로 실행된다. 따라서 아무런 문제를 일으키지 않고 사용자
        // 인터페이스와 상호작용할 수 있다.
        // 이번 구현에서는 onProgressUpdate가 FFT 객체를 통해 실행된 다음 데이터를 넘겨준다. 이 메소드는 최대
        // 100픽셀의 높이로 일련의 세로선으로
        // 화면에 데이터를 그린다. 각 세로선은 배열의 요소 하나씩을 나타내므로 범위는 15.625Hz다. 첫 번째 행은 범위가 0에서
        // 15.625Hz인 주파수를 나타내고,
        // 마지막 행은 3,984.375에서 4,000Hz인 주파수를 나타낸다.
        @Override
        protected void onProgressUpdate(double[]... toTransform) {
            double limit=1000;/*사용자 가청 주파수 범위*/
            int num_limit=0;/*안들리는 개수*/
            int num_total=0;/*전체 개수*/
            double Hz=0;

            for (int i = 0; i < toTransform[0].length; i++) {
                if((limit/15.625)<i)num_limit+=Math.abs(toTransform[0][i]*10);
                num_total+=Math.abs(toTransform[0][i]*10);
                Hz=(15.625*(i+1)*Math.abs(toTransform[0][i]*10));//가중치 평균을 위한
                if(txtView!=null)txtView.setText(db+"");
            }
            Hz/=num_total;//가중치 평균
            Log.d("안녕12345",Hz+"");

            for(int i=0;i<not.length;i++){

                if(not[i][0]<= Hz&& not[i][1]>=Hz){

                    Log.d("색깔",num_limit+"");

                    warning.setBackgroundColor(Color.RED);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            warning.setBackgroundColor(Color.GREEN);
                        }
                    }, 2000);

                        /*
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            warning.setBackgroundColor(Color.BLUE);
                        }
                    }, 1000);*/

                }
                else {
                    warning.setBackgroundColor(Color.GREEN);

                    /*
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                        }
                    }, 500);*/
                }
            }
            /*
            //num_limit>(num_total/2) 전체 ㄱ수 중 절반 이상이 안들리는 주파수일 경우
            if(warning!=null &&(num_limit>(num_total*2)/3))warning.setBackgroundColor(Color.RED);

            else warning.setBackgroundColor(Color.GREEN); */
            Log.d("정보",num_limit+"  "+num_total);

        }
    }

    public void btnClick() {
        Log.d("안녕안녕",started+"");
        // 여기서 하는 거 멈추려고
        ModulateActivity.off();
        if (started) {//멈춰야하는
            recordTask.cancel(true);
            started = false;
            startStopButton.setText("Start");
            warning.setBackgroundColor(Color.GREEN);
        } else {
            // 여기서 하는 거 시작하려고
            started = true;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }
    }

    @Override
    public void onBackPressed() {//뒤로가기 클릭 시 현재 스레드종료
        super.onBackPressed();
        Log.d("실행","종료");
        if(recordTask!=null){
            recordTask.audioInput.release();
            recordTask.cancel(true);
            started = false;
        }
    }

    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(RealtimeTalkActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "response - " + result);

            if (result == null){

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

                Log.d("hello", lf_decibel + ", " + lf_hertz + ", " +rt_decibel + ", " +rt_hertz + ", " );
                ;
            }


        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
    }

}


