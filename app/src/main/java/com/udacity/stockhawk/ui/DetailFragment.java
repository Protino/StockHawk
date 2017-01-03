package com.udacity.stockhawk.ui;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomMarkerView;
import com.udacity.stockhawk.utils.Parser;
import com.udacity.stockhawk.utils.XAxisDateFormatter;
import com.udacity.stockhawk.utils.YAxisPriceFormatter;

import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import icepick.State;

/**
 * Created by Gurupad Mamadapur on 12/27/2016.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
    //@formatter:off
    @State public String fragmentDataType;
    @State public String dateFormat;
    @State public int dataColumnPosition;
    @State public Uri stockUri;
    @State public String historyData;
    @State public int LOADER_ID;
    @BindView(R.id.chart) LineChart linechart;
    @BindColor(R.color.white) int white;
    //@formatter:on
    private Context context;

    //Lifecycle start
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Icepick.restoreInstanceState(this, savedInstanceState);
        if (savedInstanceState == null) {
            fragmentDataType = getArguments().getString(getString(R.string.FRAGMENT_DATA_TYPE_KEY));
            if (fragmentDataType.equals(getString(R.string.MONTHLY))) {
                dataColumnPosition = Contract.Quote.POSITION_MONTH_HISTORY;
                dateFormat = "MMM";
                LOADER_ID = 100;
            } else if (fragmentDataType.equals(getString(R.string.WEEKLY))) {
                dataColumnPosition = Contract.Quote.POSITION_WEEK_HISTORY;
                dateFormat = "dd";
                LOADER_ID = 200;
            } else {
                dataColumnPosition = Contract.Quote.POSITION_DAY_HISTORY;
                dateFormat = "dd";
                LOADER_ID = 300;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        context = getContext();
        if (historyData != null)
            setUpLineChart();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        stockUri = getActivity().getIntent().getData();
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }
    //Lifecycle end

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
    }

    private Entry getLastButOneData(List<Entry> dataPairs) {
        if (dataPairs.size() > 2) {
            return dataPairs.get(dataPairs.size() - 2);
        } else {
            return dataPairs.get(dataPairs.size() - 1);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (stockUri != null) {
            return new CursorLoader(
                    context,
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
        if (data.moveToFirst() && historyData == null) {
            //set up the chart with history data
            historyData = data.getString(dataColumnPosition);
            setUpLineChart();
            getActivity().supportStartPostponedEnterTransition();
        }
    }

    private void setUpLineChart() {
        Pair<Float, List<Entry>> result = Parser.getFormattedStockHistory(historyData);
        List<Entry> dataPairs = result.second;
        Float referenceTime = result.first;
        LineDataSet dataSet = new LineDataSet(dataPairs, "");
        dataSet.setColor(white);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(white);
        dataSet.setHighLightColor(white);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        linechart.setData(lineData);

        XAxis xAxis = linechart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter(dateFormat, referenceTime));
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(white);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(white);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxisRight = linechart.getAxisRight();
        yAxisRight.setEnabled(false);

        YAxis yAxis = linechart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineColor(white);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(white);
        yAxis.setTextSize(12f);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext(),
                R.layout.marker_view, getLastButOneData(dataPairs), referenceTime);


        Legend legend = linechart.getLegend();
        legend.setEnabled(false);

        linechart.setMarker(customMarkerView);

        //disable all interactions with the graph
        linechart.setDragEnabled(false);
        linechart.setScaleEnabled(false);
        linechart.setDragDecelerationEnabled(false);
        linechart.setPinchZoom(false);
        linechart.setDoubleTapToZoomEnabled(false);
        Description description = new Description();
        description.setText(" ");
        linechart.setDescription(description);
        linechart.setExtraOffsets(10, 0, 0, 10);
        linechart.animateX(1500, Easing.EasingOption.Linear);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
