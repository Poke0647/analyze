package com.pavelisaenko;

import picocli.CommandLine;
import picocli.CommandLine.*;

import java.io.*;
import java.time.Duration;

@Command(name = "analyze", mixinStandardHelpOptions = true)
public class Main implements Runnable {
    @Option(names = {"-t", "--response-time"}, required = true, description = "Maximum response time ms")
    double responseThreshold;
    @Option(names = {"-u", "--availability"}, required = true, description = "Minimum availability %")
    double availabilityThreshold;

    public static void main(String[] args) {
        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        runAnalysis(responseThreshold, availabilityThreshold);
    }

    private void runAnalysis(
            double responseThreshold,
            double availabilityThreshold){

        LogLinesProcessor logLinesProcessor = new LogLinesProcessor(
                responseThreshold,
                availabilityThreshold,
                Duration.ofSeconds(1)
        );
        IntervalProcessor intervalProcessor = new IntervalProcessor();

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in))) {

            boolean isWindowFailedBefore;
            String inputLine;
            LogLine logLine = new LogLine();

            while ((inputLine = bufferedReader.readLine()) != null) {
                if (inputLine.isBlank()) continue;

                isWindowFailedBefore = logLinesProcessor.isWindowFails();

                try {
                    logLine = LogLineParser.parseLogLine(inputLine);
                } catch (IllegalArgumentException e) {
                    System.err.printf("Wrong argument for parsing input line: %s", e);
                }

                try {
                    logLinesProcessor.processNewLogLine(logLine);
                } catch (RuntimeException e) {
                    System.err.printf("Processing log line error: %s", e);
                    continue;
                }

                if (logLinesProcessor.getHasLineErrors().test(logLine)) {

                    if (logLinesProcessor.isWindowFails() && !isWindowFailedBefore)
                        intervalProcessor.beginNewInterval(logLinesProcessor.findFirstErrorTimestampInWindow()
                                .orElseThrow(() -> new IllegalStateException("No error element found in window")));
                    intervalProcessor.considerFailedLogLine();
                } else {

                    intervalProcessor.considerCorrectLogLine();
                    if (!logLinesProcessor.isWindowFails() && isWindowFailedBefore)
                        intervalProcessor.endInterval(logLinesProcessor.findLastErrorTimestampInWindow()
                                .orElse(logLinesProcessor.getWindow().getFirst().getTimestamp()));
                }
            }
        } catch (IOException e) {
            System.err.printf("Logs reading error: %s", e.getMessage());
        }
    }
}