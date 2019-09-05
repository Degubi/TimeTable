@echo off

jar cvfm TimeTable.jar MANIFEST.MF -C bin assets -C bin timetable -C bin module-info.class

echo Build done...
pause