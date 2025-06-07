//package com.example.demo;
//
//import android.content.Context;
//import android.net.Uri;
//import android.os.Handler;
//import android.os.Looper;
//import android.widget.TextView;
//
//import org.pytorch.IValue;
//import org.pytorch.Module;
//import org.pytorch.Tensor;
//import org.pytorch.torchvision.TensorImageUtils;
//
//import java.io.File;
//import java.io.IOException;
//
//public class LocalRecognizer {
//
//    public static void recognizeImage(Uri uri, Context context, TextView resultText) {
//        try {
//            Module module = Module.load(new File(context.getFilesDir(), "model.pth").getAbsolutePath());
//
//            float[] mean = {0.485f, 0.456f, 0.406f};
//            float[] std = {0.229f, 0.224f, 0.225f};
//
//            Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
//                    android.provider.MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri),
//                    mean, std
//            );
//
//            Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
//            float[] scores = outputTensor.getDataAsFloatArray();
//
//            String result = "本地识别结果: ";
//            for (float s : scores) {
//                result += String.format("%.2f ", s);
//            }
//
//            String finalResult = result;
//            new Handler(Looper.getMainLooper()).post(() -> resultText.setText(finalResult));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            new Handler(Looper.getMainLooper()).post(() -> resultText.setText("本地识别失败"));
//        }
//    }
//}