pipeline {
    agent any

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build SanityRunner') {
            steps {
                dir('java/local') {
                    bat 'mvn clean package dependency:copy-dependencies'
                }
            }
        }

        stage('Run Tests') {
            steps {
                bat 'scripts\\run_tests.bat'
            }
        }

        stage('Reports Creation') {
            steps {
                bat 'scripts\\prepare_reports.bat'
            }
        }
    }

    post {
        always {
            emailext(
                subject: "${env.JOB_NAME} - Env #${params.ENV} Build #${env.BUILD_NUMBER}",
                from: "jenkins@mwhlvchcatools01",
                to: "AQE-OffShoreGTM_Testing@int.amdocs.com",
                replyTo: "kartikve@amdocs.com",
                body: readFile("${env.BUILD_NUMBER}/summary-report.html"),
                mimeType: 'text/html',
                attachmentsPattern: "${env.BUILD_NUMBER}\\*.*"
            )
        }
    }
}
