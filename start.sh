#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_MODULE_DIR="${ROOT_DIR}/wify-app"
FRONTEND_DIR="${ROOT_DIR}/wify-web"
LOG_DIR="${ROOT_DIR}/logs"
RUN_DIR="${ROOT_DIR}/run"
BACKEND_LOG_FILE="${LOG_DIR}/wify-app.log"
FRONTEND_LOG_FILE="${LOG_DIR}/wify-web.log"
BACKEND_PID_FILE="${RUN_DIR}/wify-app.pid"
FRONTEND_PID_FILE="${RUN_DIR}/wify-web.pid"

MYSQL_HOST="${MYSQL_HOST:-10.252.0.9}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
REDIS_HOST="${REDIS_HOST:-dev-redis-cluster.pingpongx.com}"
REDIS_PORT="${REDIS_PORT:-6379}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"
SERVICE_CHECK_RETRIES="${SERVICE_CHECK_RETRIES:-3}"
SERVICE_CHECK_INTERVAL="${SERVICE_CHECK_INTERVAL:-2}"
HEALTH_CHECK_RETRIES="${HEALTH_CHECK_RETRIES:-30}"
HEALTH_CHECK_INTERVAL="${HEALTH_CHECK_INTERVAL:-2}"
BACKEND_HEALTH_URL="http://127.0.0.1:${BACKEND_PORT}/api/v1/health"
FRONTEND_HEALTH_URL="http://127.0.0.1:${FRONTEND_PORT}"
SKIP_MYSQL_CHECK="${SKIP_MYSQL_CHECK:-0}"
SKIP_REDIS_CHECK="${SKIP_REDIS_CHECK:-0}"

BACKEND_PID=""
FRONTEND_PID=""
STARTUP_COMPLETED=0

log() {
    printf '[Wify] %s\n' "$*"
}

fail() {
    printf '[Wify] ERROR: %s\n' "$*" >&2
    exit 1
}

cleanup() {
    local exit_code=$?

    if (( STARTUP_COMPLETED == 0 )); then
        if [[ -n "${FRONTEND_PID}" ]] && kill -0 "${FRONTEND_PID}" >/dev/null 2>&1; then
            log "Stopping frontend process ${FRONTEND_PID}"
            kill "${FRONTEND_PID}" >/dev/null 2>&1 || true
            wait "${FRONTEND_PID}" >/dev/null 2>&1 || true
        fi

        if [[ -n "${BACKEND_PID}" ]] && kill -0 "${BACKEND_PID}" >/dev/null 2>&1; then
            log "Stopping backend process ${BACKEND_PID}"
            kill "${BACKEND_PID}" >/dev/null 2>&1 || true
            wait "${BACKEND_PID}" >/dev/null 2>&1 || true
        fi

        rm -f "${BACKEND_PID_FILE}" "${FRONTEND_PID_FILE}"
    fi

    if (( exit_code != 0 )); then
        printf '[Wify] ERROR: Startup aborted.\n' >&2
        printf '[Wify] Backend log: %s\n' "${BACKEND_LOG_FILE}" >&2
        printf '[Wify] Frontend log: %s\n' "${FRONTEND_LOG_FILE}" >&2
    fi
}

trap cleanup EXIT INT TERM

require_command() {
    local command_name="$1"

    command -v "${command_name}" >/dev/null 2>&1 || fail "Missing required command: ${command_name}"
}

ensure_directory() {
    local directory="$1"

    mkdir -p "${directory}" || fail "Cannot create directory: ${directory}"
}

check_tcp() {
    local host="$1"
    local port="$2"

    if command -v python3 >/dev/null 2>&1; then
        python3 - "$host" "$port" <<'PY' >/dev/null 2>&1
import socket
import sys

host = sys.argv[1]
port = int(sys.argv[2])

sock = socket.socket()
sock.settimeout(3)
try:
    sock.connect((host, port))
finally:
    sock.close()
PY
        return $?
    fi

    if command -v nc >/dev/null 2>&1; then
        nc -z -w 2 "${host}" "${port}" >/dev/null 2>&1
        return $?
    fi

    (echo >"/dev/tcp/${host}/${port}") >/dev/null 2>&1
}

ensure_service_available() {
    local service_name="$1"
    local host="$2"
    local port="$3"
    local attempt

    log "Checking ${service_name} on ${host}:${port}"
    for ((attempt = 1; attempt <= SERVICE_CHECK_RETRIES; attempt++)); do
        if check_tcp "${host}" "${port}"; then
            log "${service_name} is reachable"
            return
        fi

        if (( attempt < SERVICE_CHECK_RETRIES )); then
            sleep "${SERVICE_CHECK_INTERVAL}"
        fi
    done

    fail "${service_name} is not available on ${host}:${port}"
}

ensure_port_free() {
    local port="$1"

    if lsof -iTCP:"${port}" -sTCP:LISTEN >/dev/null 2>&1; then
        fail "Port ${port} is already in use"
    fi
}

ensure_pid_file_clear() {
    local pid_file="$1"
    local service_name="$2"
    local pid=""

    if [[ ! -f "${pid_file}" ]]; then
        return
    fi

    pid="$(tr -d '[:space:]' < "${pid_file}")"
    if [[ -n "${pid}" ]] && kill -0 "${pid}" >/dev/null 2>&1; then
        fail "${service_name} appears to be already running with PID ${pid}. Use ./stop.sh first."
    fi

    rm -f "${pid_file}"
}

install_frontend_dependencies() {
    if [[ -d "${FRONTEND_DIR}/node_modules" ]]; then
        return
    fi

    log "Installing frontend dependencies"
    (
        cd "${FRONTEND_DIR}"
        npm install
    ) || fail "Frontend dependency installation failed"
}

build_backend() {
    log "Building backend package"
    (
        cd "${ROOT_DIR}"
        mvn -pl wify-app -am package -DskipTests
    ) || fail "Backend build failed"
}

resolve_backend_jar() {
    local jar_file

    jar_file="$(find "${BACKEND_MODULE_DIR}/target" -maxdepth 1 -type f -name 'wify-app-*.jar' ! -name '*.original' | sort | tail -n 1)"
    [[ -n "${jar_file}" ]] || fail "Cannot find packaged backend jar under ${BACKEND_MODULE_DIR}/target"

    printf '%s\n' "${jar_file}"
}

start_backend() {
    local jar_file

    jar_file="$(resolve_backend_jar)"
    ensure_directory "${LOG_DIR}"
    ensure_directory "${RUN_DIR}"
    : > "${BACKEND_LOG_FILE}"

    log "Starting backend in background"
    nohup java -jar "${jar_file}" --server.port="${BACKEND_PORT}" >"${BACKEND_LOG_FILE}" 2>&1 &
    BACKEND_PID=$!
    printf '%s\n' "${BACKEND_PID}" > "${BACKEND_PID_FILE}"
}

wait_for_http_service() {
    local service_name="$1"
    local service_url="$2"
    local service_pid="$3"
    local service_log_file="$4"
    local attempt

    log "Waiting for ${service_name}: ${service_url}"
    for ((attempt = 1; attempt <= HEALTH_CHECK_RETRIES; attempt++)); do
        if ! kill -0 "${service_pid}" >/dev/null 2>&1; then
            tail -n 40 "${service_log_file}" >&2 || true
            fail "${service_name} exited unexpectedly during startup"
        fi

        if curl -fsS "${service_url}" >/dev/null 2>&1; then
            log "${service_name} is ready"
            return
        fi

        sleep "${HEALTH_CHECK_INTERVAL}"
    done

    tail -n 40 "${service_log_file}" >&2 || true
    fail "${service_name} startup timed out"
}

start_frontend() {
    local previous_dir

    ensure_directory "${LOG_DIR}"
    ensure_directory "${RUN_DIR}"
    : > "${FRONTEND_LOG_FILE}"

    log "Starting frontend development server"
    log "Requested frontend port: ${FRONTEND_PORT}"
    previous_dir="${PWD}"
    cd "${FRONTEND_DIR}" || fail "Cannot enter frontend directory: ${FRONTEND_DIR}"
    nohup npm run dev -- --port "${FRONTEND_PORT}" >"${FRONTEND_LOG_FILE}" 2>&1 &
    FRONTEND_PID=$!
    printf '%s\n' "${FRONTEND_PID}" > "${FRONTEND_PID_FILE}"
    cd "${previous_dir}" || fail "Cannot return to project root"
}

print_summary() {
    log "Backend PID: ${BACKEND_PID} (${BACKEND_PID_FILE})"
    log "Frontend PID: ${FRONTEND_PID} (${FRONTEND_PID_FILE})"
    log "Backend URL: http://127.0.0.1:${BACKEND_PORT}"
    log "Frontend URL: http://127.0.0.1:${FRONTEND_PORT}"
    log "Backend log: ${BACKEND_LOG_FILE}"
    log "Frontend log: ${FRONTEND_LOG_FILE}"
}

main() {
    require_command java
    require_command mvn
    require_command npm
    require_command curl
    require_command lsof

    [[ -d "${BACKEND_MODULE_DIR}" ]] || fail "Backend module directory not found: ${BACKEND_MODULE_DIR}"
    [[ -f "${FRONTEND_DIR}/package.json" ]] || fail "Frontend package.json not found: ${FRONTEND_DIR}/package.json"

    ensure_pid_file_clear "${BACKEND_PID_FILE}" "Backend"
    ensure_pid_file_clear "${FRONTEND_PID_FILE}" "Frontend"

    if [[ "${SKIP_MYSQL_CHECK}" != "1" ]]; then
        ensure_service_available "MySQL" "${MYSQL_HOST}" "${MYSQL_PORT}"
    else
        log "Skipping MySQL availability check"
    fi

    if [[ "${SKIP_REDIS_CHECK}" != "1" ]]; then
        ensure_service_available "Redis" "${REDIS_HOST}" "${REDIS_PORT}"
    else
        log "Skipping Redis availability check"
    fi

    ensure_port_free "${BACKEND_PORT}"
    ensure_port_free "${FRONTEND_PORT}"

    install_frontend_dependencies
    build_backend
    start_backend
    wait_for_http_service "Backend" "${BACKEND_HEALTH_URL}" "${BACKEND_PID}" "${BACKEND_LOG_FILE}"
    start_frontend
    wait_for_http_service "Frontend" "${FRONTEND_HEALTH_URL}" "${FRONTEND_PID}" "${FRONTEND_LOG_FILE}"
    STARTUP_COMPLETED=1
    print_summary
}

main "$@"
