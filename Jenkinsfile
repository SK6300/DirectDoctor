pipeline {
    agent any

    environment {
        AWS_ACCESS_KEY_ID     = credentials('AWS_ACCESS_KEY_ID')
        AWS_SECRET_ACCESS_KEY = credentials('AWS_SECRET_ACCESS_KEY')
        AWS_DEFAULT_REGION    = 'ap-southeast-2'
        S3_BUCKET             = 's3://directdoctor-builds-shiva'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/SK6300/Direct-Doctor.git'
            }
        }

        stage('Build APK') {
            steps {
                bat 'gradlew.bat assembleDebug'
            }
        }

        stage('Upload to AWS S3') {
            steps {
                bat 'aws s3 cp app/build/outputs/apk/debug/app-debug.apk %S3_BUCKET%/builds/app-debug-%BUILD_NUMBER%.apk'
            }
        }
    }
}