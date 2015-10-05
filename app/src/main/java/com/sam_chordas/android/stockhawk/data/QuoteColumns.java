package com.sam_chordas.android.stockhawk.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Created by sam_chordas on 10/5/15.
 */
public class QuoteColumns {
  @DataType(DataType.Type.INTEGER) @PrimaryKey @AutoIncrement
  public static final String _ID = "_id";
  @DataType(DataType.Type.INTEGER) @NotNull
  public static final String PERCENT_CHANGE = "percent_change";
  @DataType(DataType.Type.INTEGER) @NotNull
  public static final String CHANGE = "change";
  @DataType(DataType.Type.INTEGER) @NotNull
  public static final String TRADE_PRICE = "trade_price";
  @DataType(DataType.Type.TEXT) @NotNull
  public static final String CREATED = "created";
}
