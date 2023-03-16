package com.unitral.finalproject.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.unitral.finalproject.ml.ModelEffb1;


import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;

import java.util.Comparator;
import java.util.List;

public class Ml_Models {
    private Bitmap imageBitmap;
    private final Context context;
    private  Ml_Models INSTANCE = null;
    private final String TAG = "ML_MODEL";
    final float CONFIDANTE = 0.35F;
    String OBJECT_DETECTION = "NOT";

    public Bitmap getBitmap() {
        return imageBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.imageBitmap = Bitmap.createScaledBitmap(bitmap, 240, 240, false);
    }

    public Ml_Models(Context context) {
        this.context = context;
        Log.d(TAG,"constructor "+ this.toString());

    }

    // other instance variables can be here

   // private Ml_Models() {};




    public String Object_Detection() {
//        ModelUnquant model = null;
        try {
//            model = ModelUnquant.newInstance(context);
//
//            // Runs model inference and gets result.
//            ModelUnquant.Outputs outputs = model.process(getInputFeature());
//            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

         //   OBJECT_DETECTION =   outputFeature0.getFloatArray()[0] > CONFIDANTE?
            OBJECT_DETECTION=   Object_Recognation(imageBitmap);



        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {

           // model.close();

            Log.d(TAG, "Detection Model Closed");

        }
        return OBJECT_DETECTION;
    }
    public String Object_Recognation(Bitmap imageBitmap) {
        String Disease = "Healthy";
        ModelEffb1 model = null;
        try {
            model = ModelEffb1.newInstance(context);
            TensorImage image = TensorImage.fromBitmap(imageBitmap);



            // Runs model inference and gets result.
            ModelEffb1.Outputs outputs = model.process(image);
            @NonNull List<Category> outputFeature0 = outputs.getProbabilityAsCategoryList();

            Category category=null;
            String Name="";
            final float THRESHOLD= 0.5F;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
             category= outputFeature0.stream().filter(c -> c.getScore() > THRESHOLD).max(Comparator.comparing(Category::getScore)).orElse(null);
            }

            Disease=(null==category?"Leaf isn't Dectected":category.getLabel());
//            if(finalresult!=null){
//            Log.d("OUTPUT", finalresult.getLabel()+" "+finalresult.getScore());
//            Toast.makeText(context, finalresult.getLabel(), Toast.LENGTH_SHORT).show();}

        } catch (Exception e) {
            Log.i(TAG, e.getMessage());

        } finally {
            assert model != null;
            model.close();
            Log.d(TAG, "Recognition Model Closed");
        }

        return Disease;
    }








}
