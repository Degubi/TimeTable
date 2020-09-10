import 'dart:math';

const dayList = [ 'Hétfő', 'Kedd', 'Szerda', 'Csütörtök', 'Péntek' ];
const typeList = [ 'Előadás', 'Gyakorlat' ];

String formatTime(DateTime time) => time.hour.toString().padLeft(2, '0') + ':' + time.minute.toString().padLeft(2, '0');

class Class {
    String day;
    DateTime startTime;
    DateTime endTime;
    String name;
    String type;
    String room;
    bool unImportant;

    Class.fromJson(dynamic jsonData, DateTime now):
        this.day = jsonData['day'],
        this.startTime = parseTimeFrom(jsonData['startTime'], now),
        this.endTime = parseTimeFrom(jsonData['endTime'], now),
        this.name = jsonData['name'].substring(0, min(jsonData['name'].length as int, 15)),
        this.type = jsonData['type'],
        this.room = jsonData['room'],
        this.unImportant = jsonData['unImportant'];

    Class.fromMap(Map<String, String> map, DateTime now):
        this.day = map['Day'],
        this.startTime = parseTimeFrom(map['Start Time'], now),
        this.endTime = parseTimeFrom(map['End Time'], now),
        this.name = map['Name'].substring(0, min(map['Name'].length, 15)),
        this.type = map['Type'],
        this.room = map['Room'],
        this.unImportant = false;

    static DateTime parseTimeFrom(String time, DateTime now) {
        final split = time.split(':');
        return DateTime(now.year, now.month, now.day, int.parse(split[0]), int.parse(split[1]));
    }
}