pipeline {
    agent any
    stages {
        stage ('initialization') {
            steps {
                sh '''
                 echo 'Initialization...'
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                    '''
            }
        }
        stage('Build') {
            steps {
                echo 'Building with maven...'
                withMaven(maven: 'M3', jdk: 'OPENJDK10') {
                    sh 'mvn clean install'
                }
            }
        }
         stage('DeployDev') {
            when {
                branch 'emvista'
            }
            steps {
                 echo 'Deploying with maven on archiva..'
                withMaven(maven: 'M3', jdk: 'OPENJDK10') {
                    sh 'mvn deploy -DskipTests -DaltDeploymentRepository=dev::default::http://mercury.em:8080/repository/dev'
                }
            }
        }
    }
}