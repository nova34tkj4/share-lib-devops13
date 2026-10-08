def call(Map config) {
    // Definisikan URL Webhook Discord Anda di sini
    def discordWebhookUrl = "https://discord.com/api/webhooks/1557751335665803284/FQBI2Uxi71D6v7RHOWNUpjywwCf6D0oEMI5zH3Ld3VNDU1VgQMvozw23l2wa0qhNb-jI"

    pipeline {
        agent any

        stages {
            stage('Start Notification') {
                steps {
                    sh """
                        curl -H "Content-Type: application/json" \
                        -X POST \
                        -d '{"embeds": [{"title": "🚀 CI/CD Pipeline Started", "description": "Pipeline untuk **${SERVICE}** telah dimulai.", "color": 3447003}]}' \
                        ${discordWebhookUrl}
                    """
                }
            }
            stage('checkout') {
                steps {
                    checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/nova34tkj4/devops13-appB.git']])
                }
            }
            stage('build') {
                steps {
                    sh "docker build -t nhkwardana30/${SERVICE} ."
                }
            }
            stage('push') {
                steps {
                    sh "docker push nhkwardana30/${SERVICE}"
                    sh "docker rmi nhkwardana30/${SERVICE}"
                }
            }
            stage('deploy') {
                steps {
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@136.85.30.24 << 'EOF'
                            docker pull nhkwardana30/${SERVICE}:latest
                            docker stop servicea-container || true
                            docker rm servicea-container || true
                            docker run -d --name servicea-container -p 3000:3000 nhkwardana30/${SERVICE}:latest
                    """
                }
            }
        }

        // Blok POST untuk mendeteksi status akhir dari seluruh stage di atas
        post {
            success {
                sh """
                    curl -H "Content-Type: application/json" \
                    -X POST \
                    -d '{"embeds": [{"title": "✅ CI/CD Pipeline Success", "description": "Pipeline untuk **${SERVICE}** berhasil diselesaikan dan dideploy ke server!", "color": 3066993}]}' \
                    ${discordWebhookUrl}
                """
            }
            failure {
                sh """
                    curl -H "Content-Type: application/json" \
                    -X POST \
                    -d '{"embeds": [{"title": "❌ CI/CD Pipeline Failed", "description": "Pipeline untuk **${SERVICE}** GAGAL pada build #${BUILD_NUMBER}. Silakan periksa log Jenkins.", "color": 15158332}]}' \
                    ${discordWebhookUrl}
                """
            }
        }
    }
}
