package com.udacity.stockhawk.sync;

import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;


public class QuoteTaskService extends GcmTaskService {

    static final String PERIODIC_TAG = QuoteTaskService.class.getSimpleName() + " periodic";

    @Override
    public int onRunTask(TaskParams taskParams) {
        QuoteSyncJob.getQuotes(getApplicationContext());
        return 0;
    }

}
