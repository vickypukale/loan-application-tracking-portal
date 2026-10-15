pipeline {
    agent any

    parameters {
        choice(name: 'ENVIRONMENT', choices: ['DEV', 'STAGING', 'PROD'], description: 'Deployment Target Environment')
        string(name: 'TARGET_PORT', defaultValue: '8081', description: 'Network Port for Deployed Application')
        booleanParam(name: 'RUN_TESTS', defaultValue: true, description: 'Execute Quality Gate Test Suites')
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

        stage('2. Unit & Integration Tests') {
            when {
                expression { return params.RUN_TESTS }
            }
            steps {
                echo "Executing JUnit Unit and Integration Test Suite..."
                bat 'mvn clean test -Dtest=LoanApplicationPortalAppTests,LoanServiceTest'
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
                    // Publish test reports to Jenkins UI
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    // Archive any failure screenshots captured during test run
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('4. Package Artifact') {
            steps {
                echo "Quality gates passed! Packaging production executable JAR..."
                bat 'mvn package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('5. Deploy to Server') {
            steps {
                echo "Deploying ${env.APP_NAME} to ${params.ENVIRONMENT} environment on port ${params.TARGET_PORT}..."
                bat """
                    powershell -Command "Stop-Process -Id (Get-NetTCPConnection -LocalPort ${params.TARGET_PORT} -ErrorAction SilentlyContinue).OwningProcess -Force" || exit 0
                """
                bat """
                    powershell -Command "Start-Process java -ArgumentList '-jar', 'target/${env.JAR_NAME}', '--server.port=${params.TARGET_PORT}' -WindowStyle Hidden"
                """
                echo "Application deployment initiated on port ${params.TARGET_PORT}"
            }
        }

        stage('6. Smoke & Health Check') {
            steps {
                echo "Verifying application availability via Actuator Health endpoint..."
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
            echo "🎉 ALL QUALITY GATES PASSED! PIPELINE SUCCESSFUL!"
            echo "Target Environment : ${params.ENVIRONMENT}"
            echo "Portal URL         : http://localhost:${params.TARGET_PORT}/dashboard"
            echo "=========================================================="
        }
        failure {
            echo "=========================================================="
            echo "🚨 QUALITY GATE FAILED! DEPLOYMENT ABORTED!"
            echo "Check JUnit Test Result and Screenshots in Jenkins build artifacts."
            echo "=========================================================="
        }
    }
}
