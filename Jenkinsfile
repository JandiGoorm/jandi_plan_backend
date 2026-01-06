pipeline {
    agent any
    
    environment {
        GHCR_OWNER = 'kyj0503'
        IMAGE_NAME = 'jandi-plan'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build and Push to GHCR') {
            steps {
                script {
                    def latestImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:latest"
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    
                    docker.withRegistry("https://ghcr.io", 'github-token') {
                        // 1. 이전 캐시용 이미지 Pull (실패해도 무시)
                        try {
                            docker.image(latestImageName).pull()
                        } catch (Exception e) {
                            echo "Cache pull failed or no latest image found. Proceeding without cache."
                        }
                        
                        // 2. 캐시를 활용하여 빌드
                        docker.build(fullImageName, "--cache-from ${latestImageName} .")
                        
                        // 3. 빌드된 이미지 Push
                        docker.image(fullImageName).push()
                        
                        // 4. 다음 빌드를 위한 캐시 갱신 (latest 태그 업데이트)
                        docker.image(fullImageName).push("latest")
                    }
                }
            }
        }

        // 배포는 home-server에서 담당
        stage('Trigger Deploy') {
            steps {
                build job: 'home-server-deploy', wait: false, propagate: false
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        success {
            echo '✅ Build and Push completed successfully!'
        }
        failure {
            echo '❌ Build failed!'
        }
    }
}
