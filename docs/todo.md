# 미완료 항목

- 이미지 디렉터리(`/srv/jandi-plan`) 정기 백업 구성.
- 업로드 용량(5MB) 초과 시 HTTP 500이 반환된다. `MaxUploadSizeExceededException`을 4xx로 처리한다.
- DB 저장 실패 시 디스크에 남는 고아 파일 정리. `image.image_url` 길이 제한은 100자다.
- `/srv/jandi-plan` 사용량이 4GB를 넘으면 알림을 받도록 모니터링을 구성한다.
