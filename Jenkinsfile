pipeline {
    agent any
    

    stages {
        stage('Build Backend') {
            steps {
                script {
                    dir('BackEnd/PushOfLife') {
                        // Gradle 빌드
                        sh 'chmod +x gradlew'
                        sh './gradlew build'
                    }
                }
            }
        }
        stage('Build Backend Images') {
            steps {
                echo 'Building Backend Docker Images...'
                script {
                    // Build main server Docker image
                    dir('BackEnd/PushOfLife') {
                        sh 'docker build -t pol/spring .'
                    }
                    // Build notification server Docker image
                    dir('BackEnd/notification') {
                        sh 'docker build -t pol/notify .'
                    }
                }
            }
        }
        stage('Cleanup Old Images') {
            steps {
                script {
                    sh '''
                    # Remove dangling images
                    docker image prune -f

                    # Remove images that start with 'alleat/' and do not have the 'latest' tag
                    docker images --filter "dangling=false" --format "{{.Repository}}:{{.Tag}}" | grep '^alleat/' | grep -v ':latest' | while read -r image; do
                        docker rmi -f "$image" || true
                    done
                    '''
                    
                }
            }
        }


    }

    
    post {
        always {
            cleanWs()
        }
    }
}

