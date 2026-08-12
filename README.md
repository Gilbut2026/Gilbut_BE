# Gilbut Backend

## 1. 프로젝트 실행 준비

### 저장소 Clone

```bash
git clone https://github.com/Gilbut2026/Gilbut_BE.git
cd Gilbut_BE
```

IntelliJ에서 프로젝트를 연 뒤 Gradle 의존성을 새로고침합니다.

---

## 2. 환경변수 설정

프로젝트 최상위 경로에 `.env` 파일을 생성합니다.

```text
Gilbut_BE
├── .env
├── build.gradle
├── gradlew
├── settings.gradle
└── src
```

`.env`의 실제 내용은 **팀 Notion 환경변수 페이지에서 복사하여 사용**합니다.

```properties
# Database
DB_URL=
DB_USERNAME=
DB_PASSWORD=

# JWT
JWT_SECRET_KEY=

# Kakao Login
KAKAO_CLIENT_ID=
KAKAO_REDIRECT_URI=

# TMAP
TMAP_APP_KEY=

# AI
AI_SCORING_URL=http://localhost:8000/routes/score
```

`.env` 파일에는 DB 비밀번호와 API Key가 포함되므로 Git에 올리지 않습니다.

`.gitignore`에 아래 설정이 포함되어 있는지 확인합니다.

```gitignore
.env
.idea/
.gradle/
build/
*.iml
```

---

## 3. MySQL 설정

MySQL 서버를 실행한 뒤 `gilbut` 데이터베이스를 생성합니다.

```sql
CREATE DATABASE IF NOT EXISTS gilbut
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

`.env`의 DB 설정과 로컬 MySQL 설정이 일치해야 합니다.

```properties
DB_URL=jdbc:mysql://localhost:3306/gilbut
DB_USERNAME=root
DB_PASSWORD=로컬_MySQL_비밀번호
```

---

## 4. Spring Boot 실행

### IntelliJ 실행

`GilbutApplication.java`를 열고 Run 버튼을 누릅니다.

### 터미널 실행

Windows:

```powershell
.\gradlew.bat bootRun
```

Mac/Linux:

```bash
./gradlew bootRun
```

아래와 같은 로그가 출력되면 정상적으로 실행된 것입니다.

```text
Tomcat started on port 8080
Started GilbutApplication
```

---

## 5. Swagger 사용

서버 실행 후 아래 주소로 접속합니다.

```text
http://localhost:8080/swagger-ui/index.html
```

Swagger에서는 구현된 API 목록을 확인하고 직접 요청을 테스트할 수 있습니다.

### JWT가 필요 없는 API

- 카카오 로그인
- 토큰 갱신

### JWT가 필요한 API

- 로그아웃
- 사용자 정보 조회
- 사용자 이동 설정
- 맞춤 경로 조회 등

JWT가 필요한 API는 Swagger 상단의 `Authorize` 버튼을 누른 뒤 로그인 응답으로 받은 `accessToken`을 입력합니다.

```text
eyJhbGciOiJIUzI1NiJ9...
```

Swagger 설정에 따라 `Bearer`가 자동으로 추가되므로 일반적으로 토큰 값만 입력합니다.

---

## 6. 카카오 로그인 테스트

### 1) 카카오 인가 코드 발급

프론트엔드에서 카카오 로그인 후 Redirect URI로 전달된 `code` 값을 확인합니다.

```text
http://localhost:5173/auth/kakao/callback?code=인가코드
```

### 2) Swagger에서 로그인 API 호출

```http
POST /api/auth/kakao-login
```

Request Body:

```json
{
  "code": "카카오_인가코드"
}
```

성공 응답:

```json
{
  "success": true,
  "message": "성공",
  "data": {
    "accessToken": "서비스_access_token",
    "refreshToken": "서비스_refresh_token"
  }
}
```

카카오 인가 코드는 일회용이므로 이미 사용한 코드는 다시 사용할 수 없습니다.

---

## 7. 인증 흐름

```text
카카오 인가 코드 수신
→ 카카오 토큰 API 호출
→ 카카오 Access Token 발급
→ 카카오 사용자 정보 조회
→ DB에서 사용자 조회 또는 저장
→ 길벗 서비스용 JWT 발급
→ Access Token과 Refresh Token 반환
```

로그인 이후 API 요청에는 길벗 백엔드가 발급한 Access Token을 사용합니다.

```http
Authorization: Bearer {accessToken}
```

---

## 8. API 명세

API별 요청값, 응답값, JWT 필요 여부 및 개발 현황은 팀 Notion의 **API 명세서**에서 관리합니다.

API를 추가하거나 수정한 경우 다음 항목을 함께 업데이트합니다.

- 기능명
- HTTP Method
- URI
- JWT 필요 여부
- Request
- Response
- 오류 코드
- 개발 현황
- 프론트 연동 현황

맞춤 경로 응답의 `recommendations[]`에는 AI가 반환한 `slopeSummary`와
`scoreBreakdown.slopePenalty`가 포함됩니다. TMAP WALK 원본 좌표는 AI 호출에만
사용하며 공개 `candidate` JSON에는 포함하지 않습니다.

---

## 9. Git 작업 시 주의사항

작업 전 원격 변경사항을 확인합니다.

```bash
git pull origin develop
```

기능별 브랜치를 생성합니다.

```bash
git switch -c feature/기능명
```

작업 완료 후:

```bash
git add .
git commit -m "feat: 기능 설명"
git push origin feature/기능명
```

`.env`, API Key, 비밀번호, 실제 JWT 토큰은 절대 커밋하지 않습니다.
