package com.example.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private final int GET_PERMISSION_REQUEST = 100; //权限申请自定义码
    private Button mBtnMainVideo;
    private ImageView mImagePhotoMain;
    private VideoView mVideoViewMain;
    private ImageView mIvThumbMain;
    private ImageView mIvPlayMain;
    private FrameLayout mFl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();


    }

    private void initVideo(String mVideoPath) {
        mVideoViewMain.setVideoPath(mVideoPath);
        mVideoViewMain.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                ViewGroup.LayoutParams lp = mVideoViewMain.getLayoutParams();
                int videoWidth = mp.getVideoWidth();
                int videoHeight = mp.getVideoHeight();
                float videoProportion = (float) videoWidth / (float) videoHeight;
                int screenWidth = mFl.getWidth();
                int screenHeight = mFl.getHeight();
                float screenProportion = (float) screenWidth / (float) screenHeight;
                if (videoProportion > screenProportion) {
                    lp.width = screenWidth;
                    lp.height = (int) ((float) screenWidth / videoProportion);
                } else {
                    lp.width = (int) (videoProportion * (float) screenHeight);
                    lp.height = screenHeight;
                }
                mVideoViewMain.setLayoutParams(lp);

                Log.e("videoView",
                        "videoWidth:" + videoWidth + ", videoHeight:" + videoHeight);
            }
        });
        mVideoViewMain.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                mIvPlayMain.setVisibility(View.VISIBLE);
                mIvThumbMain.setVisibility(View.VISIBLE);

            }
        });
        videoStart();
    }

    public void videoStart() {
        mVideoViewMain.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoDestroy();
    }

    public void videoPause() {
        if (mVideoViewMain != null && mVideoViewMain.isPlaying()) {
            mVideoViewMain.pause();
        }
    }

    public void videoDestroy() {
        if (mVideoViewMain != null) {
            mVideoViewMain.stopPlayback();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_main_video:
                getPermissions();
                break;

        }
    }

    /**
     * 获取权限
     */
    private void getPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager
                    .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager
                            .PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager
                            .PERMISSION_GRANTED) {
                startActivityForResult(new Intent(MainActivity.this, Main2Activity.class), 100);
            } else {
                //不具有获取权限，需要进行权限申请
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.CAMERA}, GET_PERMISSION_REQUEST);
            }
        } else {
            startActivityForResult(new Intent(MainActivity.this, Main2Activity.class), 100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 101) {
            Log.i("CJT", "picture");
            setShow(false);
            String path = data.getStringExtra("path");
            mImagePhotoMain.setImageBitmap(BitmapFactory.decodeFile(path));
        }
        if (resultCode == 102) {

            setShow(true);
            String path = data.getStringExtra("path");
            Log.i("CJT", "video="+path);
            initVideo(path);
        }
        if (resultCode == 103) {
            Toast.makeText(this, "请检查相机权限~", Toast.LENGTH_SHORT).show();
        }
    }

    private void setShow(boolean show) {
        mFl.setVisibility(show ? View.VISIBLE : View.GONE);
        mImagePhotoMain.setVisibility(show ? View.GONE : View.VISIBLE);

    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == GET_PERMISSION_REQUEST) {
            int size = 0;
            if (grantResults.length >= 1) {
                int writeResult = grantResults[0];
                //读写内存权限
                boolean writeGranted = writeResult == PackageManager.PERMISSION_GRANTED;//读写内存权限
                if (!writeGranted) {
                    size++;
                }
                //录音权限
                int recordPermissionResult = grantResults[1];
                boolean recordPermissionGranted = recordPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!recordPermissionGranted) {
                    size++;
                }
                //相机权限
                int cameraPermissionResult = grantResults[2];
                boolean cameraPermissionGranted = cameraPermissionResult == PackageManager.PERMISSION_GRANTED;
                if (!cameraPermissionGranted) {
                    size++;
                }
                if (size == 0) {
                    startActivityForResult(new Intent(MainActivity.this, Main2Activity.class), 100);
                } else {
                    Toast.makeText(this, "请到设置-权限管理中开启", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void initView() {
        mBtnMainVideo = (Button) findViewById(R.id.btn_main_video);
        mImagePhotoMain = (ImageView) findViewById(R.id.image_photo_main);
        mVideoViewMain = (VideoView) findViewById(R.id.videoView_main);
        mIvThumbMain = (ImageView) findViewById(R.id.iv_thumb_main);
        mIvPlayMain = (ImageView) findViewById(R.id.iv_play_main);
        mFl = (FrameLayout) findViewById(R.id.fl);

        mBtnMainVideo.setOnClickListener(this);
    }
}
