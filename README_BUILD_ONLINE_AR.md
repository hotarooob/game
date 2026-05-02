# بناء APK بدون Android Studio

أفضل طريقة سهلة من المتصفح فقط: GitHub Actions.

## الخطوات

1. افتح github.com وسجّل دخولك.
2. اضغط New Repository.
3. سمّه مثلاً: CatMouseChase.
4. ارفع ملفات هذا المشروع كلها إلى المستودع، وليس ملف ZIP نفسه فقط.
5. بعد الرفع، افتح تبويب Actions.
6. اختر Build Android APK.
7. اضغط Run workflow.
8. بعد انتهاء البناء، افتح نتيجة التشغيل وانزل إلى Artifacts.
9. حمّل CatMouseChase-debug-apk.
10. داخل الملف المضغوط ستجد app-debug.apk.

## ملاحظات

- لا تحتاج Android Studio على جهازك.
- البناء يحدث على سيرفرات GitHub.
- هذا APK تجريبي Debug مناسب للتجربة على جهازك.
- للنشر على Google Play لاحقاً نحتاج Signed Release APK أو AAB.
