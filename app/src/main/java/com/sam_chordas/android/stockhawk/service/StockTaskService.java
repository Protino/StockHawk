package com.sam_chordas.android.stockhawk.service;

import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 9/30/15.
 */
public class StockTaskService extends GcmTaskService{
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;

  public StockTaskService(Context context){
    mContext = context;
  }
  String fetchData(String url) throws IOException{
    Request request = new Request.Builder()
        .url(url)
        .build();

    Response response = client.newCall(request).execute();
    return response.body().string();
  }

  @Override
  public int onRunTask(TaskParams params){
    Cursor initQueryCursor;
    String symbols = null;
    if (params.getTag().equals("init") || params.getTag().equals("periodic")){
     initQueryCursor = this.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, new String []{QuoteColumns.SYMBOL},
          QuoteColumns.ISCURRENT +" = ?", new String[]{"1"}, null);
      if (initQueryCursor != null){
        symbols = initQueryCursor.toString();
        Log.i(LOG_TAG, "cursor: " + symbols);
      }
    }
    StringBuilder urlStringBuilder = new StringBuilder();
    String urlString;
    String getResponse;
    int result = GcmNetworkManager.RESULT_FAILURE;
    try {
      urlStringBuilder.append("https://query.yahooapis.com/v1/public/yql?q=");
      urlStringBuilder.append(URLEncoder.encode("select * from yahoo.finance.quotes where symbol "
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

        try {
          ContentValues contentValues = new ContentValues();
          // update ISCURRENT to 0 (false) so new data is current
          contentValues.put(QuoteColumns.ISCURRENT, 0);
          this.getContentResolver().update(QuoteProvider.Quotes.CONTENT_URI, contentValues, null, null);
          this.getContentResolver().applyBatch(QuoteProvider.AUTHORITY,
              Utils.quoteJsonToContentVals(getResponse));
        }catch (RemoteException | OperationApplicationException e){
          Log.e(LOG_TAG, "Error applying batch insert", e);
        }

      } catch (IOException e){
        e.printStackTrace();
      }
    }

    return result;
  }

}
