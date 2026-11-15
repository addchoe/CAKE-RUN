# 제출용 release 빌드와 서명

팀명: 가도니와 애준이
앱 이름: 중요한 건 흔들리지 않는 마음

제출 APK는 `pleosRelease`이며 `simRelease`는 SDK가 없는 보조 시연용입니다. release는 디버깅을 비활성화하고 별도 RSA 키로 서명합니다. R8 최적화는 SDK의 런타임 콜백을 검증하기 전이므로 비활성화했습니다.

## 재빌드

JDK 17 / Android SDK 35를 준비하고 다음 환경 변수를 로컬에서 설정합니다. 비밀번호와 키 파일은 GitHub에 올리지 않습니다. `.env`, `.jks`, `.keystore`는 Git에서 제외합니다.

```sh
export CAKERUN_STORE_FILE="/absolute/path/to/submission-release.jks"
export CAKERUN_STORE_PASSWORD="본인의 저장소 비밀번호"
export CAKERUN_KEY_ALIAS="submission"
export CAKERUN_KEY_PASSWORD="본인의 키 비밀번호"
./gradlew :app:assemblePleosRelease :app:assembleSimRelease \
  :app:testSimReleaseUnitTest :app:lintPleosRelease :app:lintSimRelease
```

이 환경 변수가 없으면 Gradle은 서명된 제출 APK를 만들지 않습니다. `release-unsigned.apk`를 그대로 제출하거나 debug 서명을 release 서명으로 재사용하지 마세요.

빌드 결과:

- `app/build/outputs/apk/pleos/release/app-pleos-release.apk`
- `app/build/outputs/apk/sim/release/app-sim-release.apk`

서명된 APK를 각각 주 제출 경로와 보조 시뮬레이션 경로로 복사합니다. 현재 배포 파일은 Android API 28 이상에서 APK v3 서명 검증을 통과했습니다. 키 로테이션 이력은 아직 없으며, 향후 같은 패키지 업데이트에는 같은 서명키 또는 정식 로테이션 절차가 필요합니다.

## 설치 확인

```sh
adb install -r "apk/가도니와 애준이_중요한 건 흔들리지 않는 마음_release.apk"
adb shell am start -n com.cakerun.app/com.cakerun.app.MainActivity
```

기존 debug 버전은 다른 서명키를 사용하므로 업데이트 설치가 거절될 수 있습니다. 그 경우 기존 앱을 제거한 뒤 설치해야 합니다. 제거는 앱 데이터를 삭제합니다.

## 제출 시 기록

접수 이메일에 저장소 URL, `apk/`의 주 APK 경로, 제출 직전 `main` 커밋 번호를 함께 적습니다. 아직 실행하지 않은 Pleos 에뮬레이터 검증이나 화면 규격을 완료한 것으로 기재하지 않습니다.
