package com.sscommu.pokeumcho;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogTradeMenu extends DialogFragment {

    private String[] options = { "거래완료 (글 삭제)", "판매가격 변경", "끌어올리기" };

    private int mIndex;
    private MyTradeArticleActivity mMyTradeArticleActivity;

    public DialogTradeMenu(
            MyTradeArticleActivity myTradeArticleActivity, int index) {

        mMyTradeArticleActivity = myTradeArticleActivity;
        mIndex = index;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switch (options[which]) {
                    case "거래완료 (글 삭제)":
                        mMyTradeArticleActivity.executeAction(
                                MyTradeArticleActivity.Action.DELETE, mIndex);
                        break;
                    case "판매가격 변경":
                        mMyTradeArticleActivity.executeAction(
                                MyTradeArticleActivity.Action.CHANGE_PRICE, mIndex);
                        break;
                    case "끌어올리기":
                        mMyTradeArticleActivity.executeAction(
                                MyTradeArticleActivity.Action.PULL_UP, mIndex);
                        break;
                }
            }
        });
        return dialog.create();
    }
}
