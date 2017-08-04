package com.seek.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.seek.biscuit.Biscuit;
import com.seek.biscuit.CompressException;
import com.seek.biscuit.CompressListener;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import me.iwf.photopicker.PhotoPicker;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    TextView mTextView;
    StringBuilder info = null;
    CompressListener mCompressListener;
//    Biscuit mBiscuit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mTextView = (TextView) findViewById(R.id.show_info);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        Log.e(">>>>>", dm.toString());
        info = new StringBuilder();
        mCompressListener = new CompressListener() {
            @Override
            public void onSuccess(String compressedPath) {
                info.append("compressed success！ the image data has been saved at ");
                info.append(compressedPath);
                info.append("\n\n");
                mTextView.setText(info.toString());
            }

            @Override
            public void onError(CompressException e) {
                Log.e(">>>>>", "message : " + e.getMessage() + " original Path : " + e.originalPath);
            }
        };
    }

    public void getPhoto(View view) {
        PhotoPicker.builder()
                .setPhotoCount(9)
                .setShowCamera(true)
                .setShowGif(true)
                .setPreviewEnabled(false)
                .start(this, PhotoPicker.REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
            if (data != null) {
                ArrayList<String> photos =
                        data.getStringArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
                Glide.clear(mImageView);
                Glide.with(this).load(photos.get(0)).into(mImageView);
                //默认异步使用
//                if (mBiscuit==null) {
//                    mBiscuit =
//                Biscuit.with(this)
//                        .path(photos) //可以传入一张图片路径，也可以传入一个图片路径列表
//                        .loggingEnabled(true)//是否输出log 默认输出
////                        .quality(50)//质量压缩值（0...100）默认已经非常接近微信，所以没特殊需求可以不用自定义
////                        .originalName(true) //使用原图名字来命名压缩后的图片，默认不使用原图名字,随机图片名字
//                        .listener(mCompressListener)//压缩监听
//                        .targetDir(FileUtils.getImageDir())//自定义压缩保存路径
////                        .executor(executor) //自定义实现执行，注意：必须在子线程中执行 默认使用AsyncTask线程池执行
////                        .ignoreAlpha(true)//忽略alpha通道，对图片没有透明度要求可以这么做，默认不忽略。
////                        .compressType(Biscuit.SAMPLE)//采用采样率压缩方式，默认是使用缩放压缩方式，也就是和微信的一样。
//                        .ignoreLessThan(100)//忽略小于100kb的图片不压缩，返回原图路径
//                        .build().asyncCompress();
//                }else {
//                    mBiscuit.addPaths(photos);//传入压缩列表
//                    mBiscuit.asyncCompress();//开始压缩
//                }

                //使用rxjava自定义异步
//                Biscuit.with(this)
//                        .path(photos) //可以传入一张图片路径，也可以传入一个图片路径列表
//                        .loggingEnabled(true)//是否输出log 默认输出
//                        .listener(mCompressListener)//压缩监听
//                        .targetDir(FileUtils.getImageDir())//自定义压缩保存路径
//                        .executor(new Executor() {
//                            @Override
//                            public void execute(Runnable compressor) {
//                                Observable.just(compressor).doOnNext(new Consumer<Runnable>() {
//                                    @Override
//                                    public void accept(Runnable runnable) throws Exception {
//                                        runnable.run();
//                                    }
//                                }).subscribeOn(Schedulers.io()).subscribe();
//                            }
//                        }) //自定义实现执行，注意：必须在子线程中执行 默认使用AsyncTask线程池执行
//                        .ignoreLessThan(100)//忽略小于100kb的图片不压缩，返回原图路径
//                        .build().asyncCompress();

                //同步压缩,使用rxjava处理
                Observable.just(photos).map(new Function<ArrayList<String>, ArrayList<String>>() {
                    @Override
                    public ArrayList<String> apply(@NonNull ArrayList<String> strings) throws Exception {
                        return Biscuit.with(MainActivity.this)
                                .path(strings)
                                .targetDir(FileUtils.getImageDir())
                                .ignoreLessThan(100)
                                .build().syncCompress();//同步方法
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> strings) throws Exception {
                        for (String compressedPath : strings) {
                            info.append("compressed success！ the image data has been saved at ");
                            info.append(compressedPath);
                            info.append("\n\n");
                        }
                        mTextView.setText(info.toString());
                    }
                });
            }
        }
    }
}
