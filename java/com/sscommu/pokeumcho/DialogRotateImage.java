package com.sscommu.pokeumcho;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

public class DialogRotateImage extends DialogFragment {

    private UploadImage mUploadImage;

    /**
     *  UPLOAD 버튼을 누르지 않고 뒤로 가기를 누른 경우에 대처하기 위해서
     *  Dialog 내에서 Activity 값을 변경 시킨다.
     */
    private ArrayList<UploadImage> mUploadImageList;
    private ConstraintLayout mImageLayout;
    private ImageView mImageView;

    public DialogRotateImage(UploadImage uploadImage,
                             ArrayList<UploadImage> uploadImageList,
                             ConstraintLayout imageLayout,
                             ImageView imageView) {

        mUploadImage = uploadImage;
        mUploadImageList = uploadImageList;
        mImageLayout = imageLayout;
        mImageView = imageView;
    }

    public DialogRotateImage(UploadImage uploadImage,
                             ImageView imageView) {

        mUploadImage = uploadImage;
        mUploadImageList = null;
        mImageLayout = null;
        mImageView = imageView;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        androidx.appcompat.app.AlertDialog.Builder builder
                = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_rotate_image, null);

        final ImageView imageView = dialogView.findViewById(R.id.imageView);
        final ImageButton btnRotate = dialogView.findViewById(R.id.btnRotate);
        final Button btnOk = dialogView.findViewById(R.id.btnOk);

        imageView.setImageBitmap(mUploadImage.getBitmap());

        builder.setView(dialogView);

        /** 회전하기 버튼을 누를 경우 */
        btnRotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // 이미지를 회전시킨다.
                mUploadImage.rotateBitmap();

                // Dialog 창의 ImageView 설정
                imageView.setImageBitmap(mUploadImage.getBitmap());
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /** Activity 데이터 변경 */
                if (mImageLayout != null)
                    mImageLayout.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(mUploadImage.getBitmap());
                if (mUploadImageList != null)
                    mUploadImageList.add(mUploadImage);

                dismiss();
            }
        });

        /* Only for ChatRoomActivity */
        if (mImageLayout == null)
            mImageView.setImageBitmap(mUploadImage.getBitmap());

        return builder.create();
    }
}
