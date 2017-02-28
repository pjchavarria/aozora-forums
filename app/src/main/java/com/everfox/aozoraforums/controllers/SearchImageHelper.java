package com.everfox.aozoraforums.controllers;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.Html;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPather;
import org.htmlcleaner.XPatherException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

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
        public void onGetSearchImagesListener(List<String> results);
    }

    private OnGetSearchGifsListener mOnGetSearchGifsCallback;
    public interface OnGetSearchGifsListener {
        public void onGetSearchGifsListener(List<String> results);
    }
    private Context context;

    public SearchImageHelper(Context context, Activity callback) {
        this.context = context;
        this.mOnGetSearchGifsCallback = (OnGetSearchGifsListener) callback;
        this.mOnGetSearchImagesCallback = (OnGetSearchImagesListener) callback;
    }

    public void SearchImages(String text){

        SearchImagesTask searchImagesTask = new SearchImagesTask(true);
        searchImagesTask.execute(text);
    }
    public void SearchGifs(String text) {

    }

    private class SearchImagesTask extends AsyncTask<String,Void,Object[]> {

        private Boolean isImage;

        public SearchImagesTask(Boolean isImage) {
            this.isImage = isImage;
        }

        @Override
        protected Object[] doInBackground(String... strings) {
            String string = strings[0];
            string = URLEncoder.encode(string);
            String finalRequestURL = "";
            String query = queryUrl;
            query = query.replace("XXX",string);
            String finalPartOfQuery = isImage ? "" : ",itp:animated";
            query = query + finalPartOfQuery;
            Object[] imagesNodes = null;

            try {
                finalRequestURL = baseURL + query;
                if (!finalRequestURL.equals("")) {
                    HtmlCleaner cleaner = new HtmlCleaner();

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
                    String html = out.toString();
                    TagNode node = cleaner.clean(html);
                    XPather xPather = new XPather(x_path_images);
                    imagesNodes = xPather.evaluateAgainstNode(node);
                }

            } catch (IOException e) {
                    e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return imagesNodes;
        }


        @Override
        protected void onPostExecute(Object[] objects) {
            super.onPostExecute(objects);
        }
    }
}
