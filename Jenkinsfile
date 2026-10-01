pipeline {
    agent any

    environment {
        ANDROID_HOME = '/home/shiva/android-sdk'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Environment') {
            steps {
                sh '''
                    echo "sdk.dir=${ANDROID_HOME}" > local.properties
                    chmod +x ./gradlew
                '''
            }
        }

        stage('Build Debug APK') {
            steps {
                sh './gradlew assembleDebug'
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'app/build/outputs/apk/debug/*.apk', fingerprint: true
        }
    }
}
