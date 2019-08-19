package com.guro.javser.Extractors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.guro.javser.ExoPlayer;
import com.guro.javser.Utils.Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class Tanix {

    String method;
    Context mContext;
    String fileName;

    public void tanixDecoder(Context context, String videoId, String intention, String title) {
        method = intention;
        mContext = context;
        fileName = title;
        new grabTanixRefererLink().execute(videoId);
    }


    private class grabTanixRefererLink extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... strings) {
            String refererLink = "";
            String videoId = strings[0].split(":")[1];


            try {
                Document videoPage = Jsoup.connect("https://tanix.net/video/" + videoId).get();
                refererLink = videoPage.select("meta[property=og:url]").attr("content");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                String[] returning = new String[2];
                returning[0] = refererLink;
                returning[1] = videoId;
                return returning;
            }



        }

        @Override
        protected void onPostExecute(String[] refererLink) {
            String[] returning = new String[2];
            returning[0] = refererLink[0];
            returning[1] = refererLink[1];
            new grabTanix().execute(returning);
        }
    }



    private class grabTanix extends AsyncTask<String, Void, String[]> {
        String text;

        @Override
        protected String[] doInBackground(String... strings) {
            String[] returning = new String[2];
            try {
                String videoId = strings[1];

                Long newDate = new Date().getTime();
                String fullURLString = "https://play.openhub.tv/playurl?random=" + (newDate / 1000L) + "&v=" + videoId + "&source_play=tanix";
                URL url = new URL(fullURLString);




                data = "";
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                // Get the server response

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while ((line = reader.readLine()) != null) {
                    // Append server response in string
                    sb.append(line + "\n");
                }
                text = sb.toString();


                returning[0] = text;
                returning[1] = strings[0];
                return returning;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return returning;
        }

        @Override
        protected void onPostExecute(String[] videoUrlski) {
            super.onPostExecute(videoUrlski);
            if (method.equals("play")) {
                Intent playerIntent = new Intent(mContext, ExoPlayer.class);
                playerIntent.putExtra("videoUrl", videoUrlski[0]);
                playerIntent.putExtra("streamProvider", "tanix");
                playerIntent.putExtra("streamReferer", videoUrlski[1]);
                ((Activity) mContext).startActivityForResult(playerIntent, 0);
            } else {
                Downloader downloader =  new Downloader();
                downloader.downloaderMethod(videoUrlski[0], mContext, fileName);
            }

        }
    }
}
