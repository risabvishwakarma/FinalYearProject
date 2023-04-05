package com.unitral.finalproject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class GetData {

    String result="";
    ProgressBar progressBar=null;
    MainActivity MainActivityObject=null;
    ImageButton details;
    int time=3;
    public GetData(ProgressBar pb, ImageButton upload,MainActivity object){
        progressBar=pb;
        details=upload;
        MainActivityObject=object;

    }

   public  void call(String disease,Context context){
       RequestQueue queue = Volley.newRequestQueue(context);
        final String url="https://risabvishwakarma2.onrender.com/remedies/"+disease;

    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    progressBar.setVisibility(View.GONE);
                    details.setVisibility(View.VISIBLE);
                    result=response;
                    MainActivityObject.giveData(response);

                }
            }, new Response.ErrorListener() {
        @SuppressLint("SuspiciousIndentation")
        @Override
        public void onErrorResponse(VolleyError error) {
            if(--time>0){
            call(disease,context);}
            else{
            progressBar.setVisibility(View.GONE);
            details.setVisibility(View.VISIBLE);
            Toast.makeText(context, "NO INTERNET", Toast.LENGTH_SHORT).show();}
        }
    });

    queue.add(stringRequest);

    }


}
