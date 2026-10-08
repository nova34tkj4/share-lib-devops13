def call(Map config) {
    pipeline {
        agent any

        stages {
            stage('checkout') {
                steps {
                    checkout scmGit(branches: [[name: '*/main']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/nova34tkj4/devops13-appB.git']])
                }
            }
            stage('build') {
                steps {
                    sh "docker build -t nhkwardana30/servicea-jenkins ."
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
                    // Langsung eksekusi perintah ssh tanpa wrapper plugin apapun
                    sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@136.85.30.24 << 'EOF'
                            # 1. Tarik image terbaru dari Docker Hub
                            docker pull nhkwardana30/${SERVICE}:latest
                            
                            # 2. Hentikan dan hapus container lama jika sedang berjalan
                            docker stop servicea-container || true
                            docker rm servicea-container || true
                            
                            # 3. Jalankan container baru (sesuaikan port -p jika berbeda)
                            docker run -d --name servicea-container -p 3000:3000 nhkwardana30/${SERVICE}:latest
                            
                            # 4. Bersihkan image usang agar storage tidak penuh
                            docker image prune -f
    EOF
                    '''
                }
            }
        }
    }
}
