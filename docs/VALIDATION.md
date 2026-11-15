# 제출용 release 검증 결과

검증일: 2026-11-15 (Asia/Seoul)
팀: 가도니와 애준이 / 앱: 중요한 건 흔들리지 않는 마음 / 버전: 0.2.0 (2)

## 완료

- `assemblePleosRelease`, `assembleSimRelease`: 성공. 실제 release 빌드 결과물이며 기존 debug 파일을 이름만 바꾸지 않았습니다.
- `testSimReleaseUnitTest`: 5개 통과, 실패 0, 오류 0.
- `lintPleosRelease`, `lintSimRelease`: 각각 오류 0, 경고 3. 경고는 target API 34, 가로 화면 고정, 구형 Android 백업 설정 안내입니다.
- 두 APK의 `apksigner verify --verbose --print-certs`: 성공. API 28 이상에서 APK v3 서명 검증을 통과했습니다. 3072-bit RSA 제출용 키를 사용했고 Android Debug 인증서가 아님을 확인했습니다.
- 두 APK의 패키지 메타데이터: `application-debuggable` 없음, 앱 이름 `중요한 건 흔들리지 않는 마음`, minSdk 28 / targetSdk 34 확인.
- 공식 Maven SDK: Vehicle 2.3.2 / NaviHelper 3.0.5 실제 컴파일·패키징. 가짜 SDK stubs를 사용하지 않았습니다.
- 최종 APK SHA-256 체크섬을 `apk/SHA256SUMS.txt`에 기록했습니다.

테스트는 부드러운 배송의 무손상 종료, 급이벤트 중복 카운트 방지, 비정상 입력 무시·손상 상한, 결과 고정·재시작 초기화, 시나리오별 점수 차이를 검증합니다.

실행 환경: JDK 17.0.21 / Kotlin 2.0.21 / Gradle 8.9 / AGP 8.7.3 / Android SDK 및 Build Tools 35.

## 아직 실행하지 않은 항목

**Android 기기 또는 Pleos Connect Emulator에서 앱을 직접 실행한 검증은 아직 없습니다.** 실제 화면 렌더링, SDK 권한, Vehicle/Navi 서비스 연결, Virtual Console 입력, 내비 도착 콜백은 별도 수동 확인이 필요합니다.

대상은 Pleos Connect Emulator v2.1.0 / API 34 / 가로 화면입니다. 참고 화면 1280×720px는 향후 수동 검증 기준이며 실제 사용한 AVD 해상도를 의미하지 않습니다. 실차 검증, 운전 중 사용 승인, Pleos App Market 심사 통과도 주장하지 않습니다.

## 근거

`verification/release-build.txt`, `unit-tests-release.xml`, `lint-pleos-release.txt`, `lint-sim-release.txt`, `signature-*-release.txt`, `metadata-*-release.txt`에 원본 결과를 포함했습니다. 호스트의 개인 경로와 장치 이름은 치환했습니다. 이전 0.1.0 debug 검사 파일은 과거 개발 이력이며 현재 제출물의 근거는 `release`가 붙은 파일입니다.

서명키와 비밀번호는 로컬에서만 보관하며 저장소와 제출물 ZIP에 포함하지 않습니다.
