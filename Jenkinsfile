#!/usr/bin/env groovy

pipeline {
    agent any
    tools {
        jdk "jdk-17.0.1"
    }
    environment {
        discordWebhook = credentials('discordWebhook')
        CURSEFORGE_TOKEN = credentials('curseforgeApiKey')
        MODRINTH_TOKEN = credentials('modrinthApiKey')
    }
    stages {
        stage('Clean') {
            steps {
                echo 'Cleaning Project'
                sh 'chmod +x gradlew'
                sh './gradlew clean'
            }
        }
        stage('Build') {
            steps {
                echo 'Building'
                sh './gradlew build'
            }
        }
        stage('Publish') {
            when {
                anyOf {
                    branch 'main'
                }
            }
            stages {
                stage('Deploy Previews') {
                    steps {
                        echo 'Deploying previews to various places'
                        sh './gradlew publish publishToDiscord'
                    }
                }
                stage('Deploy releases') {
                    steps {
                        echo 'Maybe deploy releases'
                        sh './gradlew publishCurseforge publishModrinth'
                    }
                }
            }
        }
    }
    post {
        always {
            archiveArtifacts 'Common/build/libs/**.jar'
            archiveArtifacts 'Forge/build/libs/**.jar'
            archiveArtifacts 'Fabric/build/libs/**.jar'
        }
    }
}