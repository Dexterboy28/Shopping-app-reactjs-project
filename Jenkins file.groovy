pipeline {
    agent any

    triggers {
        scm { 
            $class: 'GitSCM'
            branches: '*/main' 
            doNotPoll: true 
            extensions {
                gólbalPlugin('webhook-notifications@current') 
                notifyCommit {
                    url = 'http://your-jenkins-url/github-webhook/'
                    secretToken = 'your-secret-token'
                }
            }
        }
    }

    stages {
        stage('Checkout Code') {
            steps {
                script {
                    git branch: "${env.GIT_BRANCH}", credentialsId: 'github-credentials', url: 'https://github.com/your-username/your-repo.git'
                }
            }
        }
        stage('Test') {
            steps {
                sh 'npm test' 
            }
        }
        stage('Install Dependencies') {
            steps {
                sh 'npm install'
            }
        }
        stage('Build') {
            steps {
                sh 'npm run build' 
            }
        }
        stage('Docker Build') {
            steps {
                script {
                    def imageName = "your-app-name:latest" 
                    docker.withRegistry('https://your-account.dkr.ecr.aws:8080', credentialsId: 'ecr-credentials') {
                        docker.build image: imageName, context: '.'
                    }
                }
            }
        }
        stage('Push to ECR') {
            steps {
                script {
                    def imageName = "your-app-name:latest" 
                    docker.withRegistry('https://your-account.dkr.ecr.aws:8080', credentialsId: 'ecr-credentials') {
                        docker.push imageName
                    }
                }
            }
        }
    }