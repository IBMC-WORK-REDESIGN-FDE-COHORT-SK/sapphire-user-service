pipeline {
    agent any
    
    environment {
        // Docker configuration
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_IMAGE_NAME = 'sapphire-user-service'
        DOCKER_CREDENTIALS_ID = 'docker-hub-credentials'
        
        // Application configuration
        APP_NAME = 'sapphire-user-service'
        APP_VERSION = "${env.BUILD_NUMBER}"
        
        // Maven configuration
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
        
        // SonarQube (optional)
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 30, unit: 'MINUTES')
        disableConcurrentBuilds()
    }
    
    stages {
        stage('Checkout') {
            steps {
                script {
                    echo "Checking out code from repository..."
                    checkout scm
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo "Building ${APP_NAME}..."
                    // For Maven-based Spring Boot project
                    sh '''
                        ./mvnw clean package -DskipTests
                    '''
                    // For Gradle, use: ./gradlew clean build -x test
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                script {
                    echo "Running unit tests..."
                    sh '''
                        ./mvnw test
                    '''
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco(
                        execPattern: '**/target/jacoco.exec',
                        classPattern: '**/target/classes',
                        sourcePattern: '**/src/main/java'
                    )
                }
            }
        }
        
        stage('Code Quality Analysis') {
            steps {
                script {
                    echo "Running code quality checks..."
                    // Uncomment if SonarQube is configured
                    // sh '''
                    //     ./mvnw sonar:sonar \
                    //       -Dsonar.host.url=${SONAR_HOST_URL} \
                    //       -Dsonar.projectKey=${APP_NAME}
                    // '''
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker image..."
                    def dockerImage = docker.build(
                        "${DOCKER_IMAGE_NAME}:${APP_VERSION}",
                        "--build-arg JAR_FILE=target/*.jar ."
                    )
                    
                    // Tag as latest
                    dockerImage.tag('latest')
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                script {
                    echo "Scanning Docker image for vulnerabilities..."
                    // Using Trivy for container scanning
                    sh """
                        docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
                          aquasec/trivy:latest image \
                          --severity HIGH,CRITICAL \
                          --exit-code 0 \
                          ${DOCKER_IMAGE_NAME}:${APP_VERSION}
                    """
                }
            }
        }
        
        stage('Push to Registry') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Pushing Docker image to registry..."
                    docker.withRegistry("https://${DOCKER_REGISTRY}", DOCKER_CREDENTIALS_ID) {
                        def image = docker.image("${DOCKER_IMAGE_NAME}:${APP_VERSION}")
                        image.push()
                        image.push('latest')
                    }
                }
            }
        }
        
        stage('Deploy to Dev') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Deploying to development environment..."
                    // Update the docker-compose or kubernetes deployment
                    sh """
                        # Example: Update image in podman-compose
                        # This assumes Jenkins has access to the compose file
                        sed -i 's|image: .*sapphire-user-service:.*|image: ${DOCKER_IMAGE_NAME}:${APP_VERSION}|' \
                          setup/podman/compose/podman-compose.yml
                        
                        # Restart the service
                        cd setup/podman/compose
                        podman-compose up -d sapphire-user-service
                    """
                }
            }
        }
        
        stage('Integration Tests') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Running integration tests..."
                    sh '''
                        # Wait for service to be healthy
                        sleep 30
                        
                        # Run integration tests
                        ./mvnw verify -Pintegration-tests
                    '''
                }
            }
        }
        
        stage('Smoke Tests') {
            when {
                branch 'main'
            }
            steps {
                script {
                    echo "Running smoke tests..."
                    sh '''
                        # Basic health check
                        curl -f http://localhost:8091/actuator/health || exit 1
                        
                        # API endpoint test
                        curl -f http://localhost:8091/actuator/info || exit 1
                    '''
                }
            }
        }
    }
    
    post {
        always {
            echo "Cleaning up workspace..."
            cleanWs()
        }
        success {
            echo "Pipeline completed successfully!"
            // Send notification (email, Slack, etc.)
            // emailext (
            //     subject: "SUCCESS: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
            //     body: "Build succeeded: ${env.BUILD_URL}",
            //     to: "team@sapphire-fitconnect.com"
            // )
        }
        failure {
            echo "Pipeline failed!"
            // Send notification
            // emailext (
            //     subject: "FAILURE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
            //     body: "Build failed: ${env.BUILD_URL}",
            //     to: "team@sapphire-fitconnect.com"
            // )
        }
        unstable {
            echo "Pipeline is unstable!"
        }
    }
}