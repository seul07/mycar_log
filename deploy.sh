#!/bin/bash
set -e

COMPOSE_FILE="docker-compose.yml"

usage() {
    echo "Usage: $0 [OPTION]"
    echo ""
    echo "Options:"
    echo "  --build    Build and start containers"
    echo "  --down     Stop and remove containers"
    echo "  --logs     Show container logs"
    echo "  --restart  Restart containers with rebuild"
    echo "  --status   Show container status"
    echo "  -h, --help Show this help message"
}

build() {
    echo "Building and starting containers..."
    docker compose -f "$COMPOSE_FILE" up -d --build
    echo "Containers started successfully."
}

down() {
    echo "Stopping and removing containers..."
    docker compose -f "$COMPOSE_FILE" down
    echo "Containers stopped."
}

logs() {
    docker compose -f "$COMPOSE_FILE" logs -f
}

restart() {
    echo "Restarting containers..."
    down
    build
}

status() {
    docker compose -f "$COMPOSE_FILE" ps
}

case "${1}" in
    --build)
        build
        ;;
    --down)
        down
        ;;
    --logs)
        logs
        ;;
    --restart)
        restart
        ;;
    --status)
        status
        ;;
    -h|--help)
        usage
        ;;
    *)
        usage
        exit 1
        ;;
esac
