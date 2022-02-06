package com.sscommu.pokeumcho;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScheduleAdapter
        extends RecyclerView.Adapter<ScheduleAdapter.ListItemHolder> {

    private List<CollegeSchedule> mSchedules;
    private ScheduleActivity mActivity;

    public ScheduleAdapter(
            ScheduleActivity scheduleActivity, List<CollegeSchedule> schedules) {

        mActivity = scheduleActivity;
        mSchedules = schedules;
    }


    @NonNull
    @Override
    public ScheduleAdapter.ListItemHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.schedule_listitem, parent, false);

        return new ListItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ScheduleAdapter.ListItemHolder holder, int position) {

        CollegeSchedule schedule = mSchedules.get(position);

        holder.mTxtLine1.setText(schedule.getTextLine1());
        holder.mTxtLine2.setText(schedule.getTextLine2());
        holder.mTxtLine3.setText(schedule.getTextLine3());
        holder.mTxtLine4.setText(schedule.getTextLine4());
        holder.mTxtLine5.setText(schedule.getTextLine5());

        final int index = position;
        holder.mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { mActivity.addScheduleClicked(index); }
        });

        /** Recycler View showing wrong data - Quick fix */
        holder.setIsRecyclable(false);
    }

    @Override
    public int getItemCount() { return mSchedules.size(); }


    public class ListItemHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        LinearLayout mScheduleLayout;

        TextView mTxtLine1;     // className
        TextView mTxtLine2;     // 분반 • classNumber
        TextView mTxtLine3;     // subjects • department • 이수구분
        TextView mTxtLine4;     // campus • classTime
        TextView mTxtLine5;     // roomAndProf

        Button mBtnAdd;

        public ListItemHolder(@NonNull View view) {
            super(view);
            mScheduleLayout = view.findViewById(R.id.scheduleLayout);
            mTxtLine1 = view.findViewById(R.id.txtLine1);
            mTxtLine2 = view.findViewById(R.id.txtLine2);
            mTxtLine3 = view.findViewById(R.id.txtLine3);
            mTxtLine4 = view.findViewById(R.id.txtLine4);
            mTxtLine5 = view.findViewById(R.id.txtLine5);
            mBtnAdd = view.findViewById(R.id.btnAdd);

            view.setClickable(true);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mActivity.showScheduleClicked(getAdapterPosition(), mScheduleLayout);
        }
    }
}
