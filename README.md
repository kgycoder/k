# iOS Launcher

## GitHub Actions 빌드
- `main` 푸시 / PR / 수동 실행(workflow_dispatch) 시 debug·release APK 빌드 → Actions 탭의 Artifacts 에서 다운로드
- `v0.1.0` 같은 태그 푸시 시 release APK 가 GitHub Release 에 자동 첨부
- 릴리스 서명(선택): 저장소 Secrets 에 `KEYSTORE_BASE64`(base64 인코딩한 .jks), `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` 등록. 없으면 debug 키로 서명됩니다.

## 로컬
- Android Studio 로 열기 / `./gradlew assembleDebug`
- minSdk 33 (AGSL). 설치 후 홈 버튼 → "iOS Launcher" 를 기본 홈 앱으로 선택.
- Backdrop 버전: gradle/libs.versions.toml
