open module degubi.timetable.backend {
    requires java.instrument;

    requires spring.beans;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.context;
    requires spring.core;
    requires spring.data.commons;
    requires spring.data.mongodb;
    requires spring.web;

    requires org.mongodb.driver.core;
    requires org.mongodb.driver.sync.client;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    requires jdk.net;
}