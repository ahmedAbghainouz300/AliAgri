#!/bin/bash
set -eu

PGDATA=/var/lib/postgresql/data
PG_USER=aliagri
PG_DB=aliagri

mkdir -p /run/postgresql "$PGDATA"
chown postgres:postgres /run/postgresql "$PGDATA"
chmod 700 "$PGDATA"

# 1) Cluster init (only when the volume is empty)
if [ ! -s "$PGDATA/PG_VERSION" ]; then
  echo ">> Initializing PostgreSQL cluster"
  su-exec postgres initdb -D "$PGDATA" -E UTF8 --auth-local=trust --auth-host=scram-sha-256
fi

# 2) Password: use DB_PASSWORD if given, else generate one once and keep it in the volume
PW_FILE="$PGDATA/.aliagri_db_password"
if [ -z "${DB_PASSWORD:-}" ]; then
  [ -s "$PW_FILE" ] || { head -c 24 /dev/urandom | base64 | tr -d '/+=\n' > "$PW_FILE"; chmod 600 "$PW_FILE"; }
  DB_PASSWORD="$(cat "$PW_FILE")"
fi
export SPRING_DATASOURCE_PASSWORD="$DB_PASSWORD"

# 3) Create role + database once (marker file, so a half-finished first run is retried)
if [ ! -f "$PGDATA/.aliagri_ready" ]; then
  echo ">> Creating role and database"
  su-exec postgres pg_ctl -D "$PGDATA" -w -o "-c listen_addresses=''" start     # unix socket only; -w waits until ready
  su-exec postgres psql -v ON_ERROR_STOP=1 -v pw="$DB_PASSWORD" -d postgres <<'SQL'
CREATE ROLE aliagri LOGIN PASSWORD :'pw';
CREATE DATABASE aliagri OWNER aliagri;
SQL
  su-exec postgres pg_ctl -D "$PGDATA" -w stop
  touch "$PGDATA/.aliagri_ready"
fi

# 4) Start everything
echo ">> Starting PostgreSQL"
su-exec postgres postgres -D "$PGDATA" -c listen_addresses=127.0.0.1 &
PG_PID=$!
until su-exec postgres pg_isready -h 127.0.0.1 -q; do
  kill -0 "$PG_PID" 2>/dev/null || { echo "!! PostgreSQL crashed during startup"; exit 1; }
  sleep 1
done

echo ">> Starting Spring Boot"
java -XX:MaxRAMPercentage=60 -jar /app/app.jar &
JAVA_PID=$!

echo ">> Starting Nginx"
nginx -g 'daemon off;' &
NGINX_PID=$!

shutdown() {
  trap - EXIT
  kill "$NGINX_PID" "$JAVA_PID" 2>/dev/null || true
  wait "$JAVA_PID" 2>/dev/null || true          # let Spring close its DB connections first
  kill -INT "$PG_PID" 2>/dev/null || true       # SIGINT = fast shutdown
  wait "$PG_PID" 2>/dev/null || true
}
trap shutdown EXIT
trap 'exit 0' TERM INT

# If any of the 3 processes dies, stop the container so "--restart unless-stopped" brings it back clean
wait -n || true
echo "!! A process exited, stopping container"
exit 1