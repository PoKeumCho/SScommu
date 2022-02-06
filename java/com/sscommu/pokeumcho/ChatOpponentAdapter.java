package com.sscommu.pokeumcho;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatOpponentAdapter
        extends RecyclerView.Adapter<ChatOpponentAdapter.ListItemHolder> {

    private List<ChatOpponent> mOpponentList;
    private ChatActivity mChatActivity;

    public ChatOpponentAdapter(
            ChatActivity chatActivity, List<ChatOpponent> opponentList) {

        mChatActivity = chatActivity;
        mOpponentList = opponentList;
    }

    @NonNull
    @Override
    public ChatOpponentAdapter.ListItemHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_opponent_listitem, parent, false);

        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ChatOpponentAdapter.ListItemHolder holder, int position) {

        ChatOpponent opponent = mOpponentList.get(position);

        holder.mTxtOpponentId.setText(opponent.getId());
        if (opponent.hasNewMessage())
            holder.mAlarmImage.setVisibility(View.VISIBLE);

        /** Recycler View showing wrong data - Quick fix */
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() { return mOpponentList.size(); }

    public class ListItemHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        TextView mTxtOpponentId;
        ImageView mAlarmImage;
        Button mBtnBlock;

        public ListItemHolder(@NonNull View itemView) {
            super(itemView);

            mTxtOpponentId = itemView.findViewById(R.id.txtOpponentId);
            mAlarmImage = itemView.findViewById(R.id.alarmImage);
            mBtnBlock = itemView.findViewById(R.id.btnBlock);

            mBtnBlock.setOnClickListener(this);

            itemView.setClickable(true);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnBlock)
                mChatActivity.blockOpponent(mOpponentList.get(getAdapterPosition()).getId());
            else
                mChatActivity.startChat(mOpponentList.get(getAdapterPosition()).getId());
        }
    }
}
