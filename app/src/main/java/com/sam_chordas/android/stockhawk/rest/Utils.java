package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      Log.i(LOG_TAG, "JSON: " + jsonObject.toString());
      if (jsonObject != null && jsonObject.length() != 0){
        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");
        String queryTime = jsonObject.getJSONObject("query").getString("created");
        if (resultsArray != null && resultsArray.length() != 0){
          for (int i = 0; i < resultsArray.length(); i++){
            ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
            jsonObject = resultsArray.getJSONObject(i);
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BID, jsonObject.getString("Bid"));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, jsonObject.getString("ChangeinPercent"));
            builder.withValue(QuoteColumns.CHANGE, change);
            builder.withValue(QuoteColumns.CREATED, queryTime);
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-'){
              builder.withValue(QuoteColumns.ISUP, 0);
            }else{
              builder.withValue(QuoteColumns.ISUP, 1);
            }
            batchOperations.add(builder.build());
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }
}
