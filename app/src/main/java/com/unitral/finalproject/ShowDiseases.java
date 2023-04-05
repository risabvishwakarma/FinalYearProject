package com.unitral.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.unitral.finalproject.image_uri.Image_Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Random;

public class ShowDiseases extends AppCompatActivity {
    TextView Disease, Causes, Remedies,Symptoms,Description;
    ImageView image;
    String value = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_diseases);
        image=findViewById(R.id.imageView2);
        Disease = findViewById(R.id.DiseaseData);
        Causes = findViewById(R.id.CausesData);
        Remedies = findViewById(R.id.RemediesData);
        Symptoms= findViewById(R.id.SymptomsData);
        Description= findViewById(R.id.DescriptionData);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("name");
            image.setImageBitmap(Image_Uri.imageBitmap);
            Log.d("ABCDE", value);
        }
                        try{
                            JSONObject json = new JSONObject(value);
                            Disease.setText(json.getString("name"));
                            Causes.setText(json.getString("causes"));
                            Remedies.setText(json.getString("remedies"));
                            Description.setText(json.getString("details"));
                            Symptoms.setText(json.getString("symptoms"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
    }
}