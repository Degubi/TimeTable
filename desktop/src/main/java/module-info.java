module degubi.timetable {
    requires java.desktop;
    requires java.sql;
    requires java.net.http;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires poi;
    requires poi.ooxml;
    requires com.google.zxing;
    requires com.google.zxing.javase;

    opens timetable to com.fasterxml.jackson.databind;
}