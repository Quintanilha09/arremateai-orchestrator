# ArremateAI Orchestrator

Microsserviço responsável por compor jornadas entre serviços do ecossistema ArremateAI.

## Requisitos

- Java 17+
- Maven 3.9+

## Configuração

1. Copie o arquivo .env.example para .env.
2. Ajuste as URLs de serviços conforme o ambiente.
3. Defina INTERNAL_API_KEY quando houver validação entre serviços.

## Execução local

```bash
mvn spring-boot:run
```

Aplicação disponível na porta definida por SERVER_PORT (padrão 8087).

## Variáveis principais

- SERVER_PORT
- PROPERTY_CATALOG_URL
- MEDIA_URL
- VENDOR_URL
- USERPROFILE_URL
- NOTIFICATION_URL
- INTERNAL_API_KEY
- ADMIN_USER_ID

## Endpoints úteis

- Health: /actuator/health
- Swagger UI: /api/orchestrator/swagger-ui.html
- OpenAPI: /api/orchestrator/v3/api-docs
