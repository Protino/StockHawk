package com.calgen.stockhawk.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.calgen.stockhawk.R;
import com.calgen.stockhawk.data.Contract;
import com.calgen.stockhawk.data.PrefUtils;
import com.calgen.stockhawk.sync.QuoteSyncJob;
import com.calgen.stockhawk.utils.BasicUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SwipeRefreshLayout.OnRefreshListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        StockAdapter.StockAdapterOnClickHandler, ViewTreeObserver.OnPreDrawListener {

    private static final int STOCK_LOADER = 1;
    //@formatter:off
    @BindView(R.id.recycler_view) RecyclerView recyclerView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.error) TextView error;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.progressBarLayout) LinearLayout progressBarLayout;
    @BindView(R.id.content_main) LinearLayout contentLayout;
    //@formatter:on
    private StockAdapter adapter;

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        adapter = new StockAdapter(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setRefreshing(true);
        if (savedInstanceState == null)
            QuoteSyncJob.initialize(this);
        setUpDeletionOnSlide();
        getSupportActionBar().setTitle(R.string.app_name);
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);
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
                int stockSize = PrefUtils.removeStock(MainActivity.this, symbol);
                // TODO: 11/28/2016 Add undo action
                getContentResolver().delete(Contract.Quote.makeUriForStock(symbol), null, null);
                if (stockSize == 0) {
                    adapter.setCursor(null);
                    updateEmptyView();
                }
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
    public void onClick(String symbol, StockAdapter.StockViewHolder viewHolder) {
        Uri stockUri = Contract.Quote.makeUriForStock(symbol);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.setData(stockUri);
        Pair<View, String> priceViewPair = Pair.create((View) viewHolder.price, getString(R.string.stock_price_transition_name));
        Pair<View, String> changeViewPair = Pair.create((View) viewHolder.change, getString(R.string.stock_change_transition_name));
        ActivityOptionsCompat optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this, priceViewPair, changeViewPair);

        ActivityCompat.startActivity(this, intent, optionsCompat.toBundle());
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
        adapter.setCursor(data);
        updateEmptyView();
        if (data.getCount() == 0)
            supportStartPostponedEnterTransition();
        else
            recyclerView.getViewTreeObserver().addOnPreDrawListener(this);
    }

    private void updateEmptyView() {
        swipeRefreshLayout.setVisibility(View.GONE);
        hideLoadingLayout(false);
        int message;

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
                    message = R.string.loading_data;
                    break;
            }
            if (!BasicUtils.isNetworkUp(this)) message = R.string.error_no_network;
            if(PrefUtils.getStocks(this).size()==0) message = R.string.error_no_stocks;
            error.setText(message);
            error.setVisibility(View.VISIBLE);
        } else if (!BasicUtils.isNetworkUp(this)) {
            Toast.makeText(this, R.string.toast_no_connectivity, Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            error.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
        hideLoadingLayout(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        swipeRefreshLayout.setRefreshing(false);
        adapter.setCursor(null);
        updateEmptyView();
    }

    void addStock(String symbol) {
        if (symbol != null && !symbol.isEmpty()) {

            if (BasicUtils.isNetworkUp(this)) {
                swipeRefreshLayout.setRefreshing(true);
            } else {
                String message = getString(R.string.toast_stock_added_no_connectivity, symbol);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
            PrefUtils.addStock(this, symbol);
            QuoteSyncJob.syncImmediately(this);
        }
    }

    public void button(View view) {
        new AddStockDialog().show(getFragmentManager(), "StockDialogFragment");
    }

    private void setDisplayModeMenuItemIcon(MenuItem item) {
        item.setIcon(PrefUtils.getDisplayMode(this)
                .equals(getString(R.string.pref_display_mode_absolute_key))
                ? R.drawable.ic_percentage
                : R.drawable.ic_dollar);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_stock_status_key))) {
            updateEmptyView();
        }
    }

    @Override
    public boolean onPreDraw() {
        if (PrefUtils.getStocks(this).size() != 0) {
            if (recyclerView.getChildCount() > 0) {
                recyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
                supportStartPostponedEnterTransition();
                hideLoadingLayout(true);
                return true;
            }
            return false;
        }
        supportStartPostponedEnterTransition();
        return true;
    }

    private void hideLoadingLayout(boolean b) {
        contentLayout.setVisibility(b ? View.VISIBLE : View.GONE);
        progressBarLayout.setVisibility(b ? View.GONE : View.VISIBLE);
    }
}
