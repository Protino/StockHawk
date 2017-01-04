package com.calgen.stockhawk.utils;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * Created by Gurupad Mamadapur on 12/26/2016.
 */
public class YAxisPriceFormatter implements IAxisValueFormatter {
    private final DecimalFormat dollarFormat;

    public YAxisPriceFormatter() {
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
        dollarFormat.setMaximumFractionDigits(0);
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return String.format(dollarFormat.format(value), value);
    }
}
