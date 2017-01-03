package com.udacity.stockhawk.sync;

import android.app.IntentService;
import android.content.Intent;


public class QuoteIntentService extends IntentService {

    public QuoteIntentService() {
        super(QuoteIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        QuoteSyncJob.getQuotes(getApplicationContext());
    }
}
