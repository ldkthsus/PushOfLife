sh pipeline {
    agent any

    stages {
        stage('Build Backend') {
            steps {
                script {
                    dir('BackEnd/AllEat') {
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
                    // Build HappyRe Docker image
                    dir('BackEnd/AllEat') {
                        sh 'docker build -t alleat/alleat-image .'
                    }
                    // Build Fast_API Docker image
                    dir('BackEnd/Fast_API') {
                        sh 'docker build -t alleat/fastapi-image .'
                    }
                }
            }
        }

        // stage('Build Frontend Image') {
        //     steps {
        //         echo 'Building Frontend Docker Image...'
        //         script {
        //             // Build Frontend Docker image
        //             dir('frontend') {
        //                 sh 'docker build -t happyjellyfish/frontend-image .'
        //             }

        //             dir('server'){
        //                 sh 'docker build -t happyjellyfish/webrtc-server .'
        //             }
        //         }
        //     }
        // }

        // stage('Push Docker Images') {
        //     steps {
        //         echo 'Pushing Docker Images to Registry...'
        //         script {
        //             withDockerRegistry(url: 'https://index.docker.io/v1/', credentialsId: "${env.DOCKER_CREDENTIALS_ID}") {
        //                 sh 'docker push happyjellyfish/alleat-image:latest'

        //             }
        //         }
        //     }
        // }

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


        stage('Deploy using Docker Down') { 
            steps{
                echo 'Docker Compose... Down'
                sh 'docker-compose -f /home/docker-compose.yml down'
            }
        }          
        stage('Deploy using Docker Compose') { 
            steps{
                echo 'Deploying using Docker Compose...'
                sh 'docker-compose -f /home/docker-compose.yml up -d'
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

