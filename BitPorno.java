package com.guro.javser.Extractors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.guro.javser.ExoPlayer;
import com.guro.javser.Utils.Downloader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class BitPorno {

    Context mContext;
    String streamId;
    String method;
    String fileName;

    public void bitPornoDecoder(Context context, String videoId, String intention, String title) {
        mContext = context;
        streamId = videoId;
        method = intention;
        fileName = title;
        System.out.println(title);
        new getBitPorno().execute(streamId);
    }

    private class getBitPorno extends AsyncTask<String, Void, String[]> {

        String[] streamURL = new String[1];

        @Override
        protected String[] doInBackground(String... strings) {
            String host = "https://www.bitporno.com/e/";
            String videoId = strings[0].split("#")[0];
            String videoQ = strings[0].split("#")[1];
            try {
                Document document = Jsoup.connect(host + videoId + "&q=" + videoQ).get();
                Elements sources = document.getElementsByTag("source");
                for (Element source : sources) {
                    if (source.attr("label").equals(videoQ)) {
                        streamURL[0] = source.attr("src");
                        break;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return streamURL;
        }

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            if (method.equals("play")) {
                Intent playerIntent = new Intent(mContext, ExoPlayer.class);
                playerIntent.putExtra("videoUrl", strings[0]);
                playerIntent.putExtra("streamProvider", "bitporno");
                ((Activity) mContext).startActivityForResult(playerIntent, 0);
            } else {
                Downloader downloader = new Downloader();
                downloader.downloaderMethod(strings[0], mContext, fileName);
            }

        }
    }
}
