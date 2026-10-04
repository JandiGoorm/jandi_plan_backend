# 미완료 항목

- 이미지 디렉터리(`/opt/home-server/data/plan-images`) 정기 백업 구성.
- 전환 확인 후 GCS 버킷(`plan-storage`)과 서비스 계정 삭제. 서비스 계정 키가 `home-server` 저장소 이력에 있으므로 키를 폐기한다.
- DB 저장 실패 시 디스크에 남는 고아 파일 정리. `image.image_url` 길이 제한은 100자다.
