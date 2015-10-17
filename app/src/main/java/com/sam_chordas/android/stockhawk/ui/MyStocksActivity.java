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
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.databinding.ActivityMyStocksBinding;
import com.sam_chordas.android.stockhawk.rest.QuoteCursorAdapter;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockIntentService;
import com.sam_chordas.android.stockhawk.service.StockTaskService;
import com.facebook.stetho.Stetho;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;
import com.melnykov.fab.FloatingActionButton;
import com.sam_chordas.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;

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

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my_stocks);
    if (savedInstanceState == null){
      Stetho.initialize(
          Stetho.newInitializerBuilder(this)
              .enableDumpapp(
                  Stetho.defaultDumperPluginsProvider(this))
              .enableWebKitInspector(
                  Stetho.defaultInspectorModulesProvider(this))
              .build());
    }

    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
    recyclerView.setLayoutManager(
        new LinearLayoutManager(recyclerView.getContext())
    );
    getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

    mCursorAdapter = new QuoteCursorAdapter(this, null);
    recyclerView.setAdapter(mCursorAdapter);

    mServiceIntent = new Intent(this, StockIntentService.class);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.attachToRecyclerView(recyclerView);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        mServiceIntent.putExtra("tag", "add");
        startService(mServiceIntent);
      }
    });

    ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mCursorAdapter);
    mItemTouchHelper = new ItemTouchHelper(callback);
    mItemTouchHelper.attachToRecyclerView(recyclerView);

    mTitle = getTitle();

    mServiceIntent.putExtra("tag", "init");
    startService(mServiceIntent);

    //IntentFilter filter = new IntentFilter(ResponseReceiver.ACTION_RESP);
    //filter.addCategory(Intent.CATEGORY_DEFAULT);
    //receiver = new ResponseReceiver();
    //registerReceiver(receiver, filter);


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



  //@Override
  //public void onNavigationDrawerItemSelected(int position) {
  //  // update the main content by replacing fragments
  //  FragmentManager fragmentManager = getSupportFragmentManager();
  //  fragmentManager.beginTransaction()
  //      .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
  //      .commit();
  //}

  //public void onSectionAttached(int number) {
  //  switch (number) {
  //    case 1:
  //      mTitle = getString(R.string.title_section1);
  //      break;
  //    case 2:
  //      mTitle = getString(R.string.title_section2);
  //      break;
  //    case 3:
  //      mTitle = getString(R.string.title_section3);
  //      break;
  //  }
 // }

  public void restoreActionBar() {
    ActionBar actionBar = getSupportActionBar();
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
    actionBar.setDisplayShowTitleEnabled(true);
    actionBar.setTitle(mTitle);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    //if (!mNavigationDrawerFragment.isDrawerOpen()) {
       //Only show items in the action bar relevant to this screen
      // if the drawer is not showing. Otherwise, let the drawer
      // decide what to show in the action bar.
      getMenuInflater().inflate(R.menu.my_stocks, menu);
      restoreActionBar();
      return true;
    //}
    //return super.onCreateOptionsMenu(menu);
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
        new String[]{ QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE, QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE},
        QuoteColumns.ISCURRENT + " = ?",
        new String[]{"1"},
        null);
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data){
    mCursorAdapter.swapCursor(data);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader){
    mCursorAdapter.swapCursor(null);
  }

  /**
   * A placeholder fragment containing a simple view.
   */
  //public static class PlaceholderFragment extends Fragment {
  //  /**
  //   * The fragment argument representing the section number for this
  //   * fragment.
  //   */
  //  private static final String ARG_SECTION_NUMBER = "section_number";
  //
  //  /**
  //   * Returns a new instance of this fragment for the given section
  //   * number.
  //   */
  //  public static PlaceholderFragment newInstance(int sectionNumber) {
  //    PlaceholderFragment fragment = new PlaceholderFragment();
  //    Bundle args = new Bundle();
  //    args.putInt(ARG_SECTION_NUMBER, sectionNumber);
  //    fragment.setArguments(args);
  //    return fragment;
  //  }
  //
  //  public PlaceholderFragment() {
  //  }
  //
  //  @Override
  //  public View onCreateView(LayoutInflater inflater, ViewGroup container,
  //      Bundle savedInstanceState) {
  //    View rootView = inflater.inflate(R.layout.fragment_my_stocks, container, false);
  //    return rootView;
  //  }
  //
  //  @Override
  //  public void onAttach(Activity activity) {
  //    super.onAttach(activity);
  //    ((MyStocksActivity) activity).onSectionAttached(
  //        getArguments().getInt(ARG_SECTION_NUMBER));
  //  }
  //}
}
