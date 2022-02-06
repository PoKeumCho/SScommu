package com.sscommu.pokeumcho;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MyDate {

    public static final long MILLI_OF_YEAR = 365L * 24L * 60L * 60L * 1000L;
    public static final long MILLI_OF_DAY = 24L * 60L * 60L * 1000L;

    /* default */
    private static final String MY_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat MY_FORMAT
            = new SimpleDateFormat("yyyy년 M월 d일 a KK:mm",
            Locale.US); // AM, PM을 영어로 표기한다.

    private Date mDate;
    private Calendar mCalendar;

    /**
     * Constructor
     * @param date : example) "2022-01-05 16:30:12"
     */
    public MyDate(String date) {
        setDate(date, MY_PATTERN);
    }
    public MyDate(String date, String pattern) {
        setDate(date, pattern);
    }

    private void setDate(String date, String pattern) {

        SimpleDateFormat simpleFormatter = new SimpleDateFormat(pattern);
        ParsePosition startPos = new ParsePosition(0);
        mDate = simpleFormatter.parse(date, startPos);

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(mDate);
    }


    public String getFormattedDate() {
        return MY_FORMAT.format(mDate);
    }

    public String getFormattedDate(SimpleDateFormat simpleFormatter) {
        return simpleFormatter.format(mDate);
    }

    /**
     *
     * @param now : new Date()
     * @return
     *      - 1년이 지난 경우 : n년 전
     *      - 다른 날짜인 경우 : MM/dd
     *      - 1분이 지나지 않은 경우 : 방금
     *      - 1시간이 지나지 않은 경우 : n분 전
     *      - 동일한 날짜인 경우 : a KK:mm
     *
     */
    public String getDateLikeEveryTime(Date now) {

        String result = "";
        SimpleDateFormat simpleFormatter;

        Calendar calNow = Calendar.getInstance();
        calNow.setTime(now);

        long diffMillis = calNow.getTimeInMillis() - mCalendar.getTimeInMillis();
        int diffYears = (int)(diffMillis / MILLI_OF_YEAR);

        if (diffYears < 1) {    /* 1년이 지나지 않은 경우 */

            if ((calNow.get(Calendar.YEAR) == mCalendar.get(Calendar.YEAR))
                    && (calNow.get(Calendar.MONTH) == mCalendar.get(Calendar.MONTH))
                    && calNow.get(Calendar.DATE) == mCalendar.get(Calendar.DATE)) {
                /* 동일한 날짜에 작성된 경우 */

                int diffMinute = (int)(diffMillis / 60000);

                if (diffMinute < 1) {
                    result = "방금";
                } else if (diffMinute < 60) {
                    result = String.valueOf(diffMinute) + "분 전";
                } else {
                    simpleFormatter = new SimpleDateFormat("a KK:mm", Locale.US);
                    result = getFormattedDate(simpleFormatter);
                }
            } else {    /* 날짜가 다른 경우 */
                simpleFormatter = new SimpleDateFormat("MM/dd");
                result = getFormattedDate(simpleFormatter);
            }
        } else {    /* 1년이 지난 경우 */
            result = String.valueOf(diffYears) + "년 전";
        }

        return result;
    }

    /** 현재 시간으로부터 일주일 경과 여부를 반환한다. */
    public boolean isWeekPassed() {

        Calendar calNow = Calendar.getInstance();
        calNow.setTime(new Date());

        long diffMillis = calNow.getTimeInMillis() - mCalendar.getTimeInMillis();
        int diffDays = (int)(diffMillis / MILLI_OF_DAY);

        if (diffDays < 7)
            return false;
        else
            return true;
    }

    /** 동일한 날짜인 경우 true 반환 */
    public boolean compareDate(String date) {

        MyDate compareTo = new MyDate(date);
        if (mCalendar.get(Calendar.YEAR) == compareTo.mCalendar.get(Calendar.YEAR)
                && (mCalendar.get(Calendar.MONTH) == compareTo.mCalendar.get(Calendar.MONTH))
                && mCalendar.get(Calendar.DATE) == compareTo.mCalendar.get(Calendar.DATE))
            return true;
        else
            return false;
    }

    /** "2022년 1월 26일 수요일" 형식의 문자열 반환 */
    public String getChatDate() {

        DateFormat formatter
                = DateFormat.getDateInstance(DateFormat.FULL, Locale.KOREA);
        return formatter.format(mDate);
    }

    public String getChatTimestamp() {

        SimpleDateFormat simpleFormatter;
        simpleFormatter = new SimpleDateFormat("a KK:mm", Locale.US);
        return getFormattedDate(simpleFormatter);
    }
}
