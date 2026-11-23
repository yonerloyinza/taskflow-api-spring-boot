#!/usr/bin/env bash
###############################################################################
# Provisionamiento de VPS para taskflow-api-spring-boot
#
# Requisitos del VPS (AWS EC2):
#   - Ubuntu 22.04/24.04 LTS o Amazon Linux 2023
#   - Usuario con sudo
#   - Puerto 22 (SSH) y 443 (HTTPS/Traefik) abiertos en el security group
#
# Uso:
#   ssh usuario@vps "bash -s" < provision-vps.sh
#   o copiar el archivo al VPS y ejecutar: sudo bash provision-vps.sh
###############################################################################
set -euo pipefail

# -----------------------------------------------------------------------------
# Configuración
# -----------------------------------------------------------------------------
REPO_DIR="/opt/taskflow-ecosystem/taskflow-api-spring-boot"
ENV_FILE="/opt/taskflow/.env.springboot"
JAVA_VERSION="21"
MAVEN_VERSION="3.9.9"
DOCKER_COMPOSE_PLUGIN_DIR="/usr/libexec/docker/cli-plugins"

# -----------------------------------------------------------------------------
# Helpers
# -----------------------------------------------------------------------------
log() { echo "[provision] $*"; }

detect_os() {
    if [ -f /etc/os-release ]; then
        . /etc/os-release
        OS_ID="$ID"
        OS_VERSION_ID="$VERSION_ID"
    else
        log "ERROR: no se pudo detectar el sistema operativo"
        exit 1
    fi
}

# -----------------------------------------------------------------------------
# 1. Actualizar paquetes del sistema
# -----------------------------------------------------------------------------
log "Paso 1: actualizando paquetes del sistema"
detect_os

case "$OS_ID" in
    ubuntu|debian)
        export DEBIAN_FRONTEND=noninteractive
        apt-get update -y
        apt-get upgrade -y
        apt-get install -y \
            curl \
            wget \
            gnupg \
            ca-certificates \
            lsb-release \
            git \
            unzip \
            fontconfig \
            libfreetype6
        ;;
    amazon|amzn)
        yum update -y
        yum install -y \
            curl \
            wget \
            gnupg \
            ca-certificates \
            git \
            unzip \
            fontconfig \
            freetype
        ;;
    *)
        log "WARN: OS no soportado automáticamente ($OS_ID). Continuando asumiendo paquetes presentes."
        ;;
esac

# -----------------------------------------------------------------------------
# 2. Instalar Java 21 (Eclipse Temurin)
# -----------------------------------------------------------------------------
log "Paso 2: instalando OpenJDK/Eclipse Temurin $JAVA_VERSION"

if ! command -v java >/dev/null 2>&1 || ! java -version 2>&1 | grep -q "openjdk version \"$JAVA_VERSION"; then
    case "$OS_ID" in
        ubuntu|debian)
            # Adicionar repositorio Adoptium (Eclipse Temurin)
            wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
            echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/temurin/debian $(lsb_release -cs) main" \
                > /etc/apt/sources.list.d/adoptium.list
            apt-get update -y
            apt-get install -y temurin-$JAVA_VERSION-jdk
            ;;
        amazon|amzn)
            # Amazon Linux usa Amazon Corretto o OpenJDK desde amazon-linux-extras
            amazon-linux-extras enable corretto21 2>/dev/null || true
            yum install -y java-21-amazon-corretto-devel || yum install -y java-21-openjdk-devel
            ;;
    esac
else
    log "Java ya instalado: $(java -version 2>&1 | head -1)"
fi

export JAVA_HOME
JAVA_HOME=$(dirname $(dirname $(readlink -f $(command -v java))))
echo "JAVA_HOME=$JAVA_HOME" >> /etc/environment

# -----------------------------------------------------------------------------
# 3. Instalar Maven 3.9
# -----------------------------------------------------------------------------
log "Paso 3: instalando Apache Maven $MAVEN_VERSION"

if ! command -v mvn >/dev/null 2>&1; then
    MAVEN_TAR="apache-maven-${MAVEN_VERSION}-bin.tar.gz"
    wget -q "https://archive.apache.org/dist/maven/maven-3/${MAVEN_VERSION}/binaries/${MAVEN_TAR}" \
        -O "/tmp/${MAVEN_TAR}"
    tar -xzf "/tmp/${MAVEN_TAR}" -C /opt
    ln -sfn "/opt/apache-maven-${MAVEN_VERSION}" /opt/maven
    ln -sfn /opt/maven/bin/mvn /usr/local/bin/mvn
    rm -f "/tmp/${MAVEN_TAR}"
else
    log "Maven ya instalado: $(mvn -version | head -1)"
fi

# -----------------------------------------------------------------------------
# 4. Instalar Docker CE + Docker Compose plugin
# -----------------------------------------------------------------------------
log "Paso 4: instalando Docker CE y Docker Compose"

if ! command -v docker >/dev/null 2>&1; then
    case "$OS_ID" in
        ubuntu|debian)
            # Repositorio oficial de Docker
            install -m 0755 -d /etc/apt/keyrings
            curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
                | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
            echo \
                "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
                https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" \
                > /etc/apt/sources.list.d/docker.list
            apt-get update -y
            apt-get install -y docker-ce docker-ce-cli containerd.io \
                docker-buildx-plugin docker-compose-plugin
            ;;
        amazon|amzn)
            yum install -y docker
            # Docker Compose plugin para Amazon Linux
            mkdir -p "$DOCKER_COMPOSE_PLUGIN_DIR"
            curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" \
                -o "$DOCKER_COMPOSE_PLUGIN_DIR/docker-compose"
            chmod +x "$DOCKER_COMPOSE_PLUGIN_DIR/docker-compose"
            ;;
    esac
    systemctl enable --now docker
else
    log "Docker ya instalado: $(docker --version)"
fi

# Agregar usuario actual al grupo docker (si no es root)
if [ "${SUDO_USER:-}" != "" ]; then
    usermod -aG docker "$SUDO_USER" || true
    log "Usuario $SUDO_USER agregado al grupo docker (rellene sesión para aplicar)"
fi

# -----------------------------------------------------------------------------
# 5. Crear directorios y archivo de entorno
# -----------------------------------------------------------------------------
log "Paso 5: creando directorios y archivo de entorno"

mkdir -p /opt/taskflow
mkdir -p "$REPO_DIR"

if [ ! -f "$ENV_FILE" ]; then
    cat > "$ENV_FILE" <<EOF
# Variables de entorno de taskflow-api-spring-boot (produccion)
# Generado por provision-vps.sh $(date -u +%F)
# Edite DATABASE_URL, DIRECT_URL y DATABASE_PASSWORD antes de desplegar.

SPRING_PROFILES_ACTIVE=production
SERVER_PORT=3000

# Reemplace POOLER_HOST y DIRECT_HOST con los endpoints de su proveedor (InsForge/Supabase)
DATABASE_URL=postgresql://POOLER_HOST:6543/postgres?sslmode=require
DIRECT_URL=postgresql://DIRECT_HOST:5432/postgres?sslmode=require
DATABASE_USER=postgres
DATABASE_PASSWORD=change_me

CORS_ORIGINS=*
RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS=100
RATE_LIMIT_WINDOW_SECONDS=60
EOF
    chmod 600 "$ENV_FILE"
    log "Archivo de entorno creado en $ENV_FILE (EDITE las credenciales antes de desplegar)"
else
    log "Archivo de entorno ya existe en $ENV_FILE (no se sobrescribe)"
fi

# -----------------------------------------------------------------------------
# 6. Clonar el repositorio si no existe
# -----------------------------------------------------------------------------
log "Paso 6: verificando repositorio"

if [ ! -d "$REPO_DIR/.git" ]; then
    log "El repositorio no esta clonado en $REPO_DIR."
    log "Clonelo manualmente o ejecute el deploy de GitHub Actions primero."
    log "Ejemplo: git clone https://github.com/yonerloyinza/taskflow-api-spring-boot.git $REPO_DIR"
else
    log "Repositorio ya presente en $REPO_DIR"
fi

# -----------------------------------------------------------------------------
# 7. Verificaciones finales
# -----------------------------------------------------------------------------
log "Paso 7: verificaciones finales"

echo "------------------------------------------------------------"
echo " Java:     $(java -version 2>&1 | head -1)"
echo " Maven:    $(mvn -version 2>&1 | head -1)"
echo " Docker:   $(docker --version)"
echo " Compose:  $(docker compose version 2>/dev/null || docker-compose version 2>/dev/null || echo 'n/a')"
echo " Repo:     $REPO_DIR"
echo " Env:      $ENV_FILE"
echo "------------------------------------------------------------"

log "Provisionamiento completado."
log "Siguientes pasos:"
log "  1. Edite $ENV_FILE con DATABASE_URL, DIRECT_URL y DATABASE_PASSWORD de su proveedor (InsForge/Supabase)."
log "  2. Asegurese de que el repo este clonado en $REPO_DIR."
log "  3. Despliegue: cd $REPO_DIR && docker compose up -d --build --no-deps api-springboot"
log "  4. Verifique: curl -sf https://<tu-host>/health | grep '\"status\":\"ok\"'"
