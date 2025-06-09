package com.pavelisaenko;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LogLineParserJavaTests {
    @Test
    void readCorrectLine(){

        String example = "92.168.32.181 - - [14/06/2017:16:47:02 +1000] \"PUT /rest/v1.4/documents?zone=default&_rid=39b3d39 HTTP/1.1\" 200 2 97.679409 \"-\" \"@list-item-updater\" prio:0";

        LogLine expected = new LogLine(
                "14/06/2017:16:47:02",
                200,
                97.679409
        );

        assertEquals(expected, LogLineParser.parseLogLine(example));
    }
}
