package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

/**
 * Created by sam_chordas on 11/6/15.
 */
public class LineGraphActivity extends AppCompatActivity implements
    LoaderManager.LoaderCallbacks<Cursor>{

  private static final int CURSOR_LOADER_ID = 0;
  private Cursor mCursor;
  private LineChartView lineChartView;
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
    for (int i = 0; i < mCursor.getCount(); i++){
      float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
      mLineSet.addPoint("test " + i, price);
      mCursor.moveToNext();
    }
    mLineSet.setColor(Color.parseColor("#e50000"))
        .setDotsStrokeThickness(Tools.fromDpToPx(2))
        .setDotsStrokeColor(Color.parseColor("#e50000"))
        .setDotsColor(Color.parseColor("#eef1f6"));
    lineChartView.addData(mLineSet);
    lineChartView.show();
  }

  private void initLineChart() {
    Paint gridPaint = new Paint();
    gridPaint.setColor(Color.parseColor("#308E9196"));
    gridPaint.setStyle(Paint.Style.STROKE);
    gridPaint.setAntiAlias(true);
    gridPaint.setStrokeWidth(Tools.fromDpToPx(1f));
    lineChartView.setBorderSpacing(1)
        .setAxisBorderValues(0, 1000, 50)
        .setXLabels(AxisController.LabelPosition.OUTSIDE)
        .setYLabels(AxisController.LabelPosition.OUTSIDE)
        .setLabelsColor(Color.parseColor("#FF8E9196"))
        .setXAxis(false)
        .setYAxis(false)
        .setBorderSpacing(Tools.fromDpToPx(5))
        .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
  }
}
