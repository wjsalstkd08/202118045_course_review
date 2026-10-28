# 강의 수강 후기 게시판 (COURSE REVIEW)

웹서비스설계 JWT 게시판 과제입니다.

## 작성자와 서비스 소개

- 학번 / 이름: 202118045 / 전민상
- 수강신청을 앞둔 학생이 강의의 수업 방식과 과제·시험 부담을 미리 확인할 수 있도록 수강 후기를 공유하는 게시판이다. 누구나 로그인 없이 후기를 읽을 수 있고, 로그인한 사용자만 후기를 작성할 수 있으며, 자신이 쓴 후기만 수정·삭제할 수 있다. 과목명은 제목에 [과목명] 형식으로 적는다.
- 선택한 기술: Vue + Spring Boot
- 주요 기능: 공개 후기 목록·상세, JWT 로그인, 내 정보 조회, 본인 후기 작성·수정·삭제, 토큰 만료·로그아웃 처리

## 보고서와 테스트 증거

- [구현 보고서 PDF](docs/report.pdf)
- [테스트 증거](docs/evidence/)

## 프로젝트 구조

```text
.
├── README.md
├── .gitignore
├── frontend-vue/      # Vue 3 + Vite (제공 템플릿)
├── backend-spring/    # Spring Boot 3.5 + SQLite (제공 템플릿)
├── tools/
│   └── check_api.py   # 제공 자동 API 검사 도구
└── docs/
    ├── report.pdf
    └── evidence/
```

## 실행 환경

| 항목 | 사용 버전 |
|---|---|
| 운영체제 | Windows 10 |
| 프론트 런타임 Node.js | v24.22.1 |
| 백엔드 런타임 | JDK Temurin 21.0.12.1 (JDK 17 이상 필요) |
| 프레임워크 | Vue 3.5.43, Vite 8.3.4 / Spring Boot 3.5.16, JJWT 0.13.0 |
| 데이터 저장 | SQLite (sqlite-jdbc 3.51.3.0) |

## 설치와 실행

### 1 저장소 복제

```sh
git clone https://github.com/wjsalstkd08/202118045_course_review
cd course_review
```

### 2 백엔드 준비와 실행 (터미널 A)

Spring은 `.env`를 자동으로 읽지 않으므로 서버를 실행할 터미널에서 환경변수를 설정합니다. 먼저 새 키를 생성합니다.

```sh
node -e "console.log(require('node:crypto').randomBytes(32).toString('hex'))"
```

Windows PowerShell:

```powershell
cd backend-spring
$env:JWT_SECRET='위에서_생성한_64자리_16진수'
$env:TOKEN_TTL='90'
.\mvnw.cmd package
java -jar target\jwt-board-1.0.0.jar
```



- 환경설정 예시 파일: `backend-spring/env.example.txt`
- JWT_SECRET 설정 방법: 위 "백엔드 준비와 실행"의 키 생성 명령으로 64자리 16진수 키를 만들고, 서버를 실행할 터미널에서 `$env:JWT_SECRET`으로 설정합니다.
- TOKEN_TTL: 기본 90초. 만료 테스트는 `TOKEN_TTL=3`으로 서버를 다시 시작합니다.
- 백엔드 주소: http://127.0.0.1:8000
- 정상 실행 확인: http://127.0.0.1:8000/api/health 에서 `"status":"ok"`

### 3 프론트 빌드와 로컬 실행 (터미널 B)

```sh
cd frontend-vue
npm ci
npm run build
npm run preview
```

- 최종 확인 주소: http://127.0.0.1:4173
- 개발 중 실행: `npm run dev`, http://127.0.0.1:5173
- API 연결: 브라우저의 `/api` 요청을 Vite preview(4173)dev(5173) 서버가 8000번 백엔드로 프록시합니다. 백엔드 포트를 바꾸면 `frontend-vue/vite.config.js`의 `server.proxy`와 `preview.proxy`를 함께 수정합니다.
- 종료: 각 터미널에서 Ctrl+C
- 코드 변경 후: 프론트는 `npm run build` 후 `npm run preview` 재실행, 백엔드는 `mvnw package` 후 jar 재실행

### 4 테스트 계정과 DB

| 사용자 | 이메일 | 실습용 비밀번호 |
|---|---|---|
| Alice | alice@class.test | Classroom!2026 |
| Bob | bob@class.test | Classroom!2026 |

- SQLite 파일: `backend-spring/data/board.sqlite` (서버 시작 시 폴더·파일·테이블 자동 생성)
- DB 초기화: 서버 종료 후 `backend-spring/data/board.sqlite` 삭제
- 서버 재시작 후 데이터 유지 확인 결과: 백엔드 종료, 재실행 전후 글, 작성자, 작성 시각 동일

## 구현 완료 범위

| 기능 | 완료 여부 | 주요 파일 또는 함수 |
|---|---|---|
| 공개 목록과 상세 | 완료 | `BoardController.list/get` (제공 코드) |
| 로그인과 JWT 발급 | 완료 | `AuthService` 생성자·`issueToken`, `api.js login` |
| JWT 검증과 내 정보 조회 | 완료 | `AuthService.currentUserId`, `BoardController.me` |
| 글 작성과 작성자 저장 | 완료 | `BoardController.create` (작성자 = 검증된 사용자 ID) |
| 본인 글 수정·삭제 | 완료 | `AuthService.assertOwner`, `api.js canEdit` |
| 토큰 만료와 로그아웃 | 완료 | `api.js request`(401 시 세션 삭제), `App.vue logout` |
| 입력 오류와 권한 오류 처리 | 완료 | `BoardController.fields`·`Errors`(제공 코드), `App.vue run`(404 목록 갱신) |


## 테스트 방법과 결과 요약

- 자동 검사: 백엔드 실행 후 저장소 루트에서 `py tools/check_api.py --mode final` → 40/40 PASS. `TOKEN_TTL=3`으로 재시작 후 `--expiry` 추가 → 48/48 PASS
- Alice/Bob 권한 구분: Bob 토큰으로 Alice 글 수정·삭제 → 403 FORBIDDEN, 데이터 변함 없음 (T07, T08)
- HTTP 상태와 오류 code: 200·201·204, 400 BAD_INPUT, 401 INVALID_CREDENTIALS·TOKEN_REQUIRED·INVALID_TOKEN·TOKEN_EXPIRED, 403 FORBIDDEN, 404 NOT_FOUND 모두 확인 (보고서 4장)
- 응답 헤더와 본문: 등록 201 + `Location: /api/posts/{id}`, 보호 API 401 + `WWW-Authenticate: Bearer`, 삭제 204 빈 본문
- 토큰 없음·변조·만료: 각각 401 TOKEN_REQUIRED / INVALID_TOKEN / TOKEN_EXPIRED (T04, T09, T10)
- 전체 T01~T17: 17개 모두 PASS, 결과와 증거는 [보고서](docs/report.pdf) 4장 및 [docs/evidence](docs/evidence/)

## 핵심 구현 설명

1. 로그인: `BoardController.login`이 제공된 `Users.verify`로 비밀번호를 확인한 뒤 `AuthService.issueToken`이 HS256 JWT(sub·iss·aud·iat·exp)를 발급합니다.
2. 토큰 보관·전송: `frontend-vue/src/api.js`가 토큰과 사용자를 모듈 변수(메모리)에만 저장하고, `authorizationHeaders`가 토큰이 있을 때만 `Authorization: Bearer` 헤더를 붙입니다.
3. 서버 검증: `AuthService.currentUserId`가 헤더 형식 확인 → JJWT로 서명·알고리즘·만료·iss·aud 검증 → sub 형식과 실제 계정 확인 후 사용자 ID를 반환합니다. 실패는 TOKEN_REQUIRED / INVALID_TOKEN / TOKEN_EXPIRED(401)로 구분합니다.
4. 작성자 권한: 수정·삭제 시 `AuthService.assertOwner`가 저장된 글의 authorId와 검증된 사용자 ID를 비교해 다르면 403 FORBIDDEN을 반환하며, 이 검사는 DB 변경 전에 실행됩니다.

## AI 활용 요약과 참고 자료

- AI 사용 여부: 사용
- 도구와 사용 목적: Claude (Anthropic) — `AuthService.java`·`api.js`·`App.vue` 구현 초안, 테스트 절차
- 반영 위치와 직접 수정·검증한 내용: `AuthService.java`, `api.js`, 테스트 과정 참고 및 사용
- 상세 활용 기록: 보고서 5장
- 참고 자료: 웹서비스설계 6·7주차 강의자료(JWT 로그인과 인증, JWT 접근제어)
