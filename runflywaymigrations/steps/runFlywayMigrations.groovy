void call() {
    node {
        // The Docker image for Flyway
        def flywayImage = 'flyway/flyway:10.17.3' 

        // Print a message indicating that migrations will start
        echo "Running Flyway Migrations..."
        def envConfig = load "environments/${params.ENVIRONMENT}.groovy"

            // Run the Flyway migration command using the injected credentials
            sh """
                docker run --rm \\
                    -v \$(pwd)/migrations:/flyway/sql \\
                    -e JAVA_TOOL_OPTIONS=--add-opens=java.base/java.nio=ALL-UNNAMED \\
                    ${flywayImage} \\
                    -url='jdbc:snowflake://${envConfig.config.SNOWFLAKE_URL}' \\
                    -user='${envConfig.config.SNOWFLAKE_USER}' \\
                    -password='${envConfig.config.SNOWFLAKE_PASSWORD}' \\
                    -schemas='${envConfig.config.SNOWFLAKE_SCHEMA}' \\
                    -locations=filesystem:/flyway/sql \\
                    -baselineOnMigrate=true \\
                    migrate
            """
    }
}
