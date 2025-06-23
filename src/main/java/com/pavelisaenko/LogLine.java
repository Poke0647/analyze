package com.pavelisaenko;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Getter
@AllArgsConstructor
@ToString
public class LogLine {

    private final Instant timestamp;
    private final int statusCode;
    private final double responseTime;

    public LogLine(
            String timestamp,
            int statusCode,
            double responseTime
    ) {
        this(
                formatTimeStringToInstant(timestamp),
                statusCode,
                responseTime
        );
    }

    public LogLine(){
        this(
                "01/01/2001:00:00:00",
                200,
                0.1
        );
    }

    /**
     * {@code dd/MM/yyyy:mm:mm:ss} DateTime format
     */
    private static final DateTimeFormatter STRING_TO_INSTANT_FORMATTER =
            new DateTimeFormatterBuilder().appendPattern("dd/MM/yyyy:HH:mm:ss")
                    .parseDefaulting(ChronoField.OFFSET_SECONDS, 0)
                    .toFormatter();

    /**
     * Parses datetime of logs field to Instant with {@link com.pavelisaenko.LogLine#STRING_TO_INSTANT_FORMATTER}
     * @param timestamp string with datetime
     * @return object with general log-line info
     */
    private static Instant formatTimeStringToInstant(String timestamp) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(timestamp, STRING_TO_INSTANT_FORMATTER);
        return offsetDateTime.toInstant();
    }

    @Override
    public boolean equals(Object object){
        if (object == this) return true;
        if (object == null || object.getClass() != this.getClass()) return false;

        LogLine logLine = (LogLine) object;
        return (logLine.getTimestamp() == timestamp ||
                (logLine.getTimestamp() != null && logLine.getTimestamp().equals(timestamp))) &&
                logLine.getStatusCode() == statusCode &&
                logLine.getResponseTime() == responseTime;
    }

    @Override
    public int hashCode(){
        int result = 1;
        result = 31 * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        result = 31 * result + statusCode;
        result = 31 * result + Double.hashCode(responseTime);

        return result;
    }
}
