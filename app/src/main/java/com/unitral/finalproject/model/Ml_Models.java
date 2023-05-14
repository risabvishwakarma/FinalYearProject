package com.unitral.finalproject.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.unitral.finalproject.Prediction;
import com.unitral.finalproject.ml.ModelEffb1;
import com.unitral.finalproject.ml.ModelUnquant;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.List;

public class Ml_Models {
    final float CONFIDANTE = 0.35F;
    private final Context context;
    private final String TAG = "ML_MODEL";
    String OBJECT_DETECTION = "NOT";
    private Bitmap imageBitmap;
    private final Ml_Models INSTANCE = null;

    public Ml_Models(Context context) {
        this.context = context;
        Log.d(TAG, "constructor " + this);

    }

    public Bitmap getBitmap() {
        return imageBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.imageBitmap = bitmap;
    }



    public String Object_Detection() {
        ModelUnquant model = null;
        try {
            model = ModelUnquant.newInstance(context);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(getInputFeature());
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            OBJECT_DETECTION = outputFeature0.getFloatArray()[0] > CONFIDANTE ?
                    Object_Recognation() : String.valueOf(Prediction.No_Leaf_Detected);

        } catch (Exception e) {
             Log.d(TAG, e.getMessage());
        }
        return OBJECT_DETECTION;
    }

    private TensorBuffer getInputFeature() {
        // Creates inputs for reference.
        Bitmap imageBitmap = Bitmap.createScaledBitmap(this.imageBitmap, 224, 224, false);
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[224 * 224];
        imageBitmap.getPixels(intValues, 0, imageBitmap.getWidth(), 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight());
        int pixel = 0;
        for (int i = 0; i < 224; i++) {
            for (int j = 0; j < 224; j++) {

                int val = intValues[pixel++];//RGB
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
            }
        }
        inputFeature0.loadBuffer(byteBuffer);
        return inputFeature0;
    }

    public String Object_Recognation() {
        String Disease = "Healthy";
        ModelEffb1 model = null;
        try {
            model = ModelEffb1.newInstance(context);
            TensorImage image = TensorImage.fromBitmap(imageBitmap);


            // Runs model inference and gets result.
            ModelEffb1.Outputs outputs = model.process(image);
            @NonNull List<Category> outputFeature0 = outputs.getProbabilityAsCategoryList();

            Category category = null;
            String Name = "";
            final float THRESHOLD = 0.1F;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                category = outputFeature0.stream()
                        .filter(c -> c.getScore() > THRESHOLD)
                        .max(Comparator.comparing(Category::getScore)).orElse(null);
            }

            Disease = (null == category ? Prediction.No_Info.toString() : category.getLabel() + " : " + (int)(category.getScore() * 100)+"%");


        } catch (Exception e) {
            Log.i(TAG, e.getMessage());

        }

        return Disease;
    }


}
