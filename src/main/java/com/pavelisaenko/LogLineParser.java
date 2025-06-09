package com.pavelisaenko;

public class LogLineParser {

    public static LogLine parseLogLine(String line){
        String[] params = line.split(" ");
        if (params.length != 14) throw new IllegalArgumentException("log string must contain 14 fields");

        return new LogLine(params[3].substring(1), Integer.parseInt(params[8]), Double.parseDouble(params[10]));
    }
}
