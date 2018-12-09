package com.example.aaa1.myapplication;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequest;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class ReadingActivity extends AppCompatActivity  {
    private static final String CLOUD_VISION_API_KEY = "hello";
    private static final String ANDROID_CERT_HEADER = "X-Android-Cert";
    private static final String ANDROID_PACKAGE_HEADER = "X-Android-Package";
    private static final int GALLERY_PERMISSIONS_REQUEST = 0;
    private static final int GALLERY_IMAGE_REQUEST = 1;
    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;
    Uri photoUrl;
    Bitmap userImageBIitmap;

    ImageView backBtn;
    Button camera_load, file_load;
    TextView text;
    public static final String FILE_NAME = "temp.jpg";

    ListView lv1;//
    BufferedReader br;
    LinearLayout fileListLayout,defaultLayout;
    ArrayList<String> name = new ArrayList<>();
    ArrayAdapter<String> adapter;
    File file,sdcard = Environment.getExternalStorageDirectory();


    String TAG="HELLO";

    private TextToSpeech tts;


    int frequency = 8000;//유저 데이터
    int hearing=4000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        lv1 = (ListView) findViewById(R.id.listview);
        fileListLayout = (LinearLayout) findViewById(R.id.fileList);
        defaultLayout = (LinearLayout) findViewById(R.id.defaultLayout);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, name);
        lv1.setAdapter(adapter);
        file = new File(sdcard.getPath()+"/KakaoTalkDownload");
        listViewAction();

        text=(TextView)findViewById(R.id.text);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });
        backBtn = (ImageView)findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        file_load=(Button)findViewById(R.id.file_load);
        camera_load=(Button)findViewById(R.id.camera_load);
        camera_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReadingActivity.this);
                builder
                        .setMessage("갤러리나 카메라를 선택해주세요")
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
            }
        });
        file_load=(Button)findViewById(R.id.file_load);
        file_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileList();
            }
        });

    }
    void showFileList() {//파일 목록 출력
        file = new File(sdcard.getPath()+"/KakaoTalkDownload");
        defaultLayout.setVisibility(View.GONE);
        fileListLayout.setVisibility(View.VISIBLE);

        FilenameFilter fileFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith("txt");
            }
        };
        name.clear();
        File[] files = file.listFiles(fileFilter);
        if(files!=null)for (File f : files) {
            name.add(f.getName());
        }
        Comparator<String> compare = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        };
        Collections.sort(name,compare);
        adapter.notifyDataSetChanged();

    }
    void listViewAction() {//초기화.

        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = sdcard .getPath()+"/KakaoTalkDownload";
                fileListLayout.setVisibility(View.GONE);
                defaultLayout.setVisibility(View.VISIBLE);

                try {
                    br = new BufferedReader(new FileReader(path +"/"+name.get(position)));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                readFile();
            }
        });
    }
    String readFile() {
        String readStr="";
        String str="";
        try {
            while ((str = br.readLine()) != null) {
                readStr += str+"\n";
            }
            br.close();
            text.setText(readStr);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return readStr;
    }
    public File getCameraFile() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return new File(dir, FILE_NAME);
    }
    public void startGalleryChooser() {
        if (PermissionUtils.requestPermission(this, GALLERY_PERMISSIONS_REQUEST, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                    GALLERY_IMAGE_REQUEST);

        }
    }
    public void startCamera() {
        if (PermissionUtils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            photoUrl = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUrl);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("안녕123",data+" ");
        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());

        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK&& photoUrl!= null) {
            // Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUrl);
        }

    }
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {

            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }
    public Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }
    public void uploadImage(Uri uri) {
        Log.d("안녕안녕",uri+"");
        if (uri != null) {
            try{

                Bitmap bitmap1 =
                        scaleBitmapDown(
                                MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                                1200);
                userImageBIitmap = bitmap1;

            }catch(Exception e){
                e.printStackTrace();
            }

        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this,"null", Toast.LENGTH_LONG).show();
        }
        try {
            text.setText("분석중입니다");
            callCloudVision(userImageBIitmap);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void callCloudVision(final Bitmap bitmap) throws IOException {
        // Switch text to loading
        // Do the real work in an async task, because we need to use the network anyway
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {
                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    com.google.api.client.json.JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer =
                            new VisionRequestInitializer(CLOUD_VISION_API_KEY) {
                                /**
                                 * We override this so we can inject important identifying fields into the HTTP
                                 * headers. This enables use of a restricted cloud platform API key.
                                 */
                                @Override
                                protected void initializeVisionRequest(VisionRequest<?> visionRequest)
                                        throws IOException {
                                    super.initializeVisionRequest(visionRequest);

                                    String packageName = getPackageName();
                                    visionRequest.getRequestHeaders().set(ANDROID_PACKAGE_HEADER, packageName);

                                    String sig = PackageManagerUtils.getSignature(getPackageManager(), packageName);

                                    visionRequest.getRequestHeaders().set(ANDROID_CERT_HEADER, sig);
                                }
                            };

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest =
                            new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(new ArrayList<AnnotateImageRequest>() {{
                        AnnotateImageRequest annotateImageRequest = new AnnotateImageRequest();

                        // Add the image
                        Image base64EncodedImage = new Image();
                        // Convert the bitmap to a JPEG
                        // Just in case it's a format that Android understands but Cloud Vision
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
                        byte[] imageBytes = byteArrayOutputStream.toByteArray();

                        // Base64 encode the JPEG
                        base64EncodedImage.encodeContent(imageBytes);
                        annotateImageRequest.setImage(base64EncodedImage);

                        // add the features we want
                        annotateImageRequest.setFeatures(new ArrayList<Feature>() {{
                            Feature labelDetection = new Feature();
                            labelDetection.setType("TEXT_DETECTION");
                            labelDetection.setMaxResults(10);
                            add(labelDetection);
                        }});

                        // Add the list of one thing to the request
                        add(annotateImageRequest);
                    }});

                    Vision.Images.Annotate annotateRequest =
                            vision.images().annotate(batchAnnotateImagesRequest);
                    // Due to a bug: requests to Vision API containing large images fail when GZipped.
                    annotateRequest.setDisableGZipContent(true);
                    Log.d(TAG, "created Cloud Vision request object, sending request");

                    //  Log.d("Hello", annotateRequest.getFields());
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return convertResponseToString(response);

                } catch (GoogleJsonResponseException e) {
                    Log.d(TAG, "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d(TAG, "failed to make API request because of other IOException " +
                            e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }



            protected void onPostExecute(String result) {


                text.setText(result);
                // mn=Integer.parseInt(money);

                tts.setPitch(((4*frequency+hearing)/5)/8000f);         // 음성 톤을 올려준다.
                tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
                // editText에 있는 문장을 읽는다.
                tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);


                //  if(result!=null)  Log.d("안녕",result.substring(0,result.indexOf('\n'))+"    "+result.substring(result.lastIndexOf('\n'),result.length()-1-result.lastIndexOf('\n')));
            }
        }.execute();
    }
    private String convertResponseToString(BatchAnnotateImagesResponse response) {
        String message = "I found these things:\n\n";
        List<AnnotateImageRequest> requests = new ArrayList<>();
        List<EntityAnnotation> entityAnnotations = response.getResponses().get(0).getTextAnnotations();
        if (entityAnnotations != null) {
            EntityAnnotation e = entityAnnotations.get(0);
            int i = 1;
            float[] eyeRectVertex = new float[4];
            Log.d("안녕",e+"");
            String s=e.getDescription();
            //int t=s.charAt('\n');
            return s;
        }
        return "없습니다";
    }
}