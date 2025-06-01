## ✈️ 프로젝트 소개

**Just Plan It**은 MBTI에서 계획 세우기를 어려워하는 **P 성향**의 사람들도 **J 성향**처럼 쉽게 여행 계획을 세울 수 있도록 돕는 플랫폼입니다.  
프론트엔드와 백엔드를 나누어 **React와 Spring 기반**으로 개발되었습니다.

프로젝트 이름인 **Just Plan It**은 J와 P 성향을 모두 담을 수 있는 이름을 고민하던 중,  
“그냥 계획해봐” 라는 의미와도 잘 어울려 선정하게 되었습니다.

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
| 김연재 | BE      | 백엔드 개발, 데이터베이스 설계, 서버 구축                         | [github.com/kyj0503](https://github.com/kyj0503)           |
| 한정희 | BE      | API 설계 및 구현, 서버 관리                          | [github.com/hhhan-jh](https://github.com/hhhan-jh)         |

<br/>

## 🏗️ 아키텍처

![Just Plan It Architecture](https://github.com/user-attachments/assets/7157abb5-f109-44e3-ae38-8d39fc01332a)

## 🏗️ ERD
![jandi_plan_erd](https://github.com/JandiGoorm/jandi_plan_backend/blob/master/jandi_plan_erd.png)

## 🏗️ API 명세서

https://www.notion.so/API-190be199511b81d29d08da36c1c4ad30

<br/>

## 🛠️ 기술 스택 (Frontend)

| 구분            | 사용 기술                                                                               |
| --------------- | --------------------------------------------------------------------------------------- |
| 개발 환경       | React, Vite                                                                             |
| 폼 관리         | react-hook-form (비제어 컴포넌트 방식으로 효율적인 폼 상태 관리)                        |
| 유효성 검증     | zod (스키마 기반 폼 유효성 검증)                                                        |
| 이미지 슬라이더 | embla-carousel<br>(초기에는 Swiper를 사용했으나, 오류와 커스터마이징의 어려움으로 교체) |
| 차트 라이브러리 | Recharts (관리자 페이지에서 데이터 시각화 제공)                                         |
| 인터랙션 효과   | framer-motion (스크롤 및 렌더링 변화에 따른 애니메이션 효과 구현)                       |
| 텍스트 에디터   | Quill 2<br>(DOMNodeInserted 이벤트 이슈로 인해 Quill 1에서 업그레이드)                  |
| 데이터 페칭     | axios, 커스텀 axiosInstance<br>(interceptor 및 baseURL 통합 관리)                       |
| 배포            | Netlify (GitHub 연동 자동 배포)                                                         |

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
