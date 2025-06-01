## ✈️ 프로젝트 소개

**Just Plan It**은 MBTI에서 계획 세우기를 어려워하는 **P 성향**의 사람들도 **J 성향**처럼 쉽게 여행 계획을 세울 수 있도록 돕는 플랫폼입니다.  
프론트엔드와 백엔드를 나누어 **React와 Spring 기반**으로 개발되었습니다.

프로젝트 이름인 **Just Plan It**은 J와 P 성향을 모두 담을 수 있는 이름을 고민하던 중,  
"그냥 계획해봐" 라는 의미와도 잘 어울려 선정하게 되었습니다.

<br/>

## 📺 서비스 체험하기

**배포 URL**: [https://justplanit.site/](https://justplanit.site/)

회원가입이 번거로우시다면, 아래 테스트 계정으로 바로 로그인해보실 수 있습니다.  
**ID**: ush0105@naver.com  
**PW**: 123456

<br/>

## 👥 팀원 소개

| 이름   | 역할    | 주요 기여                                            | GitHub                                                     |
| ------ | ------- | ---------------------------------------------------- | ---------------------------------------------------------- |
| 전민근 | FE / PM | 프론트엔드 개발, 전체 일정 관리 및 커뮤니케이션 (PM) | [github.com/Jun-min-geun](https://github.com/Jun-min-geun) |
| 윤승휘 | FE      | 프론트엔드 개발, UI/UX 구현                          | [github.com/Yoonhwi](https://github.com/Yoonhwi)           |
| 김연재 | BE      | 백엔드 개발, 데이터베이스 설계, 서버 구축            | [github.com/kyj0503](https://github.com/kyj0503)           |
| 한정희 | BE      | API 설계 및 구현, 서버 관리                          | [github.com/hhhan-jh](https://github.com/hhhan-jh)         |

<br/>

## 🏗️ 아키텍처

![Just Plan It Architecture](https://github.com/user-attachments/assets/7157abb5-f109-44e3-ae38-8d39fc01332a)

## 🏗️ ERD
![jandi_plan_erd](https://github.com/JandiGoorm/jandi_plan_backend/blob/master/jandi_plan_erd.png)

## 🏗️ API 명세서

https://www.notion.so/API-190be199511b81d29d08da36c1c4ad30

<br/>

## 🛠️ 기술 스택 (Backend)

| 구분              | 사용 기술                                                                               |
| ----------------- | --------------------------------------------------------------------------------------- |
| 개발 환경         | Spring Boot 3.4.2, Java 17                                                              |
| 빌드 도구         | Gradle 7.6                                                                              |
| 데이터베이스      | MySQL 8.0, Spring Data JPA                                                              |
| 인증/보안         | Spring Security, JWT (JSON Web Token)<br>- Custom UserDetailsService 구현<br>- JWT 기반 토큰 인증 시스템 |
| 소셜 로그인       | OAuth2 (카카오, 네이버, 구글)<br>- 다중 소셜 플랫폼 통합 인증                           |
| 파일 저장소       | Google Cloud Storage (GCS)<br>- 이미지 업로드 및 관리<br>- Base64 인코딩된 서비스 계정 키 활용 |
| 외부 API 연동     | Google Maps API (장소 정보 및 경로 검색)<br>OpenAI API (AI 기반 여행 계획 추천)         |
| 이메일 서비스     | Spring Mail (Gmail SMTP)<br>- 회원가입 이메일 인증<br>- HTML 템플릿 기반 메일 발송      |
| 검증 및 유효성    | Spring Validation<br>- Bean Validation을 통한 요청 데이터 검증                          |
| 비동기 처리       | Spring WebFlux<br>- 외부 API 호출 시 비동기 처리                                        |
| 개발 도구         | Spring Boot DevTools, Lombok<br>- 자동 재시작 및 코드 간소화                            |
| 데이터 직렬화     | Jackson (JSON 처리)<br>- 소셜 로그인 응답 데이터 파싱                                   |
| 배포 및 인프라    | Docker (Multi-stage build), Google Cloud Run<br>- 컨테이너 기반 배포 및 자동 스케일링   |
| 테스트            | JUnit 5, Spring Security Test<br>- 단위 테스트 및 보안 기능 테스트                      |
| 설정 관리         | Spring Profiles (dev, prod 환경 분리)<br>- 환경별 데이터베이스 및 API 키 관리           |

<br/>

## 🤝 협업 및 커뮤니케이션

| 항목              | 내용                                                                  |
| ----------------- | --------------------------------------------------------------------- |
| 협업 방식         | GitHub 브랜치를 팀원별로 분리하여 관리                                |
| 브랜치 전략       | 개인 브랜치 → `develop` PR → 문제 없을 시 `main` 병합 및 Netlify 배포 |
| 커뮤니케이션      | Discord를 통해 실시간 소통                                            |
| 문서 및 일정 관리 | Notion을 활용한 자료 공유 및 회의록 작성                              |
| 이슈 및 명세 관리 | Notion을 통한 기능 명세 작성 및 이슈 관리                             |

<br/>

## 🤝 요구사항

https://www.notion.so/185be199511b8117b480cbf822f84280
