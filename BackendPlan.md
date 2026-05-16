# 🛠️ Moa 백엔드 설계 문서

> **최종 수정일**: 2026-05-16  
> **마감일**: ~2026-05-29 (약 2주)

---

## 1. 기술 스택 (Tech Stack)

| 구분 | 선택 | 이유 |
|------|------|------|
| **언어** | Kotlin | 안드로이드 앱과 동일 언어 → 코드 공유 가능 |
| **프레임워크** | Spring Boot 3.x | 안정적이고 자료가 많음, 빠른 API 개발 |
| **DB** | MySQL 8.x | 요청하신 대로 MySQL 사용 |
| **ORM** | Spring Data JPA (Hibernate) | SQL 직접 안 써도 됨 |
| **인증** | JWT + OAuth2 (Google/Kakao) | 소셜 로그인 + 토큰 기반 인증 |
| **빌드** | Gradle (Kotlin DSL) | 안드로이드 프로젝트와 동일한 빌드 도구 |
| **실행 환경** | 로컬 (localhost) | 시연용이므로 같은 와이파이에서 접속 |
| **캘린더 연동** | Google Calendar API | 구글 캘린더 읽기/쓰기 연동 |

---

## 2. 프로젝트 구조

```
moa-backend/
├── src/main/kotlin/com/example/moa/
│   ├── MoaApplication.kt              # 메인 진입점
│   ├── config/
│   │   ├── SecurityConfig.kt          # JWT + OAuth2 인증 설정
│   │   ├── CorsConfig.kt              # CORS 허용 설정
│   │   └── GoogleCalendarConfig.kt    # 구글 캘린더 API 설정
│   ├── controller/                     # API 엔드포인트
│   │   ├── AuthController.kt
│   │   ├── UserController.kt
│   │   ├── GroupController.kt
│   │   ├── ScheduleController.kt
│   │   └── CalendarController.kt
│   ├── service/                        # 비즈니스 로직
│   │   ├── AuthService.kt
│   │   ├── OAuthService.kt            # 소셜 로그인 처리
│   │   ├── GoogleCalendarService.kt   # 구글 캘린더 API 연동
│   │   ├── UserService.kt
│   │   ├── GroupService.kt
│   │   ├── ScheduleService.kt
│   │   └── CalendarService.kt
│   ├── repository/                     # DB 접근 (JPA)
│   │   ├── UserRepository.kt
│   │   ├── GroupRepository.kt
│   │   ├── ScheduleRepository.kt
│   │   └── EventRepository.kt
│   ├── entity/                         # DB 테이블 매핑
│   │   ├── User.kt
│   │   ├── Group.kt
│   │   ├── GroupMember.kt
│   │   ├── Schedule.kt
│   │   ├── TimeSlot.kt
│   │   └── CalendarEvent.kt
│   ├── dto/                            # 요청/응답 데이터 형식
│   │   ├── request/
│   │   └── response/
│   └── security/
│       ├── JwtTokenProvider.kt
│       └── JwtAuthFilter.kt
├── src/main/resources/
│   └── application.yml                 # DB 접속 정보, OAuth 키, 서버 포트 등
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 3. 데이터베이스 스키마 (ERD)

### 테이블 구조

#### USER (회원)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 고유 ID |
| email | VARCHAR (UK, nullable) | 이메일 (일반 로그인용, 소셜 로그인 시 null 가능) |
| password | VARCHAR (nullable) | 암호화된 비밀번호 (소셜 로그인 시 null) |
| nickname | VARCHAR | 닉네임 |
| profile_image_url | VARCHAR | 프로필 이미지 경로 |
| provider | VARCHAR | LOCAL / GOOGLE / KAKAO |
| provider_id | VARCHAR | 소셜 로그인 고유 ID (구글/카카오 유저 ID) |
| google_access_token | VARCHAR (nullable) | 구글 캘린더 연동용 액세스 토큰 |
| google_refresh_token | VARCHAR (nullable) | 구글 캘린더 연동용 리프레시 토큰 |
| created_at | DATETIME | 가입일 |

#### MEETING_GROUP (모임/그룹)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 고유 ID |
| name | VARCHAR | 그룹 이름 |
| description | TEXT | 그룹 설명 |
| invite_code | VARCHAR (UK) | 초대코드 (예: MOA-A3X9K2) |
| color | VARCHAR | 그룹 색상 |
| created_by | BIGINT (FK→USER) | 생성자 |
| created_at | DATETIME | 생성일 |

#### GROUP_MEMBER (그룹-멤버 연결)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 고유 ID |
| group_id | BIGINT (FK) | 그룹 ID |
| user_id | BIGINT (FK) | 유저 ID |
| role | VARCHAR | ADMIN / MEMBER |
| joined_at | DATETIME | 가입일 |

#### SCHEDULE (일정 조율)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 고유 ID |
| group_id | BIGINT (FK) | 그룹 ID |
| created_by | BIGINT (FK) | 생성자 |
| title | VARCHAR | 일정 제목 |
| description | TEXT | 설명 |
| start_date | DATE | 조율 시작일 |
| end_date | DATE | 조율 종료일 |
| status | VARCHAR | WAITING / ADJUSTING / CONFIRMED / DONE |
| confirmed_datetime | DATETIME | 확정된 일시 |
| created_at | DATETIME | 생성일 |

#### TIME_SLOT (가능 시간대)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 고유 ID |
| schedule_id | BIGINT (FK) | 일정 ID |
| user_id | BIGINT (FK) | 유저 ID |
| slot_start | DATETIME | 가능 시작 시간 |
| slot_end | DATETIME | 가능 종료 시간 |

#### CALENDAR_EVENT (캘린더 일정)
| 컬럼 | 타입 | 설명 |
|------|------|------|
| id | BIGINT (PK) | 고유 ID |
| user_id | BIGINT (FK) | 유저 ID |
| group_id | BIGINT (FK, nullable) | 그룹 ID |
| schedule_id | BIGINT (FK, nullable) | 일정 ID |
| title | VARCHAR | 일정 제목 |
| event_start | DATETIME | 시작 시간 |
| event_end | DATETIME | 종료 시간 |
| color | VARCHAR | 표시 색상 |
| source | VARCHAR | MANUAL / GROUP / EXTERNAL |
| created_at | DATETIME | 생성일 |

### 관계 요약
- USER ↔ GROUP_MEMBER ↔ MEETING_GROUP (N:M 관계)
- MEETING_GROUP → SCHEDULE (1:N)
- SCHEDULE → TIME_SLOT (1:N)
- USER → TIME_SLOT (1:N)
- USER → CALENDAR_EVENT (1:N)

---

## 4. REST API 명세

### 4.1. 인증 (Auth)

> ⚠️ **소셜 로그인 전제 조건**
> - **구글**: Google Cloud Console에서 OAuth2 클라이언트 ID 발급 필요
> - **카카오**: Kakao Developers에서 앱 등록 및 REST API 키 발급 필요

#### 로그인 흐름
```
[일반 로그인]
앱 → POST /api/auth/login → 서버 → JWT 발급

[구글 로그인]
앱(Google Sign-In SDK) → 구글 ID Token 획득
→ POST /api/auth/google → 서버에서 검증 → JWT 발급

[카카오 로그인]
앱(Kakao SDK) → 카카오 Access Token 획득
→ POST /api/auth/kakao → 서버에서 카카오 사용자 정보 조회 → JWT 발급
```

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/auth/signup` | 일반 회원가입 | ❌ |
| `POST` | `/api/auth/login` | 일반 로그인 → JWT 반환 | ❌ |
| `POST` | `/api/auth/google` | 구글 로그인 (ID Token 전달) | ❌ |
| `POST` | `/api/auth/kakao` | 카카오 로그인 (Access Token 전달) | ❌ |
| `POST` | `/api/auth/refresh` | JWT 토큰 갱신 | ✅ |

#### 회원가입 요청/응답
```json
// POST /api/auth/signup
// Request
{
  "email": "yoonseo@example.com",
  "password": "password123",
  "nickname": "윤서"
}

// Response (201 Created)
{
  "id": 1,
  "email": "yoonseo@example.com",
  "nickname": "윤서",
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

#### 구글 로그인 요청/응답
```json
// POST /api/auth/google
// Request - 앱에서 Google Sign-In SDK로 받은 ID Token 전달
{
  "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
}

// Response (200 OK) - 신규 회원이면 자동 회원가입 후 토큰 발급
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "isNewUser": false,
  "user": {
    "id": 1,
    "nickname": "윤서",
    "email": "yoonseo@gmail.com",
    "provider": "GOOGLE",
    "profileImageUrl": "https://lh3.googleusercontent.com/..."
  }
}
```

#### 카카오 로그인 요청/응답
```json
// POST /api/auth/kakao
// Request - 앱에서 Kakao SDK로 받은 Access Token 전달
{
  "accessToken": "gQM8nxBPDf1Yt1Kk..."
}

// Response (200 OK)
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "isNewUser": true,
  "user": {
    "id": 2,
    "nickname": "카카오유저",
    "email": null,
    "provider": "KAKAO",
    "profileImageUrl": "http://k.kakaocdn.net/..."
  }
}
```

#### 일반 로그인 요청/응답
```json
// POST /api/auth/login
// Request
{
  "email": "yoonseo@example.com",
  "password": "password123"
}

// Response (200 OK)
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "nickname": "윤서",
    "profileImageUrl": null
  }
}
```

---

### 4.2. 유저 (User)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/users/me` | 내 프로필 조회 | ✅ |
| `PUT` | `/api/users/me` | 프로필 수정 (닉네임, 이미지) | ✅ |
| `GET` | `/api/users/me/groups` | 내가 속한 그룹 목록 | ✅ |

---

### 4.3. 모임/그룹 (Group)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/groups` | 그룹 생성 | ✅ |
| `GET` | `/api/groups/{id}` | 그룹 상세 조회 | ✅ |
| `POST` | `/api/groups/join` | 초대코드로 그룹 입장 | ✅ |
| `GET` | `/api/groups/{id}/members` | 그룹 멤버 목록 | ✅ |
| `DELETE` | `/api/groups/{id}/leave` | 그룹 탈퇴 | ✅ |

#### 그룹 생성
```json
// POST /api/groups
// Request
{
  "name": "멋쟁이사자처럼",
  "description": "백엔드 스터디 모임",
  "color": "#90EE90"
}

// Response (201 Created)
{
  "id": 1,
  "name": "멋쟁이사자처럼",
  "inviteCode": "MOA-A3X9K2",
  "color": "#90EE90",
  "memberCount": 1,
  "createdAt": "2026-05-15T11:00:00"
}
```

#### 초대코드로 입장
```json
// POST /api/groups/join
// Request
{
  "inviteCode": "MOA-A3X9K2"
}

// Response (200 OK)
{
  "groupId": 1,
  "groupName": "멋쟁이사자처럼",
  "message": "그룹에 성공적으로 입장했습니다!"
}
```

---

### 4.4. 일정 조율 (Schedule) - ⭐ 핵심 기능

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/groups/{groupId}/schedules` | 일정 조율 생성 | ✅ |
| `GET` | `/api/groups/{groupId}/schedules` | 그룹 내 일정 목록 | ✅ |
| `GET` | `/api/schedules/{id}` | 일정 상세 (+ 멤버별 응답) | ✅ |
| `POST` | `/api/schedules/{id}/timeslots` | 내 가능 시간 입력 | ✅ |
| `GET` | `/api/schedules/{id}/analysis` | ⭐ 겹치는 시간 분석 결과 | ✅ |
| `PUT` | `/api/schedules/{id}/confirm` | 최종 시간 확정 | ✅ |

#### 일정 조율 생성
```json
// POST /api/groups/1/schedules
// Request
{
  "title": "운영체제 세미나",
  "description": "기말고사 전 마지막 세미나",
  "startDate": "2026-05-20",
  "endDate": "2026-05-25"
}

// Response (201 Created)
{
  "id": 1,
  "title": "운영체제 세미나",
  "status": "WAITING",
  "startDate": "2026-05-20",
  "endDate": "2026-05-25",
  "respondedCount": 0,
  "totalMembers": 5
}
```

#### 가능 시간 입력
```json
// POST /api/schedules/1/timeslots
// Request
{
  "slots": [
    { "start": "2026-05-20T14:00", "end": "2026-05-20T18:00" },
    { "start": "2026-05-21T10:00", "end": "2026-05-21T12:00" },
    { "start": "2026-05-23T15:00", "end": "2026-05-23T20:00" }
  ]
}

// Response (200 OK)
{
  "message": "가능 시간이 등록되었습니다.",
  "respondedCount": 3,
  "totalMembers": 5
}
```

#### ⭐ 겹치는 시간 분석 (핵심!)
```json
// GET /api/schedules/1/analysis
// Response (200 OK)
{
  "scheduleId": 1,
  "title": "운영체제 세미나",
  "totalMembers": 5,
  "recommendations": [
    {
      "rank": 1,
      "start": "2026-05-21T10:00",
      "end": "2026-05-21T12:00",
      "availableCount": 5,
      "availableMembers": ["윤서", "민준", "서연", "지호", "하은"]
    },
    {
      "rank": 2,
      "start": "2026-05-23T15:00",
      "end": "2026-05-23T17:00",
      "availableCount": 4,
      "availableMembers": ["윤서", "민준", "서연", "하은"]
    }
  ],
  "heatmap": {
    "2026-05-20": { "14:00": 3, "15:00": 3, "16:00": 2, "17:00": 1 },
    "2026-05-21": { "10:00": 5, "11:00": 5, "14:00": 2 },
    "2026-05-23": { "15:00": 4, "16:00": 4, "17:00": 3 }
  }
}
```

#### 시간 확정
```json
// PUT /api/schedules/1/confirm
// Request
{
  "confirmedStart": "2026-05-21T10:00",
  "confirmedEnd": "2026-05-21T12:00"
}

// Response (200 OK)
{
  "message": "일정이 확정되었습니다!",
  "status": "CONFIRMED",
  "calendarEventCreated": true
}
```

---

### 4.6. 캘린더 외부 연동 (Google Calendar)

> **현재 범위 (안드로이드 v1.0)**  
> ✅ **구글 캘린더** 연동 - 안드로이드 기반으로 완전 지원  
> 🚫 **애플 캘린더** - iOS 버전 출시 시 추가 예정 (구글 iCloud가 없어 안드로이드 앱에서 직접 연동 불가)

#### 구글 캘린더 연동 흐름
```
1. 사용자가 앱에서 "구글 캘린더 연동" 버튼 클릭
2. 앱 → Google OAuth 팝업 → 캘린더 읽기/쓰기 권한 요청
3. 구글이 Authorization Code 반환
4. 앱 → POST /api/calendar/google/connect (code 전달)
5. 서버 → 구글 토큰 교환 → DB에 access/refresh token 저장
6. 이후 일정 확정 시 자동으로 구글 캘린더에 추가
```

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `POST` | `/api/calendar/google/connect` | 구글 캘린더 연동 (OAuth Code 전달) | ✅ |
| `DELETE` | `/api/calendar/google/disconnect` | 구글 캘린더 연동 해제 | ✅ |
| `GET` | `/api/calendar/google/events` | 구글 캘린더 일정 가져오기 | ✅ |
| `POST` | `/api/calendar/google/sync` | 확정 일정을 구글 캘린더에 추가 | ✅ |

#### 구글 캘린더 연동
```json
// POST /api/calendar/google/connect
// Request - 앱에서 OAuth 후 받은 인증 코드
{
  "authCode": "4/0AX4XfWjq..."
}

// Response (200 OK)
{
  "connected": true,
  "googleEmail": "yoonseo@gmail.com",
  "message": "구글 캘린더가 연동되었습니다!"
}
```

#### 구글 캘린더 일정 가져오기 (모아 캘린더에 표시)
```json
// GET /api/calendar/google/events?month=2026-05
// Response (200 OK)
{
  "source": "GOOGLE",
  "events": [
    {
      "id": "구글캘린더이벤트ID",
      "title": "맥날 알바",
      "start": "2026-05-22T18:00",
      "end": "2026-05-22T22:00",
      "color": "#FFD700"
    }
  ]
}
```

#### 안드로이드 기기 로컬 캘린더 저장 (앱 단에서 처리)
```
안드로이드 CalendarContract API를 통해 기기 캘린더에 직접 일정 추가
→ 아이폰 사용자도 구글 캘린더 동기화 시 자동 반영
→ 별도 서버 API 불필요 (앱에서 직접 기기 DB 접근)
```

---

### 4.7. 캘린더 (Moa 내부)

| Method | Endpoint | 설명 | 인증 |
|--------|----------|------|------|
| `GET` | `/api/calendar/events` | 내 캘린더 일정 전체 조회 | ✅ |
| `GET` | `/api/calendar/events?month=2026-05` | 월별 조회 | ✅ |
| `POST` | `/api/calendar/events` | 개인 일정 수동 추가 | ✅ |
| `PUT` | `/api/calendar/events/{id}` | 일정 수정 | ✅ |
| `DELETE` | `/api/calendar/events/{id}` | 일정 삭제 | ✅ |

#### 월별 캘린더 조회
```json
// GET /api/calendar/events?month=2026-05
// Response (200 OK)
{
  "month": "2026-05",
  "events": [
    {
      "id": 1,
      "title": "운영체제 세미나",
      "start": "2026-05-21T10:00",
      "end": "2026-05-21T12:00",
      "color": "#90EE90",
      "source": "GROUP",
      "groupName": "멋쟁이사자처럼"
    },
    {
      "id": 2,
      "title": "맥날 알바",
      "start": "2026-05-22T18:00",
      "end": "2026-05-22T22:00",
      "color": "#FFD700",
      "source": "MANUAL",
      "groupName": null
    }
  ]
}
```

---

## 5. 에러 응답 형식 (공통)

모든 에러는 통일된 형식으로 응답합니다:

```json
{
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "이미 사용 중인 이메일입니다.",
  "timestamp": "2026-05-15T11:43:00"
}
```

| HTTP 상태 | 의미 |
|-----------|------|
| `200` | 성공 |
| `201` | 생성 성공 |
| `400` | 잘못된 요청 (입력값 오류) |
| `401` | 인증 실패 (토큰 없음/만료) |
| `403` | 권한 없음 (그룹 관리자만 가능 등) |
| `404` | 리소스 없음 (그룹/일정 못 찾음) |
| `409` | 충돌 (이미 가입된 이메일 등) |

---

## 6. 2주 개발 일정표

### 1주차 (5/15 ~ 5/21): 백엔드 기반 + 핵심 API

| 날짜 | 작업 내용 |
|------|----------|
| **5/15 (목)** | Spring Boot 프로젝트 생성, MySQL 연결, Entity 정의 |
| **5/16 (금)** | JWT 인증 구현 (일반 회원가입/로그인) |
| **5/17 (토)** | 소셜 로그인 구현 (구글 OAuth2, 카카오 OAuth2) |
| **5/18 (일)** | 그룹 CRUD API (생성/조회/입장/탈퇴) |
| **5/19 (월)** | 일정 조율 API (생성/시간입력) + ⭐ 겹치는 시간 분석 알고리즘 |
| **5/20 (화)** | 캘린더 API + 구글 캘린더 연동 API |
| **5/21 (수)** | API 전체 테스트 (Postman) + 버그 수정 |

### 2주차 (5/22 ~ 5/28): 안드로이드 연동 + 시연 준비

| 날짜 | 작업 내용 |
|------|----------|
| **5/22 (목)** | 안드로이드 Retrofit 세팅 + **구글/카카오 소셜 로그인** 화면 연동 |
| **5/23 (금)** | 그룹 화면 연동 (생성/입장/목록) |
| **5/24 (토)** | 캘린더 화면 ↔ 서버 데이터 연동 + **구글 캘린더 연동** UI |
| **5/25 (일)** | 일정 조율 화면 구현 + 시간 선택 UI |
| **5/26 (월)** | 시각화 화면 (히트맵/추천 결과) 구현 |
| **5/27 (화)** | 전체 시연 흐름 테스트 + UI 다듬기 |
| **5/28 (수)** | 최종 점검 + 시연 리허설 |

---

## 7. 로컬 실행 방법 (시연 시)

```bash
# 1. MySQL 서버 실행 확인
mysql -u root -p

# 2. DB 생성
CREATE DATABASE moa_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. application.yml에 OAuth 키 설정
# google.client-id=YOUR_GOOGLE_CLIENT_ID
# google.client-secret=YOUR_GOOGLE_CLIENT_SECRET
# kakao.rest-api-key=YOUR_KAKAO_REST_API_KEY

# 4. Spring Boot 서버 실행
cd moa-backend
./gradlew bootRun
# → http://localhost:8080 에서 API 서버 가동

# 5. 안드로이드 앱에서 접속
# application.yml의 서버 주소를 같은 와이파이 내 IP로 설정
# 예: http://192.168.0.10:8080
```

### 사전 준비 (개발 시작 전 필수!)

| 준비 사항 | 방법 |
|----------|------|
| **구글 OAuth 클라이언트 ID** | [Google Cloud Console](https://console.cloud.google.com) → API 및 서비스 → 사용자 인증 정보 → OAuth 클라이언트 ID 생성 |
| **구글 캘린더 API 활성화** | Google Cloud Console → API 라이브러리 → Google Calendar API 활성화 |
| **카카오 앱 등록** | [Kakao Developers](https://developers.kakao.com) → 앱 생성 → REST API 키 확인 → 카카오 로그인 활성화 |

---

## 8. 시연 시나리오 (Demo Flow)

1. **구글/카카오 소셜 로그인** → 클릭 한 번으로 빠르게 로그인
2. **구글 캘린더 연동** → 기존 일정이 모아 캘린더에 자동 표시
3. **모임 생성** → "멋쟁이사자처럼" 그룹 생성 → 초대코드 발급
4. **모임 입장** → 다른 기기(또는 계정)에서 초대코드로 입장
5. **일정 조율** → "백엔드 세미나" 일정 생성 (5/20~5/25)
6. **시간 입력** → 각 멤버가 가능한 시간 선택
7. **분석 결과** → 겹치는 시간 히트맵 시각화 확인
8. **시간 확정** → 최적 시간 선택 → 구글 캘린더에도 자동 추가 확인!
