## Letöltés, futtatás:
  - https://drive.google.com/uc?id=1oIVd7ITG6z5dTI27mlJz7o5Tki5wVFsR&export=download
  - Unzipelni (érdemes pl. Program Files-ba)
  - Ha van Python telepítve akkor shortcut.py és a parancsikonon keresztül futtatni
  - Ha nincs Python telepítve akkor parancsikont kell létrehozni a jar-fájlhoz és a célt kell ilyen formátumra megcsinálni:  
  "C:\Program Files\TimeTable\bin\javaw.exe" -jar "C:\Program Files\TimeTable\TimeTable.jar"  
  Itt az első paraméter a javaw.exe helye ami a bin mappában van  
  A második paraméter pedig a TimeTable.jar helye
  - Ha van java 13+ telepítve akkor lehet jar-t is dupla kattintani
  - Lehet cmd-ből futtatni: bin\javaw -jar TimeTable.jar

## Buildeléshez: 
  - Kell lennie JDK-nak PATH-on (jlink, javac, jar)
  - Pythonnak telepítve (build script)
