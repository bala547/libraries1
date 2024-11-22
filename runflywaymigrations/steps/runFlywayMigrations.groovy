void call() {
    node {
        def flywayImage = 'flyway/flyway:10.17.3' 

        echo "Running Flyway Migrations..."

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
