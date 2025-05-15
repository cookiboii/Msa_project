# 🎖️온라인 강의 플랫폼

## 1. 기획서

### 프로젝트 개요

본 프로젝트는 Spring Boot 기반의 마이크로서비스 아키텍처를 적용한 온라인 강의 플랫폼입니다. 강의 영상 업로드, 검색, 구매 및 수강 기능을 제공하며, 수강생과 강사로 구분된 서비스를 제공합니다.

### 목표 및 범위

**목표**: 온라인 강의 시장에 최적화된 확장 가능하고 유지보수가 용이한 플랫폼 구축

**범위**:
- **포함**: 사용자 인증, 강의 등록 및 조회, 결제, 동영상 스트리밍, 마이페이지
- **제외**: 실시간 스트리밍 기능, 모바일 앱 구현

### 타겟 사용자

- **일반 수강생**: 강의를 검색하고 구매 및 수강하는 사용자
- **강사**: 강의를 등록하고 강의 수익을 확인할 수 있는 사용자

### 주요 기능 목록

1. **회원가입 및 로그인 (JWT 인증)**
2. **강의 목록 조회 및 검색**
3. **강의 상세 페이지**
4. **강의 구매 및 결제 처리**
5. **강의 영상 스트리밍**
6. **강사 전용 강의 업로드/수정/삭제**

### 기술 스택

- **백엔드**: Spring Boot, Spring Cloud (Config, Eureka, Gateway), Spring Security, Spring Data JPA
- **프론트엔드**: React, Vite, Axios, CSS
- **DB**: MySQL
- **인프라**: AWS EC2, S3, RDS, CloudFront

### 마이크로서비스 구조

1. **Auth-Service**: 사용자 인증 및 권한 관리 (JWT 기반)
2. **User-Service**: 사용자 정보 관리 (수강생, 강사, 관리자)
3. **Course-Service**: 강의 등록, 조회, 수정, 삭제
4. **Order-Service**: 강의 구매 및 결제 내역 관리
5. **Streaming-Service**: S3 기반 강의 영상 스트리밍 처리
6. **Notification-Service**: 결제 성공, 강의 승인 등 이벤트 알림
7. **Gateway-Service**: API 게이트웨이 (라우팅, 인증 필터)
8. **Config-Service**: 공통 설정 관리 (Spring Cloud Config)
9. **Eureka-Service**: 서비스 디스커버리
![제목 없는 다이어그램 drawio (2)](https://github.com/user-attachments/assets/d0e9b721-18c8-4484-942d-d547c937fea4)
## 2. 요구사항 정의서

### 2-1. 기능 요구사항

#### 사용자 서비스
| ID | 요구사항명 | 내용 | 담당자 | 우선순위 |
| --- | --- | --- | --- | --- |
| US-01 | 회원가입 | 사용자 정보를 입력받아 회원가입 처리 및 DB 저장 | 나석후 | 높음 |
| US-02 | 로그인 | 사용자 입력 정보와 DB정보를 대조하여 로그인 처리 | 나석후 | 높음 |
| US-03 | 로그아웃 | 로그인 상태 종료 및 세션 삭제 처리 | 나석후 | 중간 |
| US-04 | 마이페이지  | 마이페이지 들어가서 비밀번호 변경 구현  | 나석후 | 중간 |
 

#### 결제 서비스
| ID | 요구사항명 | 내용 | 담당자 | 우선순위 |
| --- | --- | --- | --- | --- |
| OD-01 | 강의 담기 | 사용자가 결제 전 장바구니에 강의를 담을 수 있음 | 김현지 | 중간 |
| OD-02 | 담은 강의 삭제 | 사용자가 장바구니에 담은 강의를 삭제 | 김현지 | 중간 |
| OD-03 | 구매 결제 | 사용자가 강의를 구매하고 결제 | 김현지 | 높음 |
| OD-04 | 결제 취소 | 사용자가 결제한 강의를 결제 취소 | 김현지 | 중간 |

#### 강의 서비스
| ID | 요구사항명 | 내용 | 담당자 | 우선순위 |
| --- | --- | --- | --- | --- |
| COR-01 | 강의 검색 | 사용자가 수강할 수 있는 강의 조회 | 김지원 | 높음 |
| COR-02 | 강의 추가 | 강사가 강의 콘텐츠를 업로드 | 김지원 | 높음 |
| COR-03 | 강의 수정 | 강사가 본인이 등록한 강의를 수정 | 김지원 | 중간 |
| COR-04 | 강의 삭제 | 강사가 본인이 등록한 강의를 삭제 | 김지원 | 중간 |

#### 댓글 서비스
| ID | 요구사항명 | 내용 | 담당자 | 우선순위 |
| --- | --- | --- | --- | --- |
| PO-01 | 댓글 추가 | 사용자가 강의에 댓글을 남김 | 이은혁 | 높음 |
| PO-02 | 댓글 수정 | 사용자가 본인이 남긴 댓글을 수정 | 이은혁 | 중간 |
| PO-03 | 댓글 삭제 | 사용자가 본인이 남긴 댓글을 삭제 | 이은혁 | 중간 |

### 2-2. 비기능 요구사항(예시/성능, 보안, 확장성 등 시스템의 품질 속성)
| ID | 요구사항명 | 내용 | 담당자 | 우선순위 |
| --- | --- | --- | --- | --- |
| NF-01 | 서버 이용량 | 시스템은 동시에 100명의 사용자를 처리할 수 있어야 한다. | - | 중간 |
| NF-02 | 로딩시간 | 페이지 로딩 시간은 3초 이내여야 한다. | - | 높음 |
| NF-03 | DB 저장형식 | 모든 비밀번호는 암호화되어 저장되어야 한다. | - | 높음 |

### 2-3. 제약사항(예시/개발 과정에서 고려해야 할 제한사항)
| ID | 요구사항명 | 내용 | 담당자 | 우선순위 |
| --- | --- | --- | --- | --- |
| C-01 | AWS 서비스(EC2, S3)만을 사용하여 배포해야 한다. | - | 중간 |
| C-02 | 마이크로서비스 간 통신은 REST API를 통해 이루어져야 한다. | - | 중간 |

## 3. 인터페이스 설계서

### 3-1. API 설계

### 🔸사용자 서비스 API
  1. **회원가입**
     - URL: `POST /api/users`
     - Request Body:
       ```json
       {
         "email": "user@example.com",
         "password": "password123",
         "name": "홍길동"
       }
       ```
     - Response (성공 - 201 Created):
       ```json
       {
         "id": "12345",
         "email": "user@example.com",
         "name": "홍길동"
       }
       ```
  
  2. **로그인**
     - URL: `POST /api/auth/login`
     - Request Body:
       ```json
       {
         "email": "user@example.com",
         "password": "password123"
       }
       ```
     - Response (성공 - 200 OK):
       ```json
       {
         "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
         "user": {
           "id": "12345",
           "email": "user@example.com",
           "name": "홍길동"
         }
       }
       ```

### 🔸강의 서비스 API
  1. **강의 목록 조회**
     - URL: `GET /api/lectures`
     - Response (성공 - 200 OK):
       ```json
       [
         {
           "id": "1",
           "title": "Java 기초 강의",
           "description": "Java를 배우는 기초 강의입니다.",
           "price": 10000
         },
         {
           "id": "2",
           "title": "Spring Boot 강의",
           "description": "Spring Boot로 웹 애플리케이션을 개발하는 강의입니다.",
           "price": 20000
         }
       ]
       ```
  
  2. **강의 상세 조회**
     - URL: `GET /api/lectures/{lectureId}`
     - Response (성공 - 200 OK):
       ```json
       {
         "id": "1",
         "title": "Java 기초 강의",
         "description": "Java를 배우는 기초 강의입니다.",
         "price": 10000,
         "videoUrl": "https://s3.amazonaws.com/video/java_intro.mp4",
         "comments": [
           {
             "id": "101",
             "userId": "12345",
             "content": "이 강의 정말 유익했어요!",
             "createdAt": "2025-05-12T10:00:00",
             "replies": [
               {
                 "id": "201",
                 "userId": "67890",
                 "content": "감사합니다! 더 많은 강의 준비 중입니다.",
                 "createdAt": "2025-05-12T10:15:00"
               }
             ]
           }
         ]
       }
       ```
  
  3. **강의 등록 (강사 전용)**
    - URL: `POST /api/lectures`
    - Request Body:
      ```json
      {
        "title": "Spring Boot 입문",
        "description": "Spring Boot 기본 개념 강의입니다.",
        "price": 15000,
        "category": "백엔드",
        "instructorId": "999"
      }
      ```
    - Response (성공 - 201 Created):
      ```json
      {
        "id": "101",
        "title": "Spring Boot 입문",
        "instructorId": "999"
      }
      ```
     

### 🔸댓글 서비스 API
  1. **강의 댓글 달기**
     - URL: `POST /api/lectures/{lectureId}/comments`
     - Request Body:
       ```json
       {
         "userId": "12345",
         "content": "이 강의 정말 유익했어요!"
       }
       ```
     - Response (성공 - 201 Created):
       ```json
       {
         "id": "101",
         "userId": "12345",
         "content": "이 강의 정말 유익했어요!",
         "createdAt": "2025-05-12T10:00:00"
       }
       ```
  
  2. **강사 댓글에 답글 달기**
     - URL: `POST /api/comments/{commentId}/replies`
     - Request Body:
       ```json
       {
         "userId": "67890",  // 강사 ID
         "content": "감사합니다! 더 많은 강의 준비 중입니다."
       }
       ```
     - Response (성공 - 201 Created):
       ```json
       {
         "id": "201",
         "userId": "67890",  // 강사 ID
         "content": "감사합니다! 더 많은 강의 준비 중입니다.",
         "createdAt": "2025-05-12T10:15:00"
       }
       ```

### 🔸결제 서비스 API
  1. **강의 구매**
     - URL: `POST /api/orders`
     - Request Body:
       ```json
       {
         "lectureId": "1",
         "userId": "12345",
         "paymentMethod": "카드"
       }
       ```
     - Response (성공 - 201 Created):
       ```json
       {
         "orderId": "98765",
         "status": "SUCCESS",
         "totalPrice": 10000
       }
       ```

### 3-2. 서비스 간 통신 설계

1. **강의 서비스 -> 결제 서비스**
   - 목적: 강의 구매 시 결제 처리
   - 통신 방식: REST API 호출
   - 엔드포인트: `POST /api/orders`

2. **결제 서비스 -> 알림 서비스**
   - 목적: 결제 완료 후 사용자에게 알림 발송
   - 통신 방식: 비동기 메시지 (이벤트 발행)
   - 이벤트 타입: `OrderCompletedEvent`

3. **강의 서비스 -> 스트리밍 서비스**
   - 목적: 강의 상세 페이지에서 영상 스트리밍 URL 제공
   - 통신 방식: S3 URL 호출
   - 엔드포인트: `GET /api/lectures/{lectureId}/video`

4. **강의 서비스 -> 댓글 서비스**
   - 목적: 강의에 대한 댓글과 답글 조회 및 작성
   - 통신 방식: REST API 호출
   - 엔드포인트:
     - 댓글 조회: `GET /api/lectures/{lectureId}/comments`
     - 댓글 달기: `POST /api/lectures/{lectureId}/comments`
     - 답글 달기: `POST /api/comments/{commentId}/replies`

5. **결제 서비스 -> 강의 서비스**
   - 목적: 환불 시 강의 수강권 해지
   - 통신 방식: REST API 호출
   - 엔드포인트: `DELETE /api/lectures/{lectureId}/enrollments/{userId}`
    
6. **강의 서비스 -> 알림 서비스**
   - 목적: 강의 등록/삭제 시 알림
   - 이벤트 타입: `LectureCreatedEvent`, `LectureDeletedEvent`


### 3-3. UI 화면 설계

  ### 📌 **회원가입 화면**
  - **구성 요소**:
    - 이메일 입력 필드
    - 비밀번호 입력 필드
    - 이름 입력 필드
    - 회원가입 버튼
  - **사용자 인터랙션**:
    - 사용자는 이메일, 비밀번호, 이름을 입력하고 "회원가입" 버튼을 클릭하여 회원가입을 진행합니다.
  
  ### 📌 **강의 목록 화면**
  - **구성 요소**:
    - 강의 목록 (강의 제목, 가격, 짧은 설명)
    - 강의 검색 필터 (카테고리, 키워드)
    - 각 강의를 클릭하면 강의 상세 페이지로 이동
  - **사용자 인터랙션**:
    - 사용자는 강의를 클릭하여 강의 상세 페이지로 이동하거나, 검색 필터를 사용하여 원하는 강의를 찾을 수 있습니다.
  
  ### 📌 **강의 상세 페이지**
  - **구성 요소**:
    - 강의 제목, 설명, 가격
    - 강의 영상 스트리밍 (S3 기반)
    - "구매하기" 버튼
    - 댓글 목록
    - 댓글 작성 입력 필드
    - 강사 댓글 답글 작성 필드
  - **사용자 인터랙션**:
    - 사용자는 강의 영상을 보고 "구매하기" 버튼을 클릭하여 강의를 구매할 수 있습니다.
    - 사용자는 강의에 댓글을 달 수 있습니다.
    - 강사는 자신이 올린 강의에 달린 댓글에 답글을 달 수 있습니다.
   
  ### 📌 **강사 강의 등록 화면**
  - **구성 요소**:
    - 강의 제목 입력 필드
    - 강의 설명 입력 필드
    - 강의 가격 입력 필드
    - 강의 url 입력 필드
    - 카테고리 선택
    - "강의 등록" 버튼
  - **사용자 인터랙션**:
    - 사용자(강사)는 강의 제목, 설명, 가격, url을 입력하고 "강의 등록" 버튼을 클릭하여 강의 등록을 진행합니다.
   
  ### 📌 **마이페이지 > 구매 강의 목록 화면**
  - **구성 요소**:
    - 수강중인 강의 리스트
    - "수강 취소" 버튼(조건부 노출)
  - **사용자 인터랙션**:
    - 사용자는 각 강의 옆에 있는 "수강 취소" 버튼을 눌러 구매한 강의의 수강을 철회할 수 있습니다.
   

### 3.4 사용자 흐름도
  
  🔹 A[로그인한 사용자] --> B[강의 목록 조회] --> C[강의 구매] --> D[수강 시작] --> E[댓글 작성] --> F[강사 답글 작성]  
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
 ㄴ--> G[강의 취소 요청] --> H[환불 처리 완료]  
  
 🔹 A[로그인한 강사] --> I[강의 등록] --> J[강의 상세 페이지 생성] --> K[강의 삭제 요청] --> L[강의 삭제]  
     

## 4. 소스코드

## 5. 테스트 케이스

## 6. 테스트 케이스 결과



