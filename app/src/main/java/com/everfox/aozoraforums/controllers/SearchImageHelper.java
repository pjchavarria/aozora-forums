package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.everfox.aozoraforums.models.ImageData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;

/**
 * Created by daniel.soto on 2/28/2017.
 */

public class SearchImageHelper {

    String baseURL = "https://www.google.com/search";
    String queryUrl = "?q=XXX&tbm=isch&safe=active&tbs=isz:m";
    String x_path_images = "//div[@id='rg_s']//div//div[@class='rg_meta']";

    private final OkHttpClient client = new OkHttpClient();
    private static int RESULT_MAX = 20;
    private OnGetSearchImagesListener mOnGetSearchImagesCallback;
    public interface OnGetSearchImagesListener {
        public void onGetSearchImagesListener(List<ImageData> results);
    }

    private OnGetSearchGifsListener mOnGetSearchGifsCallback;
    public interface OnGetSearchGifsListener {
        public void onGetSearchGifsListener(List<ImageData> results);
    }
    private Context context;

    public SearchImageHelper(Context context, Activity callback) {
        this.context = context;
        this.mOnGetSearchGifsCallback = (OnGetSearchGifsListener) callback;
        this.mOnGetSearchImagesCallback = (OnGetSearchImagesListener) callback;
    }

    public void SearchImages(String text){

        SearchImagesTask searchImagesTask = new SearchImagesTask(true);
        searchImagesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,text);
    }
    public void SearchGifs(String text) {

        SearchImagesTask searchImagesTask = new SearchImagesTask(false);
        searchImagesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,text);
    }

    private class SearchImagesTask extends AsyncTask<String,Void,String> {

        private Boolean isImage;

        public SearchImagesTask(Boolean isImage) {
            this.isImage = isImage;
        }

        @Override
        protected String doInBackground(String... strings) {
            String string = strings[0];
            string = URLEncoder.encode(string);
            String finalRequestURL = "";
            String query = queryUrl;
            query = query.replace("XXX",string);
            String finalPartOfQuery = isImage ? "" : ",itp:animated";
            query = query + finalPartOfQuery;
            Object[] imagesNodes = null;
            String html ="";
            try {
                finalRequestURL = baseURL + query;
                if (!finalRequestURL.equals("")) {

                    // OPEN A CONNECTION TO THE DESIRED URL
                    URL url = new URL(finalRequestURL);
                    URLConnection conn = url.openConnection();
                    conn.setRequestProperty("Host","www.google.com");
                    conn.setRequestProperty("Accept","ext/html, application/xhtml+xml, image/jxr, */*");
                    conn.setRequestProperty("Accept-Encoding:","gzip, deflate");
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2486.0 Safari/537.36 Edge/13.10586");
                    conn.setRequestProperty("X-Requested-With",null);

                    //USE THE CLEANER TO "CLEAN" THE HTML AND RETURN IT AS A TAGNODE OBJECT

                    final int bufferSize = 1024;
                    final char[] buffer = new char[bufferSize];
                    final StringBuilder out = new StringBuilder();
                    Reader in = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    for (; ; ) {
                        int rsz = in.read(buffer, 0, buffer.length);
                        if (rsz < 0)
                            break;
                        out.append(buffer, 0, rsz);
                    }
                    html = out.toString();
                    int indexOfFirstDiv = html.indexOf("<div style=\"visibility:hidden\" data-jiis=\"up\" data-async-type=\"ichunk\" id=\"rg_s\" ");
                    html = html.substring(indexOfFirstDiv);
                    int indexOfLimit = html.indexOf("<script>(function()");
                    html = html.substring(0,indexOfLimit);



                }

            } catch (IOException e) {
                    e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return html;
        }


        @Override
        protected void onPostExecute(String html) {
            super.onPostExecute(html);

            ArrayList<ImageData> lst = new ArrayList<>();
            while (true) {
                int indexItem = html.indexOf("<div class=\"rg_meta\">");
                if(indexItem <1)
                    break;
                String item =html.substring(indexItem);
                String itemJson = item.substring(item.indexOf("{"),item.indexOf("}<")+1);
                try {
                    JSONObject jsonObject = new JSONObject(itemJson);
                    ImageData imgData = new ImageData();
                    imgData.setHeight(jsonObject.getInt("oh"));
                    imgData.setWidth(jsonObject.getInt("ow"));
                    imgData.setUrl(jsonObject.getString("ou"));
                    if(imgData.getHeight() <= 1400 && imgData.getWidth() <=1400)
                        lst.add(imgData);
                    html = html.substring(indexItem + item.indexOf("}<") + 1);
                }
                catch (StringIndexOutOfBoundsException sex) {
                    break;
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if (isImage)
                mOnGetSearchImagesCallback.onGetSearchImagesListener(lst);
            else
                mOnGetSearchGifsCallback.onGetSearchGifsListener(lst);
        }
    }
}
