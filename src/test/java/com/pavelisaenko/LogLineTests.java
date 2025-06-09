package com.pavelisaenko;

import org.junit.jupiter.api.Test;

import java.time.DateTimeException;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class LogLineTests {

    @Test
    void parseCorrectTimeString() {
        LogLine logLine = new LogLine(
                "20/12/2000:20:20:20",
                0,
                0
        );
        Instant expected = Instant.parse("2000-12-20T20:20:20.00Z");

        assertEquals(expected, logLine.getTimestamp());
    }

    @Test
    void parseEmptyTimeString(){
        assertThrows(DateTimeException.class, () -> new LogLine(
                "",
                0,
                0
        ));
    }

    @Test
    void equalsByValueTest(){
        LogLine a = new LogLine(
                "20/12/2000:20:20:20",
                200,
                43.345632
        );
        LogLine b = new LogLine(
                "20/12/2000:20:20:20",
                200,
                43.345632
        );
        assert(a.equals(b));
    }

    @Test
    void notEqualsTest(){
        LogLine a = new LogLine(
                "20/12/2000:20:20:21",
                200,
                43.345632
        );
        LogLine b = new LogLine(
                "20/12/2000:20:20:20",
                200,
                43.345632
        );
        assert(!a.equals(b));
    }

    @Test
    void hashCodeTest(){
        LogLine a = new LogLine(
                "20/12/2000:20:20:21",
                200,
                43.345632
        );
        LogLine b = new LogLine(
                "20/12/2000:20:20:21",
                200,
                43.345632
        );
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCodeFailTest(){
        LogLine a = new LogLine(
                "20/12/2000:20:20:21",
                200,
                43.345632
        );
        LogLine b = new LogLine(
                "20/12/2000:20:20:20",
                200,
                43.345632
        );
        assertNotEquals(a.hashCode(), b.hashCode());
    }

}
