package com.sam_chordas.android.stockhawk.rest;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperAdapter;
import com.sam_chordas.android.stockhawk.touch_helper.ItemTouchHelperViewHolder;
import com.sam_chordas.android.stockhawk.touch_helper.OnStartDragListener;

/**
 * Created by sam_chordas on 10/6/15.
 *  Credit to skyfishjy gist:
 *    https://gist.github.com/skyfishjy/443b7448f59be978bc59
 * for the code structure
 */
public class QuoteCursorAdapter extends CursorRecyclerViewAdapter<QuoteCursorAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{
  private Context mContext;
  //private final OnStartDragListener mDragListener;
  private boolean isPercent;
  public QuoteCursorAdapter(Context context, Cursor cursor){
    super(context, cursor);
    //mDragListener = dragListener;
    mContext = context;
  }

  //public static class ViewHolder extends RecyclerView.ViewHolder{
  //  public ViewHolder(View view){
  //    super(view);
  //  }
  //}

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
    View itemView = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.list_item_quote, parent, false);
    ViewHolder vh = new ViewHolder(itemView);
    return vh;
  }

  @Override
  public void onBindViewHolder(final ViewHolder viewHolder, final Cursor cursor){
    viewHolder.symbol.setText(cursor.getString(cursor.getColumnIndex("symbol")));
    viewHolder.bidPrice.setText(cursor.getString(cursor.getColumnIndex("bid_price")));
    viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
    isPercent = true;
    viewHolder.change.setOnClickListener(new View.OnClickListener(){
      @Override public void onClick(View v) {
        if (isPercent) {
          viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("value_change")));
        }
        else{
          viewHolder.change.setText(cursor.getString(cursor.getColumnIndex("percent_change")));
        }
        isPercent = !isPercent;
      }
    });
  }

  @Override public boolean onItemMove(int fromPosition, int toPosition) {
    return false;
  }

  @Override public void onItemDismiss(int position) {

  }

  @Override public int getItemCount() {
    return super.getItemCount();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder
      implements ItemTouchHelperViewHolder{
    public final TextView symbol;
    public final TextView bidPrice;
    public final TextView change;
    public ViewHolder(View itemView){
      super(itemView);
      symbol = (TextView) itemView.findViewById(R.id.stock_symbol);
      bidPrice = (TextView) itemView.findViewById(R.id.bid_price);
      change = (TextView) itemView.findViewById(R.id.change);
    }

    @Override
    public void onItemSelected(){
      itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear(){
      itemView.setBackgroundColor(0);
    }
  }
}
