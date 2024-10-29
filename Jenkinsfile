sh pipeline {
    agent any

    stages {
        stage('Build Backend') {
            steps {
                script {
                    echo "aaaaaa"
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

