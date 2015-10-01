package com.example.sam_chordas.stockhawk;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;

/**
 * Created by sam_chordas on 10/1/15.
 */
public class StocksAPI {

  static OkHttpClient client = new OkHttpClient();
  public static String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }
}
