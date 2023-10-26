package tn.potatodisease;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class MainActivity extends AppCompatActivity {

    ImageView backimg, imgpredict;
    ImageButton camerabutton, gallerybutton;
    TextView textuse;
    FrameLayout fl;
    ProgressBar pb;

    Uri image_uri;

    Retrofit retrofit;
    Handler handler;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;
    private static final int PERMISSION_CODE_CAMERA = 1002;
    private static final int PERMISSION_CODE_INTERNET = 1003;
    private static final int IMAGE_CAPTURE_CODE = 1004;

    public interface PredictClass{
        @Multipart
        @POST("predictdisease")
        Call<PredictionResponse> predict(@Part MultipartBody.Part file);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(checkSelfPermission(android.Manifest.permission.INTERNET)  == PackageManager.PERMISSION_GRANTED) {

            retrofit = new Retrofit.Builder()
                    .baseUrl("https://potatodiseaseclassification.azurewebsites.net/api/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        else{

                String[] permissions = {Manifest.permission.INTERNET};
                requestPermissions(permissions, PERMISSION_CODE_INTERNET);


        }



        backimg = (ImageView) findViewById(R.id.backimg);
        imgpredict = (ImageView) findViewById(R.id.imgpredict);
        camerabutton = (ImageButton) findViewById(R.id.camerabutton);
        gallerybutton = (ImageButton) findViewById(R.id.gallerybutton);
        textuse = (TextView) findViewById(R.id.textuse);
        fl = (FrameLayout) findViewById(R.id.frame);
        pb = (ProgressBar) findViewById(R.id.progressBar);
        pb.setVisibility(View.GONE);


        gallerybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED){
                        PickImageFromGallery();
                    }
                    else{
                        String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permissions, PERMISSION_CODE_CAMERA);
                    }
                }

            }
        });

        camerabutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA)  == PackageManager.PERMISSION_GRANTED
                        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)  == PackageManager.PERMISSION_GRANTED){
                        OpenCamera();
                    }
                    else{
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
                        requestPermissions(permissions, PERMISSION_CODE);
                    }
                }
            }
        });

    }

    private void predict(){

            PredictClass service = retrofit.create(PredictClass.class);
            Bitmap bitmap = ((BitmapDrawable) imgpredict.getDrawable()).getBitmap();

            File imageFile = new File(getCacheDir(), "image.jpg");
            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }catch (java.io.IOException e){
                Toast.makeText(getApplicationContext(),"Please try again",Toast.LENGTH_SHORT).show();

            }

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData("file", imageFile.getName(), requestBody);
            Call<PredictionResponse> call = service.predict(filePart);
            call.enqueue(new Callback<PredictionResponse>() {
                @Override
                public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                    pb.setVisibility(View.GONE);
                    textuse.setText("");
                    if (response.isSuccessful()) {
                        PredictionResponse prediction = response.body();
                        String class_predicted = prediction.getClass_predicted();
                        String confidence = prediction.getConfidence();
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textuse.getLayoutParams();
                        params.setMarginStart(120);
                        textuse.setLayoutParams(params);
                        textuse.setText("predicted class :"+class_predicted+"\n confidence :"+confidence+"%");
                        textuse.setTextSize(30);

                    } else {
                        // Handle the error
                        Toast.makeText(getApplicationContext(),"Please try again", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<PredictionResponse> call, Throwable t) {
                    pb.setVisibility(View.GONE);
                    textuse.setText("");
                    Toast.makeText(getApplicationContext(),"network error",Toast.LENGTH_SHORT).show();

                }
            });
        }


    private void OpenCamera(){
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(cameraintent, IMAGE_CAPTURE_CODE);


    }

    private void PickImageFromGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    PickImageFromGallery();
                }
                else{
                    Toast.makeText(this, "Permission denied ..!", Toast.LENGTH_SHORT).show();
                }

            case PERMISSION_CODE_CAMERA:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    OpenCamera();
                }
                else{
                    Toast.makeText(this, "Permission denied ..!", Toast.LENGTH_SHORT).show();
                }

            case PERMISSION_CODE_INTERNET:
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    retrofit = new Retrofit.Builder()
                            .baseUrl("https://potatodiseaseclassification.azurewebsites.net/api/predictdisease/")
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                }
                else{
                    Toast.makeText(this, "Permission denied ..!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE){
            imgpredict.setImageURI(data.getData());

            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textuse.getLayoutParams();
            params.setMarginStart(110);
            textuse.setLayoutParams(params);
            textuse.setText("processing");
            pb.setVisibility(View.VISIBLE);
            predict();

        }

        if(resultCode == RESULT_OK && requestCode == IMAGE_CAPTURE_CODE) {
            imgpredict.setImageURI(image_uri);
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) textuse.getLayoutParams();
            params.setMarginStart(110);
            textuse.setLayoutParams(params);
            textuse.setText("processing");
            pb.setVisibility(View.VISIBLE);
            predict();
        }

    }
}