# CAKE RUN · Pleos Connect MVP

케이크를 꾸미고, 부드러운 가상 주행으로 배달하는 Kotlin Android 앱입니다. 외부 이미지 없이 Canvas로 2.5D 케이크를 그립니다. 기본 앱은 차량·네트워크 권한 없이 실행됩니다.

## 컴퓨터 브라우저 미리보기

저장소를 내려받은 뒤 `preview/index.html`을 Chrome 또는 Safari에서 엽니다. Android Studio 없이 꾸미기·가상 주행·배송 결과를 체험할 수 있습니다. 이 미리보기는 Android APK를 실행하는 것이 아니라 동일한 핵심 흐름을 재현한 브라우저 체험판입니다.

GitHub의 파일 화면에서는 코드가 보입니다. **저장소 ZIP을 내려받아 압축 해제한 뒤 파일을 열어주세요.** GitHub Pages는 아직 설정하지 않았습니다.

## 빌드된 Android 설치 파일

- [시뮬레이션 APK](downloads/CAKE-RUN-simulation.apk)
- [Pleos SDK 포함 APK](downloads/CAKE-RUN-pleos.apk)

Android 기기에 내려받아 설치할 수 있습니다. GitHub 파일 화면에서는 Download raw를 누릅니다. 개발용 debug APK이며 실행 검증 범위는 아래 문서를 참고하세요.

## 바로 실행

1. Android Studio에서 이 폴더를 Open합니다. Gradle JDK는 **17**, SDK는 **Android 35**를 사용합니다.
2. Gradle Sync 후 **Build Variants → app → simDebug**를 선택합니다.
3. Android API 28 이상 기기 또는 에뮬레이터에서 Run합니다. 가로 화면에 맞추었습니다.
4. 크림·토핑·장식·주행 시나리오를 바꾸고 `60초 시뮬레이션 출발`을 누릅니다.
5. 가상 급가속·급제동·급회전 버튼을 누르면 케이크가 기울고 손상됩니다. 60초 후 결과로 이동합니다. `데모 조기 도착`으로 바로 확인할 수도 있습니다.

터미널에서는 다음과 같습니다. Windows는 `gradlew.bat`을 사용하세요.

```sh
./gradlew :app:assembleSimDebug :app:testSimDebugUnitTest
adb install -r app/build/outputs/apk/sim/debug/app-sim-debug.apk
adb shell am start -n com.cakerun.app.sim/com.cakerun.app.MainActivity
```

SDK 경로는 Android Studio가 만든 `local.properties` 또는 `ANDROID_HOME`으로 지정합니다. JDK·Android SDK·Gradle 의존성은 처음 한 번 인터넷 다운로드가 필요합니다. Gradle wrapper를 포함합니다.

## 구현한 범위

- 바닐라/딸기/초콜릿 크림 × 딸기/블루베리/망고 토핑 × 메시지 픽/캔들, **18개 조합**
- 가속·감속·회전에 따른 보간 애니메이션, 크림 균열과 토핑 손실
- 꾸미기 → 배송 중 → 결과 → 재시작
- 부드러운 주행/도심/거친 주행, 1.2초 가상 입력 버튼
- 게임용 보존 점수, 급가속·급제동·급회전 이벤트 횟수, 배송 등급
- 화면이 가려지면 시뮬레이션 정지, 결과/꾸미기 저장. 프로세스 재시작 시 진행 중 배송은 저장된 상태로 종료하여 결과 표시
- 공식 SDK를 사용하는 별도 `pleosDebug` 변형과 오류/미지원 상태 표시

## 시뮬레이션과 SDK의 경계

| 기능 | simDebug | pleosDebug |
|---|---|---|
| 속도·종/횡가속도·이동거리 | 시뮬레이션 | **동일한 시뮬레이션 fallback** |
| 케이크 움직임·손상·점수 | 게임 규칙 | 게임 규칙 |
| Vehicle 연결·브레이크 읽기 | 없음 | 공식 SDK capability 확인 후 현재값 조회·구독, 상단 상태 표시 |
| Navi 연결·경로·잔여 거리/시간·도착 | 없음 | 공식 NaviHelper 어댑터 |
| 배송 종료 | 60초 또는 데모 버튼 | 60초/데모 버튼, 요청한 SDK 경로의 도착 콜백 |

**pleosDebug도 실제 주행 점수를 계산하지 않습니다.** 브레이크 ON/OFF는 감속 크기와 다릅니다. 공식 문서에서 확인하지 못한 물리량 API를 만들지 않았습니다. 앱 내부 `Motion`, `PlatformBridge`, `Simulator`는 CAKE RUN 자체 타입이며 SDK API가 아닙니다.

SDK 예제 목적지는 `42dot`입니다. 앱 화면의 `달콤한 작업실`은 가상 배송 이름입니다. 경로 요청은 준비 화면에서 별도로 누르는 에뮬레이터 테스트 기능입니다. SDK 경로와 60초 가상 배송은 독립적이며 지도는 구현하지 않았습니다.

## 문서와 검증

- [Pleos 에뮬레이터 실행 안내](docs/EMULATOR.md)
- [공식 API 근거·어댑터 설명](docs/SDK-ADAPTERS.md)
- [데모 및 수동 검증 절차](docs/DEMO-CHECKLIST.md)
- [빌드·테스트 결과](docs/VALIDATION.md)

동승자 모바일 꾸미기/평가, 주변 차량 소셜, 서버·로그인·보상, 실제 주행 데이터 수집 및 운전 중 사용 승인은 후속 범위입니다. 현재 산출물은 개발용 MVP이며 Pleos App Market 등록·승인 또는 실제 차량 검증을 완료한 제품은 아닙니다.
