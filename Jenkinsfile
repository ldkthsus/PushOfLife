pipeline {
    agent any
    environment {
        SONARQUBE_SERVER = 'SonarQube'  // SonarQube 서버의 이름
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build gradle') {
            steps {
                echo 'gradle clean package'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar -Dsonar.projectKey=POLmain -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 1, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
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

