# Android Studio / Pleos Emulator 실행

2026-11-15에 확인한 [공식 설치 안내](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/setup-connect-sdk-emulator)를 기준으로 작성했습니다.

## 일반 Android에서 먼저 시연

Android Studio → Open → CAKE-RUN → SDK 35 설치 → Gradle JDK 17 → Sync → `simDebug` 선택 → API 28 이상 AVD 또는 기기에서 Run. Android Studio가 없는 경우 동봉된 sim APK를 Android 기기에 설치할 수 있습니다. Mac에서 APK 자체를 직접 실행할 수는 없습니다.

## Pleos 환경

1. Android Studio → Tools → SDK Manager → SDK Update Sites → `+`.
2. 이름은 `Pleos Connect System Image`, URL은 아래 중 운영체제에 맞게 입력합니다.

   - Mac: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-arm64-sys-img.xml`
   - Windows/Linux: `https://nexus-playground.pleos.ai/repository/raw-releases/connect-avd/2.x/crp-sys-img.xml`

3. SDK Platforms → Show Package Details → Android API 34 안의 Pleos 이미지 설치 → Android Studio 재시작.
4. Tools → Device Manager → Create Device → Automotive → Pleos Connect v2.1.0.
5. Advanced Settings → Startup / Default boot를 **Cold**, RAM을 **4GB**로 설정합니다. Device 탭에서 API 34의 `Pleos Connect v2.1.0 APIs` 이미지를 선택합니다.
6. Device Manager에서 AVD 실행. Build Variants를 `pleosDebug`로 바꾸고 Run합니다.

공식 최소 minSdk는 28, Gradle은 8.0 이상입니다. 이 프로젝트는 minSdk 28 / compileSdk 35 / targetSdk 34 / Gradle 8.9 / AGP 8.7.3을 사용합니다. **에뮬레이터 API 34와 compileSdk 35는 서로 다른 설정입니다.**

[설정 문서](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/configure-pleos-connect-emulator)는 VHAL 사용을 위해 cold boot를 요구합니다. 이미지와 호스트 그래픽 지원 조건도 확인하세요. 이 앱은 STT를 사용하지 않으므로 마이크 설정은 필요하지 않습니다.

## Virtual Console

공식 [Virtual Console 안내](https://document.pleos.ai/docs/connect/guide/getting-started/application-development/use-vehicle-emulator)에 따라:

```sh
adb devices
adb forward tcp:8088 tcp:8088
```

PC 브라우저에서 `http://127.0.0.1:8088/`을 엽니다. 장치가 여러 개면 `adb -s <serial> forward tcp:8088 tcp:8088`으로 대상을 지정합니다.

Emulation Controls → Motion에서 브레이크 상태를 바꾸고 앱 상단의 `브레이크 ON/OFF (실제 SDK)`를 확인합니다. 권한/서비스가 없으면 사용 불가 상태가 표시되고 **케이크는 가상 주행을 계속**합니다. Console 속도나 스티어링을 바꾸어도 이 MVP의 케이크 물리량은 바뀌지 않습니다.

준비 화면의 `공식 예제 목적지 경로 요청`은 NaviHelper가 연결된 뒤 누릅니다. 요청 직후 성공으로 처리하지 않고 `onRoutingCompleted` 또는 `onError`를 기다립니다. 목적지/잔여 거리/시간은 SDK 응답을 상단에 표시합니다. 내비게이션 앱의 데이터·서비스·지원 여부에 따라 콜백 수신이 달라질 수 있습니다.

## 명령줄 빌드 / 설치

```sh
./gradlew :app:assemblePleosDebug
adb install -r app/build/outputs/apk/pleos/debug/app-pleos-debug.apk
adb shell am start -n com.cakerun.app/com.cakerun.app.MainActivity
```

`simDebug`와 `pleosDebug`는 패키지 이름이 달라 함께 설치할 수 있습니다. SDK Maven은 `settings.gradle.kts`에 설정되어 있습니다. 공개 Maven 접근 실패 시 임의 AAR이나 가짜 SDK로 대체하지 말고 `simDebug`로 시연하세요.

## 권한과 문제 해결

- `pleosDebug`만 `CAR_DRIVE`, `NAVI_ROUTE`, `NAVI_ROUTE_SEARCH`를 선언합니다. 일반 Android의 `pm grant`로 모든 Pleos 권한을 강제로 부여할 수 있다고 가정하지 않습니다. Emulator/프로젝트의 SDK 사용 상태와 공식 API의 권한을 확인합니다.
- Playground 프로젝트의 SDK 상태를 [프로젝트/SDK 안내](https://document.pleos.ai/en/docs/pleos-only/create-my-project)에서 확인합니다. 현재 공개 안내상 Vehicle State와 NaviHelper는 Ready to Use 범주지만 실제 프로젝트 상태를 우선합니다.
- `SDK location not found`: Android Studio SDK Manager 설치 위치를 `local.properties`의 `sdk.dir`에 설정.
- `JAVA_HOME` 오류: Gradle JDK 17 선택.
- Console 접속 실패: AVD v2.1.0 이상, `adb devices`, 포트 중복, forward 재설정을 확인.
- 브레이크 미지원/권한 오류: 상단 오류가 표시되는 것이 정상 fallback 동작. 실제 차량 모델명으로 지원 여부를 추측하지 않습니다.

## 공모전 제출용 APK

0.2.0부터 제출 APK는 별도 release 서명을 사용합니다. README의 `apk/` 경로를 우선 참고하세요. 이 문서의 debug 빌드 명령은 소스 개발 시의 실행 예시입니다. 위 에뮬레이터 설치 절차는 실행 완료 기록이 아닙니다. 실제 AVD 검증 상태는 `VALIDATION.md`를 참고하세요.
