package com.unitral.finalproject;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Random;

public class ShowDiseases extends AppCompatActivity {
    ArrayList<RemediesDataObject> list = new ArrayList<>();
    TextView Disease, Causes, Remedies;
    String value = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_diseases);
        int numr = new Random().nextInt(16 - 1 + 1) + 1;
        Disease = (TextView) findViewById(R.id.DiseaseData);
        Causes = (TextView) findViewById(R.id.CausesData);
        Remedies = (TextView) findViewById(R.id.RemediesData);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            value = extras.getString("name");
            Disease.setText(value);
        }
        //*************************************
        RequestQueue queue = Volley.newRequestQueue(this);
        final String url = "https://ap-south-1.aws.data.mongodb-api.com/app/application-0-zubdb/endpoint/remedies?secret=abcd1234";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.

                        JSONArray jsonArr;
                        try {
                            jsonArr = new JSONArray(response);
                            Log.i("TAG", "Response is: " + jsonArr.length());

                            for (int i = 1; i < jsonArr.length(); i++) {

                                JSONObject data = jsonArr.getJSONObject(i);
                                if (numr == i) {
                                    Causes.setText(data.getString("Causes"));
                                    Remedies.setText(data.getString("Remedies"));
                                    break;
                                }
                                list.add(new RemediesDataObject(data.getString("Disease"),
                                        data.getString("Causes"), data.getString("Remedies")));

                            }

                            Log.i("TAG", "Response is: " + list.size());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("TAG", "That didn't work!");
                Toast.makeText(ShowDiseases.this, "response.substring(0,50)", Toast.LENGTH_LONG).show();
            }
        });

        queue.add(stringRequest);


    }
}