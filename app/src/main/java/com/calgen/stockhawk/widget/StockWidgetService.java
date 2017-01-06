package com.calgen.stockhawk.widget;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.calgen.stockhawk.R;
import com.calgen.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Gurupad Mamadapur on 1/1/2017.
 */

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory();
    }

    public class ListRemoteViewFactory implements RemoteViewsService.RemoteViewsFactory {

        private Cursor data = null;

        //Lifecycle start
        @Override
        public void onCreate() {
            //No action needed
        }

        @Override
        public void onDestroy() {
            if (data != null) {
                data.close();
                data = null;
            }

        }
        //Lifecycle end

        @Override
        public void onDataSetChanged() {
            if (data != null) data.close();

            final long identityToken = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.uri,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);
            Binder.restoreCallingIdentity(identityToken);
        }

        @Override
        public int getCount() {
            return data == null ? 0 : data.getCount();
        }

        @SuppressLint("PrivateResource")
        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || data == null
                    || !data.moveToPosition(position)) {
                return null;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.list_item_quote);

            String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            int backgroundDrawable;

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+");
            dollarFormatWithPlus.setMaximumFractionDigits(2);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);
            dollarFormatWithPlus.setMinimumFractionDigits(2);

            if (absoluteChange > 0) {
                backgroundDrawable = R.drawable.percent_change_pill_green;
            } else {
                backgroundDrawable = R.drawable.percent_change_pill_red;
            }

            remoteViews.setTextViewText(R.id.symbol, stockSymbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(stockPrice));
            remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));
            remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);
            remoteViews.setInt(R.id.list_item_quote, "setBackgroundResource", R.color.material_grey_850);

            final Intent fillInIntent = new Intent();
            Uri stockUri = Contract.Quote.makeUriForStock(stockSymbol);
            fillInIntent.setData(stockUri);
            remoteViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);
            return remoteViews;

        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return data.moveToPosition(i) ? data.getLong(Contract.Quote.POSITION_ID) : i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
