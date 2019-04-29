package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.cjt2325.cameralibrary.JCameraView;
import com.cjt2325.cameralibrary.listener.ClickListener;
import com.cjt2325.cameralibrary.listener.ErrorListener;
import com.cjt2325.cameralibrary.listener.JCameraListener;
import com.cjt2325.cameralibrary.util.FileUtil;
import com.iceteck.silicompressorr.SiliCompressor;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class Main2Activity extends AppCompatActivity {
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private JCameraView mJCameraView;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main2);
        initView();

        initCameraView();
    }

    private void initCameraView() {
        mJCameraView.setSaveVideoPath(Environment.getExternalStorageDirectory().getPath() + File.separator + "JCamera");
        //设置只能录像或只能拍照或两种都可以（默认两种都可以）
        mJCameraView.setFeatures(JCameraView.BUTTON_STATE_BOTH);

//设置视频质量
        mJCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);
        mJCameraView.setTip("JCameraView Tip");
        mJCameraView.setMediaQuality(JCameraView.MEDIA_QUALITY_MIDDLE);

        mJCameraView.setErrorLisenter(new ErrorListener() {
            @Override
            public void onError() {
                //打开Camera失败回调
                Intent intent = new Intent();
                setResult(103, intent);
                finish();
            }

            @Override
            public void AudioPermissionError() {
                //没有录取权限回调
                Toast.makeText(Main2Activity.this, "给点录音权限可以?", Toast.LENGTH_SHORT).show();
            }
        });
        mJCameraView.setJCameraLisenter(new JCameraListener() {
            @Override
            public void captureSuccess(Bitmap bitmap) {
                //获取图片bitmap
                //获取图片bitmap
//                Log.i("JCameraView", "bitmap = " + bitmap.getWidth());
                String path = FileUtil.saveBitmap("JCamera", bitmap);
                Intent intent = new Intent();
                intent.putExtra("path", path);
                setResult(101, intent);
                finish();
            }

            @Override
            public void recordSuccess(String url, Bitmap firstFrame) {
                //获取视频路径
                String path = FileUtil.saveBitmap("JCamera", firstFrame);
                Log.i("CJT", "url = " + url + ", Bitmap = " + path);
//                Intent intent = new Intent();
//                intent.putExtra("path", path);
//                setResult(101, intent);
                compressVideo(url);
            }

        });


        mJCameraView.setLeftClickListener(new ClickListener() {
            @Override
            public void onClick() {
                Main2Activity.this.finish();
            }
        });
        mJCameraView.setRightClickListener(new ClickListener() {
            @Override
            public void onClick() {
                Toast.makeText(Main2Activity.this, "Right", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //全屏显示
        if (Build.VERSION.SDK_INT >= 19) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(option);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mJCameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mJCameraView.onPause();
    }

    private void initView() {
        mContext = this;
        mJCameraView = (JCameraView) findViewById(R.id.jcameraview);
    }


    /**
     * 视频压缩
     */
    private void compressVideo(final String srcPath) {
        NormalProgressDialog
                .showLoading(this, "视频处理中", false);
      final   String destDirPath = VideoUtil.getTrimmedVideoDir(this, "small_video");
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                try {
                    int outWidth = 0;
                    int outHeight = 0;
                    //竖屏
                    outWidth = 480;
                    outHeight = 720;
                    String compressedFilePath = SiliCompressor.with(Main2Activity.this)
                            .compressVideo(srcPath, destDirPath, outWidth, outHeight, 900000);
                    emitter.onNext(compressedFilePath);
                } catch (Exception e) {
                    emitter.onError(e);
                }
                emitter.onComplete();
            }
        })
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposables.add(d);
                    }

                    @Override
                    public void onNext(String outputPath) {
                        //源路径: /storage/emulated/0/Android/data/com.kangoo.diaoyur/cache/small_video/trimmedVideo_20180514_163858.mp4
                        //压缩路径: /storage/emulated/0/Android/data/com.kangoo.diaoyur/cache/small_video/VIDEO_20180514_163859.mp4
                        Log.e("==", "compressVideo---onSuccess");
                        //获取视频第一帧图片
                        NormalProgressDialog.stopLoading();
                        Intent intent = new Intent();
                        intent.putExtra("path", outputPath);
                        setResult(102, intent);
                        finish();
//                        ExtractVideoInfoUtil mExtractVideoInfoUtil = new ExtractVideoInfoUtil(outputPath);
//                        Bitmap bitmap = mExtractVideoInfoUtil.extractFrame();
//                        String firstFrame = FileUtil.saveBitmap("small_video", bitmap);
//                        if (bitmap != null && !bitmap.isRecycled()) {
//                            bitmap.recycle();
//                            bitmap = null;
//                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e("===", "compressVideo---onError:" + e.toString());
                        Toast.makeText(Main2Activity.this, "视频压缩失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposables != null && !mDisposables.isDisposed()) {
            mDisposables.dispose();
            mDisposables.clear();
        }
    }
}
