# E16-H7 — Não aplicável ao arremateai-orchestrator

## Contexto

A história **E16-H7** ("Flyway migrations nos serviços pendentes") requer:
1. Adicionar `flyway-core` ao `pom.xml`.
2. Criar `V1__init.sql` em `src/main/resources/db/migration/`.
3. Mudar `spring.jpa.hibernate.ddl-auto` para `validate`.

## Por que não se aplica a este serviço

O microsserviço `arremateai-orchestrator` é um **REST aggregator/BFF sem persistência própria**:

- **`pom.xml`**: não há `spring-boot-starter-data-jpa`, `postgresql` nem nenhum driver JDBC.
- **`application.yml`**: não há bloco `spring.datasource` nem `spring.jpa`.
- **Entidades JPA**: zero arquivos anotados com `@Entity` em `src/main/java`.
- **Responsabilidade**: compor chamadas REST para `arremateai-property-catalog`, `arremateai-vendor`, `arremateai-media` e `arremateai-userprofile`, retornando payloads agregados ao front. Estado fica nos serviços subjacentes.

Portanto não há schema próprio para gerenciar via Flyway.

## Evidência

| Verificação | Resultado |
| --- | --- |
| Busca por `@Entity` em `src/main/java` | 0 matches |
| `spring-boot-starter-data-jpa` em `pom.xml` | ausente |
| Bloco `spring.datasource` em `application.yml` | ausente |

## Conclusão

Este serviço está marcado como **N/A** no relatório consolidado da E16-H7.
Caso futuramente seja introduzida persistência relacional (por exemplo, para cache de agregações ou auditoria), a história correspondente deverá incluir a adição do Flyway junto com o JPA.
