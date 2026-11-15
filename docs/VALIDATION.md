# 빌드 / 검증 결과

검증일: 2026-11-15 (Asia/Seoul).

## 완료한 검증

- JDK 17.0.21, Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, Android SDK 35로 두 변형 빌드 성공.
- `assembleSimDebug`: 성공. 공식 SDK 없이 실행하도록 패키징된 기본 데모.
- `assemblePleosDebug`: 성공. 공식 Maven의 Vehicle 2.3.2 / NaviHelper 3.0.5를 실제로 해석·컴파일·패키징. 가짜 SDK stubs를 사용하지 않음.
- `testSimDebugUnitTest`: **5개 통과, 실패 0, 오류 0**.
- `lintSimDebug` / `lintPleosDebug`: 각각 **오류 0, 경고 3**, 검사 성공. API 34 대상 및 가로 고정 화면 경고 등은 개발용 차량 화면 설정과 관련됨. 상세 경고는 원본 보고서 참고.
- 두 최종 APK의 `apksigner verify`: 성공, APK v2 서명 확인. 개발용 debug 서명이며 배포용 릴리즈 키/키 로테이션 설정을 대신하지 않음.
- APK 메타데이터에서 minSdk 28, targetSdk 34, 메인 Activity 및 시뮬레이션 패키지 이름 확인.

검증 명령:

```sh
./gradlew :app:assembleSimDebug :app:assemblePleosDebug \
  :app:testSimDebugUnitTest :app:lintSimDebug :app:lintPleosDebug
```

테스트한 동작: 부드러운 배송의 무손상 종료, 급이벤트의 지속시간 중 중복 카운트 방지와 재진입 카운트, 비정상 입력 무시 및 손상 상한, 결과 고정 및 재시작 초기화, 거친 주행과 부드러운 주행의 점수 차이.

## 실행하지 않은 검증

이 환경에는 기존 Android Studio/AVD가 없었습니다. 빌드용 JDK·SDK는 작업 폴더에 준비했지만, **Android 기기나 Pleos Emulator에서 앱을 직접 실행한 검증은 하지 않았습니다.** 화면의 실제 렌더링, SDK 권한 부여, Vehicle Service/Navi Service 연결, Virtual Console 및 실제 도착 콜백은 에뮬레이터에서 수동 확인이 필요합니다.

이 결과는 빌드/공개 API 서명/게임 로직 검증이며, 실차 테스트나 Pleos App Market 심사 통과를 의미하지 않습니다. Android Studio의 시각적 미리보기용 화면은 별도로 만들지 않았으며, 실제 화면은 Kotlin에서 생성됩니다.

## 근거 파일

`verification/`에 최종 빌드 로그, 테스트 XML, 두 lint 텍스트 보고서, APK 서명 검증 결과를 포함했습니다. 소스 ZIP에는 Gradle wrapper와 문서를 포함하며 로컬 SDK, 개발 키, 빌드 캐시, 공식 SDK 바이너리는 포함하지 않습니다.
