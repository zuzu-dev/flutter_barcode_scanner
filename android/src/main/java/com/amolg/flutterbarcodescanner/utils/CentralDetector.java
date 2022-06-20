package com.amolg.flutterbarcodescanner.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.ByteArrayOutputStream;

// imports for saving the file
import java.io.File;
import java.io.FileOutputStream;
import android.content.Context;

public class CentralDetector extends Detector<Barcode> {
    private Detector<Barcode> mDelegate;
    private int mBoxWidth, mBoxHeight;
    private Context mContext;

    public CentralDetector(Detector<Barcode> delegate, int boxWidth, int boxHeight, Context context) {
        mDelegate = delegate;
        mBoxWidth = boxWidth;
        mBoxHeight = boxHeight;
        mContext = context;
    }

    public static byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
        // Rotate the Y luma
        int i = 0;
        for (int x = 0; x < imageWidth; x++) {
            for (int y = imageHeight - 1; y >= 0; y--) {
                yuv[i] = data[y * imageWidth + x];
                i++;
            }
        }
        // Rotate the U and V color components
        i = imageWidth * imageHeight * 3 / 2 - 1;
        for (int x = imageWidth - 1; x > 0; x = x - 2) {
            for (int y = 0; y < imageHeight / 2; y++) {
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
                i--;
                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
                        + (x - 1)];
                i--;
            }
        }
        return yuv;
    }

    public int writeFileOnInternalStorage(String barcode, Frame frame){
        try {
            File file = new File(mContext.getFilesDir() + "/barcodes/");
            if (!file.exists()) {
                file.mkdirs();
            }
            File jpeg = new File(file.getAbsolutePath(), barcode + ".jpeg");
            FileOutputStream fos = new FileOutputStream(jpeg.getAbsolutePath());
            int width = frame.getMetadata().getWidth();
            int height = frame.getMetadata().getHeight();
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
            yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, fos);
            System.out.println(jpeg.getAbsolutePath());
            System.out.println(jpeg.exists());
            fos.close();
            return 0;
        } catch (Exception e) {
            System.out.println(e.toString());
            return 1;
        }
    }

    public SparseArray<Barcode> detect(Frame frame) {
        SparseArray<Barcode> result = mDelegate.detect(frame);
        if(result.size() > 0) {
            System.out.println("----------------------- " + result.valueAt(0).rawValue);
            String barcode = result.valueAt(0).rawValue;
            int success = this.writeFileOnInternalStorage(barcode, frame);
            if(success == 0) {
                System.out.println("______________________SUCCESS");
            } else {
                System.out.println("______________________FAIL");
            }
        }

        return result;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}
