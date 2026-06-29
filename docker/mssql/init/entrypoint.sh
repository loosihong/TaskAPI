#!/bin/bash

# Start MSSQL in background
/opt/mssql/bin/sqlservr &
MSSQL_PID=$!

echo "Waiting for MSSQL to start..."
until /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" -Q "SELECT 1" -No &>/dev/null; do
    sleep 2
done

echo "MSSQL ready. Running init scripts..."
/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P "$MSSQL_SA_PASSWORD" \
  -v DB_NAME="$DB_NAME" APP_USER="$APP_USER" APP_PASSWORD="$APP_PASSWORD" \
  -i /docker-entrypoint-initdb.d/01-init.sql -No

echo "Init complete."
wait $MSSQL_PID