# Jandi Plan Backend API 명세서

## 개요

본 문서는 Jandi Plan 백엔드의 모든 REST API를 정의한다. 각 엔드포인트의 인증 요구사항, 요청/응답 형식, 주요 예외 처리를 포함한다.

---

# 1. 사용자 관리 (User Domain)

## 1.1 인증/회원가입

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 회원가입 | POST | `/api/users/register` | X | 성, 이름, 이메일, 비밀번호, 닉네임으로 가입 |
| 이메일 중복 확인 | GET | `/api/users/register/checkEmail` | X | 이메일 사용 가능 여부 확인 |
| 닉네임 중복 확인 | GET | `/api/users/register/checkName` | X | 닉네임 사용 가능 여부 확인 |
| 로그인 | POST | `/api/users/login` | X | 이메일, 비밀번호로 로그인 → JWT 발급 |
| 토큰 갱신 | POST | `/api/users/token/refresh` | X | 리프레시 토큰으로 새 액세스 토큰 발급 |
| 이메일 인증 | GET | `/api/users/verify` | X | 이메일 인증 토큰 검증 |
| 비밀번호 찾기 | POST | `/api/users/forgot` | X | 임시 비밀번호 이메일 발송 |
| 프로필 조회 | GET | `/api/users/profile` | O | 현재 로그인한 사용자 정보 조회 |
| 비밀번호 변경 | PUT | `/api/users/change-password` | O | 기존 비밀번호 확인 후 변경 |
| 닉네임 변경 | PATCH | `/api/users/change-username` | O | 닉네임 변경 |
| 회원 탈퇴 | DELETE | `/api/users/del-user` | O | 계정 삭제 |

## 1.2 소셜 로그인

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 카카오 로그인 | GET | `/api/auth/kakaoLogin` | X | 카카오 OAuth 로그인 |
| 구글 로그인 | GET | `/api/auth/googleLogin` | X | 구글 OAuth 로그인 |
| 네이버 로그인 | GET | `/api/auth/naverLogin` | X | 네이버 OAuth 로그인 |

## 1.3 선호 여행지 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 대륙 목록 조회 | GET | `/api/trip/continents` | X | 필터링 가능한 대륙 목록 |
| 국가 목록 조회 | GET | `/api/trip/countries` | X | 필터/카테고리로 국가 조회 |
| 도시 목록 조회 | GET | `/api/trip/cities` | X | 필터/카테고리로 도시 조회 |
| 인기 도시 조회 | GET | `/api/trip/rank` | X | 좋아요/조회수 기준 상위 N개 |
| 선호 도시 조회 | GET | `/api/trip/cities/prefer` | O | 사용자의 선호 도시 목록 조회 |
| 선호 도시 등록 | POST | `/api/trip/cities/prefer` | O | 선호 도시 추가 |
| 선호 도시 수정 | PATCH | `/api/trip/cities/prefer` | O | 선호 도시 목록 수정 |

---

# 2. 여행 계획 (TripPlan Domain)

## 2.1 여행 계획 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 전체 여행 조회 | GET | `/api/trip/allTrips` | △ | 공개된 여행 계획 목록 (페이징) |
| 인기 여행 조회 | GET | `/api/trip/top-likes` | X | 좋아요 상위 10개 여행 |
| 내 여행 목록 | GET | `/api/trip/my/allTrips` | O | 내 여행 계획 목록 |
| 좋아요한 여행 | GET | `/api/trip/my/likedTrips` | O | 좋아요한 여행 목록 |
| 여행 상세 조회 | GET | `/api/trip/{tripId}` | △ | 특정 여행 계획 상세 |
| 여행 생성 | POST | `/api/trip/my/create` | O | 여행 계획 생성 |
| 여행 수정 | PATCH | `/api/trip/my/{tripId}` | O | 제목, 공개/비공개 수정 |
| 여행 삭제 | DELETE | `/api/trip/my/{tripId}` | O | 여행 계획 삭제 |
| 여행 좋아요 | POST | `/api/trip/my/likedTrips/{tripId}` | O | 좋아요 추가 |
| 좋아요 취소 | DELETE | `/api/trip/my/likedTrips/{tripId}` | O | 좋아요 해제 |
| 여행 검색 | GET | `/api/trip/search` | △ | 카테고리/키워드 검색 |

## 2.2 동반자 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 동반자 목록 | GET | `/api/trip/{tripId}/participants` | X | 여행 동반자 목록 조회 |
| 동반자 추가 | POST | `/api/trip/{tripId}/participants` | O | 닉네임으로 동반자 추가 |
| 동반자 삭제 | DELETE | `/api/trip/{tripId}/participants/{userName}` | O | 동반자 제거 |

## 2.3 일정 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 일정 조회 | GET | `/api/trip/itinerary/{tripId}` | △ | 여행의 전체 일정 조회 |
| 일정 생성 | POST | `/api/trip/itinerary/{tripId}` | O | 새 일정 추가 |
| 일정 수정 | PATCH | `/api/trip/itinerary/{itineraryId}` | O | 일정 정보 수정 |
| 일정 삭제 | DELETE | `/api/trip/itinerary/{itineraryId}` | O | 일정 삭제 |

## 2.4 예약 정보 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 예약 조회 | GET | `/api/trip/reservation/{tripId}` | O | 여행의 예약 정보 조회 |
| 예약 생성 | POST | `/api/trip/reservation/{tripId}` | O | 예약 정보 추가 |
| 예약 수정 | PATCH | `/api/trip/reservation/{reservationId}` | O | 예약 정보 수정 |
| 예약 삭제 | DELETE | `/api/trip/reservation/{reservationId}` | O | 예약 정보 삭제 |

## 2.5 장소 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 장소 조회 | GET | `/api/place/{placeId}` | X | 특정 장소 정보 조회 |
| 전체 장소 | GET | `/api/place` | X | 모든 장소 목록 조회 |
| 장소 생성 | POST | `/api/place` | O | 새 장소 등록 |

---

# 3. 커뮤니티 (Community Domain)

## 3.1 게시물 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 게시물 목록 | GET | `/api/community/posts` | X | 게시물 목록 조회 (페이징) |
| 게시물 상세 | GET | `/api/community/posts/{postId}` | △ | 특정 게시물 조회 |
| 게시물 생성 | POST | `/api/community/posts` | O | 게시물 작성 (임시→최종) |
| 게시물 수정 | PATCH | `/api/community/posts/{postId}` | O | 게시물 수정 |
| 게시물 삭제 | DELETE | `/api/community/posts/{postId}` | O | 게시물 삭제 |
| 게시물 좋아요 | POST | `/api/community/posts/likes/{postId}` | O | 게시물 좋아요 |
| 좋아요 취소 | DELETE | `/api/community/posts/likes/{postId}` | O | 좋아요 취소 |
| 게시물 신고 | POST | `/api/community/posts/reports/{postId}` | O | 게시물 신고 |
| 게시물 검색 | GET | `/api/community/search` | X | 카테고리/키워드 검색 |

## 3.2 댓글 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 댓글 조회 | GET | `/api/community/comments/{postId}` | △ | 게시물의 댓글 목록 조회 |
| 답글 조회 | GET | `/api/community/replies/{commentId}` | △ | 댓글의 답글 목록 조회 |
| 댓글 작성 | POST | `/api/community/comments/{postId}` | O | 댓글 작성 |
| 답글 작성 | POST | `/api/community/replies/{commentId}` | O | 답글 작성 |
| 댓글 수정 | PATCH | `/api/community/comments/{commentId}` | O | 댓글/답글 수정 |
| 댓글 삭제 | DELETE | `/api/community/comments/{commentId}` | O | 댓글/답글 삭제 |
| 댓글 좋아요 | POST | `/api/community/comments/likes/{commentId}` | O | 댓글 좋아요 |
| 좋아요 취소 | DELETE | `/api/community/comments/likes/{commentId}` | O | 좋아요 취소 |
| 댓글 신고 | POST | `/api/community/comments/reports/{commentId}` | O | 댓글 신고 |

---

# 4. 리소스 (Resource Domain)

## 4.1 배너 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 배너 목록 | GET | `/api/banner/lists` | X | 활성화된 배너 목록 조회 |
| 배너 생성 | POST | `/api/banner/lists` | O (관리자) | 새 배너 등록 |
| 배너 수정 | PATCH | `/api/banner/lists/{bannerId}` | O (관리자) | 배너 정보 수정 |
| 배너 삭제 | DELETE | `/api/banner/lists/{bannerId}` | O (관리자) | 배너 삭제 |

## 4.2 공지사항 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 공지 목록 | GET | `/api/notice/lists` | X | 공지사항 목록 조회 |
| 공지 생성 | POST | `/api/notice` | O (관리자) | 공지사항 작성 |
| 공지 수정 | PATCH | `/api/notice/{noticeId}` | O (관리자) | 공지사항 수정 |
| 공지 삭제 | DELETE | `/api/notice/{noticeId}` | O (관리자) | 공지사항 삭제 |

---

# 5. 이미지 (Image Domain)

## 5.1 이미지 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 이미지 업로드 | POST | `/api/images/upload` | O | 이미지 업로드 → GCS 저장 |
| 이미지 조회 | GET | `/api/images/{imageId}` | X | 이미지 공개 URL 조회 |
| 이미지 수정 | PUT | `/api/images/{imageId}` | O | 이미지 파일 교체 |
| 이미지 삭제 | DELETE | `/api/images/{imageId}` | O | 이미지 삭제 (인증 필수) |

## 5.2 타겟별 이미지 업로드

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 게시글 이미지 | POST | `/api/images/upload/community` | O | 게시글 이미지 업로드 |
| 공지사항 이미지 | POST | `/api/images/upload/notice` | O (관리자) | 공지사항 이미지 업로드 |
| 프로필 이미지 | POST | `/api/images/upload/profile` | O | 프로필 사진 업로드 |
| 여행 이미지 | POST | `/api/images/upload/trip` | O | 여행 계획 이미지 업로드 |

## 5.3 임시 게시물

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 임시 ID 발급 | POST | `/api/temp` | O | 이미지 업로드용 임시 ID 발급 |

---

# 6. Google Maps 추천 (GooglePlace Domain)

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 맛집 추천 | POST | `/api/map/recommend/restaurant` | X | 도시별 맛집 추천 (Google API) |

---

# 7. 관리자 (Admin Domain)

## 7.1 사용자 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 전체 사용자 | GET | `/api/manage/user/all` | O (관리자) | 전체 사용자 목록 조회 |
| 제한 사용자 | GET | `/api/manage/user/reported` | O (관리자) | 제한된 사용자 목록 조회 |
| 사용자 제한 | POST | `/api/manage/user/permit/{userId}` | O (관리자) | 사용자 제한/해제 토글 |
| 사용자 탈퇴 | DELETE | `/api/manage/user/delete/{userId}` | O (관리자) | 강제 탈퇴 처리 |
| 권한 변경 | PUT | `/api/manage/user/change-role/{userId}` | O (관리자) | 사용자 권한 변경 |

## 7.2 커뮤니티 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 신고 게시물 | GET | `/api/manage/community/reported/posts` | O (관리자) | 신고된 게시물 목록 조회 |
| 신고 댓글 | GET | `/api/manage/community/reported/comments` | O (관리자) | 신고된 댓글 목록 조회 |
| 게시물 삭제 | DELETE | `/api/manage/community/delete/posts/{postId}` | O (관리자) | 신고 게시물 삭제 |
| 댓글 삭제 | DELETE | `/api/manage/community/delete/comments/{commentId}` | O (관리자) | 신고 댓글 삭제 |

## 7.3 여행지 관리

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 대륙 생성 | POST | `/api/manage/trip/continents` | O (관리자) | 새 대륙 등록 |
| 국가 생성 | POST | `/api/manage/trip/countries` | O (관리자) | 새 국가 등록 |
| 도시 생성 | POST | `/api/manage/trip/cities` | O (관리자) | 새 도시 등록 |
| 국가 수정 | PATCH | `/api/manage/trip/countries/{countryId}` | O (관리자) | 국가 정보 수정 |
| 도시 수정 | PATCH | `/api/manage/trip/cities/{cityId}` | O (관리자) | 도시 정보 수정 |
| 국가 삭제 | DELETE | `/api/manage/trip/countries/{countryId}` | O (관리자) | 국가 삭제 |
| 도시 삭제 | DELETE | `/api/manage/trip/cities/{cityId}` | O (관리자) | 도시 삭제 |

## 7.4 통계

| 기능 | Method | Endpoint | 인증 | 설명 |
|------|--------|----------|------|------|
| 월별 가입자 | GET | `/api/manage/util/month/users` | O (관리자) | 최근 12개월 가입자 통계 |
| 전체 통계 | GET | `/api/manage/util/all` | O (관리자) | 게시글/여행/사용자 수, 7일 가입자 |

---

# 8. 스케줄러 서비스 (Background Jobs)

| 서비스 | 주기 | 설명 |
|--------|------|------|
| RoleMonitorService | 1시간마다 | 비정상적인 권한 변경 감지 및 로깅 |
| UserCleanupService | 매 정각 | 이메일 미인증 사용자 자동 삭제 |
| ImageCleanupService | 트랜잭션 후 | 고아 이미지(미사용) 정리 |

---

# 인증 범례

| 기호 | 의미 |
|------|------|
| X | 인증 불필요 (공개 API) |
| O | JWT 인증 필수 |
| O (관리자) | JWT 인증 + 관리자 권한 필수 |
| △ | 선택적 인증 (로그인 시 추가 정보 제공) |
