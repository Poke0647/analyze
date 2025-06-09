package com.pavelisaenko;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

public class IntervalProcessor {
    private Instant beginTime;
    private Instant endTime;
    private int errorsCounter = 0;
    private int totalCounter = 0;


    /**
     * Initialises and sets variables to 0;
     * Begins new failing interval
     * @param beginTime first time-point of failing Interval
     */
    public void beginNewInterval(Instant beginTime){
        this.beginTime = beginTime;
        endTime = null;
        errorsCounter = 0;
        totalCounter = 0;
    }

    /**
     * Sets ending point to Interval and prints Interval
     * @param endTime last time-point of failing Interval
     */
    public void endInterval(Instant endTime){
        this.endTime = endTime;
        printInterval();
    }

    /**
     * Increments total counter.
     */
    public void considerCorrectLogLine(){
        totalCounter++;
    }

    /**
     * Increments total and error counters.
     */
    public void considerFailedLogLine(){
        errorsCounter++;
        totalCounter++;
    }

    /**
     * @return {@code availability} of current interval in percents
     */
    private double getAvailability(){
        return Math.round(100 * (totalCounter - errorsCounter) / (double)totalCounter * 10) / 10.0;
    }

    /**
     *  Puts interval to {@link System#out}
     */
    private void printInterval(){
        System.out.println(getInfo());
    }

    /**
     * {@code HH:mm:ss} DateTime format
     */
    private static final DateTimeFormatter STRING_TO_INSTANT_FORMATTER =
            new DateTimeFormatterBuilder().appendPattern("HH:mm:ss")
                    .toFormatter();

    /**
     * Formats timestamp to String for output
     * @param timestamp time stamp of Interval
     * @return time stamp string
     */
    public static String formatTimeInstantToString(Instant timestamp){
        OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(timestamp, ZoneId.of("UTC"));
        return offsetDateTime.format(STRING_TO_INSTANT_FORMATTER);
    }

    public String getInfo(){
        return formatTimeInstantToString(beginTime) + " " + formatTimeInstantToString(endTime) + " " + getAvailability();
    }
}
