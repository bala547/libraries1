void call() {
    node {
        // Define the config object with environment variables
        def config = [
            ENV_NAME: 'dev', // Update this with the actual environment or parameter
            SNOWFLAKE_SCHEMA: 'BALA'
        ]

        // Print debug information about the config object
        echo "Config Object: ${config}"
        echo "Running Flyway migrations for environment: ${config.ENV_NAME}"
        echo "Using schema: ${config.SNOWFLAKE_SCHEMA}"

        // The Docker image for Flyway
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
                    echo "Running Flyway migrations for environment: ${ENV_NAME}"
                    docker run --rm \\
                        -v \$(pwd)/migrations:/flyway/sql \\
                        -e JAVA_TOOL_OPTIONS=--add-opens=java.base/java.nio=ALL-UNNAMED \\
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
