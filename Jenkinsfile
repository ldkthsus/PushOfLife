pipeline {
    agent any
    
    environment {
        SONARQUBE_SERVER = 'SonarQube'  // SonarQube 서버의 이름
        scannerHome = tool name: 'SonarQube Scanner'
        SONAR_TOKEN = credentials('SONAR_TOKEN')
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
            steps{
                dir('./BackEnd/PushOfLife'){
                    withSonarQubeEnv() {
                        sh "./gradlew sonar"
                    }
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

