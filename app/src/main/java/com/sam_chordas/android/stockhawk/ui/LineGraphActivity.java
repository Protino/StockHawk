package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;

/**
 * Created by sam_chordas on 11/6/15.
 */
public class LineGraphActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor>{

  private static final int CURSOR_LOADER_ID = 0;
  private Cursor mCursor;
  private LineChartView lineChartView;
  private ArrayList<Float> bidPrices;
  private LineSet mLineSet;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_line_graph);
    mLineSet = new LineSet();
    lineChartView = (LineChartView) findViewById(R.id.linechart);
    initLineChart();
    Intent intent = getIntent();
    Bundle args = new Bundle();
    args.putString("symbol", intent.getStringExtra("symbol"));
    getLoaderManager().initLoader(CURSOR_LOADER_ID, args, this);
  }

  @Override public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
        new String[]{ QuoteColumns.BIDPRICE},
        QuoteColumns.SYMBOL + " = ?",
        new String[]{args.getString("symbol")},
        null);
  }

  @Override public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mCursor = data;
    fillLineSet();
  }

  @Override public void onLoaderReset(Loader<Cursor> loader) {

  }

  private void fillLineSet(){
    mCursor.moveToFirst();
    mLineSet.setColor(Color.parseColor("#a34545"))
        .setFill(Color.parseColor("#a34545"))
        .setSmooth(true);
    for (int i = 0; i < mCursor.getCount(); i++){
      float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
      mLineSet.addPoint("test", price);
      mCursor.moveToNext();
    }
    lineChartView.addData(mLineSet);
    lineChartView.show();
  }

  private void initLineChart() {
    Paint gridPaint = new Paint();
    gridPaint.setColor(Color.parseColor("#7F97B867"));
    gridPaint.setStyle(Paint.Style.STROKE);
    gridPaint.setAntiAlias(true);
    gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
    lineChartView.setXAxis(true);
    lineChartView.setYAxis(true);
    lineChartView.setStep(2);
    lineChartView.setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
  }
}
