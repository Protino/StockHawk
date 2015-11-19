package com.sam_chordas.android.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.Utils;

/**
 * Created by Daniel on 11/16/15.
 */
public class QuoteWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor data = null;
            @Override
            public void onCreate() {
                // Nothing to do
            }

            @Override
            public void onDataSetChanged() {
                if (data != null) {
                    data.close();
                }

                final long identityToken = Binder.clearCallingIdentity();
                data = getContentResolver().query(
                        QuoteProvider.Quotes.CONTENT_URI,
                        new String[] {
                                QuoteColumns._ID,
                                QuoteColumns.SYMBOL,
                                QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE,
                                QuoteColumns.CHANGE,
                                QuoteColumns.ISUP
                        },
                        QuoteColumns.ISCURRENT + " = ?",
                        new String[]{"1"},
                        null);
                Binder.restoreCallingIdentity(identityToken);
            }

            @Override
            public void onDestroy() {

            }

            @Override
            public int getCount() {
                int count =  data == null ? 0 : data.getCount();
                Log.d("QuoteWidget", "" + count);
                return count;
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        data == null || !data.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_collection_item);
                views.setTextViewText(R.id.stock_symbol, data.getString(data.getColumnIndex
                        ("symbol")));

                int sdk = Build.VERSION.SDK_INT;
                if (data.getInt(data.getColumnIndex("is_up")) == 1){
                        views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }

                if (Utils.showPercent) {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("percent_change")));
                } else {
                    views.setTextViewText(R.id.change, data.getString(data.getColumnIndex("change")));
                }

                final Intent fillInIntent = new Intent();
                fillInIntent.putExtra("symbol", data.getString(data.getColumnIndex("symbol")));
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return new RemoteViews(getPackageName(), R.layout.widget_collection_item);
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (data != null && data.moveToPosition(position)) {
                    final int QUOTES_ID_COL = 0;
                    return data.getLong(QUOTES_ID_COL);
                }
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
