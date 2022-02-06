package com.sscommu.pokeumcho;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogCommentMenu extends DialogFragment {

    private int mCommentId;
    private String mUserId;
    private boolean mIsWithdrawalUser;
    private String[] options = { "채팅", "비추", "신고" };

    private ViewGeneralActivity mViewGeneralActivity;

    public DialogCommentMenu(ViewGeneralActivity viewGeneralActivity,
                             int commentId, String userId, boolean isWithdrawalUser) {

        mViewGeneralActivity = viewGeneralActivity;
        mCommentId = commentId;
        mUserId = userId;
        mIsWithdrawalUser = isWithdrawalUser;
    }

    // 본인이 작성한 글인 경우
    public void setIsMine() { options = new String[]{ "게시글 삭제" }; }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setItems(options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        switch (options[which]) {
                            case "채팅":
                                if (!mIsWithdrawalUser) {
                                    Intent chatActivityIntent
                                            = new Intent(getActivity(), ChatActivity.class);
                                    chatActivityIntent.putExtra("OPPONENT_ID", mUserId);
                                    startActivity(chatActivityIntent);
                                } else
                                    mViewGeneralActivity.showWithdrawalUserMessage();
                                break;
                            case "비추":
                                generalCommentHandler(
                                        getResources().getString(R.string.general_dislikes_msg),
                                        ViewGeneralActivity.Action.DISLIKES);
                                break;
                            case "신고":
                                generalCommentHandler(
                                        getResources().getString(R.string.general_expel_msg),
                                        ViewGeneralActivity.Action.EXPEL);
                                break;

                            case "게시글 삭제":
                                generalCommentHandler(
                                        getResources().getString(R.string.general_delete_msg),
                                        ViewGeneralActivity.Action.DELETE);
                                break;
                        }
                    }
                });
        return dialog.create();
    }

    private void generalCommentHandler(String message, ViewGeneralActivity.Action action) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(message)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        generalCommentActionHandler(action);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void generalCommentActionHandler(ViewGeneralActivity.Action action) {
        try {
            mViewGeneralActivity.generalCommentActionHandler(mCommentId, mUserId, action);
        } catch (Exception exc) {
            // Handle exceptions
        }
    }
}
