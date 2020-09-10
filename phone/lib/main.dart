import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'timetable.dart';
import 'editor.dart';
import 'utils.dart';

void main() {
    runApp(ClassTable());
    SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeLeft,
        DeviceOrientation.landscapeRight,
    ]);
}

class ClassTable extends StatelessWidget {

    @override
    Widget build(BuildContext context) {
        return MaterialApp(
            home: Scaffold(
                appBar: AppBar(
                    title: Text('TimeTable'),
                    actions: [ PopupMenuButton(
                        onSelected: onMenuItemSelected,
                        itemBuilder: createMenuButtons)
                    ]
                ),
                body: SingleChildScrollView(child: TimeTable())
            )
        );
    }

    void onMenuItemSelected(String item) {
        print(item);
    }

    List<PopupMenuItem<String>> createMenuButtons(BuildContext ctx) {
        return ['Logout', 'Settings'].map((k) =>
                PopupMenuItem<String>(
                  value: k,
                  child: Text(k)
                )).toList();
    }
}

class EditorForm extends StatelessWidget {

    final Class selectedClass;
    final void Function(Map<String, String>) onSaveFunction;

    EditorForm(this.selectedClass, this.onSaveFunction);

    @override
    Widget build(BuildContext context) {
        final navigator = Navigator.of(context);

        return MaterialApp(
            home: Scaffold(
                appBar: AppBar(
                    title: Text('Edit Class'),
                    leading: IconButton(
                        icon: Icon(Icons.arrow_back, color: Colors.black),
                        onPressed: () => navigator.pop()
                    )
                ),
                body: SingleChildScrollView(
                    child: Container(
                        margin: EdgeInsets.all(24),
                        child: ClassEditor(selectedClass, onSaveFunction, navigator)
                    )
                )
            )
        );
    }
}