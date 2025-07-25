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
                    def fullImageName = "ghcr.io/${env.GHCR_OWNER}/${env.IMAGE_NAME}:${env.BUILD_NUMBER}"
                    docker.build(fullImageName, '.')
                    docker.withRegistry("https://ghcr.io", 'github-token') { // Jenkins에 등록된 GHCR 인증 정보 ID
                        docker.image(fullImageName).push()
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
