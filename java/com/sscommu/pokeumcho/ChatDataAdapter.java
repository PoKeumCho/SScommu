package com.sscommu.pokeumcho;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatDataAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final String URL_PREFIX = "https://sscommu.com/file/images/chat/";

    private List<ChatData> mChatList;
    private ChatRoomActivity mChatRoomActivity;

    private ArrayList<AsyncTask> mAsyncTaskList;

    public ChatDataAdapter(ChatRoomActivity chatRoomActivity,
                           List<ChatData> chatList,
                           ArrayList<AsyncTask> asyncTaskList) {

        mChatRoomActivity = chatRoomActivity;
        mChatList = chatList;
        mAsyncTaskList = asyncTaskList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View view;
        Context context = parent.getContext();
        LayoutInflater inflater
                = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (viewType) {
            case ChatData.ViewType.ME_TEXT:
                view = inflater.inflate(R.layout.item_chat_me_text, parent, false);
                return new MeTextViewHolder(view);
            case ChatData.ViewType.ME_FILE:
                view = inflater.inflate(R.layout.item_chat_me_file, parent, false);
                return new MeFileViewHolder(view);
            case ChatData.ViewType.OTHER_TEXT:
                view = inflater.inflate(R.layout.item_chat_other_text, parent, false);
                return new OtherTextViewHolder(view);
            case ChatData.ViewType.OTHER_FILE:
                view = inflater.inflate(R.layout.item_chat_other_file, parent, false);
                return new OtherFileViewHolder(view);
            default:
                view = inflater.inflate(R.layout.item_chat_date, parent, false);
                return new DateViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ChatData chat = mChatList.get(position);

        /* ME: TEXT */
        if (holder instanceof MeTextViewHolder) {
            ((MeTextViewHolder) holder).setMessage(chat.getContent());
            ((MeTextViewHolder) holder).setTimestamp(getChatTimestamp(chat.getDate()));
        }
        /* ME: FILE */
        else if (holder instanceof MeFileViewHolder) {

            if (chat.getBitmap() == null) {
                RecyclerViewBitmapLoadTask bitmapLoadTask
                        = new RecyclerViewBitmapLoadTask(URL_PREFIX,
                        ((MeFileViewHolder) holder).getImageView(), chat, mAsyncTaskList);
                bitmapLoadTask.execute();
            } else {
                ((MeFileViewHolder) holder).setImage(chat.getBitmap());
            }
            ((MeFileViewHolder) holder).setTimestamp(getChatTimestamp(chat.getDate()));

            ((MeFileViewHolder) holder).getImageView()
                    .setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mChatRoomActivity.clickImageView(chat.getContent());
                }
            });
        }
        /* OTHER: TEXT */
        else if (holder instanceof OtherTextViewHolder) {
            ((OtherTextViewHolder) holder).setMessage(chat.getContent());
            ((OtherTextViewHolder) holder).setTimestamp(getChatTimestamp(chat.getDate()));
        }
        /* OTHER: FILE */
        else if (holder instanceof OtherFileViewHolder) {

            if (chat.getBitmap() == null) {
                RecyclerViewBitmapLoadTask bitmapLoadTask
                        = new RecyclerViewBitmapLoadTask(URL_PREFIX,
                        ((OtherFileViewHolder) holder).getImageView(), chat, mAsyncTaskList);
                bitmapLoadTask.execute();
            } else {
                ((OtherFileViewHolder) holder).setImage(chat.getBitmap());
            }
            ((OtherFileViewHolder) holder).setTimestamp(getChatTimestamp(chat.getDate()));

            ((OtherFileViewHolder) holder).getImageView()
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mChatRoomActivity.clickImageView(chat.getContent());
                        }
                    });
        }
        /* DATE */
        else {
            ((DateViewHolder) holder).setDate(chat.getDate());
        }

        /** Recycler View showing wrong data - Quick fix */
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemViewType(int position) {
        return mChatList.get(position).getViewType();
    }

    @Override
    public int getItemCount() { return mChatList.size(); }


    private String getChatTimestamp(String date) {

        MyDate myDate = new MyDate(date);
        return myDate.getChatTimestamp();
    }


    /** ME: TEXT */
    public class MeTextViewHolder
            extends RecyclerView.ViewHolder {

        TextView mMessage;
        TextView mTimestamp;

        public MeTextViewHolder(@NonNull View itemView) {
            super(itemView);

            mMessage = itemView.findViewById(R.id.chat_message);
            mTimestamp = itemView.findViewById(R.id.chat_timestamp);
        }

        public void setMessage(String message) { mMessage.setText(message); }
        public void setTimestamp(String timestamp) { mTimestamp.setText(timestamp); }
    }

    /** ME: FILE */
    public class MeFileViewHolder
            extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView mTimestamp;

        public MeFileViewHolder(@NonNull View itemView) {
            super(itemView);

            mImage = itemView.findViewById(R.id.chat_image);
            mTimestamp = itemView.findViewById(R.id.chat_timestamp);
        }

        public ImageView getImageView() { return mImage; }
        public void setImage(Bitmap bitmap) { mImage.setImageBitmap(bitmap); }
        public void setTimestamp(String timestamp) { mTimestamp.setText(timestamp); }
    }

    /** OTHER: TEXT */
    public class OtherTextViewHolder
            extends RecyclerView.ViewHolder {

        TextView mMessage;
        TextView mTimestamp;

        public OtherTextViewHolder(@NonNull View itemView) {
            super(itemView);

            mMessage = itemView.findViewById(R.id.chat_message);
            mTimestamp = itemView.findViewById(R.id.chat_timestamp);
        }

        public void setMessage(String message) { mMessage.setText(message); }
        public void setTimestamp(String timestamp) { mTimestamp.setText(timestamp); }
    }

    /** OTHER: FILE */
    public class OtherFileViewHolder
            extends RecyclerView.ViewHolder {

        ImageView mImage;
        TextView mTimestamp;

        public OtherFileViewHolder(@NonNull View itemView) {
            super(itemView);

            mImage = itemView.findViewById(R.id.chat_image);
            mTimestamp = itemView.findViewById(R.id.chat_timestamp);
        }

        public ImageView getImageView() { return mImage; }
        public void setImage(Bitmap bitmap) { mImage.setImageBitmap(bitmap); }
        public void setTimestamp(String timestamp) { mTimestamp.setText(timestamp); }
    }

    /** DATE */
    public class DateViewHolder
            extends RecyclerView.ViewHolder {

        TextView mDate;

        public DateViewHolder(@NonNull View itemView) {
            super(itemView);

            mDate = itemView.findViewById(R.id.text_chat_date);
        }

        public void setDate(String date) { mDate.setText(date); }
    }
}