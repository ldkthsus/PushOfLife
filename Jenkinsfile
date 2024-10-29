pipeline {
    agent any
    
    environment {
        SONARQUBE_SERVER = 'SonarQube'  // SonarQube 서버의 이름
        scannerHome = tool name: 'SonarQube Scanner'
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
                    sh '${scannerHome}/bin/sonar-scanner --version'
                    // sh 'sonar-scanner -Dsonar.projectKey=POLmain -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}'
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

