package com.guro.javser.Extractors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.guro.javser.ExoPlayer;
import com.guro.javser.Utils.Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import io.github.lucasepe.m3u.M3UParser;
import io.github.lucasepe.m3u.models.Media;
import io.github.lucasepe.m3u.models.Playlist;
import io.github.lucasepe.m3u.models.Segment;
import io.github.lucasepe.m3u.models.Stream;

public class AV01 {

    String method;
    Context mContext;
    String videoID;
    String fileName;


    public void av01Decoder(Context context, String videoId, String intention, String title) {
        mContext = context;
        method = intention;
        videoID = videoId;
        fileName = title;
        new getPageStatus().execute(videoId.split("#")[0]);
    }


    public class getPageStatus extends AsyncTask<String, Void, Boolean> {

        boolean websiteStatus;
        @Override
        protected Boolean doInBackground(String... strings) {
            URL url;
            HttpURLConnection urlConnection = null;
            final Uri uri;
            try {
                url = new URL("https://www.av01.tv/video/" + strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    websiteStatus = true;
                } else {
                    websiteStatus = false;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return websiteStatus;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean == true) {
                new avtvURL().execute(videoID);
            } else {
                Toast.makeText(mContext,
                        "Looks like the server is down at the moment.", Toast.LENGTH_LONG).show();
            }
        }
    }


    public class avtvURL extends AsyncTask<String, Void, String[]> {


        Matcher matcher;
        @Override
        protected String[] doInBackground(String... strings) {
            String[] returning = new String[2];
            String url = "https://www.av01.tv/video/" + strings[0].split("#")[0];
            try {
                Document videoPage = Jsoup.connect(url).get();
                String converted = String.valueOf(videoPage);
                final Pattern pattern = Pattern.compile("scriptElement.src = '(.+?)';", Pattern.DOTALL);
                matcher = pattern.matcher(converted);
                matcher.find();
            }
            catch (Exception e) {

                e.printStackTrace();
            }
            returning[0] = matcher.group(1);
            returning[1] = strings[0];
            return returning;
        }

        @Override
        protected void onPostExecute(String[] s) {
            new avtv().execute(s);

        }
    }




    private class avtv extends AsyncTask<String, Void, String[]> {
        Matcher matcher;
        @Override
        protected String[] doInBackground(String... strings) {
            String[] returning = new String[2];
            String text;
            String data;
            String videoId = strings[1];

            try {
                String videoLink = strings[0];
                URL url = new URL(videoLink);
                data = "";
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoOutput(true);
                conn.setRequestProperty("Referer", "https://www.av01.tv/video/" + videoId.split("#")[0] +"/wanz-802-if-you-can-endure-akari-mitani-s-techniques-you-can-have-creampie-sex-with-her");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");



                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();
                int number = conn.getResponseCode();

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


                final Pattern pattern = Pattern.compile(", src: '(.+?)'", Pattern.DOTALL);
                matcher = pattern.matcher(text);
                matcher.find();

                returning[0] = matcher.group(1);
                returning[1] = videoId;
                return  returning;




            } catch (Exception e) {
                e.printStackTrace();
            }
            return  returning;

        }

        protected void onPostExecute(String[] strings) {
            new grabHlsInfo().execute(strings);

        }
    }


    private class grabHlsInfo extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            io.github.lucasepe.core.uri.Uri uri = io.github.lucasepe.core.uri.Uri.parse(strings[0]);


            M3UParser parser = new M3UParser();
            Playlist playlist = null;
            try {
                playlist = parser.parse(uri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String firstPart = strings[0].split("master")[0];
            String converted = String.valueOf(playlist);
            final Pattern pattern = Pattern.compile("uri=hotlink(.+?),", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(converted);
            matcher.find();
            String finalUrl = matcher.group(1).replace("}]", "");

            String bitrate = strings[1].split("#")[1];

            String gigafinal = firstPart + bitrate+ "/index" + finalUrl;
            
            List<Stream> streams = playlist.getStreams();
            for (Stream stream: streams) {
                System.out.println(stream);
            }


            List<Segment> segments = playlist.getSegments();
            for (Segment segment: segments) {
                System.out.println(segment);
            }


            List<Media> media = playlist.getMedia();
            for (Media m: media) {
                System.out.println(media);
            }

            return gigafinal;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if (method.equals("play")) {
                Intent playerIntent = new Intent(mContext, ExoPlayer.class);
                playerIntent.putExtra("videoUrl", s);
                playerIntent.putExtra("streamProvider", "av01");
                ((Activity) mContext).startActivityForResult(playerIntent, 0);
                return;
            } else {
                Downloader downloader =  new Downloader();
                downloader.downloaderMethod(s, mContext, fileName);
            }

        }
    }
}
