pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['DEV', 'STAGING', 'PROD'], description: 'Deployment Target Environment')
        string(name: 'TARGET_PORT', defaultValue: '8081', description: 'Host Port for Deployed Container')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Execute Quality Gate Test Suites')
        string(name: 'DOCKER_REGISTRY_USER', defaultValue: '', description: 'Optional: Docker Hub Username to push image')
    }

    environment {
        APP_NAME = 'loan-portal'
        IMAGE_NAME = "loan-portal:build-${BUILD_NUMBER}"
        LATEST_IMAGE = "loan-portal:latest"
        CONTAINER_NAME = 'loan-portal-app'
    }

    stages {
        stage('1. Checkout SCM') {
            steps {
                echo "=========================================================="
                echo "Checking out source code for ${env.APP_NAME} (Build #${BUILD_NUMBER})..."
                echo "=========================================================="
                checkout scm
            }
        }

        stage('2. Unit & Integration Tests') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                echo "Executing JUnit Unit and Integration Test Suite..."
                bat 'mvn clean test -Dtest=LoanApplicationPortalAppTests,LoanServiceTest,UserServiceTest'
            }
        }

        stage('3. Continuous Testing - Selenium Quality Gate') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                echo "Executing Selenium WebDriver Headless E2E Quality Gate..."
                bat 'mvn test -Dtest=LoanPortalSeleniumE2ETest'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('4. Build Versioned Docker Image') {
            steps {
                echo "Building Multi-Stage Docker Image: ${env.IMAGE_NAME}..."
                bat "docker build -t ${env.IMAGE_NAME} -t ${env.LATEST_IMAGE} ."
                echo "Listing generated Docker images..."
                bat "docker images ${env.APP_NAME}"
            }
        }

        stage('5. Push to Image Registry (Optional)') {
            when {
                expression { return params.DOCKER_REGISTRY_USER != null && params.DOCKER_REGISTRY_USER.trim() != '' }
            }
            steps {
                echo "Tagging and pushing image to Docker Hub under user: ${params.DOCKER_REGISTRY_USER}..."
                bat """
                    docker tag ${env.IMAGE_NAME} ${params.DOCKER_REGISTRY_USER}/${env.APP_NAME}:build-${BUILD_NUMBER}
                    docker tag ${env.IMAGE_NAME} ${params.DOCKER_REGISTRY_USER}/${env.APP_NAME}:latest
                    docker push ${params.DOCKER_REGISTRY_USER}/${env.APP_NAME}:build-${BUILD_NUMBER}
                    docker push ${params.DOCKER_REGISTRY_USER}/${env.APP_NAME}:latest
                """
            }
        }

        stage('6. Automated Container Continuous Deployment') {
            steps {
                echo "Deploying fresh container [${env.CONTAINER_NAME}] on Port ${params.TARGET_PORT}..."
                // Stop and remove previous container instance gracefully
                bat """
                    docker stop ${env.CONTAINER_NAME} || exit 0
                    docker rm -f ${env.CONTAINER_NAME} || exit 0
                """
                // Run fresh container with restart policy and port mapping
                bat """
                    docker run -d --name ${env.CONTAINER_NAME} -p ${params.TARGET_PORT}:8081 --restart unless-stopped ${env.IMAGE_NAME}
                """
                echo "Container launched. Inspecting status..."
                bat "docker ps -f name=${env.CONTAINER_NAME}"
            }
        }

        stage('7. Container Health Check Verification') {
            steps {
                echo "Verifying container availability and Actuator health..."
                sleep time: 15, unit: 'SECONDS'
                bat """
                    powershell -Command "Invoke-RestMethod -Uri http://localhost:${params.TARGET_PORT}/actuator/health -Method Get"
                """
                echo "Container is UP, healthy, and serving requests on port ${params.TARGET_PORT}"
            }
        }
    }

    post {
        success {
            echo "=========================================================="
            echo "🎉 END-TO-END DOCKER CONTINUOUS DEPLOYMENT SUCCESSFUL!"
            echo "Deployed Image     : ${env.IMAGE_NAME}"
            echo "Container Name     : ${env.CONTAINER_NAME}"
            echo "Target Environment : ${params.ENVIRONMENT}"
            echo "Live Portal URL    : http://localhost:${params.TARGET_PORT}/dashboard"
            echo "=========================================================="
        }
        failure {
            echo "=========================================================="
            echo "❌ Pipeline failed! Deployment was aborted."
            echo "=========================================================="
        }
    }
}
