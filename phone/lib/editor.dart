import 'utils.dart';

import 'package:flutter/material.dart';

class ClassEditor extends StatefulWidget {

    final Class selectedClass;
    final void Function(Map<String, String>) onSaveFunction;
    final NavigatorState navigator;

    ClassEditor(this.selectedClass, this.onSaveFunction, this.navigator);

    @override
    State<StatefulWidget> createState() => ClassEditorState(selectedClass, onSaveFunction, navigator);
}

class ClassEditorState extends State<ClassEditor> {

    final formKey = GlobalKey<FormState>();
    final outputValues = Map<String, String>();
    final startTimeController = TextEditingController();
    final endTimeController = TextEditingController();
    final void Function(Map<String, String>) onSaveFunction;
    final NavigatorState navigator;

    ClassEditorState(Class selectedClass, this.onSaveFunction, this.navigator) {
        if(selectedClass != null) {   // Null if we are creating a new Class
            outputValues['Name'] = selectedClass.name;
            outputValues['Room'] = selectedClass.room;
            outputValues['Day'] = selectedClass.day;
            outputValues['Type'] = selectedClass.type;
            outputValues['Start Time'] = formatTime(selectedClass.startTime);
            outputValues['End Time'] = formatTime(selectedClass.endTime);
        }else{
            outputValues['Start Time'] = '08:00';
            outputValues['End Time'] = '10:00';
        }

        startTimeController.text = outputValues['Start Time'];
        endTimeController.text = outputValues['End Time'];
    }

    @override
    Widget build(BuildContext context) {
        return Form(
            key: formKey,
            child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                    createTextFormField('Name'),
                    createDropdownButtonFormField('Type', typeList),
                    createDropdownButtonFormField('Day', dayList),
                    createTextFormField('Room'),
                    createTimePicker('Start Time', startTimeController),
                    createTimePicker('End Time', endTimeController),
                    SizedBox(height: 50),
                    RaisedButton(
                        onPressed: () {
                            formKey.currentState.save();
                            onSaveFunction(outputValues);
                            navigator.pop();
                        },
                        child: Text('Save')
                    )
                ]
            )
        );
    }

    Row createTimePicker(String property, TextEditingController controller) {
        return Row(
            children: [
                Flexible(
                    child: TextFormField(
                        enabled: false,
                        controller: controller,
                        decoration: InputDecoration(labelText: property),
                        onSaved: (val) => outputValues[property] = val
                    )
                ),
                SizedBox(width: 30),
                Flexible(child: RaisedButton(
                    onPressed: () async {
                        final oldTimeSplit = outputValues[property].split(':');
                        final newTime = await showTimePicker(
                            context: context,
                            initialTime: TimeOfDay(
                                hour: int.parse(oldTimeSplit[0]),
                                minute: int.parse(oldTimeSplit[1])
                            )
                        );

                        if(newTime != null) {
                            final newValue = '${newTime.hour}:${newTime.minute}';

                            controller.text = newValue;
                            outputValues[property] = newValue;
                        }
                    },
                    child: Text('Pick'))
                )
            ]
        );
    }

    DropdownButtonFormField createDropdownButtonFormField(String property, List<String> items) {
        return DropdownButtonFormField(
            decoration: InputDecoration(labelText: property),
            value: outputValues[property],
            items: items.map((e) => DropdownMenuItem(child: Text(e), value: e)).toList(),
            onChanged: (val) => outputValues[property] = val
        );
    }

    TextFormField createTextFormField(String property) {
        return TextFormField(
            initialValue: outputValues[property],
            decoration: InputDecoration(labelText: property),
            onSaved: (val) => outputValues[property] = val
        );
    }
}