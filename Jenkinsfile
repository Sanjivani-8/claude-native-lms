// Declarative Jenkins pipeline mirroring .github/workflows/ci-cd.yml
// Required Jenkins plugins: Pipeline, Git, Docker Pipeline, JUnit, JaCoCo,
// SonarQube Scanner, Kubernetes CLI.
// Required credentials (Jenkins Credentials store):
//   - "sonarqube-token"      (Secret text)   -> SonarQube auth token
//   - "registry-credentials" (Username/Pwd)  -> container registry login
//   - "kubeconfig-lms"       (Secret file)   -> kubeconfig for the target cluster
// Required Jenkins global tool config: "JDK11", "Maven3"

def SERVICES = [
    'auth-service',
    'student-service',
    'course-service',
    'assignment-service',
    'notification-service',
    'api-gateway'
]

pipeline {
    agent any

    tools {
        jdk 'JDK11'
        maven 'Maven3'
    }

    environment {
        REGISTRY        = 'ghcr.io/your-org/lms'
        IMAGE_TAG       = "${env.BUILD_NUMBER}-${env.GIT_COMMIT?.take(7) ?: 'local'}"
        SONAR_HOST_URL  = 'https://sonarqube.your-domain.com'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '20'))
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Unit Test') {
            steps {
                sh 'mvn -B clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    jacoco execPattern: '**/target/jacoco.exec'
                }
            }
        }

        stage('SAST - SonarQube') {
            when { expression { return env.SONAR_ENABLED == 'true' } }
            steps {
                withCredentials([string(credentialsId: 'sonarqube-token', variable: 'SONAR_TOKEN')]) {
                    sh """
                        mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
                          -Dsonar.projectKey=lms-microservices \
                          -Dsonar.host.url=${SONAR_HOST_URL} \
                          -Dsonar.token=\$SONAR_TOKEN
                    """
                }
            }
        }

        stage('SCA - Dependency & IaC Scan (Trivy fs)') {
            steps {
                sh '''
                    trivy fs --severity HIGH,CRITICAL --exit-code 1 --ignore-unfixed . \
                        || { echo "Trivy found HIGH/CRITICAL issues - failing build"; exit 1; }
                '''
            }
        }

        stage('Build & Scan Images') {
            steps {
                script {
                    for (svc in SERVICES) {
                        sh """
                            docker build -f ${svc}/Dockerfile -t ${REGISTRY}/${svc}:${IMAGE_TAG} .
                            trivy image --severity HIGH,CRITICAL --exit-code 1 --ignore-unfixed ${REGISTRY}/${svc}:${IMAGE_TAG}
                        """
                    }
                }
            }
        }

        stage('Push Images') {
            when { branch 'main' }
            steps {
                withCredentials([usernamePassword(credentialsId: 'registry-credentials', usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
                    sh 'echo "$REG_PASS" | docker login ghcr.io -u "$REG_USER" --password-stdin'
                    script {
                        for (svc in SERVICES) {
                            sh "docker push ${REGISTRY}/${svc}:${IMAGE_TAG}"
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            when { branch 'main' }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-lms', variable: 'KUBECONFIG')]) {
                    dir('k8s') {
                        script {
                            for (svc in SERVICES) {
                                sh "kustomize edit set image lms/${svc}=${REGISTRY}/${svc}:${IMAGE_TAG}"
                            }
                        }
                        sh '''
                            kubectl apply -k .
                            kubectl -n lms rollout status deployment/api-gateway --timeout=180s
                        '''
                    }
                }
            }
        }
    }

    post {
        always {
            sh 'docker system prune -f || true'
        }
        failure {
            echo "Build failed - check console output. (Wire up Slack/email notification here.)"
        }
    }
}
