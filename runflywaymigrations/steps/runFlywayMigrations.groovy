def call() {
    node {
        // Load the environment-specific configuration file based on the selected ENV_NAME
        def envConfigFile = "environments/${params.ENV_NAME}.groovy"

        // Check if the config file exists in the 'environments' folder
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
