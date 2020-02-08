### Fő program képernyő:
  - Új óra hozzáadás: Dupla kattintás a napra
  - Óra módosítás/törlés/nem fontossá jelölés: Jobb kattintás az órára
  - Következő óra: piros, elmúlt: sárga, következendő: zöld, más nap: kék, nem fontos: fehér
  - Gyakorlat: Vastag betű, Előadás: Normál betű
  - Óra előtti tálca értesítés
  - Névnapok a főképernyőn
  - Idő alapú éjszakai mód  
    <img src="images/base.jpg" width="1000"/>

### Tálca menü:
  - Alvó mód: Óra előtti értesítések kikapcsolása
  - Importálás: Excelből (Neptun órarend 1 hét exportja vagy kurzus exportja) vagy Json
  - Exportálás: Képbe, Excelbe (Neptun órarend formátumban) vagy Json-ba  
   <img src="images/menu.jpg" width="250"/>

### Beállítások menü:
  - Minden szín testreszabható
  - Különféle időpontok/tartamok testreszabhatóak
  - Lehetőség van számítógéppel történő indításra is (háttérben marad a tálcán)  
   <img src="images/settings.jpg" width="350"/>

### Letöltés, futtatás:
  - 'Releases' Github menüfülből le kell szedni a legújabb zip-et
  - Unzipelni (érdemes pl. Program Files-ba)
  - Lekell futtatni a createShortcut.vbs-t
  - Az Asztalon levő parancsikonnal kell futtatni a programot

### Buildeléshez:
  - Kell lennie Pythonnak telepítve és JDK-nak PATH-on, illetve Eclipse-nek vagy Maven-nek telepítve
  - Vagy Eclipse-ből le kell futtatni a ExportLibraries.launch
  - Vagy cmd-ből ha van maven telepítve: 'mvn dependency:copy-dependencies -DoutputDirectory=${project.build.directory}/lib'
  - Le kell futtatni a build.py-t
  - A TimeTable mappába kialakul a Google Drive-on található zip struktúrája