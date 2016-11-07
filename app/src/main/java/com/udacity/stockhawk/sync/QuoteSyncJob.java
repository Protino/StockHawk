package com.udacity.stockhawk.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    public static final int PERIOD = 300;

    public static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -2);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();


                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();

                float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                StringBuilder historyBuilder = new StringBuilder();

                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                }

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.uri,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    public static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");
        PeriodicTask.Builder builder = new PeriodicTask.Builder();
        builder.setTag(QuoteTaskService.PERIODIC_TAG)
                .setService(QuoteTaskService.class)
                .setPeriod(PERIOD)
                .setUpdateCurrent(true);

        GcmNetworkManager.getInstance(context).schedule(builder.build());
    }


    synchronized public static void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }

    synchronized public static void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {
            OneoffTask.Builder builder = new OneoffTask.Builder();
            builder.setTag(QuoteTaskService.PERIODIC_TAG)
                    .setService(QuoteTaskService.class)
                    .setExecutionWindow(0, 60)
                    .setUpdateCurrent(true);
            GcmNetworkManager.getInstance(context).schedule(builder.build());
        }
    }


}
