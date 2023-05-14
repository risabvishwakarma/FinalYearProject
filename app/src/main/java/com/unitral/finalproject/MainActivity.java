package com.unitral.finalproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.system.Os;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.unitral.finalproject.databinding.ActivityMainBinding;
import com.unitral.finalproject.image_uri.Image_Uri;
import com.unitral.finalproject.model.Ml_Models;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener {
    private static final int PICK_IMAGE = 1999;
    static String imageUri = "";
    int REQUEST_IMAGE_CAPTURE = 1004;
    ImageButton buttonCamera, buttonGallery, buttonUpload;
    ImageView imageView;
    TextView diseaseName;
    boolean imageViewHaveImage = false;
    ActivityMainBinding binding;
    Uri imguri;
    Uri contentUri;
    Location currLocation = null;
    String currentPhotoPath;
    Bitmap imageBitmap;
    static String information;
    ProgressBar progressBar=null;
    String TAG = "MAIN_ACTI";
    FusedLocationProviderClient fusedLocationClient = null;


    ActivityResultLauncher<Intent> mGallery = null;


    String currentPath;
    Ml_Models model = null;

    public void giveData(String response) {
        information=response;
        DetailsButtonPressed();

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "OnCREATE " + getApplicationContext());

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        model = new Ml_Models(getApplicationContext());

        setContentView(binding.getRoot());
        progressBar=binding.progressBar;
        imageView = findViewById(R.id.imageView);
        diseaseName = findViewById(R.id.diseasename);
        imageView.setOnClickListener(this);
        findViewById(R.id.btnCamera).setOnClickListener(this);
        findViewById(R.id.btnCamera2).setOnClickListener(this);
        findViewById(R.id.upload).setOnClickListener(this);


        //location
        ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts
                        .RequestMultiplePermissions(), result -> {
                    Boolean fineLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(
                            Manifest.permission.ACCESS_COARSE_LOCATION, false);
                    if (fineLocationGranted != null && fineLocationGranted) {
                        //Toast.makeText(getApplicationContext(), fineLocationGranted.toString(), Toast.LENGTH_SHORT).show();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {

                    } else {

                        // No location access granted.

                        Toast.makeText(getApplicationContext(), "Not Granted", Toast.LENGTH_SHORT).show();
                    }


                }
        );
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });


        //Image From Gallery

        mGallery = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK)
                        try {
                            assert result.getData() != null;
                            setImageWithUri(MediaStore.Images.Media.getBitmap(this.getContentResolver(), result.getData().getData()));
                        } catch (IOException e) {
                            Log.d(TAG, Arrays.toString(e.getStackTrace()), new RuntimeException(e));

                        }
                }
        );

    }


    @Override
    protected void onStart() {
        super.onStart();
        //LOAD IMAGE
        if (Image_Uri.getFile() != null) {
            File f = Image_Uri.getFile();

            ExifInterface exif = null;
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    exif = new ExifInterface(f);
                }
                assert exif != null;
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                int rotationInDegrees = exifToDegrees(rotation);

                Matrix matrix = new Matrix();
                if (rotation != 0) {
                    matrix.preRotate(rotationInDegrees);
                }
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.fromFile(f));

                Bitmap bitmap1 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                setImageWithUri(bitmap1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            f.delete();

        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            getLocation();
        }

        Log.d(TAG, "OnSTART" + getApplicationContext());

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d(TAG, "OnPOST");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnSTOP");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OnDESTROY");
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.flushLocations();
        Log.d(TAG, "OnPAUSE");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "OnRESUME");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "OnBACK_PRESSED");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "OnRESTART");
    }


    private int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    // startLockTask();
                    if (location == null) {
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);


                        alertDialog.setTitle("GPS is not Enabled!");

                        alertDialog.setMessage("Do you want to turn on GPS?");


                        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                MainActivity.this.startActivity(intent);
                            }
                        });


                        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        alertDialog.show();


                    } else

                        setCurrent_location(location.getLatitude(), location.getLongitude());


                });


    }

    @SuppressLint("SetTextI18n")
    private void setCurrent_location(double Latitude, double Longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        try {

            addresses = geocoder.getFromLocation(Latitude, Longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String division = addresses.get(0).getSubAdminArea();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();

            binding.location.setText(city + "," + state);

        } catch (IOException ignored) {
        }

    }

    @SuppressLint("SuspiciousIndentation")
    private void setImageWithUri(Bitmap bitmap) throws IOException {
        imageViewHaveImage = true;
        if (null != bitmap) {
            int mindimention = Math.min(bitmap.getHeight(), bitmap.getWidth());
            int maxdimention = Math.max(bitmap.getHeight(), bitmap.getWidth());
            int dim = Math.abs(maxdimention - mindimention) / 2;
            bitmap = Bitmap.createBitmap(bitmap, bitmap.getHeight() < bitmap.getWidth() ? dim : 0, bitmap.getHeight() > bitmap.getWidth() ? dim : 0, mindimention, mindimention);
            imageView.setImageBitmap(bitmap);
            Image_Uri.imageBitmap=bitmap;
            model.setBitmap(bitmap);
            diseaseName.setText(model.Object_Detection());

            information=null;
            imageViewHaveImage= !(diseaseName.getText().toString().equals(String.valueOf(Prediction.No_Leaf_Detected))
                    ||diseaseName.getText().toString().equals(String.valueOf(Prediction.No_Info)))
                    && !diseaseName.getText().toString().contains("healthy");

            binding.upload.performClick();
        }
    }


    @SuppressLint({"NonConstantResourceId", "SuspiciousIndentation"})
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.btnCamera: {

                startActivity(new Intent(MainActivity.this, CameraActivity.class));
                break;
            }
            case R.id.btnCamera2: {
                mGallery.launch(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI));

                break;
            }
            case R.id.upload: {

                if(imageViewHaveImage  && information==null){
                    runOnUiThread(() -> {
                        binding.progressBar.setVisibility(View.VISIBLE);
                        binding.upload.setVisibility(View.GONE);

                    });
                    binding.progressBar.setVisibility(View.VISIBLE);
                    binding.upload.setVisibility(View.GONE);

                    new GetData(progressBar,binding.upload,this).call(diseaseName.getText().toString().split(":")[0].trim(),getApplicationContext());
                }else if(imageViewHaveImage){
                    DetailsButtonPressed();
                }
                break;
            }
            case R.id.imageView: {
                //Not Implemented
                break;
            }
            default: {
            }
        }

    }
     private void DetailsButtonPressed(){

         if (imageViewHaveImage && information!=null){
             Intent i=  new Intent(MainActivity.this, ShowDiseases.class);
             i.putExtra("name",information);
             startActivity(i);}

     }
}
