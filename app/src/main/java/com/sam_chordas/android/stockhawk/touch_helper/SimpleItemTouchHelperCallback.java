package com.sam_chordas.android.stockhawk.touch_helper;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by sam_chordas on 10/6/15.
 */
public class SimpleItemTouchHelperCallback extends ItemTouchHelper.Callback{
  private final ItemTouchHelperAdapter mAdapter;
  public static final float ALPHA_FULL = 1.0f;

  public SimpleItemTouchHelperCallback(ItemTouchHelperAdapter adapter){
    mAdapter = adapter;
  }

  @Override
  public boolean isItemViewSwipeEnabled(){
    return true;
  }

  @Override
  public boolean isLongPressDragEnabled(){
    return true;
  }

  @Override
  public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder){
    final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
    final int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
    return makeMovementFlags(dragFlags, swipeFlags);
  }

  @Override
  public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder sourceViewHolder, RecyclerView.ViewHolder targetViewHolder){
    if (sourceViewHolder.getItemViewType() != targetViewHolder.getItemViewType()){
      return false;
    }
    mAdapter.onItemMove(sourceViewHolder.getAdapterPosition(), targetViewHolder.getAdapterPosition());
    return true;
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int i){
    mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
  }

  @Override
  public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isActive){
    if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE){
      final float alpha = ALPHA_FULL - Math.abs(dX) /(float) viewHolder.itemView.getWidth();
      viewHolder.itemView.setAlpha(alpha);
      viewHolder.itemView.setTranslationX(dX);
    }else{
      super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isActive);
    }
  }

  @Override
  public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState){
    if (actionState != ItemTouchHelper.ACTION_STATE_IDLE){
      if (viewHolder instanceof ItemTouchHelperViewHolder){
        ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
        itemViewHolder.onItemClear();
      }
    }
  }
}
