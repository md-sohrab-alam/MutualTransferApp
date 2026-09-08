# MutualTransfer App - Release Checklist

## 🔐 Keystore Configuration
- [x] Keystore file kept **off git** (`mutual-transfer-keystore.jks` is gitignored)
- [x] Local signing via gitignored `keystore.properties` (copy from `keystore.properties.example`)
- [x] CI signing via GitHub Actions secrets: `SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`
- [x] Signing configuration in `app/build.gradle.kts` (loads `keystore.properties` only)

## 📱 App Configuration
- [x] Application ID: `com.shikshak.transfer`
- [x] Version Code: 1
- [x] Version Name: "1.0"
- [x] Min SDK: 26 (Android 8.0)
- [x] Target SDK: 35 (Android 15)
- [x] Compile SDK: 35

## 🔥 Firebase Configuration
- [x] `google-services.json` present and configured
- [x] Firebase Auth enabled
- [x] Firebase Firestore enabled
- [x] Firebase Messaging enabled
- [x] Project ID: `mutualtransferapp-2be66`

## 🛡️ Security & Privacy
- [x] Network security config for HTTPS only
- [x] ProGuard rules configured
- [x] Code obfuscation enabled for release
- [x] Resource shrinking enabled
- [x] FileProvider configured for secure file sharing

## 📋 Required Steps Before Release

### 1. Local signing (never commit)

Copy `keystore.properties.example` to `keystore.properties` and fill in store/key passwords. Keep `*.jks` and this file off git.

CI uses repository secrets only — see [`docs/SECURITY.md`](docs/SECURITY.md).

### 2. Test Release Build
```bash
./gradlew assembleRelease
```

### 3. Verify Release APK
```bash
./gradlew installRelease
```

### 4. Test All Features
- [ ] Phone authentication
- [ ] Profile creation and editing
- [ ] Transfer request creation
- [ ] Matching functionality
- [ ] Contact sharing
- [ ] Language switching
- [ ] Push notifications
- [ ] About dialog with clickable email

### 5. Firebase Console Setup
- [ ] Enable Google Play App Signing (recommended)
- [ ] Configure Firebase Dynamic Links (if needed)
- [ ] Set up Firebase Analytics
- [ ] Configure Firebase Crashlytics

### 6. Google Play Console
- [ ] Create app listing
- [ ] Upload signed APK or AAB
- [ ] Add app screenshots
- [ ] Write app description
- [ ] Set up content rating
- [ ] Configure pricing and distribution

## 🚀 Build Commands

### Debug Build
```bash
./gradlew assembleDebug
./gradlew installDebug
```

### Release Build
```bash
./gradlew assembleRelease
./gradlew installRelease
```

### Bundle for Play Store
```bash
./gradlew bundleRelease
```

## 📊 App Size Optimization
- [x] ProGuard enabled
- [x] Resource shrinking enabled
- [x] Unused resources removed
- [x] Images optimized

## 🔍 Testing Checklist
- [ ] Install on different Android versions (8.0+)
- [ ] Test on different screen sizes
- [ ] Verify all permissions work
- [ ] Test offline functionality
- [ ] Verify Firebase integration
- [ ] Test language switching
- [ ] Verify sharing functionality
- [ ] Test notification delivery

## 📝 Release Notes Template
```
Version 1.0.0
- Initial release of MutualTransfer app
- Teacher profile management
- Transfer request creation and matching
- Bilingual support (English/Hindi)
- Push notifications
- Contact sharing via WhatsApp
- Secure authentication via phone number
```

## ⚠️ Important Notes
1. **Never commit keystore passwords to version control**
2. **Keep keystore file secure and backed up**
3. **Test thoroughly before release**
4. **Monitor Firebase console for any issues**
5. **Set up crash reporting for production**

## 🎯 Next Steps
1. Confirm GitHub Actions secrets are set (`SIGNING_KEY`, `KEY_STORE_PASSWORD`, `ALIAS`, `KEY_PASSWORD`)
2. Merge to `master`/`main` so CI produces signed APK + AAB
3. Test the Play Store AAB before publishing 