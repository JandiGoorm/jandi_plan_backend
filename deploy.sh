#!/bin/bash
source /home/ubuntu/justplanit-app/.env
export NEW_IMAGE=$1
GITHUB_USER="kyj0503" # 본인의 GitHub 사용자 이름

cd /home/ubuntu/justplanit-app

echo "Deploying Justplanit to server..."

# GHCR 로그인
echo $GITHUB_PAT | docker login ghcr.io -u $GITHUB_USER --password-stdin

# 기존 컨테이너 내리기
docker-compose down

# 새 이미지로 컨테이너 올리기
NEW_IMAGE=${NEW_IMAGE} docker-compose up -d

# 불필요한 이미지 정리
docker image prune -af

echo "Justplanit Deployment complete."
