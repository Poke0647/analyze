package com.pavelisaenko;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Processes log lines. Checks them for errors. Alters counters (total and errors).
 * Enters {@link LogLine} into {@link #window}.
 * Deletes expired records.
 */
@NoArgsConstructor
@Getter
public class LogLinesProcessor {

    /**
     * Maximum size of window. Divides by 100 because of system limitations
     * @see IllegalStateException
     * @implNote Internally {@link ArrayDeque} class calls its
     * private {@code newCapacity(int, int)} method which will
     * throw {@link IllegalStateException} if the requested
     * capacity is more than {@link Integer#MAX_VALUE}
     */
    private final int MAX_WINDOW_SIZE_IN_SECONDS = Integer.MAX_VALUE / 100;

    private double responseThreshold = 45;
    private double availabilityThreshold = 99.9;
    private Duration windowSize = Duration.ofMinutes(1);

    private int totalLinesNumber = 0;
    private int errorLinesNumber = 0;

    private final ArrayDeque<LogLine> window = new ArrayDeque<>((int) windowSize.getSeconds() * 80);

    /**
     * @param windowSize expiration value of lines. Max value is {@link LogLinesProcessor#MAX_WINDOW_SIZE_IN_SECONDS}.
     * @param responseThreshold the delay threshold at which a record is considered to contain an error. Must be positive.
     * @param availabilityThreshold minimum acceptable availability level.
     *                              If current availability is lower of this level – window fails.
     * @exception IllegalArgumentException if {@link #windowSize} is bigger than
     * {@link LogLinesProcessor#MAX_WINDOW_SIZE_IN_SECONDS}.
     * @exception IllegalArgumentException if {@link  #responseThreshold} is not positive
     * @exception IllegalArgumentException if {@link  #availabilityThreshold} is not in [0.1...100]
     */
    public LogLinesProcessor(double responseThreshold, double availabilityThreshold, Duration windowSize){
        if (windowSize.getSeconds() > MAX_WINDOW_SIZE_IN_SECONDS)
            throw new IllegalArgumentException("Maximum window size in seconds is " + MAX_WINDOW_SIZE_IN_SECONDS);

        if (responseThreshold <= 0)
            throw new IllegalArgumentException("Response threshold must be positive");

        if (availabilityThreshold < 0.1 || availabilityThreshold > 100)
            throw new IllegalArgumentException("Availability threshold must be in [0.1...100]");

        this.responseThreshold = responseThreshold;
        this.availabilityThreshold = availabilityThreshold;
        this.windowSize = windowSize;
    }

    /**
     * Verifies 5xx errors. Returns {@code true} if {@code statusCode} in [500...599].
     */
    private final Predicate<Integer> is5xxError = (statusCode) -> 500 <= statusCode && statusCode < 600;

    /**
     * Verifies if LogLine object has any errors.
     */
    private final Predicate<LogLine> hasLineErrors = (logLine) ->
            is5xxError.test(logLine.getStatusCode()) || logLine.getResponseTime() > responseThreshold;

    /**
     * @return availability of current {@link #window}.
     */
    private double getCurrentAvailability(){
        if (totalLinesNumber == 0) return 100.0;
        return 100 * (totalLinesNumber - errorLinesNumber) / (double) totalLinesNumber;
    }

    /**
     * @return true if {@link #window} fails.
     */
    public boolean isWindowFails(){
        return getCurrentAvailability() < availabilityThreshold;
    }


    public Optional<Instant> findFirstErrorTimestampInWindow(){
        for (LogLine logLine : window) {
            if (hasLineErrors.test(logLine)) return Optional.of(logLine.getTimestamp());
        }
        return Optional.empty();
    }


    public Optional<Instant> findLastErrorTimestampInWindow(){
        Iterator<LogLine> logLineIterator = window.descendingIterator();
        LogLine logLine;
        while (logLineIterator.hasNext()){
            logLine = logLineIterator.next();
            if (hasLineErrors.test(logLine))
                return Optional.of(logLine.getTimestamp());
        }
        return Optional.empty();
    }

    /**
     * @param logLine prepared log line
     */
    public void processNewLogLine(LogLine logLine){
        if (logLine == null) throw new IllegalArgumentException("Log line must not be null");

        Instant current = logLine.getTimestamp();

        window.addLast(logLine);
        if (hasLineErrors.test(logLine)) errorLinesNumber++;

        Instant cutoff = current.minus(windowSize);

        while (!window.isEmpty() && window.getFirst().getTimestamp().isBefore(cutoff)) {
            LogLine expired = window.removeFirst();
            if (hasLineErrors.test(expired)) errorLinesNumber--;
        }

        totalLinesNumber = window.size();
    }
}