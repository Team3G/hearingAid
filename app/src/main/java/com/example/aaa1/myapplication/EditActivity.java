package com.example.aaa1.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class EditActivity extends AppCompatActivity {

    TextView confirmPwdText, userNameText, userAgeText;
    EditText idEdit,passwordEdit,passwordEditCopied,nameEdit,ageEdit;
    Button EditBtn;
    ImageView backBtn;



    //카메라때문에 추가한 것
    private static final int PICK_FROM_CAMERA =0;
    private static final int PICK_FROM_ALBUM =1;
    private static final int CROP_FROM_iMAGE =2;

    private Uri mImageCaptureUri;
    //    private ImageView iv_UserPhoto;
    private int id_view;
    private String absoultePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        userNameText = (TextView)findViewById(R.id.userNameText);
        userAgeText = (TextView)findViewById(R.id.userAgeText);


        confirmPwdText = (TextView)findViewById(R.id.confirmPwdText);
        idEdit = (EditText)findViewById(R.id.idEdit);
        passwordEdit = (EditText)findViewById(R.id.passwordEdit);
        passwordEditCopied = (EditText)findViewById(R.id.passwordEditCopied);
        nameEdit = (EditText)findViewById(R.id.nameEdit);
        ageEdit = (EditText)findViewById(R.id.ageEdit);

        EditBtn = (Button)findViewById(R.id.EditBtn);

        backBtn = (ImageView)findViewById(R.id.backBtn);



        passwordEditCopied.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String password = passwordEdit.getText().toString();
                String confirm = passwordEditCopied.getText().toString();

                if(password.equals(confirm)){
                    passwordEdit.setBackgroundColor(Color.GREEN);
                    passwordEditCopied.setBackgroundColor(Color.GREEN);
                    confirmPwdText.setText("");
                    EditBtn.setEnabled(true);
                }
                else {
                    passwordEdit.setBackgroundColor(Color.RED);
                    passwordEditCopied.setBackgroundColor(Color.RED);
                    confirmPwdText.setText("비밀번호가 일치하지 않습니다.");
                    EditBtn.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        // 수정버튼 누르면 수정 되야 한다.
        EditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeUserInfo();
            }
        });




        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        init();

    }


    // 시작할 때 정보 초기화 시켜주기
    public void init()
    {
        String name = "", age="";

        // db에서 불러 와야 함
        name = "박진숙";
        age = "68";

        userNameText.setText(name);
        userAgeText.setText(age+"세");

    }
    public void doTakePhotoAction()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String url = "tmp_"+String.valueOf(System.currentTimeMillis())+".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),url));

        intent.putExtra(MediaStore.EXTRA_OUTPUT,mImageCaptureUri);
        startActivityForResult(intent,PICK_FROM_CAMERA);
    }

    public void doTakeAlbumAction()
    {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent,PICK_FROM_ALBUM);
    }

    private  void storeCropImage(Bitmap bitmap, String filePath){
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SmartWheel";
        File directory_SmartWheel = new File(dirPath);

        if(!directory_SmartWheel.exists())
            directory_SmartWheel.mkdir();

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try{
            copyFile.createNewFile();
            out= new BufferedOutputStream(new FileOutputStream(copyFile));
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);

            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(copyFile)));

            out.flush();
            out.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


    // 사용자 정보 수정하는 함수
    public void changeUserInfo()
    {

        // idEdit,passwordEdit,passwordEditCopied,nameEdit,ageEdit;
        // 보이는 정보 수정하기

        SettingActivity.userNameText.setText(nameEdit.getText().toString());
        SettingActivity.userAgeText.setText(ageEdit.getText().toString()+"세");
        Toast.makeText(getApplicationContext(),"수정완료!",Toast.LENGTH_SHORT).show();
        finish();
    }




}
