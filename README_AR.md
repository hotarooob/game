# Cat Mouse Chase - قطة المطاردة

لعبة Android أصلية خفيفة جداً بدون Game Engine.

## التقنية
- Java فقط
- Android Canvas / Custom View
- بدون Unity
- بدون Godot
- بدون LibGDX
- بدون صور خارجية: القطة والفئران مرسومون بالكود

## فكرة اللعبة
تحرك القطة باللمس لتلحق الفئران. كل فأر تمسكه يزيد النقاط. مدة الجولة 60 ثانية.

## طريقة بناء APK
افتح المجلد في Android Studio ثم:

1. انتظر Gradle Sync.
2. من القائمة اختر:
   Build > Build Bundle(s) / APK(s) > Build APK(s)
3. ستجد ملف APK غالباً هنا:
   app/build/outputs/apk/debug/app-debug.apk

أو من Terminal، إذا عندك Gradle و Android SDK مثبتين:

```bash
gradle assembleDebug
```

## مكان الكود الأساسي
`app/src/main/java/com/upfunding/catmousechase/MainActivity.java`

## تعديلات سهلة
داخل `MainActivity.java` يمكنك تعديل:
- عدد الفئران: `int mouseCount = 4;`
- مدة الجولة: `timeLeft = 60f;`
- سرعة القطة: `catSpeed`
- ألوان اللعبة من المتغيرات أعلى `CatChaseView`
