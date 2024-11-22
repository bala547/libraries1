void call() {
    node {
        // The Docker image for Flyway
        def flywayImage = 'flyway/flyway:10.17.3' 

        // Print a message indicating that migrations will start
        echo "Running Flyway Migrations..."

        // Use the 'withCredentials' block to inject the stored secrets (SNOWFLAKE_URL, SNOWFLAKE_USERNAME, SNOWFLAKE_PASSWORD)
        withCredentials([string(credentialsId: 'SNOWFLAKE_URL', variable: 'SNOWFLAKE_URL'),
                         string(credentialsId: 'SNOWFLAKE_USERNAME', variable: 'SNOWFLAKE_USERNAME'),
                         string(credentialsId: 'SNOWFLAKE_PASSWORD', variable: 'SNOWFLAKE_PASSWORD')]) {

            // Run the Flyway migration command using the injected credentials
            sh """
                docker run --rm \\
                    -v \$(pwd)/migrations:/flyway/sql \\
                    -e JAVA_TOOL_OPTIONS=--add-opens=java.base/java.nio=ALL-UNNAMED \\
                    ${flywayImage} \\
                    -url='jdbc:snowflake://${SNOWFLAKE_URL}' \\
                    -user='${SNOWFLAKE_USERNAME}' \\
                    -password='${SNOWFLAKE_PASSWORD}' \\
                    -schemas='BALA' \\
                    -locations=filesystem:/flyway/sql \\
                    -baselineOnMigrate=true \\
                    migrate
            """
        }
    }
}
