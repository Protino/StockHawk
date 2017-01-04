package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomBundler;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private static final int PAGE_LIMIT = 2;
    private static int LOADER_ID = 0;
    private static Cursor oldCursor;
    @State(CustomBundler.class)
    public Map<Integer, String> fragmentTags = new HashMap<>();
    //@formatter:off
    @State Boolean dataLoaded = false;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.stock_name) TextView tvStockName;
    @BindView(R.id.stock_exchange) TextView tvStockExchange;
    @BindView(R.id.stock_price) TextView tvStockPrice;
    @BindView(R.id.day_highest) TextView tvDayHighest;
    @BindView(R.id.day_lowest)TextView tvDayLowest;
    @BindView(R.id.absolute_change) TextView tvAbsoluteChange;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.tabs) TabLayout tabLayout;
    private Uri stockUri;
    private ViewPagerAdapter viewPagerAdapter;
    //@formatter:on

    //Lifecycle start
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportPostponeEnterTransition();
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stockUri = getIntent().getData();
        setupViewPager();
        tabLayout.setupWithViewPager(viewPager, true);

        //Initialize loader
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private void setupViewPager() {

        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        DetailFragment monthlyStock = new DetailFragment();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), getString(R.string.MONTHLY));
        monthlyStock.setArguments(bundle);

        DetailFragment weeklyStock = new DetailFragment();
        bundle = new Bundle();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), getString(R.string.WEEKLY));
        weeklyStock.setArguments(bundle);

        DetailFragment dailyStock = new DetailFragment();
        bundle = new Bundle();
        bundle.putString(getString(R.string.FRAGMENT_DATA_TYPE_KEY), getString(R.string.DAILY));
        dailyStock.setArguments(bundle);

        viewPagerAdapter.addFragment(dailyStock, "5 DAYS");
        viewPagerAdapter.addFragment(weeklyStock, "5 WEEKS");
        viewPagerAdapter.addFragment(monthlyStock, "5 MONTHS");

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_LIMIT);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    this,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            String stockExchange = data.getString(Contract.Quote.POSITION_EXCHANGE);
            String stockName = data.getString(Contract.Quote.POSITION_NAME);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absolutionChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            Float dayLowest = data.getFloat(Contract.Quote.POSITION_LOWEST);
            Float dayHighest = data.getFloat(Contract.Quote.POSITION_HIGHEST);

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setMaximumFractionDigits(2);
            dollarFormatWithPlus.setMinimumFractionDigits(2);
            dollarFormatWithPlus.setPositivePrefix("+");

            tvStockExchange.setText(stockExchange);
            tvStockName.setText(stockName);
            tvStockPrice.setText(dollarFormat.format(stockPrice));
            tvAbsoluteChange.setText(dollarFormatWithPlus.format(absolutionChange));
            if (dayHighest != -1) {
                tvDayHighest.setText(dollarFormat.format(dayHighest));
                tvDayLowest.setText(dollarFormat.format(dayLowest));
            } else {
                tvDayLowest.setVisibility(View.GONE);
                tvDayHighest.setVisibility(View.GONE);
            }

            if (absolutionChange > 0) {
                tvAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_green);
            } else {
                tvAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_red);
            }

        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        public Fragment getFragment(int position) {
            String tag = fragmentTags.get(position);
            if (tag == null)
                return null;
            return getSupportFragmentManager().findFragmentByTag(tag);
        }
    }
}
