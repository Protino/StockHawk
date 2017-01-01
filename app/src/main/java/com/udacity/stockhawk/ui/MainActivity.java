package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        StockAdapter.StockAdapterOnClickHandler {

    private static final int STOCK_LOADER = 1;
    //@formatter:off
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.error) TextView error;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    //@formatter:on
    private StockAdapter adapter;

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        adapter = new StockAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        onRefresh();

        QuoteSyncJob.initialize(this);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

        setUpDeletionOnSlide();

        getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    protected void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_settings, menu);
        MenuItem item = menu.findItem(R.id.action_change_units);
        setDisplayModeMenuItemIcon(item);
        return true;
    }

    @Override
    protected void onDestroy() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
//Lifecycle end

    private void setUpDeletionOnSlide() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                String symbol = adapter.getSymbolAtPosition(viewHolder.getAdapterPosition());
                PrefUtils.removeStock(MainActivity.this, symbol);
                // TODO: 11/28/2016 Add undo action
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                updateEmptyView();
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_change_units) {
            PrefUtils.toggleDisplayMode(this);
            setDisplayModeMenuItemIcon(item);
            adapter.notifyDataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(String symbol) {
        Uri stockUri = Contract.Quote.makeUriForStock(symbol);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.setData(stockUri);
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        QuoteSyncJob.syncImmediately(this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                Contract.Quote.uri,
                Contract.Quote.QUOTE_COLUMNS,
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView();
        adapter.setCursor(data);
    }

    private void updateEmptyView() {
        swipeRefreshLayout.setVisibility(View.GONE);
        int message = R.string.empty_stock_list;

        if (adapter.getItemCount() == 0) {
            @QuoteSyncJob.StockStatus int status = PrefUtils.getStockStatus(this);
            switch (status) {
                case QuoteSyncJob.STOCK_STATUS_EMPTY:
                    message = R.string.error_no_stocks;
                    break;
                case QuoteSyncJob.STOCK_STATUS_SERVER_DOWN:
                    message = R.string.error_server_down;
                    break;
                case QuoteSyncJob.STOCK_STATUS_SERVER_INVALID:
                    message = R.string.error_server_invalid;
                    break;
                case QuoteSyncJob.STOCK_STATUS_UNKNOWN:
                    message = R.string.empty_stock_list;
                    break;
                default:
                    message = R.string.empty_stock_list;
                    break;
            }
            if (!networkUp()) message = R.string.error_no_network;
        } else if (!networkUp()) {
            message = R.string.error_no_network;
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        error.setText(message);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        updateEmptyView();
        adapter.setCursor(null);
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (networkUp()) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }

            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    private boolean networkUp() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    public void button(View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        if (PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))) {
            item.setIcon(R.drawable.ic_percentage);
        } else {
            item.setIcon(R.drawable.ic_dollar);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_stock_status_key))) updateEmptyView();
    }
}
