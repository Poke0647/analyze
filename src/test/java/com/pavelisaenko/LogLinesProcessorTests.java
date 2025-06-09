package com.pavelisaenko;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class LogLinesProcessorTests {

    @Test
    public void CorrectLogLinesProcessorNoArgsConstructorTest(){
        LogLinesProcessor logLinesProcessor = new LogLinesProcessor();
        assert (logLinesProcessor.getErrorLinesNumber() == 0);
        assert (logLinesProcessor.getTotalLinesNumber() == 0);
        assert (logLinesProcessor.getResponseThreshold() == 45);
        assert (logLinesProcessor.getAvailabilityThreshold() == 99.9);
        assert (logLinesProcessor.getWindowSize().equals(Duration.ofMinutes(1)));
    }
    @Test
    public void CorrectLogLinesProcessorWithArgsConstructorTest(){
        LogLinesProcessor logLinesProcessor = new LogLinesProcessor(
                49,
                95.2,
                Duration.ofSeconds(20)
        );
        assertEquals(0, logLinesProcessor.getErrorLinesNumber());
        assertEquals (0, logLinesProcessor.getTotalLinesNumber());
        assertEquals (95.2, logLinesProcessor.getAvailabilityThreshold());
        assertEquals (49, logLinesProcessor.getResponseThreshold());
        assertEquals(Duration.ofSeconds(20), logLinesProcessor.getWindowSize());
    }

    @Test
    public void tooBigWindowSizeConstructorTest(){
        assertThrows(IllegalArgumentException.class, () -> new LogLinesProcessor(
                49,
                95.2,
                Duration.ofDays(365)
        ));
    }

    @Test
    public void tooSmallResponseThresholdConstructorTest(){
        assertThrows(IllegalArgumentException.class, () -> new LogLinesProcessor(
                -1,
                99.9,
                Duration.ofMinutes(1)
        ), "Response threshold must be positive");
    }

    @Test
    public void incorrectAvailabilityThresholdConstructorTest(){
        assertThrows(IllegalArgumentException.class, () -> new LogLinesProcessor(
                45,
                103,
                Duration.ofMinutes(1)
        ), "Availability threshold must be in [0.1...100]");

        assertThrows(IllegalArgumentException.class, () -> new LogLinesProcessor(
                45,
                -3,
                Duration.ofMinutes(1)
        ), "Availability threshold must be in [0.1...100]");
    }

    @Test
    public void is5xxErrorTest(){
        LogLinesProcessor logLinesProcessor = new LogLinesProcessor(
                49,
                95.2,
                Duration.ofSeconds(20)
        );
        assertTrue(logLinesProcessor.getIs5xxError().test(500));
        assertFalse(logLinesProcessor.getIs5xxError().test(400));
        assertFalse(logLinesProcessor.getIs5xxError().test(600));
    }

    @Test
    public void hasLineErrorsFalseTest(){
        LogLinesProcessor logLinesProcessor = new LogLinesProcessor(
                49,
                95.2,
                Duration.ofSeconds(20)
        );
        assertFalse(logLinesProcessor.getHasLineErrors().test(
                new LogLine(
                        "14/06/2017:16:47:02",
                        200,
                        40
                )
        ));
    }

    @Test
    public void hasLineErrorsTrueStatusCode(){
        LogLinesProcessor logLinesProcessor = new LogLinesProcessor(
                49,
                95.2,
                Duration.ofSeconds(20)
        );
        assertTrue(logLinesProcessor.getHasLineErrors().test(
                new LogLine(
                        "14/06/2017:16:47:02",
                        501,
                        40
                )
        ));
        assertTrue(logLinesProcessor.getHasLineErrors().test(
                new LogLine(
                        "14/06/2017:16:47:02",
                        200,
                        100
                )
        ));
    }

//    @Test
//    public void getCurrentAvailabilitySuccessTest() throws IllegalAccessException, InvocationTargetException {
//        LogLinesProcessor logLinesProcessor = new LogLinesProcessor(
//                49,
//                95.2,
//                Duration.ofSeconds(20)
//        );
//        Field totalNumber, errorsNumber;
//        try {
//            totalNumber = LogLinesProcessor.class.getDeclaredField("totalLinesNumber");
//            errorsNumber = LogLinesProcessor.class.getDeclaredField("errorLinesNumber");
//            totalNumber.setAccessible(true);
//            errorsNumber.setAccessible(true);
//        } catch (NoSuchFieldException e) {
//            throw new RuntimeException(e);
//        }
//        totalNumber.set(logLinesProcessor, 10000);
//        errorsNumber.set(logLinesProcessor, 5000);
//
//        Method getCurrentAvailability;
//        try {
//            getCurrentAvailability = LogLinesProcessor.class.getDeclaredMethod("getCurrentAvailability");
//            getCurrentAvailability.setAccessible(true);
//        } catch (NoSuchMethodException e) {
//            throw new RuntimeException(e);
//        }
//
//        Assertions.assertEquals(getCurrentAvailability.invoke(logLinesProcessor, getCurrentAvailability));
//
//    }


}
