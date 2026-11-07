# SIRAM frontend

Interfaz web ciudadana para registrar incidentes y consultar reportes por tipo.
Se comunica con el Gateway GraphQL y usa únicamente los campos definidos por su
esquema actual.

## Desarrollo local

Requiere Node.js y npm. Inicia primero los servicios backend:

```powershell
docker compose up -d emergencias-db notificacion-db zookeeper kafka emergencias-service notificacion-service graphql-gateway
```

Luego, desde `frontend/`:

```powershell
npm install
npm run dev
```

Abre <http://localhost:5173>. Vite reenvía `/graphql` al Gateway en
`http://localhost:8080/graphql`.

Para registrar un incidente, permite al navegador acceder a la ubicación del
dispositivo. La ubicación se obtiene al tocar el botón y solo se envía al crear
el reporte. El navegador puede requerir una conexión segura para compartirla;
`localhost` se considera segura durante el desarrollo.

## Docker Compose

Desde la raíz del proyecto:

```powershell
docker compose up --build frontend
```

La interfaz queda en <http://localhost:5173>. Nginx reenvía `/graphql` al
servicio `graphql-gateway` dentro de la red de Compose.
