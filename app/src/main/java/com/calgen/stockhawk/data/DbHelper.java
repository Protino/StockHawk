package com.calgen.stockhawk.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.calgen.stockhawk.data.Contract.Quote;


public class DbHelper extends SQLiteOpenHelper {


    static final String NAME = "StockHawk.db";
    private static final int VERSION = 3;


    public DbHelper(Context context) {
        super(context, NAME, null, VERSION);
    }

    //Lifecycle start
    @Override
    public void onCreate(SQLiteDatabase db) {
        String builder = "CREATE TABLE " + Quote.TABLE_NAME + " (" +
                Quote._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Quote.COLUMN_SYMBOL + " TEXT NOT NULL, " +
                Quote.COLUMN_PRICE + " REAL NOT NULL, " +
                Quote.COLUMN_ABSOLUTE_CHANGE + " REAL NOT NULL, " +
                Quote.COLUMN_PERCENTAGE_CHANGE + " REAL NOT NULL, " +
                Quote.COLUMN_MONTH_HISTORY + " TEXT NOT NULL, " +
                Quote.COLUMN_DAY_HISTORY + " TEXT NOT NULL, " +
                Quote.COLUMN_WEEK_HISTORY + " TEXT NOT NULL, " +
                Quote.COLUMN_STOCK_EXCHANGE + " TEXT NOT NULL, " +
                Quote.COLUMN_STOCK_NAME + " TEXT NOT NULL, " +
                Quote.COLUMN_DAY_HIGHEST + " REAL NOT NULL, " +
                Quote.COLUMN_DAY_LOWEST + " REAL NOT NULL, " +
                "UNIQUE (" + Quote.COLUMN_SYMBOL + ") ON CONFLICT REPLACE);";

        db.execSQL(builder);

    }
//Lifecycle end

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(" DROP TABLE IF EXISTS " + Quote.TABLE_NAME);

        onCreate(db);
    }
}
