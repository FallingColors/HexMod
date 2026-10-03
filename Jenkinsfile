#!/usr/bin/env groovy

RELEASE_BRANCHES = ["main", "1.21"]

pipeline {
    agent any
    tools {
        jdk "jdk-21"
    }
    parameters {
        booleanParam(
            name: "PUBLISH_CURSEFORGE_AND_MODRINTH",
            description: "Publish to CurseForge and modrinth",
            defaultValue: false,
        )
        booleanParam(
            name: "FORMAL_RELEASE",
            description: "decides if is a formal release (without -pre)",
            defaultValue: false,
        )
    }
    environment {
        discordWebhook = credentials('discordWebhook')
        CURSEFORGE_TOKEN = credentials('curseforgeApiKey')
        MODRINTH_TOKEN = credentials('modrinthApiKey')
        FORMAL_RELEASE = "${params.FORMAL_RELEASE ? 'true' : 'false'}"
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
        stage('Run Datagen') {
            steps {
                echo 'Running datagen tasks'
                sh './gradlew runAllDatagen'
            }
        }
        stage('Check Datagen') {
            steps {
                echo 'Checking for modified files'
                // also fail if there are new untracked files
                sh 'git add --intent-to-add .'
                // cache isn't reproducible, so ignore modifications to it
                // https://stackoverflow.com/a/71878316
                sh 'git diff --name-only --exit-code -- ":!:*/src/generated/resources/.cache/*"'
            }
        }
        stage('Publish') {
            when {
                allOf {
                    expression { env.BRANCH_NAME in RELEASE_BRANCHES }
                    not { changeRequest() }
                }
            }
            stages {
                stage('Deploy Previews') {
                    steps {
                        echo 'Deploying previews to various places'
                        sh './gradlew publish announceDiscord'
                    }
                }
                stage('Deploy releases') {
                    when {
                        expression { params.CURSEFORGE_AND_MODRINTH}
                    }
                    steps {
                        echo 'Maybe deploy releases'
                        sh './gradlew publishCurseforge publishModrinth'
                    }
                }}
            }
        }
    }
    post {
        always {
            archiveArtifacts 'Common/build/libs/**.jar'
            archiveArtifacts 'Neoforge/build/libs/**.jar'
            archiveArtifacts 'Fabric/build/libs/**.jar'
        }
    }
}