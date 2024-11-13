# Android-BackgroundDownloadWorker
--> ระบบนี้จะทำงานพื้นหลัง ในการ download ไฟล์ขนาดใหญ่โดยใช้เงื่อนไขครั้งเเรกที่ reboot เครื่องจะไปดึงไฟล์ตาม URL ที่กำหนดมา save ลง
บน เครื่อง device ที่ต้องการ ละสามารถจะทำการดึงข้อมูลซ้ำๆตามช่วงเวลาที่กำหนดได้

** Set Emulator
ถ้าใช้ Android 11 ตั้งค่าเครื่อง Emulator ให้เป็นสถานะ Charging เเละเลือก Charger Connection เป็น AC Charger
เพื่อลดข้อจำกัดในการควบคุมพลังงานของ Service Background

** Step Build Project ** { Window }
ไปที่ Root Project
-> Ex: D:\Documents\PosBackgroundDownloadWorker>
-- Build Debug
-> ใช้คำสั้ง .\gradlew assembleDebug
-> จะได้ไฟล์ APK ที่ : Ex: D:\Documents\PosBackgroundDownloadWorker\app\build\outputs\apk\debug>
-- Build Release
-> ใช้คำสั้ง .\gradlew assembleRelease
-> จะได้ไฟล์ APK ที่ : Ex: D:\Documents\PosBackgroundDownloadWorker\app\build\outputs\apk\release>
-> ไฟล์ APK จะเป็น unsigend ดังนั้นเอาไป sige ก่อน

** Step Install Run Project **
-> build มาเเบบไหนไปที่ โฟลเดอร์นั้น
-> cd D:\Documents\PosBackgroundDownloadWorker\app\build\outputs\apk\release>
-> adb install ".\app-release.apk" or adb install ".\app-debug.apk" ตามเวอชั่นไฟล์ที่ใช้
-> adb shell am start -n pos.background.downloadworker/.MainActivity
-> หลักจาก start app เเล้วไปให้ไปที่หน้าจอ ของ Emulator เพื่อ Allow app ให้รันใน background ได้ (เป็นข้อจำกัดของ android 11 ที่ต้องให้ user ยอมรับการรันพื้นหลัง)
-> adb reboot [ ทำการ reboot เพื่อให้ระบบการ boot มันตรวจจับเเล้วเริ่มทำงาน ]

-- สามารถเช็ค Logs -> โดยใช้ชื่อเเอพในการ filter : pos.background.downloadworker
-- สามารถเช็คไฟล์ที่ download ได้ที่ /storage/emulated/0/Documents/PosData/large.json



