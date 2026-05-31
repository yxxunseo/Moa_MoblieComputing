# 📝 모아(Moa) 프로젝트 작업 일지 (Work Log)

각 페이지 및 기능 구현이 완료될 때마다 이 파일에 간략히 진행 상황을 누적하여 요약합니다.

---

### [2026-05-15] 1. 초기 홈 화면 (Initial Home Screen) 구현 완료
* **생성 파일**: `InitialHomeScreen.kt`
* **사용 기술**: Jetpack Compose
* **핵심 구현 내용**:
  - 앱 첫 실행 시 보이는 "안녕하세요" 인사말 및 슬로건 UI 구현 (`SB 어그로` 폰트 적용)
  - "모임 생성하기" / "모임 입장하기" 동작을 위한 커스텀 카드 컴포넌트(`MeetingActionCard`) 제작
  - Figma 디자인 시안과 유사한 부드러운 그림자 효과, 카드 내부 텍스트 및 이미지 정렬(고정 너비) 처리
  - 메인 캐릭터 이미지가 생성하기 카드 테두리에 자연스럽게 걸터앉도록 `offset` 및 `zIndex` 처리 완료
* **비고**:
  - `MainActivity.kt`의 시작 화면을 해당 화면으로 교체
  - (트러블슈팅) 외부 라이브러리 충돌로 인한 `minSdk 24` 상향 조정 및 SVG 이미지 호환성 가이드 완료

---

### [2026-05-15] 2. 하단 네비게이션 바 컴포넌트화 및 리팩토링
* **생성 파일**: 
  - `ui/components/MoaBottomNavigationBar.kt`
  - `ui/components/MeetingActionCard.kt`
  - `ui/theme/Type.kt` (폰트 분리)
* **사용 기술**: Jetpack Compose (Scaffold, NavigationBar)
* **핵심 구현 내용**:
  - 앱 전역에서 재사용 가능한 **하단 네비게이션 바**(`MoaBottomNavigationBar`) 제작
  - "홈", "캘린더", "그룹", "마이" 4개의 탭 구성
  - "마이" 탭은 프로필 사진을 동적으로 받을 수 있도록 설계 (기본값: 원형으로 자른 `ic_character` 이미지 적용)
  - 기존의 `MeetingActionCard` 코드를 별도의 파일로 분리하여 **재사용성 극대화**
  - `InitialHomeScreen`에 `Scaffold`를 적용하여 하단 바를 고정 부착

---

### [2026-05-15] 3. 애플 캘린더 스타일 월간(Month) 화면 초기 구현
* **생성 파일**: `ui/calendar/CalendarScreen.kt`
* **핵심 구현 내용**:
  - 하단 네비게이션 "캘린더" 탭 클릭 시 연결되는 월간 달력 뷰 제작
  - 애플 캘린더 UI를 모티브로 하여 상단 바(년도, 액션 아이콘), 요일 헤더, 날짜 그리드 구현
  - 날짜 박스 안에 일정이 **알약 형태(색상 띠)** 로 표시되도록 `CalendarRow` 컴포넌트 커스텀
  - 오늘 날짜(예: 12일)를 빨간색 동그라미로 하이라이트 처리
  - (임시) 맥날 알바, 멋사 아이디 등 캡처해주신 시안과 똑같은 더미 데이터를 채워 넣어 시각적 확인이 가능하게 설정

---

### [2026-05-25] 4. 일정 조율(시간 선택) 화면 구현
* **생성 파일**: `ui/schedule/ScheduleCoordinationScreen.kt`
* **사용 기술**: Jetpack Compose (LazyRow, LazyColumn)
* **핵심 구현 내용**:
  - 수평으로 스크롤 가능한 **날짜 선택 칩(Date Selector)** `LazyRow`로 구현 (선택된 날짜를 하이라이트 처리)
  - 선택한 날짜에 해당하는 **시간 슬롯 그리드** 구현 (`09:00` ~ `22:00`)
  - 각 시간 슬롯 박스를 터치하여 **가능한 시간을 다중 선택 및 취소** 가능하도록 상태(`mutableStateListOf`) 관리
  - 하단에 현재 선택된 시간 개수와 `입력 완료` 버튼을 담은 고정 바(BottomSubmitBar) 적용
  - 깔끔한 그림자(`shadow`)와 테두리(`border`), MoaBlue 브랜드 컬러 적용으로 직관적인 사용자 경험 제공

---

### [2026-05-26] 5. 시각화 화면 (히트맵 및 추천 결과) 구현
* **생성 파일**: `ui/schedule/ScheduleResultScreen.kt`
* **사용 기술**: Jetpack Compose
* **핵심 구현 내용**:
  - API 분석 결과를 바탕으로 도출된 **추천 시간대 리스트 (1~3순위)** 카드 뷰 구현
  - 1순위 추천 시간에는 별도 배지(Star) 디자인과 강조 테두리 적용하여 가시성 확보
  - 구성원들의 선택 빈도를 색상의 진하기(Opacity)로 표현한 **시간대별 혼잡도(히트맵) 목업 UI** 추가
  - 최적의 시간 확정을 위한 '이 시간으로 확정하기' 버튼 연결

---

### [2026-05-27] 6. API 모델 설계 및 UI 네비게이션 연동
* **수정 파일**: `MoaApi.kt`, `MainActivity.kt`, `ScheduleDto.kt`
* **사용 기술**: Retrofit2, Jetpack Compose Navigation
* **핵심 구현 내용**:
  - `ScheduleDto.kt`에 백엔드와 통신할 일정 관련 DTO(`TimeSlotRequest`, `ScheduleAnalysisResponse` 등) 정의
  - `MoaApi.kt`에 일정 상세, 가능 시간 전송, 히트맵 분석 결과, 시간 확정 API 엔드포인트 추가
  - `MainActivity.kt`의 `NavHost`에 `schedule_coordination`과 `schedule_result` 라우트를 추가하여 UI 간 이동 경로 설정
  - `MeetingsScreen`의 모임 항목 클릭 시 일정 조율 화면으로 부드럽게 넘어가도록 네비게이션 이벤트 연결
  - 뷰 컴포넌트(modifier) 관련 unresolved reference 에러 수정 및 안정화

---

### [2026-05-27] 7. 프론트엔드-백엔드 데이터 연동 (ViewModels)
* **수정/생성 파일**: `AuthViewModel.kt`, `MeetingsViewModel.kt`, `CalendarViewModel.kt`, `UserViewModel.kt` 등
* **사용 기술**: ViewModel, Coroutines, StateFlow, Retrofit2
* **핵심 구현 내용**:
  - 기존 하드코딩된 더미 데이터(Mock Data)를 제거하고 실제 API 통신으로 대체
  - **로그인 연동**: 구글/카카오 소셜 로그인 성공 시 백엔드로 인증 토큰을 전송하여 서버 세션/토큰 발급
  - **모임 연동 (`MeetingsScreen`)**: `/api/users/me/groups`에서 가져온 실제 그룹 목록 렌더링
  - **캘린더 연동 (`CalendarScreen`)**: `/api/calendar/events`에서 월별 일정을 가져와 달력에 표시 및 일정 추가 로직 구현
  - **내 정보 연동 (`InitialHomeScreen`, `MyPageScreen`)**: `/api/users/me`를 호출해 실제 사용자 닉네임과 프로필 정보를 화면에 반영

---

### [2026-05-29] 8. JWT 토큰 관리 + 인증 헤더 자동 주입
* **생성/수정 파일**: `network/TokenManager.kt`, `network/RetrofitClient.kt`, `MoaApplication.kt`, `ui/login/AuthViewModel.kt`, `app/build.gradle.kts`
* **핵심 구현 내용**:
  - `TokenManager` 싱글톤: JWT 토큰·유저ID·닉네임을 SharedPreferences에 저장/로드/삭제
  - `AuthInterceptor`: 모든 API 요청에 `Authorization: Bearer <token>` 헤더 자동 주입
  - `OkHttpClient`에 `AuthInterceptor` + `HttpLoggingInterceptor` 연결
  - `AuthViewModel` 로그인 성공 시 `TokenManager.saveToken()` 호출, `logout()` 함수 추가
  - `okhttp3:okhttp:4.12.0`, `okhttp3:logging-interceptor:4.12.0` 의존성 추가

---

### [2026-05-29] 9. 모임 생성/입장 화면 (바텀시트) 구현
* **생성 파일**: `ui/meetings/GroupActionViewModel.kt`, `ui/meetings/CreateOrJoinMeetingSheet.kt`
* **수정 파일**: `ui/meetings/MeetingsScreen.kt`
* **핵심 구현 내용**:
  - `GroupActionViewModel`: 그룹 생성·초대코드 입장 API 연동
  - `CreateOrJoinMeetingSheet`: 탭 전환형 바텀시트 (모임 만들기 / 코드로 입장)
    - 이름·설명·색상 팔레트(8색) 입력, 초대코드 자동 대문자 변환
    - 성공 시 `MeetingsViewModel.fetchMyGroups()` 호출하여 목록 새로고침
  - `MeetingsScreen` `+` 버튼에 `ModalBottomSheet` 연결

---

### [2026-05-29] 10. 백엔드 일정 상세 조회 + 스케줄 화면 실제 연동
* **수정 파일**: `controller/ScheduleController.kt`, `service/ScheduleService.kt`, `MainActivity.kt`
* **핵심 구현 내용**:
  - 백엔드 `GET /api/schedules/{id}` 엔드포인트 추가
  - `schedule_coordination/{link}` 라우트: 시간 제출 시 `GuestScheduleViewModel.submitTimeSlots()` 실제 API 호출
  - `schedule_result/{link}` 라우트: `uniqueLink`를 ScheduleResultScreen에 전달하여 실제 분석 결과 표시

---

### [2026-05-29] 11. 홈 바텀시트 연동 + 모임 상태별 UI + 일정 라우팅 분기
* **수정 파일**: `InitialHomeScreen.kt`, `ui/meetings/CreateOrJoinMeetingSheet.kt`, `ui/meetings/MeetingsScreen.kt`, `ui/meetings/GroupDetailScreen.kt`, `MainActivity.kt`
* **핵심 구현 내용**:
  - **홈화면 바텀시트 연동**: "모임 생성하기" 카드 → `initialTab=0`(만들기), "모임 입장하기" 카드 → `initialTab=1`(코드 입장) 탭으로 `CreateOrJoinMeetingSheet` 직접 오픈. `Meetings` 탭 이동 없이 홈에서 즉시 모임 생성/입장 가능
  - **`CreateOrJoinMeetingSheet` `initialTab` 파라미터 추가**: 호출 위치에 따라 원하는 탭을 초기값으로 설정 가능
  - **`MeetingsScreen` 상태별 UI 분기**: API 로딩 중 CircularProgressIndicator, 에러 시 에러 메시지 + 재시도 버튼, 빈 목록 시 안내 카드 표시. 더 이상 sampleMeetings로 폴백하지 않음
  - **`GroupDetailScreen` 일정 클릭 분기**: `CONFIRMED`/`DONE` 상태 → 결과 화면(`schedule_result_group`), `WAITING`/`ADJUSTING` 상태 → 조율 화면(`schedule_coordination_group`) 으로 자동 라우팅
  - **`MainActivity` `onCoordinateClick` 연결**: `GroupDetailScreen`에 조율 화면 이동 콜백 추가

---

### [2026-05-30] 12. 잔여 작업 전체 마무리
* **수정 파일**: `GroupDetailScreen.kt`, `MyPageScreen.kt`, `RetrofitClient.kt`, `app/build.gradle.kts`, `local.properties`
* **핵심 구현 내용**:
  - **초대코드 복사 기능**: `GroupDetailScreen`의 `GroupInfoCard`에 초대코드 클릭 시 클립보드 복사 + Toast 안내. `ContentCopy` 아이콘 버튼 형태로 UX 개선
  - **마이페이지 실제 참여 모임 수 연동**: `MeetingsViewModel`에서 실제 그룹 목록을 받아 `ProfileSummaryCard`의 "참여 중인 모임" 통계를 실시간으로 표시 (하드코딩 "8" 제거)
  - **알림/캘린더 토글 영구 저장**: `IntegrationSettingsCard`의 4가지 토글(Google Calendar, Apple Calendar, 일정 확정 알림, 캘린더 추가 알림)을 `SharedPreferences("moa_settings")`에 저장하여 앱 재시작 후에도 유지
  - **서버 URL 동적 설정**: `RetrofitClient`의 `BASE_URL`을 `BuildConfig.SERVER_URL`로 교체. `local.properties`에서 `SERVER_URL` 값을 읽으므로 에뮬레이터↔실기기 전환 시 소스 코드 수정 없이 `local.properties`의 한 줄만 변경하면 됨
    - 에뮬레이터: `SERVER_URL=http://10.0.2.2:8080/`
    - 실기기: `SERVER_URL=http://192.168.0.X:8080/`

---

### [2026-05-30] 13. 잔여 기능 구현 (JWT·리액션·구글·고정시간표·캘린더 수정)
* **백엔드 생성/수정**:
  - `JwtTokenProvider.kt`, `AuthController.kt`: Refresh Token 발급 및 `POST /api/auth/refresh`
  - `ScheduleReaction.kt`, `ScheduleReactionService.kt`: `GET/PUT/DELETE /api/schedules/{id}/reactions`
  - `FixedTimeSlot.kt`, `FixedTimeSlotService.kt`, `FixedTimeSlotController.kt`: `/api/users/me/fixed-slots` CRUD
  - `CalendarService.updateEvent()`, `CalendarController`: `PUT /api/calendar/events/{id}`
  - `GoogleCalendarService`: 연결 상태 조회·일정 동기화, `GoogleCalendarController` `/status`, `/sync`
* **앱 생성/수정**:
  - `TokenRefresher.kt`, `TokenManager.saveTokens()`: 401 시 자동 토큰 갱신
  - `ScheduleViewModel` + `ScheduleResultScreen`: 리액션 API 연동 (`ReactionBar`)
  - `GoogleCalendarHelper.kt`, `MyPageScreen`: 구글 캘린더 OAuth 연결/해제·동기화
  - `FixedScheduleSheet.kt`, `BusyTimeHelper.kt`: 고정 시간표 등록 및 조율 시 불가 시간 반영
  - `CalendarScreen.kt`: 수동 일정 탭 → 수정, 길게 누르기 → 삭제
  - `MoaNotificationHelper.kt`: 일정 확정·캘린더 추가 로컬 알림 (`MainActivity` POST_NOTIFICATIONS 권한)

---

### [2026-05-30] 14. 게스트 웹 링크 + UI 개선
* **수정 파일**: `application.yml`, `GuestScheduleService.kt`, `guest.html`, `SecurityConfig.kt`, `GuestLinkHelper.kt`, `ScheduleCoordinationScreen.kt`, `InitialHomeScreen.kt`
* **핵심 구현 내용**:
  - **게스트 링크 접속 오류 해결**: `server.public-url` 설정, API 응답에 `webLink` 포함, `GuestLinkHelper`로 앱에서 올바른 URL 생성
  - **`guest.html`**: 이름 입력 후 날짜 변경 시 이름 유지, ◀▶ 날짜 네비게이션
  - **앱 조율 UI**: 웹과 동일한 3열 시간 그리드(8~22시), ◀▶ 날짜 이동 카드 스타일
  - **홈 화면 분기**: 모임 없음 → 생성/입장 카드만, 모임 있음 → 대시보드(활동·다가오는 일정) 표시

---

### [2026-05-31] 15. 디자인 시스템 통일
* **생성/수정 파일**: `ui/theme/Color.kt`, `Theme.kt`, `Type.kt`, `ui/components/MoaComponents.kt`
* **핵심 구현 내용**:
  - 공통 토큰 중앙화: `#F7F8FC` 배경, `#2179FE` MoaBlue, `#101B33`/`#737C99` 텍스트, `#DDE4F2` 카드 그림자
  - `MoaOutlinedTextField`, `MoaDialogButtonText`, `moaCard()` 공통 컴포넌트 추가
  - Material Dynamic Color 비활성화, SB 어그로 Typography 전역 적용
  - **적용 화면**: `CalendarScreen`, `LoginScreen`, `SignUpScreen`, `FixedScheduleSheet`, `GroupDetailScreen` 다이얼로그, `InitialHomeScreen`, `ScheduleResultScreen` 히트맵, `MoaBottomNavigationBar`

---

### [2026-05-31] 16. 마이페이지·모임 UX 개선
* **수정 파일**: `MyPageScreen.kt`, `MeetingsScreen.kt`, `InitialHomeScreen.kt`, `MainActivity.kt`
* **핵심 구현 내용**:
  - **모임 지수 제거**: `MannerScoreCard` 삭제, 프로필 카드 간소화
  - **Apple Calendar 토글 제거**: 연동 설정에서 iCloud 항목 삭제 (Google Calendar·알림 토글만 유지)
  - **모임 하트(즐겨찾기)**: `GroupFavoriteManager`로 로컬 저장, 하트 누른 모임 목록 최상단 정렬, 마이페이지 "관심 모임" 수 반영
  - **홈에서 단기 일정 링크 생성**: 온보딩·대시보드 모두 `CreateGuestScheduleSheet` 바텀시트 연결

---

### [2026-05-31] 17. 내 단기 일정 목록 + 확정/완료 처리
* **백엔드**:
  - `GET /api/guest-schedules/mine/list`: 본인이 생성한 단기 일정 목록 (상태·확정 시간 포함)
  - `PUT /api/guest-schedules/{link}/complete`: 확정된 일정 완료(`DONE`) 처리
  - `GuestScheduleResponse`에 `status`, `confirmedStart`, `confirmedEnd` 필드 추가
* **앱**:
  - `GuestScheduleListViewModel.kt`, `MyGuestSchedulesSection.kt` 생성
  - **홈·모임 탭**에 "내 단기 일정" 섹션 표시 (상태 배지, 조율/확정 이동, 링크 복사, 완료 버튼)
  - 여러 개의 단기 일정을 한곳에서 관리 가능

---

### 미구현 / 후순위
| 기능 | 상태 |
|------|------|
| FCM 푸시 알림 | 로컬 알림만 구현 (Firebase 설정 필요) |
| 게스트 일정 리액션 | 미구현 |
