import 'dart:convert';
import 'dart:io';

import 'package:barcode_scan/barcode_scan.dart';
import 'package:http/http.dart' as http;
import 'package:collection/collection.dart';
import 'package:path_provider/path_provider.dart';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

const backendURL = 'https://timetable-backend.herokuapp.com/timetable';
const dayList = [ 'Hétfő', 'Kedd', 'Szerda', 'Csütörtök', 'Péntek', 'Szombat', 'Vasárnap' ];

void main() {
    runApp(Root());
    SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
    ]);
}

String formatTime(DateTime time) => time.hour.toString().padLeft(2, '0') + ':' + time.minute.toString().padLeft(2, '0');


class Root extends StatelessWidget {

    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            title: 'TimeTable',
            home: ClassTable()
        );
    }
}

class ClassTable extends StatefulWidget {

    @override
    State<StatefulWidget> createState() => ClassTableState();
}

class ClassTableState extends State<ClassTable> {
    var unimportantClassColor = Color.fromRGBO(192, 192, 192, 0.25);
    var currentClassColor = Color.fromRGBO(255, 69, 69, 1);
    var upcomingClassColor = Color.fromRGBO(0, 147, 3, 1);
    var otherDayClassColor = Color.fromRGBO(84, 113, 142, 1);
    var pastClassColor = Color.fromRGBO(247, 238, 90, 1);
    var lightGray = Color.fromRGBO(128, 128, 128, 1);
    final buttonShape = RoundedRectangleBorder(
        borderRadius: BorderRadius.circular(5),
        side: BorderSide(color: Colors.black87)
    );
    final classButtonPadding = EdgeInsets.symmetric(vertical: 5);
    final dayColumnPadding = EdgeInsets.symmetric(horizontal: 5);

    Class currentClass;
    Map<String, List<Class>> classes = Map();
    bool hasLocalID = false;
    File settingsFile;

    @override
    Widget build(BuildContext context) {
        currentClass = null;

        final now = DateTime.now();

        return Scaffold(
            appBar: AppBar(
                title: Text('TimeTable'),
                    actions: [ PopupMenuButton(
                        onSelected: onMenuItemSelected,
                        itemBuilder: (ctx) => [
                            PopupMenuItem<int>(
                                child: Text('Importálás QR Kódból'),
                                value: 1,
                                enabled: !hasLocalID
                            ),
                            PopupMenuItem<int>(
                                child: Text('Importálás Azonosítóból'),
                                value: 2,
                                enabled: !hasLocalID
                            ),
                            PopupMenuItem<int>(
                                child: Text('Szinkronizálás'),
                                value: 3,
                                enabled: hasLocalID
                            ),
                            PopupMenuItem<int>(
                                child: Text('Törlés'),
                                value: 4,
                                enabled: hasLocalID
                            )
                        ])
                    ]
                ),
            body: SingleChildScrollView(
                child: Row(
                    children: this.classes.keys.map((k) => createColumnForDay(k, now)).toList(),
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    crossAxisAlignment: CrossAxisAlignment.start
                )
            )
        );
    }

    @override
    void initState() {
        super.initState();
        initFromSettings();
    }

    void initFromSettings() async {
        final docDir = await getApplicationDocumentsDirectory();

        this.settingsFile = File('${docDir.path}/settings.json');

        if(this.settingsFile.existsSync()) {
            final localSettingsObject = jsonDecode(this.settingsFile.readAsStringSync()) as Map<String, dynamic>;

            this.hasLocalID = localSettingsObject.containsKey('cloudID');
            initializeClasses(localSettingsObject);
        }else{
            this.settingsFile.createSync();

            defaultInitializeSettings();
        }
    }

    void onMenuItemSelected(int option) async {
        if(option == 1) {
            final scanResult = await BarcodeScanner.scan();

            if(scanResult.type == ResultType.Barcode) {
                updateClassListFromBackend(scanResult.rawContent);
            }
        }else if(option == 2) {
            final controller = TextEditingController();

            await showDialog(
                context: context,
                child: AlertDialog(
                    contentPadding: EdgeInsets.all(16),
                    content: Row(
                        children: [
                            Expanded(
                                child: TextField(
                                    autofocus: true,
                                    controller: controller
                                )
                            )
                        ]
                    ),
                    actions: [
                        FlatButton(
                            child: Text('Vissza'),
                            onPressed: () => Navigator.pop(context)
                        ),
                        FlatButton(
                            child: Text('Importálás'),
                            onPressed: () {
                                updateClassListFromBackend(controller.text);
                                Navigator.pop(context);
                            }
                        )
                    ]
                )
            );
        }else if(option == 3) {
            final localSettingsObject = jsonDecode(this.settingsFile.readAsStringSync()) as Map<String, dynamic>;

            updateClassListFromBackend(localSettingsObject['cloudID']);
        }else if(option == 4) {
            defaultInitializeSettings();
        }
    }

    void defaultInitializeSettings() {
        const emptyContent = {
            'classes': []
        };

        this.hasLocalID = false;

        initializeClasses(emptyContent);
        this.settingsFile.writeAsString(jsonEncode(emptyContent));
    }

    void updateClassListFromBackend(String id) async {
        showDialog(
            context: context,
            child: AlertDialog(
                title: Text('Szinkronizálás'),
                contentPadding: EdgeInsets.all(16),
                content: Text('Szinkronizálás a felhővel folyamatban')
            )
        );
        final backendResponse = await http.get('$backendURL?id=$id', headers: { 'Content-Type': 'application/json' });

        Navigator.pop(context);

        if(backendResponse.statusCode == 200) {
            this.hasLocalID = true;
            
            final responseSettingsObject = jsonDecode(backendResponse.body) as Map<String, dynamic>;
            final classesObject = initializeClasses(responseSettingsObject);
            final fileContent = {
                'cloudID': id,
                'classes': classesObject
            };

            this.settingsFile.writeAsStringSync(jsonEncode(fileContent));
        }else{
            showDialog(
                context: context,
                child: AlertDialog(
                    title: Text('Hiba'),
                    contentPadding: EdgeInsets.all(16),
                    content: Text("Nem sikerült lekérni az órarendet ehhez az azonosítóhoz: \n'$id'"),
                    actions: [
                        RaisedButton(
                            child: Text('Vissza'),
                            onPressed: () => Navigator.pop(context)
                        )
                    ]
                )
            );
        }
    }

    List<dynamic> initializeClasses(Map<String, dynamic> decodedData) {
        final classesObject = decodedData['classes'] as List<dynamic>;
        final now = DateTime.now();
        final classList = classesObject.map((e) => Class.fromJson(e, now)).toList();
        final sortedClasses = groupBy(classList, (Class k) => k.day).entries.toList();

        sortedClasses.sort((k, v) => 1);
        sortedClasses.forEach((k) => k.value.sort((l1, l2) => l1.startTime.compareTo(l2.startTime)));

        setState(() => this.classes = Map.fromEntries(sortedClasses));

        return classesObject;
    }

    Widget createColumnForDay(String day, DateTime now) {
        return Expanded(
            child: Padding(
                padding: dayColumnPadding,
                child: Column(
                    crossAxisAlignment: CrossAxisAlignment.stretch,
                    children: createButtonsForColumn(day, now)
                )
            )
        );
    }

    List<Widget> createButtonsForColumn(String day, DateTime now) {
        final result = List<Widget>();

        result.add(RaisedButton(
            onPressed: () => {},
            shape: buttonShape,
            child: Text(day)
        ));

        this.classes[day].forEach((k) => result.add(createClassButton(k, now)));
        return result;
    }

    Widget createClassButton(Class clazz, DateTime now) {
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

        return Padding(
            padding: classButtonPadding,
            child: RaisedButton(
                onPressed: () => {},
                shape: buttonShape,
                child: Padding(
                    padding: classButtonPadding,
                    child: Text(buttonText, style: TextStyle(color: clazz.unImportant ? lightGray : Colors.black))
                ),
                color: clazz.unImportant ? unimportantClassColor : isNext ? currentClassColor : isBefore ? upcomingClassColor : isAfter ? pastClassColor : otherDayClassColor
            )
        );
    }
}

class Class {
    final String day;
    final DateTime startTime;
    final DateTime endTime;
    final String name;
    final String type;
    final String room;
    final bool unImportant;

    Class.fromJson(dynamic jsonData, DateTime now):
        this.day = jsonData['day'],
        this.startTime = parseTimeFrom(jsonData['startTime'], now),
        this.endTime = parseTimeFrom(jsonData['endTime'], now),
        this.name = jsonData['name'],
        this.type = jsonData['type'],
        this.room = jsonData['room'],
        this.unImportant = jsonData['unImportant'];

    static DateTime parseTimeFrom(String time, DateTime now) {
        final split = time.split(':');
        return DateTime(now.year, now.month, now.day, int.parse(split[0]), int.parse(split[1]));
    }
}