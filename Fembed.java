package com.guro.javser.Extractors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.guro.javser.ExoPlayer;
import com.guro.javser.Utils.Downloader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fembed {
    String method;
    Context mContext;
    String fileName;


    public void fembedDecoder(Context context, String videoId, String intention, String title) {
        method = intention;
        mContext = context;
        fileName = title;
        new getFembedResponse().execute(videoId);
    }

    private class getFembedResponse extends AsyncTask<String, Void, String> {
        String streamFinalURL;

        @Override
        protected String doInBackground(String... strings) {
            String data = null;

            String text = "";
            BufferedReader reader = null;

            // Send data
            try {

                String fullURLString = "https://youapi.ml/api/source/" + strings[0];
                String[] separatedURL = strings[0].split("#");
                String streamQuality = separatedURL[1];


                // Defined URL  where to send data
                URL url = new URL(fullURLString);

                // Send POST data request

                data = "";
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }


                text = sb.toString();
                JSONObject responseJSON = new JSONObject(text);
                JSONArray responseStreamArray = responseJSON.getJSONArray("data");
                for (int i = 0; i < responseStreamArray.length(); i++) {
                    if (responseStreamArray.getJSONObject(i).getString("label").equals(streamQuality)) {
                        streamFinalURL = responseStreamArray.getJSONObject(i).getString("file");

                    }

                }
            } catch (Exception ex) {
                ex.printStackTrace();

            }
            return streamFinalURL;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (method.equals("play")) {
                Intent playerIntent = new Intent(mContext, ExoPlayer.class);
                playerIntent.putExtra("videoUrl", result);
                playerIntent.putExtra("streamProvider", "fembed");
                ((Activity) mContext).startActivityForResult(playerIntent, 0);
            } else {
                Downloader downloader =  new Downloader();
                downloader.downloaderMethod(result, mContext, fileName);
            }
        }
    }
}
