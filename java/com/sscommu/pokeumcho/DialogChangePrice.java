package com.sscommu.pokeumcho;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogChangePrice extends DialogFragment {

    private int mCurrentPrice;
    MyTradeArticleActivity mMyTradeArticleActivity;

    public DialogChangePrice(
            MyTradeArticleActivity myTradeArticleActivity, int currentPrice) {

        mMyTradeArticleActivity = myTradeArticleActivity;
        mCurrentPrice = currentPrice;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_change_price, null);

        final EditText editTextPrice = dialogView.findViewById(R.id.editTextPrice);
        final TextView priceErrorTxt = dialogView.findViewById(R.id.priceErrorTxt);
        final Button btnOk = dialogView.findViewById(R.id.btnOk);

        editTextPrice.setText(String.valueOf(mCurrentPrice));
        priceErrorTxt.setVisibility(View.GONE);

        editTextPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {

                int price;
                try { price = Integer.parseInt(s.toString().trim()); }
                catch (Exception exc) { price = -1; }

                if (price < 0 || price > 1000000)
                    priceErrorTxt.setVisibility(View.VISIBLE);
                else
                    priceErrorTxt.setVisibility(View.GONE);
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int price;
                try {
                    price = Integer.parseInt(editTextPrice.getText().toString().trim());
                } catch (Exception exc) { price = -1; }

                if (price < 0 || price > 1000000) {
                    priceErrorTxt.setVisibility(View.VISIBLE);
                } else {
                    if (price != mCurrentPrice)
                        mMyTradeArticleActivity.changePrice(price);
                    dismiss();
                }
            }
        });

        builder.setView(dialogView);
        return builder.create();
    }
}
