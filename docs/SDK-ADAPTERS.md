# Pleos 어댑터 · 공식 근거

확인 날짜: 2026-11-15. 모든 SDK 호출은 `app/src/pleos/java/com/cakerun/app/BridgeFactory.kt`에 격리했습니다. 기본 `sim` 변형에는 Pleos 라이브러리가 포함되지 않습니다.

## 버전 및 증거

- [Vehicle 가져오기](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/Vehicle/project-setup): `ai.pleos.playground:Vehicle:2.3.2`
- [NaviHelper 가져오기](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/NaviHelper/importing-sdk): `ai.pleos.playground:NaviHelper:3.0.5`
- Maven: `https://nexus-playground.pleos.ai/repository/maven-releases/`
- [한국어 SDK 릴리즈 노트](https://document.pleos.ai/docs/pleos-only/release-note/sdk-release-note): Vehicle 2.3.2. 영문 검색 결과 일부에는 2.3.1이 남아 있어 실제 의존성 설정 페이지와 공식 배포 AAR을 함께 대조했습니다.

공식 배포 AAR의 `classes.jar`를 분석하여 공개 메서드 서명도 확인했습니다. NaviHelper 3.0.5 POM은 `navi-data:3.0.4`를 참조합니다. 임의로 모든 하위 모듈 버전을 3.0.5로 맞추지 않았습니다. SDK 바이너리는 소스 ZIP에 재배포하지 않으며 Gradle이 공식 저장소에서 내려받습니다.

## Vehicle

[도메인 접근](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/Vehicle/obtain-domain-object) 및 [권한](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle-permission)에 따른 호출 흐름:

1. `Vehicle(applicationContext)` → `initialize()` → `getBrake()`.
2. [`checkBrakePedalStatusCapabilities`](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/Vehicle/vehicle/ai.pleos.playground.vehicle.api/brake/check-brake-pedal-status-capabilities)의 `CapabilityConfig.accessType`이 READ 또는 READ_WRITE인지 확인.
3. `getCurrentBrakePedalStatus(onComplete, onFailed)`로 Boolean 현재값을 조회하고 `registerBrakePedalStatus(listener)`로 변경을 구독.
4. `BrakePedalStatusListener.onBrakePedalStatusUpdated(Boolean?)`와 상위 `VehicleListener.onFailed(Exception)` 처리.
5. `unregisterBrakePedalStatus` 후 비동기 capability/get 콜백이 정리되면 `release()`.

권한은 `pleos.car.permission.CAR_DRIVE`. 상태와 오류는 UI 스레드로 전달합니다. 화면 종료 후 콜백은 UI를 갱신하지 않으며 구독을 다시 등록하지 않습니다. SDK가 미완료 콜백을 영원히 반환하지 않는 특수 상황에는 release 대기가 남을 수 있어 실제 대상 SDK 서비스에서 lifecycle 검증이 필요합니다.

브레이크 Boolean은 제동 강도가 아닙니다. `VehicleSpeedListener`는 최신 [릴리즈 노트](https://document.pleos.ai/docs/pleos-only/release-note/sdk-release-note)에서 제거되었으므로 호출하지 않습니다. 속도·종가속도·횡가속도·조향각·정차 판정은 이 어댑터가 제공하지 않습니다.

향후 실제 motion 연결은 [API 가용성 정책](https://document.pleos.ai/docs/pleos-only/vehicle-app-planning-guide/api-compatibility-availability)에 따라 AAOS property 정의·장치 지원·권한을 확인한 별도 구현이 필요합니다. 조향각을 횡가속도로 간주하거나 브레이크 여부를 임의 감속값으로 바꾸지 않습니다.

## NaviHelper

- [초기화/해제](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/NaviHelper/initialization-and-release): `NaviHelper(applicationContext)`, `initialize(onConnected, onFailed)`.
- [리스너](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/NaviHelper/event-listeners): 연결 후 동일 인스턴스를 `addListener`; 정리 시 `removeListener`, `release`.
- [목적지](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/NaviHelper/destinations): `requestRoute(RouteInfo(...))`, 경로 완료 후 `getDestinationInfo()`, 결과는 `onDestinationInfo`.
- `onRoutingCompleted`, `onError(NaviErrorCode)`, `onDestinationArrived(DestinationArrivedInfo)` 처리.
- `DestinationInfo.remainDistance` / `remainTime`을 사용합니다. RouteInfo는 **latitude/longitude 이름 인자**를 사용합니다. 옛 Java 예제에 보이는 좌표 순서를 복사하지 않았습니다.
- [Navi 권한](https://document.pleos.ai/en/docs/pleos-only/connect-sdk-pleos/NaviHelper/declaring-permissions): 정보 조회는 `NAVI_ROUTE`, 경로 요청은 `NAVI_ROUTE_SEARCH`. 지도 화면 API를 쓰지 않으므로 `NAVI_CUSTOM_MAP`은 요청하지 않습니다.

예제 POI는 공식 Kotlin 문서의 42dot 좌표/ID/주소를 사용합니다. 요청 버튼은 경로 설정을 내비게이션 서비스에 전달하며, 실제 탐색 성공은 콜백을 통해 표시합니다. 앱이 요청한 경로가 있을 때 도착 이벤트로 가상 배송을 종료할 수 있습니다. MVP에는 다중 경로 세션 식별·다른 앱이 목적지를 바꾸는 충돌 처리가 없으므로 에뮬레이터에서 단일 경로만 사용하세요.

## 앱 내부 경계

`PlatformBridge.start/report/arrived`, `Motion`, `Mission`, `Simulator`는 앱이 정의한 인터페이스입니다. Pleos API처럼 보이도록 가짜 SDK 패키지, reflection shim, 임의 권한 문자열, 가짜 API 성공 응답을 만들지 않았습니다. SDK 오류 시 연결 상태를 표시하고 motion은 시뮬레이션으로 유지합니다.
