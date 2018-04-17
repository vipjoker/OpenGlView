package com.example.oleh.opengl2.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by oleh on 11/8/17.
 */

public class ResourceLoader {
    private static final String TAG = ResourceLoader.class.getName();
    public static String loadRawResource(Context context,int id){
        InputStream inputStream = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder builder = new StringBuilder();
        try{
        for(String line = reader.readLine();line != null; line = reader.readLine()){
            builder.append(line).append("\n");
        }
        reader.close();
            inputStream.close();
             return builder.toString();
        }catch (IOException e){
            Log.e(TAG, "loadRawResource: ",e);
        }
        return null;

    }
}
