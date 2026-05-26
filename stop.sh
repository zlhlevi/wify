#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RUN_DIR="${ROOT_DIR}/run"
BACKEND_PID_FILE="${RUN_DIR}/wify-app.pid"
FRONTEND_PID_FILE="${RUN_DIR}/wify-web.pid"
STOP_TIMEOUT="${STOP_TIMEOUT:-15}"
KILL_WAIT_SECONDS="${KILL_WAIT_SECONDS:-3}"

log() {
    printf '[Wify] %s\n' "$*"
}

stop_service() {
    local service_name="$1"
    local pid_file="$2"
    local pid=""
    local waited=0

    if [[ ! -f "${pid_file}" ]]; then
        log "${service_name} PID file not found, skipping"
        return 0
    fi

    pid="$(tr -d '[:space:]' < "${pid_file}")"
    if [[ -z "${pid}" ]]; then
        rm -f "${pid_file}"
        log "${service_name} PID file is empty, cleaned up"
        return 0
    fi

    if ! kill -0 "${pid}" >/dev/null 2>&1; then
        rm -f "${pid_file}"
        log "${service_name} process ${pid} is not running, cleaned up stale PID file"
        return 0
    fi

    log "Stopping ${service_name} process ${pid} with SIGTERM"
    kill -TERM "${pid}" >/dev/null 2>&1 || true

    while kill -0 "${pid}" >/dev/null 2>&1; do
        if (( waited >= STOP_TIMEOUT )); then
            log "${service_name} did not exit within ${STOP_TIMEOUT}s, sending SIGKILL"
            kill -KILL "${pid}" >/dev/null 2>&1 || true
            sleep "${KILL_WAIT_SECONDS}"
            break
        fi

        sleep 1
        waited=$((waited + 1))
    done

    if kill -0 "${pid}" >/dev/null 2>&1; then
        log "ERROR: ${service_name} process ${pid} could not be stopped"
        return 1
    fi

    rm -f "${pid_file}"
    log "${service_name} stopped"
}

main() {
    local failed=0

    stop_service "Frontend" "${FRONTEND_PID_FILE}" || failed=1
    stop_service "Backend" "${BACKEND_PID_FILE}" || failed=1

    if (( failed != 0 )); then
        printf '[Wify] ERROR: Some processes could not be stopped cleanly.\n' >&2
        exit 1
    fi
}

main "$@"
