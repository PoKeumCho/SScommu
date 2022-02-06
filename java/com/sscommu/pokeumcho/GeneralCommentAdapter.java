package com.sscommu.pokeumcho;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GeneralCommentAdapter
        extends RecyclerView.Adapter<GeneralCommentAdapter.ListItemHolder> {

    private String mUserId;

    private List<GeneralComment> mGeneralComments;
    private ViewGeneralActivity mViewGeneralActivity;

    public GeneralCommentAdapter(ViewGeneralActivity viewGeneralActivity,
                                    List<GeneralComment> generalComments,
                                    String userId) {

        mViewGeneralActivity = viewGeneralActivity;
        mGeneralComments = generalComments;
        mUserId = userId;
    }

    @NonNull
    @Override
    public GeneralCommentAdapter.ListItemHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.general_comment_listitem, parent, false);

        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GeneralCommentAdapter.ListItemHolder holder,
                                 int position) {

        GeneralComment generalComment = mGeneralComments.get(position);

        holder.mCommentAccountImageView.setImageResource(generalComment.getUserAccountImg());
        holder.mCommentNicknameTxt.setText(generalComment.getUserNickname());
        holder.mCommentTextTxt.setText(generalComment.getText());
        holder.mCommentDateTxt.setText(generalComment.getFormattedDate());
        holder.mCommentLikesTxt.setText(generalComment.getLikesString());
        holder.mCommentCommentsTxt.setText(generalComment.getCommentsString());

        if (generalComment.getUserId().equals(mUserId))
            holder.mCommentNicknameTxt.setTextColor(Color.parseColor("#038C9E"));

        holder.mBtnWriteComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewGeneralActivity.generalCommentWriteComment(
                        holder.mCommentLayout, generalComment.getId());
            }
        });

        holder.mCommentBtnLikes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(mViewGeneralActivity);
                    dialog.setMessage(mViewGeneralActivity.getResources()
                            .getString(R.string.general_likes_msg))
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        mViewGeneralActivity.generalCommentActionHandler(
                                            generalComment.getId(),
                                            generalComment.getUserId(),
                                            ViewGeneralActivity.Action.LIKES);
                                    } catch (Exception exc) {
                                        // Handle exceptions
                                    }
                                }
                            })
                            .setNegativeButton("취소", null)
                            .show();
            }
        });

        holder.mBtnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogCommentMenu dialog
                        = new DialogCommentMenu(mViewGeneralActivity,
                            generalComment.getId(), generalComment.getUserId(),
                            generalComment.isWithdrawalUser());

                if (generalComment.getUserId().equals(mUserId))
                    dialog.setIsMine();

                dialog.show(mViewGeneralActivity.getSupportFragmentManager(), "123");
            }
        });

        GeneralCCommentAdapter adapter
                = new GeneralCCommentAdapter(mViewGeneralActivity,
                    generalComment.getComments(),
                    mUserId);
        holder.mListView.setAdapter(adapter);
        holder.mListView.setDivider(null);

        /** Recycler View showing wrong data - Quick fix */
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() {
        return mGeneralComments.size();
    }

    public class ListItemHolder extends RecyclerView.ViewHolder {

        RelativeLayout mCommentLayout;
        ImageView mCommentAccountImageView;
        TextView mCommentNicknameTxt;
        Button mBtnWriteComment;
        Button mCommentBtnLikes;
        Button mBtnMenu;
        TextView mCommentTextTxt;
        TextView mCommentDateTxt;
        TextView mCommentLikesTxt;
        ImageView mCommentImageView;
        TextView mCommentCommentsTxt;
        ListView mListView;


        public ListItemHolder(@NonNull View view) {
            super(view);

            mCommentLayout = view.findViewById(R.id.commentLayout);
            mCommentAccountImageView = view.findViewById(R.id.commentAccountImageView);
            mCommentNicknameTxt = view.findViewById(R.id.commentNicknameTxt);
            mBtnWriteComment = view.findViewById(R.id.btnWriteComment);
            mCommentBtnLikes = view.findViewById(R.id.commentBtnLikes);
            mBtnMenu = view.findViewById(R.id.btnMenu);
            mCommentTextTxt = view.findViewById(R.id.commentTextTxt);
            mCommentDateTxt = view.findViewById(R.id.commentDateTxt);
            mCommentLikesTxt = view.findViewById(R.id.commentLikesTxt);
            mCommentImageView = view.findViewById(R.id.commentImageView);
            mCommentCommentsTxt = view.findViewById(R.id.commentCommentsTxt);
            mListView = view.findViewById(R.id.listView);

            mBtnWriteComment.setVisibility(View.VISIBLE);
            mCommentImageView.setVisibility(View.VISIBLE);
            mCommentCommentsTxt.setVisibility(View.VISIBLE);
        }
    }
}
