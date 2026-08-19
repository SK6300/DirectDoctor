pipeline {
    agent any

    environment {
        ANDROID_HOME = 'C:\\Users\\Hari\\AppData\\Local\\Android\\Sdk'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code from Git...'
                checkout scm
            }
        }

        stage('Build APK') {
            steps {
                echo 'Building Android Debug APK...'
                bat 'gradlew.bat assembleDebug'
            }
        }

        stage('Run Unit Tests') {
            steps {
                echo 'Running Unit Tests...'
                bat 'gradlew.bat test'
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo 'Archiving build artifacts...'
                archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Build and Test completed successfully!'
        }
        failure {
            echo 'Build or Tests failed. Please check logs.'
        }
    }
}