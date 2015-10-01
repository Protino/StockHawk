package com.example.sam_chordas.stockhawk.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by sam_chordas on 9/30/15.
 */
public class SharedPreferencesManager {
  private static Context context;

  public SharedPreferencesManager(Context context){
    this.context = context;
  }

  public static SharedPreferences getSharedPreferences(){
    return PreferenceManager.getDefaultSharedPreferences(context);
  }
}
