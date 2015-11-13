package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.afollestad.materialdialogs.MaterialDialog;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.facebook.stetho.Stetho;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;

public class MyStocksActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

  /**
   * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
   */

  /**
   * Used to store the last screen title. For use in {@link #restoreActionBar()}.
   */
  private CharSequence mTitle;
  private int taskId = 0;
  private Intent mServiceIntent;
  private ItemTouchHelper mItemTouchHelper;
  private static final int CURSOR_LOADER_ID = 0;
  private QuoteCursorAdapter mCursorAdapter;
  private Context mContext;
  private Cursor mCursor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_stocks);
    mServiceIntent = new Intent(this, StockIntentService.class);
    if (savedInstanceState == null){
      Stetho.initialize(Stetho.newInitializerBuilder(this)
              .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
              .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
              .build());

      mServiceIntent.putExtra("tag", "init");
      startService(mServiceIntent);
    }
    mContext = this;
    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recyclerView.addOnItemTouchListener(
        new RecyclerViewItemClickListener(this, new RecyclerViewItemClickListener.OnItemClickListener() {
          @Override public void onItemClick(View v, int position) {
            Intent graphIntent = new Intent(mContext, LineGraphActivity.class);
            mCursor.moveToPosition(position);
            graphIntent.putExtra("symbol", mCursor.getString(mCursor.getColumnIndex("symbol")));
            mContext.startActivity(graphIntent);
          }
        })
    );
    recyclerView.setAdapter(mCursorAdapter);


    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.attachToRecyclerView(recyclerView);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        new MaterialDialog.Builder(mContext)
            .title(R.string.symbol_search)
            .content(R.string.content_test)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(R.string.input_hint, R.string.input_prefill, new MaterialDialog.InputCallback() {
              @Override
              public void onInput(MaterialDialog dialog, CharSequence input) {
                Cursor c = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[] { QuoteColumns.SYMBOL }, QuoteColumns.SYMBOL + "= ?",
                    new String[] { input.toString()}, null);
                if (c.getCount() != 0){
                  Toast toast = Toast.makeText(MyStocksActivity.this, "This stock is already saved!",
                      Toast.LENGTH_LONG);
                  toast.setGravity(Gravity.CENTER, Gravity.CENTER, 0);
                  toast.show();
                  return;
                } else{
                  mServiceIntent.putExtra("tag", "add");
                  mServiceIntent.putExtra("symbol", input.toString());
                  startService(mServiceIntent);
                }
              }
            }).show();
      }
    });

    //ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    //mItemTouchHelper = new ItemTouchHelper(callback);
    //mItemTouchHelper.attachToRecyclerView(recyclerView);

    mTitle = getTitle();

    long period = 3600L;
    long flex = 10L;
    String periodicTag = "periodic";


    PeriodicTask periodicTask = new PeriodicTask.Builder()
        .setService(StockTaskService.class)
        .setPeriod(period)
        .setFlex(flex)
        .setTag(periodicTag)
        .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
        .setRequiresCharging(false)
        .build();

    GcmNetworkManager.getInstance(this).schedule(periodicTask);
  }


  @Override
  public void onResume() {
    super.onResume();
    getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
  }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.my_stocks, menu);
      restoreActionBar();
      return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    if (id == R.id.action_change_units){
      Utils.showPercent = !Utils.showPercent;
      this.getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args){
    return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
        QuoteColumns.ISCURRENT + " = ?",
        new String[]{"1"},
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursorAdapter.swapCursor(data);
    mCursor = data;
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
    mCursorAdapter.swapCursor(null);
  }

}
