package com.sscommu.pokeumcho;


import android.graphics.Bitmap;
import android.graphics.Matrix;

import java.io.OutputStream;

public class UploadImage {

    private Bitmap mBitmap;
    private String mMimeType;
    private String mName;
    private Long mSize;

    /** Constructor */
    public UploadImage(Bitmap bitmap,
                       String mimeType,
                       String name,
                       Long size) {

        mBitmap = bitmap;
        mMimeType = mimeType;
        mName = name;
        mSize = size;
    }

    /** Getter & Setter */
    public Bitmap getBitmap() { return mBitmap; }
    public void setBitmap(Bitmap bitmap) { mBitmap = bitmap; }

    public String getMimeType() { return mMimeType; }
    public void setMimeType(String mimeType) { mMimeType = mimeType; }

    public String getName() { return mName; }
    public void setName(String name) { mName = name; }

    public Long getSize() { return mSize; }
    public void setSize(Long size) { mSize = size; }


    /* 시계 방향으로 90도 회전한다. */
    public void rotateBitmap() {
        Matrix matrix = new Matrix();
        matrix.preRotate(90);
        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0,
                mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
    }

    public boolean compress (Bitmap.CompressFormat format,
                             int quality,
                             OutputStream stream) {

        return mBitmap.compress(format, quality, stream);
    }

    /* 파일 확장자명을 변경한다. */
    public void changeFileExtension(String extension) {

        StringBuilder sb = new StringBuilder();
        String[] parts = mName.split("\\.");
        for (int i = 0; i < (parts.length - 1); i++) {
            sb.append(parts[i]).append('.');
        }
        sb.append(extension);

        mName = sb.toString();
    }
}
