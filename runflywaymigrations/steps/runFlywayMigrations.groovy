// runflywaymigrations.groovy

def call() {
    node {
        // Ensure ENV_NAME is set (from parameter passed to pipeline)
        if (!params.ENV_NAME) {
            error "ENV_NAME parameter is missing or not set."
        }

        // Define the environment configuration file path based on the ENV_NAME parameter
        def envConfigFile = "environments/${params.ENV_NAME}.groovy"

        // Ensure the config file exists in the GitHub repository
        echo "Looking for config file: ${envConfigFile}"

        // Assuming that the environment files (dev.groovy, prod.groovy, uat.groovy) are in the repository
        // and are being checked out during the pipeline
        if (!fileExists(envConfigFile)) {
            error "Config file for environment ${params.ENV_NAME} does not exist."
        }

        // Load the environment-specific configuration (e.g., dev.groovy, uat.groovy, prod.groovy)
        load envConfigFile

        // Debugging information: Print out the loaded environment config
        echo "Using configuration for ${config.ENV_NAME} environment"
        echo "Using schema: ${config.SNOWFLAKE_SCHEMA}"

        // Define the Docker image for Flyway
        def flywayImage = 'flyway/flyway:10.17.3'

        // Print a message indicating that migrations will start
        echo "Running Flyway Migrations..."

        // Use the 'withCredentials' block to inject the stored secrets (SNOWFLAKE_URL, SNOWFLAKE_USERNAME, SNOWFLAKE_PASSWORD)
        withCredentials([string(credentialsId: 'SNOWFLAKE_URL', variable: 'SNOWFLAKE_URL'),
                         string(credentialsId: 'SNOWFLAKE_USERNAME', variable: 'SNOWFLAKE_USERNAME'),
                         string(credentialsId: 'SNOWFLAKE_PASSWORD', variable: 'SNOWFLAKE_PASSWORD')]) {

            // Set the ENV_NAME as an environment variable inside the 'sh' block
            withEnv(["ENV_NAME=${config.ENV_NAME}"]) {
                // Run the Flyway migration command using the injected credentials
                sh """
                    echo "Running Flyway migrations for environment: ${config.ENV_NAME}"
                    docker run --rm \\
                        -v \$(pwd)/migrations:/flyway/sql \\
                        -e JAVA_TOOL_OPTIONS=--add-opens=java.base/java.nio=ALL-UNNAMED \\
                        -e SNOWFLAKE_URL=${SNOWFLAKE_URL} \\
                        -e SNOWFLAKE_USERNAME=${SNOWFLAKE_USERNAME} \\
                        -e SNOWFLAKE_PASSWORD=${SNOWFLAKE_PASSWORD} \\
                        ${flywayImage} \\
                        -url='jdbc:snowflake://${SNOWFLAKE_URL}' \\
                        -user='${SNOWFLAKE_USERNAME}' \\
                        -password='${SNOWFLAKE_PASSWORD}' \\
                        -schemas='${config.SNOWFLAKE_SCHEMA}' \\
                        -locations=filesystem:/flyway/sql \\
                        -baselineOnMigrate=true \\
                        migrate
                """
            }
        }
    }
}
