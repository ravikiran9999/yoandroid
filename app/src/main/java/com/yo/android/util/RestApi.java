package com.yo.android.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.io.File;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

public class RestApi {
    public void exce() {
        RestAdapter.Builder builder = new RestAdapter.Builder();
        builder.setLogLevel(RestAdapter.LogLevel.FULL);
        builder.setEndpoint("http://ec2-52-74-44-63.ap-southeast-1.compute.amazonaws.com");
        UploadService uplaodService = builder.build().create(UploadService.class);
//            /storage/emulated/0/Parking/1466424966632.png

        File file = new File("/storage/emulated/0/Parking/1466424966632.png");
        TypedFile typedFile = new TypedFile("multipart/form-data", file);
//        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);

        uplaodService.upload(typedFile, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    public void upload(Context context) {
        Ion.getDefault(context).configure().setLogging("MyLogs", Log.DEBUG);
        Ion.with(context)
                .load("http://ec2-52-74-44-63.ap-southeast-1.compute.amazonaws.com/api/v1/files/upload")
                .addHeader("Authorization", "kiViMBW2tfmx2ZA9e9qHK5xIJdDkyjNio9k26XuBPsqd3ybm5rBsTxZOIwQfYqLV")
                .addHeader("Content-Type", "multipart/form-data")
                .setMultipartFile("FileUpload", "multipart/form-data", new File("/storage/emulated/0/Parking/1466424966632.png"))
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {

                    }
                });

    }

    public interface UploadService {
        @Multipart
        @POST("/api/v1/files/upload")
        @Headers({"Authorization:kiViMBW2tfmx2ZA9e9qHK5xIJdDkyjNio9k26XuBPsqd3ybm5rBsTxZOIwQfYqLV",
                "content-type:multipart/form-data"
        })
        void upload(@Part("FileUpload") TypedFile file, Callback<Response> responseCallback);
    }
}
