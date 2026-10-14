pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['DEV', 'STAGING', 'PROD'], description: 'Deployment Target Environment')
        string(name: 'TARGET_PORT', defaultValue: '8081', description: 'Network Port for Deployed Application')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Execute JUnit Automated Test Suite')
    }

    environment {
        APP_NAME = 'loan-application-tracking-portal'
        JAR_NAME = 'loan-application-tracking-portal-1.0.0-SNAPSHOT.jar'
    }

    stages {
        stage('1. Checkout SCM') {
            steps {
                echo "=========================================================="
                echo "Checking out source code for Environment: ${params.ENVIRONMENT}..."
                echo "=========================================================="
                checkout scm
            }
        }

        stage('2. Build & Test') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                echo "Compiling Java sources and executing automated JUnit tests..."
                bat 'mvn clean test'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('3. Package Artifact') {
            steps {
                echo "Packaging production-ready JAR/WAR artifact..."
                bat 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('4. Deploy to Server') {
            steps {
                echo "Deploying ${env.APP_NAME} to ${params.ENVIRONMENT} environment on port ${params.TARGET_PORT}..."
                // 1. Stop any existing instance running on the target port
                bat """
                    powershell -Command "Stop-Process -Id (Get-NetTCPConnection -LocalPort ${params.TARGET_PORT} -ErrorAction SilentlyContinue).OwningProcess -Force" || exit 0
                """
                // 2. Launch the new build in the background
                bat """
                    powershell -Command "Start-Process java -ArgumentList '-jar', 'target/${env.JAR_NAME}', '--server.port=${params.TARGET_PORT}' -WindowStyle Hidden"
                """
                echo "Application deployment initiated on port ${params.TARGET_PORT}"
            }
        }

        stage('5. Smoke & Health Check') {
            steps {
                echo "Verifying application availability via Actuator Health endpoint..."
                // Wait 12 seconds for Spring Boot container initialization
                sleep time: 12, unit: 'SECONDS'
                bat """
                    powershell -Command "Invoke-RestMethod -Uri http://localhost:${params.TARGET_PORT}/actuator/health -Method Get"
                """
                echo "Health Check passed: Application is UP and responding on port ${params.TARGET_PORT}"
            }
        }
    }

    post {
        success {
            echo "=========================================================="
            echo "🎉 PIPELINE SUCCESSFUL!"
            echo "Target Environment : ${params.ENVIRONMENT}"
            echo "Portal URL         : http://localhost:${params.TARGET_PORT}/dashboard"
            echo "Health Endpoint    : http://localhost:${params.TARGET_PORT}/actuator/health"
            echo "=========================================================="
        }
        failure {
            echo "❌ Pipeline failed! Check stage failure logs and notify developers."
        }
    }
}
