package com.calgen.stockhawk.utils;

import android.content.Context;
import android.widget.TextView;

import com.calgen.stockhawk.R;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * It helps to display a custom texView when the chart is clicked at
 * a certain data point.
 */
public class CustomMarkerView extends MarkerView {

    private final Date date;
    private final SimpleDateFormat dateFormat;
    private final TextView textView;
    private final Entry finalEntry;
    private final Float referenceTime;
    private final DecimalFormat dollarFormat;

    /**
     * Constructor. Sets up the MarkerView with a custom layout resource.
     *
     * @param context        this is need to fetch string resources.
     * @param layoutResource the layout resource to use for the MarkerView.
     * @param referenceTime  value relative to each data value. This is needed to normalize the values back.
     */
    public CustomMarkerView(Context context, int layoutResource, Entry finalEntry, Float referenceTime) {
        super(context, layoutResource);
        textView = (TextView) findViewById(R.id.marker_text);
        dateFormat = new SimpleDateFormat("d MMM y", Locale.getDefault());
        date = new Date();
        this.finalEntry = finalEntry;
        this.referenceTime = referenceTime;
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @Override
    public void refreshContent(Entry e, Highlight highlight) {
        super.refreshContent(e, highlight);
        Float stockValue = e.getY();
        date.setTime((long) (e.getX() + referenceTime));
        String formattedDate = dateFormat.format(date);
        textView.setText(String.format(getContext().getString(R.string.marker_text), dollarFormat.format(stockValue), formattedDate));

        if (e.getX() >= finalEntry.getX()) {
            setOffset(-256, -32);
        } else {
            setOffset(-128, -32);
        }
    }
}
