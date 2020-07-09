pipeline {
 agent {
  docker 'adoptopenjdk/maven-openjdk11'
 }

 stages {

  stage('Build') {
   steps {
    echo 'Building with maven...'
    configFileProvider([configFile(fileId: 'daa266f8-d247-499d-97bc-a7b4826e8d3f', variable: 'SETTINGS')]) {
     sh 'mvn -s $SETTINGS clean package'
    }
   }
  }
  stage('Sonar') {
   when {
    not {
     branch 'dev'
    }
   }
   steps {
    echo 'Run Sonar...'
    configFileProvider([configFile(fileId: 'daa266f8-d247-499d-97bc-a7b4826e8d3f', variable: 'SETTINGS')]) {
     sh 'mvn -s $SETTINGS clean -DskipTest verify sonar:sonar -Dsonar.host.url=http://sonar.em'
    }
   }
  }
  stage('DeployDev') {
   when {
    branch 'dev'
   }
   steps {
    echo 'Deploying with maven on archiva..'
    configFileProvider([configFile(fileId: 'daa266f8-d247-499d-97bc-a7b4826e8d3f', variable: 'SETTINGS')]) {
     sh 'mvn -s $SETTINGS deploy -DskipTests -DaltDeploymentRepository=dev::default::http://archiva.em/repository/dev'
    }
   }
  }
 }
 post {
  always {
   echo 'Cleaning workspace'
   deleteDir() /* clean up our workspace */
  }
 }

}