import 'dart:convert';

import 'package:collection/collection.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'utils.dart';
import 'main.dart';

class TimeTable extends StatefulWidget {

    @override
    State<StatefulWidget> createState() => TimeTableState();
}

class TimeTableState extends State<TimeTable> {
    static final unimportantClassColor = Color.fromRGBO(192, 192, 192, 0.25);
    static final currentClassColor = Color.fromRGBO(255, 69, 69, 1);
    static final upcomingClassColor = Color.fromRGBO(0, 147, 3, 1);
    static final otherDayClassColor = Color.fromRGBO(84, 113, 142, 1);
    static final pastClassColor = Color.fromRGBO(247, 238, 90, 1);
    static final lightGray = Color.fromRGBO(128, 128, 128, 1);
    static final buttonShape = RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(5),
        side: BorderSide(color: Colors.black87)
    );
    static Class currentClass;

    Map<String, List<Class>> classes = Map();

    @override
    void initState() {
        super.initState();
        initClassList();
    }

    void initClassList() async {
        final classesFileContent = await rootBundle.loadString('assets/classes.json');
        final classesData = jsonDecode(classesFileContent) as Map<String, dynamic>;
        final classesObject = classesData['classes'] as List<dynamic>;
        final now = DateTime.now();
        final classList = classesObject.map((e) => Class.fromJson(e, now)).toList();
        final sortedClasses = groupBy(classList, (Class k) => k.day).entries.toList();

        sortedClasses.sort((k, v) => 1);
        sortedClasses.forEach((k) => k.value.sort((l1, l2) => l1.startTime.compareTo(l2.startTime)));

        setState(() => this.classes = Map.fromEntries(sortedClasses));
    }

    @override
    Widget build(BuildContext context) {
        currentClass = null;

        final now = DateTime.now();

        return Column(children: [
                    Row(children: createColumnsForTable(context, now),
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        crossAxisAlignment: CrossAxisAlignment.start)
                    ]
                );
    }

    List<Column> createColumnsForTable(BuildContext context, DateTime now) {
        return classes.keys.map((k) => Column(children: createButtonsForColumn(k, context, now)))
                           .toList();
    }

    List<Widget> createButtonsForColumn(String day, BuildContext context, DateTime now) {
        List<Widget> result = List();
        result.add(RaisedButton(
            onPressed: () => {},
            onLongPress: () => onDayButtonLongPress(day),
            shape: buttonShape,
            child: Text(day)
        ));

        result.add(SizedBox(height: 15));

        final currentClasses = classes[day];
        for(var i = 0; i < currentClasses.length; ++i) {
            result.add(createClassButton(currentClasses[i], now, i, context));
            result.add(SizedBox(height: 10));
        }

        return result;
    }

    Widget createClassButton(Class clazz, DateTime now, int index, BuildContext context) {
        final today = dayList[now.weekday - 1];
        final isToday = clazz.day == today;
        final isBefore = isToday && now.isBefore(clazz.startTime);
        final isAfter = isToday && (now.isAfter(clazz.startTime) || now == clazz.startTime);
        final isNext = currentClass == null && !clazz.unImportant && isBefore || (isToday && now == clazz.startTime);

        if(isNext) {
            currentClass = clazz;
        }

        final buttonText = 'Óra: ${clazz.name}\n' +
                           'Idő: ${formatTime(clazz.startTime)}-${formatTime(clazz.endTime)}\n' +
                           'Típus: ${clazz.type}\n' +
                           'Terem: ${clazz.room}';

        return RaisedButton(
            onPressed: () => {},
            onLongPress: () => onClassButtonLongPress(context, clazz.day, index),
            shape: buttonShape,
            child: Text(buttonText, style: TextStyle(color: clazz.unImportant ? lightGray : Colors.black)),
            color: clazz.unImportant ? unimportantClassColor : isNext ? currentClassColor : isBefore ? upcomingClassColor : isAfter ? pastClassColor : otherDayClassColor
        );
    }

    void onClassButtonLongPress(BuildContext context, String day, int index) async {
        final value = await showMenu(
            context: context,
            position: RelativeRect.fromLTRB(100, 100, 100, 100),
            items: [
            PopupMenuItem(
                value: 0,
                child: Text('Modify')
            ),
            PopupMenuItem(
                value: 1,
                child: Text('Delete')
            ),
            PopupMenuItem(
                value: 2,
                child: Text('Ignore')
            )
            ],
            elevation: 8.0
        );

        final classesForDay = this.classes[day];

        if(value == 0) {
            final clazz = classesForDay[index];
            Navigator.push(context, MaterialPageRoute(
                builder: (context) => EditorForm(clazz, (newData) => updateExistingClass(newData, clazz, day)))
            );
        }else if(value == 1) {
            setState(() => classesForDay.removeAt(index));
        }else if(value == 2) {
            setState(() => classesForDay[index].unImportant = !classesForDay[index].unImportant);
        }
    }

    void updateExistingClass(Map<String, String> newData, Class oldClazz, String day) {
        classes[day].remove(oldClazz);

        final newDay = newData['Day'];
        final newClazz = Class.fromMap(newData, DateTime.now());
        classes[newDay].add(newClazz);

        setState(() {});
    }

    void onDayButtonLongPress(String day) {
        Navigator.push(context, MaterialPageRoute(builder: (context) => EditorForm(null, (k) => {})));
    }
}