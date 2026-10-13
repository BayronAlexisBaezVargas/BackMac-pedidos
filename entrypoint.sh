#!/bin/sh
set -e

echo "Arrancando PostgreSQL en el puerto 5435..."
su-exec postgres pg_ctl start -D /var/lib/postgresql/data -w -o "-p 5435"

echo "Configurando la base de datos y usuario para Pedidos..."
su-exec postgres psql -p 5435 -c "CREATE DATABASE pedidos_db;" || true
su-exec postgres psql -p 5435 -c "CREATE USER springuser WITH PASSWORD 'springpassword';" || true
su-exec postgres psql -p 5435 -c "GRANT ALL PRIVILEGES ON DATABASE pedidos_db TO springuser;" || true
su-exec postgres psql -p 5435 -d pedidos_db -c "GRANT ALL ON SCHEMA public TO springuser;" || true

echo "Arrancando microservicio de Pedidos..."
exec java -jar /app/app.jar
