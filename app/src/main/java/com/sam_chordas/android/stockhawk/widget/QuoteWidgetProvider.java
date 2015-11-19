package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.support.v4.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.ui.LineGraphActivity;
import com.sam_chordas.android.stockhawk.ui.MyStocksActivity;

/**
 * Created by sam_chordas on 11/4/15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class QuoteWidgetProvider extends AppWidgetProvider{
  @Override
  public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    for (int appWidgetId : appWidgetIds) {
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_collection);

      // Create intent to launch MainActivity
      Intent intent = new Intent(context, MyStocksActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
      views.setOnClickPendingIntent(R.id.widget, pendingIntent);

      // Set up collection
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
        setRemoteAdapter(context, views);
      } else {
        setRemoteAdapterV11(context, views);
      }

      // Set up collection items
      Intent clickIntentTemplate = new Intent(context, LineGraphActivity.class);
      PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
              .addNextIntentWithParentStack(clickIntentTemplate)
              .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
      views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntentTemplate);
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }
  }

  /**
   * Sets the remote adapter used to fill in the list items
   *
   * @param context the context used to launch the intent
   * @param views RemoteViews to set the RemoteAdapter
   */
  @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
  private void setRemoteAdapter(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(R.id.widget_list,
            new Intent(context, QuoteWidgetRemoteViewsService.class));
  }

  /**
   * Sets the remote adapter used to fill in the list items
   *
   * @param context the context to launch the intent
   * @param views RemoteViews to set the RemoteAdapter
   */
  @SuppressWarnings("deprecation")
  private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views) {
    views.setRemoteAdapter(0, R.id.widget_list,
            new Intent(context, QuoteWidgetRemoteViewsService.class));
  }
}
