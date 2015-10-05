package com.sam_chordas.android.stockhawk.service;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by sam_chordas on 9/30/15.
 */
public class StockTaskService extends GcmTaskService{
  public SharedPreferences mSharedPreferences;

  OkHttpClient client = new OkHttpClient();

  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  @Override
  public int onRunTask(TaskParams params){
    Log.d(StockTaskService.class.getSimpleName(), "In service");
//    mSharedPreferences = SharedPreferencesManager.getSharedPreferences();
    StringBuilder urlStringBuilder = new StringBuilder();
    String urlString;
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;
    try {
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
      urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quote where symbol "
          + "in (\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")", "UTF-8"));
      urlStringBuilder.append("&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
          + "org%2Falltableswithkeys&callback=");
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    if (urlStringBuilder != null){
      urlString = urlStringBuilder.toString();
      try{
        getResponse = fetchData(urlString);
        Log.i(StockTaskService.class.getSimpleName(), "STOCK JSON: " + getResponse);
        result = GcmNetworkManager.RESULT_SUCCESS;
      } catch (IOException e){
        e.printStackTrace();
      }
    }


    return result;
  }

}
