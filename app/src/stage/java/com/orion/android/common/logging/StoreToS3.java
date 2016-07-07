package com.orion.android.common.logging;

import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import java.io.File;

/**
 * Attempts to store a file to Amazon S3 Service using
 * AWS Android SDK. If successful, returns the URL of
 * the file which is stored, else a null.
 *
 * @author Vishal | Paradigm Creatives
 */
public class StoreToS3 {

    public static final String TAG = StoreToS3.class.getSimpleName();
    private static AmazonS3Client s3Client;


    private StoreToS3(){
        //default constructor needed
    }
    /**
     * Sends a file to S3 bucket with a given key and attempts
     * to store it on S3 Server. If successful, returns the URL
     * of the given file.
     *
     * @param file <code>File</code> to be stored to S3 Server.
     * @param key  Key of the file by which it will be identified.
     * @return URL of the stored file if stored successfully, else
     * null.
     */
    public static String sendToS3(File file, String key, ProgressListener progressListener) {

        try {
            Log.e(TAG, "sendToS3: key=" + key);
            AWSCredentials awsCredentials = new BasicAWSCredentials("", "");
            s3Client = new AmazonS3Client(awsCredentials);
        } catch (Exception e) {
            Logger.logStackTrace(e);
            Log.e(TAG, "Error creating AmazonS3Client: " + e.getMessage());
            return null;
        }
        String objectURL = null;
        try {
            PutObjectRequest putObjectRequest = new PutObjectRequest(GlobalClass.BUCKET_NAME, key, file);
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType("text/plain");
            putObjectRequest = putObjectRequest.withMetadata(objectMetadata);
            //setting permission for all to read

            putObjectRequest.setCannedAcl(CannedAccessControlList.PublicRead);
            putObjectRequest.setGeneralProgressListener(progressListener);
            s3Client.putObject(putObjectRequest);
            objectURL = "https://s3.amazonaws.com/" + GlobalClass.BUCKET_NAME + "/" + key;
            Log.e(TAG, "sendToS3: objectURL=" + objectURL);
        } catch (AmazonS3Exception as3e) {
            Logger.logStackTrace(as3e);
            Log.e(TAG, "ERROR! AmazonS3Exception due to: " + as3e.getMessage());
            objectURL = null;
        } catch (AmazonServiceException ase) {
            Logger.logStackTrace(ase);
            Log.e(TAG, "ERROR! AmazonServiceException due to: " + ase.getMessage());
            objectURL = null;
        } catch (AmazonClientException ace) {
            Logger.logStackTrace(ace);
            Log.e(TAG, "ERROR! AmazonClientException due to: " + ace.getMessage());
            objectURL = null;
        } catch (Exception e) {
            Logger.logStackTrace(e);
            Log.e(TAG, "ERROR! Exception due to: " + e.getMessage());
            objectURL = null;
        }

        return objectURL;
    }
    //end of sentToS3()

}
//end of Class