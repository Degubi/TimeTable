from subprocess import call
from shutil import rmtree
from os import rename

# We need to generate a runtime by hand because some of the libraries are not modularized
jlinkCommand = (r'jlink --output ./runtime/ '
                 '--no-man-pages '
                 '--no-header-files '
                 '--add-modules java.base,java.desktop,java.sql,jdk.charsets,java.net.http,jdk.crypto.ec,java.logging '
                 '--compress=2')

print('Generating runtime')
call(jlinkCommand)

print('Copying libraries')
call('mvn dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=lib', shell = True)

print('Creating TimeTable.jar')
call('mvn package', shell = True)
rename('target/TimeTable_Desktop-1.0.jar', 'lib/TimeTable.jar')

print('Creating installer file')
call((r'jpackage --runtime-image runtime -i lib --main-class timetable.Main --main-jar TimeTable.jar '
      r'--name TimeTable --vendor Degubi --description TimeTable --icon icon.ico '
      r'--win-per-user-install --win-dir-chooser --win-shortcut'))

print('Cleaning up')
rename('TimeTable-1.0.exe', 'TimeTableInstaller.exe')
rmtree('lib')
rmtree('runtime')

print('\nDone!')