void call() {
    node {
            def flywayImage = 'flyway/flyway:10.17.3'
            echo "Pulling Flyway Docker image: ${flywayImage}"
            sh "docker pull ${flywayImage}"
            echo "Flyway Docker image ${flywayImage} is ready for use"
    }
}
