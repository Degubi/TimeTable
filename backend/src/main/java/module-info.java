open module degubi.timetable.backend {
    requires java.json;
    requires java.json.bind;
    requires java.instrument;

    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.mongodb;
    requires spring.web;

    requires mongo.java.driver;
}