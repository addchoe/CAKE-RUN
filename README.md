# 중요한 건 흔들리지 않는 마음

**팀: 가도니와 애준이** · 현대자동차그룹 차량용 앱 개발 공모전 개발 산출물

케이크가 흔들리고 손상되는 모습으로 부드러운 주행의 가치를 체험하는 Pleos Connect용 Kotlin Android 앱 MVP입니다. 초기 개발명은 CAKE RUN이며 코드의 패키지 이름은 `com.cakerun.app`을 유지합니다.

## 제출용 APK

| 구분 | 저장소 내 파일 | 패키지 |
|---|---|---|
| **주 제출물 · Pleos SDK 포함 release** | [APK 다운로드](<apk/가도니와 애준이_중요한 건 흔들리지 않는 마음_release.apk>) | `com.cakerun.app` |
| 보조 · SDK 없는 시뮬레이션 release | [APK 다운로드](<apk/simulation/가도니와 애준이_중요한 건 흔들리지 않는 마음_release.apk>) | `com.cakerun.app.sim` |

두 파일 모두 **release 빌드, 별도 제출용 키로 서명, debuggable=false**입니다. debug APK의 파일명만 변경한 것이 아닙니다. 주 제출물은 SDK 연결·브레이크 상태·내비 경로 어댑터를 포함하지만, **케이크의 물리 입력과 점수는 시뮬레이션입니다.** 실제 서비스가 없거나 권한이 없으면 상태를 표시하고 가상 배송을 유지합니다.

제출할 APK 경로:

```text
apk/가도니와 애준이_중요한 건 흔들리지 않는 마음_release.apk
```

GitHub 파일 화면에서 **Download raw**를 눌러 Android 기기로 내려받아 설치합니다. APK는 Mac 자체에서 직접 실행되지 않습니다. 체크섬은 [apk/SHA256SUMS.txt](apk/SHA256SUMS.txt)를 참고하세요.

## 실행 방법

### APK 설치

1. Pleos Connect Emulator 또는 Android API 28 이상 기기를 준비합니다.
2. 주 제출용 APK를 설치하고 앱 목록에서 **중요한 건 흔들리지 않는 마음**을 실행합니다.
3. 크림·토핑·장식을 선택하고 주행 시나리오를 고릅니다.
4. **60초 시뮬레이션 출발** → 가상 급가속·급제동·급회전 → 배송 결과를 체험합니다.
5. 일반 Android에서 SDK 서비스 없이 시연하려면 보조 시뮬레이션 APK를 사용하세요.

```sh
adb install -r "apk/가도니와 애준이_중요한 건 흔들리지 않는 마음_release.apk"
adb shell am start -n com.cakerun.app/com.cakerun.app.MainActivity
```

기존 debug APK와 release APK는 서명키가 다릅니다. 동일 패키지의 debug 버전이 설치되어 있으면 기존 버전을 제거한 뒤 설치해야 하며, 제거하면 그 앱의 저장된 배송 결과도 삭제됩니다.

### Android Studio에서 소스 실행

이 저장소를 Open → SDK 35 설치 → Gradle JDK 17 → Gradle Sync → Build Variants에서 `pleosDebug` 또는 `simDebug` → Run. 서명된 release 재빌드는 [서명·빌드 안내](docs/RELEASE-BUILD.md)를 따릅니다.

### 컴퓨터 브라우저 미리보기

**Code → Download ZIP → 압축 해제 → `preview/index.html` 열기**. Android Studio 없이 앱의 주요 흐름을 체험할 수 있습니다. Android APK를 구동하는 방식은 아니며 실제 SDK와 연결되지 않습니다. GitHub Pages는 설정하지 않았습니다.

## 테스트 환경과 필요한 설정

| 항목 | 설정 / 확인 범위 |
|---|---|
| 개발 언어 / JDK | Kotlin 2.0.21 / JDK 17.0.21 |
| 빌드 | Gradle wrapper 8.9 / AGP 8.7.3 / Build Tools 35.0.0 |
| Android | minSdk 28 / compileSdk 35 / targetSdk 34 |
| 공식 SDK | Vehicle 2.3.2 / NaviHelper 3.0.5 |
| 버전 | 0.2.0 / versionCode 2 |
| 네트워크 | 최초 Gradle 의존성 다운로드에 필요. 시뮬레이션은 서버 불필요 |
| Pleos 권한 | 주 제출물만 `CAR_DRIVE`, `NAVI_ROUTE`, `NAVI_ROUTE_SEARCH` 선언 |
| 검사 | release 빌드, 서명·메타데이터 검사, 게임 규칙 테스트 및 lint. 상세는 [검증 결과](docs/VALIDATION.md) |

### Pleos Connect 에뮬레이터 버전과 화면 규격

- **대상 에뮬레이터:** Pleos Connect Emulator **v2.1.0**, Android **API 34**, RAM 4GB, Cold boot.
- **화면 설계:** 가로 방향, 좌측 케이크/우측 제어·결과 영역. 16:9 **1280 × 720px를 수동 검증의 기준 화면**으로 정했습니다. 작은 가로 화면에서는 우측 영역을 스크롤할 수 있습니다.
- **실제 사용·실행 상태:** 이 프로젝트는 호스트의 Android SDK로 빌드했으며, **Pleos AVD에서 직접 실행한 이력은 아직 없습니다.** 따라서 위 버전·화면 규격은 대상/검증 계획이며, 실제 사용한 AVD 규격이나 검증 완료를 뜻하지 않습니다. 심사 전 실제 AVD의 `adb shell wm size` / `adb shell wm density` 결과와 실행 결과를 추가할 필요가 있습니다.

에뮬레이터 설정과 Virtual Console 연결 방법은 [공식 문서 기반 실행 안내](docs/EMULATOR.md)에 있습니다. 개발 산출물의 에뮬레이터 실행 검증이 완료되었다고 주장하지 않습니다.

## 구현 범위

- 크림 3종 × 토핑 3종 × 장식 2종 = **18가지 케이크 조합**
- Canvas 기반 2.5D 케이크, 가속·감속·회전에 따른 보간 애니메이션
- 손상 누적에 따른 균열·토핑 손실
- 꾸미기 → 배송 중 → 결과 → 재시작
- 부드러운 주행 / 도심 / 거친 주행, 가상 입력 버튼
- 게임용 보존 점수·이벤트 횟수·배송 등급
- 백그라운드 시뮬레이션 정지, 꾸미기·결과 저장
- Vehicle capability 확인, 브레이크 현재값 조회·구독 및 정리
- NaviHelper 연결, 예제 목적지 요청, 잔여 거리·시간·도착 이벤트 처리

## 실제 SDK와 시뮬레이션의 경계

| 데이터 / 기능 | 현재 구현 |
|---|---|
| 속도·종/횡가속도·주행 거리 | 두 변형 모두 시뮬레이션 |
| 케이크 손상·점수 | 검증된 운전 평가가 아닌 게임 규칙 |
| 브레이크 ON/OFF | 주 제출물은 실제 Vehicle SDK 호출. 감속 크기로 변환하지 않음 |
| 내비 정보·경로 | 주 제출물의 공식 NaviHelper 어댑터 |
| 배송 종료 | 60초 / 데모 조기 도착 / 앱이 요청한 내비 경로의 도착 콜백 |

내비 예제 목적지는 공식 문서의 `42dot`이며, `달콤한 작업실`은 가상 배송 이름입니다. SDK 경로와 60초 가상 배송은 독립적입니다. 미확인 SDK API나 가짜 SDK 패키지를 사용하지 않았습니다.

동승자 모바일 꾸미기·평가, 주변 차량 소셜, 서버·로그인·보상, 실제 주행 물리량 수집과 실차 검증은 후속 범위입니다.

## 제출 안내 및 문서

[공식 제출 안내](https://appdevchallenge.auto-hmg.io/guide/submission)에 맞춰 `apk/팀명_앱이름_release.apk`, 프로젝트 소스, 실행·환경 안내를 준비했습니다. **이 저장소 업데이트 자체는 공모전 접수가 아닙니다.** 서명한 공식 접수양식(기획서)을 이메일로 제출하고, 저장소 URL·APK 경로·최종 커밋 번호를 함께 기재해야 합니다.

- [공식 SDK 근거](docs/SDK-ADAPTERS.md)
- [에뮬레이터 실행](docs/EMULATOR.md)
- [release 빌드·서명](docs/RELEASE-BUILD.md)
- [수동 데모 검증 절차](docs/DEMO-CHECKLIST.md)
- [완료한 검증 / 미실행 항목](docs/VALIDATION.md)
