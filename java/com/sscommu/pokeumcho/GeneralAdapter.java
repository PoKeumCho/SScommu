package com.sscommu.pokeumcho;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GeneralAdapter
        extends RecyclerView.Adapter<GeneralAdapter.ListItemHolder> {

    private List<General> mGeneralList;
    private AppCompatActivity mActivity;

    public GeneralAdapter(AppCompatActivity activity, List<General> generalList) {
        mActivity = activity;
        mGeneralList = generalList;
    }

    @NonNull
    @Override
    public GeneralAdapter.ListItemHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.general_listitem, parent, false);

        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull GeneralAdapter.ListItemHolder holder, int position) {

        General general = mGeneralList.get(position);

        holder.mAccountImageView.setImageResource(general.getUserAccountImg());
        holder.mNicknameTxt.setText(general.getUserNickname());
        holder.mDateTxt.setText(general.getDateLikeEveryTime());
        holder.mTextTxt.setText(general.getText());
        holder.mLikesTxt.setText(general.getLikesString());
        holder.mCommentsTxt.setText(general.getCommentsString());

        if (general.getImgCount() > 0)
            holder.showImageCounter(general.getImgCount());

        if (!general.getCategoryName().equals(""))
            holder.showCategoryName(general.getCategoryName());

        /** Recycler View showing wrong data - Quick fix */
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return mGeneralList.size();
    }

    // Used as the holder for each list item.
    public class ListItemHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView mAccountImageView;
        TextView mNicknameTxt;
        TextView mDateTxt;
        TextView mTextTxt;
        TextView mLikesTxt;
        TextView mCommentsTxt;
        ImageView mImageIcon;
        TextView mImgTxt;
        TextView mCategoryNameTxt;

        public ListItemHolder(View view) {
            super(view);

            mAccountImageView = view.findViewById(R.id.accountImageView);
            mNicknameTxt = view.findViewById(R.id.nicknameTxt);
            mDateTxt = view.findViewById(R.id.dateTxt);
            mTextTxt = view.findViewById(R.id.textTxt);
            mLikesTxt = view.findViewById(R.id.likesTxt);
            mCommentsTxt = view.findViewById(R.id.commentsTxt);
            mImageIcon = view.findViewById(R.id.imageIcon);
            mImgTxt = view.findViewById(R.id.imgTxt);
            mCategoryNameTxt = view.findViewById(R.id.categoryNameTxt);

            mImageIcon.setVisibility(View.GONE);
            mImgTxt.setVisibility(View.GONE);
            mCategoryNameTxt.setVisibility(View.GONE);

            view.setClickable(true);
            view.setOnClickListener(this);
        }

        // 이미지 파일이 존재하는 경우
        public void showImageCounter(int count) {
            mImageIcon.setVisibility(View.VISIBLE);
            mImgTxt.setVisibility(View.VISIBLE);

            mImgTxt.setText(String.valueOf(count));
        }

        // MyGeneralArticleActivity 관련
        // 카테고리명을 사용자 화면에 띄운다.
        public void showCategoryName(String categoryName) {
            mCategoryNameTxt.setVisibility(View.VISIBLE);
            mCategoryNameTxt.setText(categoryName);
        }

        @Override
        public void onClick(View v) {

            if (mActivity instanceof GeneralActivity)
                ((GeneralActivity) mActivity).showGeneral(getAdapterPosition());
            else if (mActivity instanceof MyGeneralArticleActivity)
                ((MyGeneralArticleActivity) mActivity).showGeneral(getAdapterPosition());
        }

    }

}
