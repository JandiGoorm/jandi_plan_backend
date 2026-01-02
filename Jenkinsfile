pipeline {
    agent any
    
    environment {
        GHCR_OWNER = 'kyj0503'
        EC2_HOST = 'plan.yeonjae.kr'
        EC2_USER = 'ubuntu'
        IMAGE_NAME = 'justplanit'
    }
    
    stages {
        stage('Checkout') {
            steps {
                // GitHub 리포지토리의 코드를 가져옵니다.
                checkout scm
            }
        }
        
        stage('Build and Push to GHCR') {
            steps {
                script {
                    // Dockerfile을 사용하여 이미지를 빌드하고 GHCR에 푸시합니다.
                    // 캐싱 전략: latest 이미지를 pull하여 캐시로 활용합니다.
                    def latestImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:latest"
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    
                    docker.withRegistry("https://ghcr.io", 'github-token') {
                        // 1. 이전 캐시용 이미지 Pull (실패해도 무시 - 첫 빌드 대비)
                        try {
                            docker.image(latestImageName).pull()
                        } catch (Exception e) {
                            echo "Cache pull failed or no latest image found. Proceeding without cache. Error: ${e.getMessage()}"
                        }
                        
                        // 2. 캐시를 활용하여 빌드 (--cache-from 옵션 추가)
                        // 주의: docker.build() 메서드는 추가 build arg를 문자열로 받습니다.
                        docker.build(fullImageName, "--cache-from ${latestImageName} .")
                        
                        // 3. 빌드된 이미지 Push
                        docker.image(fullImageName).push()
                        
                        // 4. 다음 빌드를 위한 캐시 갱신 (latest 태그 업데이트)
                        docker.image(fullImageName).push("latest")
                    }
                }
            }
        }
        
        stage('Deploy to EC2') {
            steps {
                script {
                    // EC2 서버에 접속하여 배포 스크립트를 실행합니다.
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    withCredentials([sshUserPrivateKey(credentialsId: 'ec2-ssh-key', keyFileVariable: 'EC2_PRIVATE_KEY')]) {
                        sh """
                            scp -o StrictHostKeyChecking=no -i \${EC2_PRIVATE_KEY} docker-compose.yml ${env.EC2_USER}@${env.EC2_HOST}:/home/ubuntu/justplanit-app/docker-compose.yml
                            scp -o StrictHostKeyChecking=no -i \${EC2_PRIVATE_KEY} deploy.sh ${env.EC2_USER}@${env.EC2_HOST}:/home/ubuntu/justplanit-app/deploy.sh
                            ssh -o StrictHostKeyChecking=no -i \${EC2_PRIVATE_KEY} ${env.EC2_USER}@${env.EC2_HOST} \
                            "bash /home/ubuntu/justplanit-app/deploy.sh ${fullImageName}"
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            // 빌드가 끝나면 작업 공간을 정리합니다.
            cleanWs()
        }
    }
}
