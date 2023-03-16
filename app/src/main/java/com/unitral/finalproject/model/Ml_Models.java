package com.unitral.finalproject.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.unitral.finalproject.ml.ModelEffb1;
import com.unitral.finalproject.ml.ModelUnquant;
import com.unitral.finalproject.ml.ModelUnquant17;
import com.unitral.finalproject.ml.ModelUnquant2718;
import com.unitral.finalproject.ml.ModelUnquant917;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
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
        this.imageBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, false);
    }

    public Ml_Models(Context context) {
        this.context = context;
        Log.d(TAG,"constractor "+ this.toString());

    }

    // other instance variables can be here

   // private Ml_Models() {};




    public String Object_Detection() {
        ModelUnquant model = null;
        try {
            model = ModelUnquant.newInstance(context);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(getInputFeature());
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            OBJECT_DETECTION =   outputFeature0.getFloatArray()[0] > CONFIDANTE?
                Object_Recognation(imageBitmap):"Leaf is not detected";

          Object_Recognation1(imageBitmap);

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {

            model.close();

            Log.d(TAG, "Detection Model Closed");

        }
        return OBJECT_DETECTION;
    }
    public void Object_Recognation1(Bitmap imageBitmap) {
        ModelEffb1 model = null;
        try {
            model = ModelEffb1.newInstance(context);
            TensorImage image = TensorImage.fromBitmap(imageBitmap);



            // Runs model inference and gets result.
            ModelEffb1.Outputs outputs = model.process(image);
            @NonNull List<Category> outputFeature0 = outputs.getProbabilityAsCategoryList();
           float d=.000000001f;
            String Name="";

            for( Category c:outputFeature0){
                if(c.getScore()>d){d=c.getScore();Name=c.getLabel();}
            }
            Log.d("OUTPUT",outputFeature0.toString());

            Log.d("OUTPUT", Name+" "+d);
            Toast.makeText(context, Name, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {

            model.close();

            Log.d(TAG, "Detection Model Closed");

        }
    }

    public String Object_Recognation(Bitmap imageBitmap) {
        String Disease = "Healthy";
        ModelUnquant17 model = null;
        ModelUnquant2718 model1 = null;
        ModelUnquant917 model2 = null;
        try {
            model = ModelUnquant17.newInstance(context);
            model1 = ModelUnquant2718.newInstance(context);
            model2 = ModelUnquant917.newInstance(context);


            // Runs model inference and gets result.
            ModelUnquant17.Outputs outputs = model.process(getInputFeature());
            ModelUnquant2718.Outputs outputs1 = model1.process(getInputFeature());
            ModelUnquant917.Outputs outputs2 = model2.process(getInputFeature());

            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            TensorBuffer outputFeature2718 = outputs1.getOutputFeature0AsTensorBuffer();
            TensorBuffer outputFeature178 = outputs2.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            float[] confidences2718 = outputFeature2718.getFloatArray();
            float[] confidences178 = outputFeature178.getFloatArray();


            Disease = getDisease(getConfidenceArray(confidences, confidences178, confidences2718));


        } catch (Exception e) {
            Log.i(TAG, e.getMessage());

        } finally {
            model.close();
            Log.d(TAG, "Recognition Model Closed");
        }
        Log.d(TAG, "Recognition as : "+Disease);

        return Disease;
    }

    private TensorBuffer getInputFeature() {
        // Creates inputs for reference.
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

    private int getConfidenceArray(float[] confidences, float[] confidences917, float[] confidences2718) {

        // confidences[0]=0;
        ArrayList<Float> list = new ArrayList<>();

        for (int i = 0; i < confidences.length; i++)
            list.add(confidences[i]);

        for (int i = 0; i < confidences917.length; ++i)
            list.add(confidences917[i]);

        for (int i = confidences2718.length - 1; i >= 0; i--)
            list.add(confidences2718[i]);

        int detected_class = 0;
        int max = 0;
        for (int i = 0; i < list.size(); i++) {
            int temp = (int) (list.get(i) * 1000000000);
            if (max <= temp) {
                max = temp;
                detected_class = i;
            }
        }

        return detected_class;

    }

    private String getDisease(int x) {

        String[] diseaseList = {"Apple___Apple_scab",
                "Apple___Black_rot",
                "Apple___Cedar_apple_rust",
                "Apple___healthy",
                "Blueberry___healthy",
                "Cherry_(including_sour)___healthy",
                "Cherry_(including_sour)___Powdery_mildew",
                "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot",
                "Corn_(maize)___Common_rust_",
                "Corn_(maize)___healthy",
                "Corn_(maize)___Northern_Leaf_Blight",
                "Grape___Black_rot",
                "Grape___Esca_(Black_Measles)",
                "Grape___healthy",
                "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
                "Orange___Haunglongbing_(Citrus_greening)",
                "Peach___Bacterial_spot",
                "Peach___healthy",
                "Pepper,_bell___Bacterial_spot",
                "Pepper,_bell___healthy",
                "Potato___Early_blight",
                "Potato___healthy",
                "Potato___Late_blight",
                "Raspberry___healthy",
                "Soybean___healthy",
                "Squash___Powdery_mildew",
                "Strawberry___Leaf_scorch"};
        return diseaseList[x];
    }
}
