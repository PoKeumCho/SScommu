package com.sscommu.pokeumcho;

import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TradeAdapter
        extends RecyclerView.Adapter<TradeAdapter.ListItemHolder> {

    private final String URL_PREFIX = "https://sscommu.com/file/images/trade/";

    private List<Trade> mTradeList;

    private TradeMainFragment mFragment;
    private MyTradeArticleActivity mActivity;

    private ArrayList<AsyncTask> mAsyncTaskList;

    public TradeAdapter(TradeMainFragment tradeMainFragment,
                        List<Trade> tradeList,
                        ArrayList<AsyncTask> asyncTaskList) {

        mActivity = null;
        mFragment = tradeMainFragment;
        mTradeList = tradeList;
        mAsyncTaskList = asyncTaskList;
    }

    public TradeAdapter(MyTradeArticleActivity myTradeArticleActivity,
                          List<Trade> tradeList,
                          ArrayList<AsyncTask> asyncTaskList) {

        mFragment = null;
        mActivity = myTradeArticleActivity;
        mTradeList = tradeList;
        mAsyncTaskList = asyncTaskList;
    }

    @NonNull
    @Override
    public TradeAdapter.ListItemHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View itemView = (mFragment != null) ?
                /* TradeMainFragment */
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.trade_listitem, parent, false) :
                /* MyTradeArticleActivity */
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.mytrade_listitem, parent, false);

        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull TradeAdapter.ListItemHolder holder, int position) {

        final int index = position;
        Trade trade = mTradeList.get(position);

        if (trade.getRepresentativeBitmap() == null) {
            RecyclerViewBitmapLoadTask bitmapLoadTask
                    = new RecyclerViewBitmapLoadTask(
                            URL_PREFIX, holder.mImageView, trade, mAsyncTaskList);
            bitmapLoadTask.execute();
        } else {
            holder.mImageView.setImageBitmap(trade.getRepresentativeBitmap());
        }

        holder.mTitleTxt.setText(trade.getTitle());
        holder.mCampusTxt.setText(trade.getCampusString());
        holder.mDateTxt.setText(trade.getDateLikeEveryTime());
        holder.mPriceTxt.setText(trade.getPriceString());

        if (mActivity != null) {    /* MyTradeArticleActivity */
            holder.mBtnMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogTradeMenu dialog
                            = new DialogTradeMenu(mActivity, index);
                    dialog.show(mActivity.getSupportFragmentManager(), "123");
                }
            });
        }

        /** Recycler View showing wrong data - Quick fix */
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() { return mTradeList.size(); }


    public class ListItemHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView mImageView;
        TextView mTitleTxt;
        TextView mCampusTxt;
        TextView mDateTxt;
        TextView mPriceTxt;

        Button mBtnMenu;
        ImageButton mBtnExpel;

        public ListItemHolder(@NonNull View view) {
            super(view);
            mImageView = view.findViewById(R.id.imageView);
            mTitleTxt = view.findViewById(R.id.titleTxt);
            mCampusTxt = view.findViewById(R.id.campusTxt);
            mDateTxt = view.findViewById(R.id.dateTxt);
            mPriceTxt = view.findViewById(R.id.priceTxt);

            // To make an ImageView with rounded corners
            mImageView.setClipToOutline(true);

            if (mActivity != null)
                mBtnMenu = view.findViewById(R.id.btnMenu);

            if (mFragment != null) {
                mBtnExpel = view.findViewById(R.id.btnExpel);
                mBtnExpel.setVisibility(View.VISIBLE);
                mBtnExpel.setOnClickListener(this);

                view.setClickable(true);
                view.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {

            if (mFragment != null) {
                if (v.getId() == R.id.btnExpel)
                    mFragment.expelTrade(getAdapterPosition());
                else
                    mFragment.showTrade(getAdapterPosition());
            }
        }
    }
}
