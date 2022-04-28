pipeline {
    environment{
    registry = "vikaspolicedockerhub/document-service"
    registryCredential = "Docker-Hub-Cred"
    dockerImage = ''
  }
    agent any
    tools{
        maven "maven"
    } 
    stages {
        stage('Build'){                
                steps{
                   sh 'mvn clean install -DskipTests=true'
            
            }
        }
        stage('Build image'){
            steps{
                echo "Building docker image"
                script{
                    dockerImage = docker.build registry + ":$BUILD_NUMBER"
                }
                    
            }
        }
        stage('Push Image'){
            steps{
                echo "Pushing docker image"
                     script{
                        docker.withRegistry('',registryCredential) {
                        dockerImage.push()
                        dockerImage.push('latest')
             }
           }  
         }
       }
    }
}