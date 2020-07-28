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
  - 'Releases' Github menüfülből le kell szedni a legújabb telepítő exe-t
  - Telepíteni kell (Ne a program files mappába)
  - Az Asztalon levő parancsikonnal kell futtatni a programot

### Buildeléshez:
  - Kell lennie Pythonnak telepítve
  - Kell lennie Maven-nek telepítve
  - Kell lennie jdk14-nek telepítve (egyenlőre hardcodeolva van az elérési út, jdk15 után javítva lesz)
  - Lekell futtatni a build.py-t