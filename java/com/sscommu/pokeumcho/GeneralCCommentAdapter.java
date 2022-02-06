package com.sscommu.pokeumcho;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class GeneralCCommentAdapter extends BaseAdapter {

    private String mUserId;

    private List<GeneralComment> mGeneralComments;
    private ViewGeneralActivity mViewGeneralActivity;

    public GeneralCCommentAdapter(ViewGeneralActivity viewGeneralActivity,
                                    ArrayList<GeneralComment> generalComments,
                                    String userId) {

        mViewGeneralActivity = viewGeneralActivity;
        mGeneralComments = generalComments;
        mUserId = userId;
    }

    @Override
    public int getCount() {
        return mGeneralComments.size();
    }

    @Override
    public Object getItem(int position) {
        return mGeneralComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        GeneralComment generalComment = mGeneralComments.get(position);

        ImageView mCommentAccountImageView;
        TextView mCommentNicknameTxt;
        Button mCommentBtnLikes;
        Button mBtnMenu;
        TextView mCommentTextTxt;
        TextView mCommentDateTxt;
        TextView mCommentLikesTxt;

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        view = inflater.inflate(R.layout.general_ccomment_listitem, parent, false);

        mCommentAccountImageView = view.findViewById(R.id.commentAccountImageView);
        mCommentNicknameTxt = view.findViewById(R.id.commentNicknameTxt);
        mCommentBtnLikes = view.findViewById(R.id.commentBtnLikes);
        mBtnMenu = view.findViewById(R.id.btnMenu);
        mCommentTextTxt = view.findViewById(R.id.commentTextTxt);
        mCommentDateTxt = view.findViewById(R.id.commentDateTxt);
        mCommentLikesTxt = view.findViewById(R.id.commentLikesTxt);

        mCommentAccountImageView.setImageResource(generalComment.getUserAccountImg());
        mCommentNicknameTxt.setText(generalComment.getUserNickname());
        mCommentTextTxt.setText(generalComment.getText());
        mCommentDateTxt.setText(generalComment.getFormattedDate());
        mCommentLikesTxt.setText(generalComment.getLikesString());

        if (generalComment.getUserId().equals(mUserId))
            mCommentNicknameTxt.setTextColor(Color.parseColor("#038C9E"));

        mCommentBtnLikes.setOnClickListener(new View.OnClickListener() {
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

        mBtnMenu.setOnClickListener(new View.OnClickListener() {
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

        return view;
    }
}
