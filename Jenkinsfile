pipeline {
    agent any
stages {
        stage('Build Backend') {
            steps {
                script {
                    echo 'Building Backend Docker Images...'
                }
            }
        }

    }

    post {
        always {
            // Clean up resources if necessary
            cleanWs()
        }
    }
}

