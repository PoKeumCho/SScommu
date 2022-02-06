package com.sscommu.pokeumcho;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SimpleDialog extends DialogFragment {

    private String mMessage;

    public SimpleDialog(String message) {
        mMessage = message;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        // Use the Builder class because this dialog has simple UI
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(mMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Nothing happening here
                    }
                });

        // Create the object and return it
        return builder.create();
    }
}
