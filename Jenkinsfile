pipeline {
    agent any
    post {
        always {
            echo 'Cleaning workspace'
            deleteDir() /* clean up our workspace */
        }
        success {
            echo 'succeeeded!'
        }
        unstable {
            echo 'unstable'
        }
        failure {
            echo 'failed'
        }
        changed {
            echo 'Things were different before...'
        }
    }
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
        stage('Sonar') {
            environment {
                scannerHome = tool 'SonarQubeScanner'
                }    
            steps {
                withSonarQubeEnv('Sonar') {
                    //sh "${scannerHome}/bin/sonar-scanner"
                    withMaven(maven:'M3', jdk: 'OPENJDK10') {
                        sh 'mvn clean verify sonar:sonar -DskipTests  -Ddocker.skip=true'
                    }
                    
                }
                timeout(time: 10, unit: 'MINUTES') {
                       waitForQualityGate abortPipeline: true
                }   
                
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
                branch 'dev'
            }
            steps {
                 echo 'Deploying with maven on archiva..'
                withMaven(maven: 'M3', jdk: 'OPENJDK10') {
                    sh 'mvn deploy -DskipTests -DaltDeploymentRepository=dev::default::http://archiva.em/repository/dev'
                }
            }
        }
    }
}