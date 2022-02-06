package com.sscommu.pokeumcho;

import com.github.tlaabs.timetableview.Schedule;
import com.github.tlaabs.timetableview.Time;
import com.github.tlaabs.timetableview.TimetableView;

import java.util.ArrayList;

public class TimetableManager {

    private TimetableView mTimetable;
    private ArrayList<Schedule> schedules;

    private String mId;
    private String mClassName;
    private String mClassInfo;

    public TimetableManager(TimetableView timetable) {

        mTimetable = timetable;
    }

    /* Delete schedule */
    public void remove(int idx) { mTimetable.remove(idx); }
    public void clear() { mTimetable.removeAll(); }  // remove all items

    public void addSchedule(MySchedule mySchedule) {

        schedules = null;
        schedules = new ArrayList<Schedule>();

        mId = String.valueOf(mySchedule.getId());
        mClassName = mySchedule.getClassName();
        mClassInfo = mySchedule.getClassInfo();

        String classTime = mySchedule.getClassTime();
        String[] classTimes = classTime.split(",");
        if (classTimes.length > 0) {
            for (int i = 0; i < classTimes.length; i++) {
                String[] subClassTimes = classTimes[i].split("/");
                addOneDaySchedule(subClassTimes[0], subClassTimes[1]);
            }
        }

        mTimetable.add(schedules);
    }

    private void addOneDaySchedule(String day, String time) {

        int dayInt = convertDay(day);
        if (dayInt == -1) return;   // no match

        String[] timeStrings = time.split("-");
        Integer[] timeInts = new Integer[2];
        timeInts[0] = Integer.parseInt(timeStrings[0]);
        if (timeStrings.length == 1)
            timeInts[1] = Integer.parseInt(timeStrings[0]);
        else
            timeInts[1] = Integer.parseInt(timeStrings[1]);
        convertTime(timeInts);

        Schedule schedule = new Schedule();
        schedule.setClassTitle(mClassName);
        schedule.setClassPlace(mClassInfo);
        schedule.setProfessorName(mId);         // Set identification number for deletion
        schedule.setDay(dayInt);
        schedule.setStartTime(new Time(timeInts[0],0));
        schedule.setEndTime(new Time(timeInts[1],0));
        schedules.add(schedule);
    }

    private int convertDay(String day) {

        if (day.equals("월")) return 0;
        else if (day.equals("화")) return 1;
        else if (day.equals("수")) return 2;
        else if (day.equals("목")) return 3;
        else if (day.equals("금")) return 4;

        return -1;  // no match
    }

    private void convertTime(Integer[] times) {

        for (int i = 0; i < times.length; i++)
            for (int j = 1; j <= 12; j++)
                if (times[i] == j) {
                    times[i] = (8 + i + j);
                    break;
                }
    }
}
