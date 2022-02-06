package com.sscommu.pokeumcho;

/* -------------------------------------------------------------------------------------------
    Copyright 2019 tlaabs

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
------------------------------------------------------------------------------------------- */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.TimetableView;

import java.util.ArrayList;

public class ScheduleMainFragment extends Fragment
        implements TimetableView.OnStickerSelectedListener {

    private String mUserId;

    private ArrayList<MySchedule> mUserSchedules;
    private ArrayList<CollegeSchedule> mCollegeSchedules;

    private TimetableView timetable;
    private TimetableManager timetableManager;

    private ImageButton addBtn;

    ActivityResultLauncher<Intent> mLauncher;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) // Do when show
            getAndDisplaySchedules();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Data which sent from activity
        mUserId = getArguments().getString("USER_ID");

        setLauncher();

        View view = inflater.inflate(R.layout.content_main_schedule,
                container, false);

        mUserSchedules = new ArrayList<MySchedule>();
        mCollegeSchedules = new ArrayList<CollegeSchedule>();

        // timeTable UI settings
        timetable = view.findViewById(R.id.timetable);
        timetable.setOnStickerSelectEventListener(this);
        timetableManager = new TimetableManager(timetable);

        addBtn = view.findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { startScheduleActivity(); }
        });

        return view;
    }

    // OnStickerSelectedListener is invoked when clicked by user.
    // idx is used to edit or delete.
    @Override
    public void OnStickerSelected(int idx, ArrayList<Schedule> schedules) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setMessage(schedules.get(0).getClassTitle() + " 수업을 시간표에서 삭제하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteSchedule(schedules.get(0).getProfessorName(), idx, false);
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void getAndDisplaySchedules() {

        if (isNetworkAvailable()) {
            GetScheduleTask getScheduleTask = new GetScheduleTask(
                    mUserId, mUserSchedules, mCollegeSchedules, timetableManager);
            getScheduleTask.execute();
        } else {
            // Handle network not available.
            Toast.makeText(getActivity(),
                    R.string.network_not_available,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteSchedule(String idString, int idx, boolean showErrorMsg) {

        if (isNetworkAvailable()) {

            DeleteScheduleTask deleteScheduleTask
                    = new DeleteScheduleTask(this, mUserId,
                    mUserSchedules, mCollegeSchedules, timetableManager,
                    idString, idx);

            if (deleteScheduleTask.isValid())
                deleteScheduleTask.execute();

        } else {
            // Handle network not available.
            if (showErrorMsg)
                Toast.makeText(getActivity(),
                        R.string.network_not_available,
                        Toast.LENGTH_SHORT).show();
        }
    }

    private void setLauncher() {

        mLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_CANCELED) {
                            getAndDisplaySchedules();
                        }
                    }
                });
    }

    private void startScheduleActivity() {

        Intent scheduleIntent = new Intent(getActivity(), ScheduleActivity.class);
        mLauncher.launch(scheduleIntent);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
