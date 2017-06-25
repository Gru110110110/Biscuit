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
import com.seek.biscuit.CompressListener;

import java.util.ArrayList;

import me.iwf.photopicker.PhotoPicker;

public class MainActivity extends AppCompatActivity {

    ImageView mImageView;
    TextView mTextView;
    StringBuilder info = null;
    CompressListener mCompressListener;

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
                info.append("\n");
                mTextView.setText(info.toString());
            }

            @Override
            public void onError(Exception e) {
                Log.e(">>>>>", e.getMessage());
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
                Glide.with(this).load(photos.get(0)).into(mImageView);
                Biscuit.with(this)
                        .path(photos)
                        .originalName(false) //使用原图名字
                        .listener(mCompressListener)//压缩监听
                        .targetDir(FileUtils.getImageDir())//自定义保存路径
                        .build();
            }
        }
    }
}
