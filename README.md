# Projeto - Cidades ESG Inteligentes

API REST em **Java Spring Boot** para registrar e consultar **indicadores ESG** (Ambiental, Social e Governança) de cidades — por exemplo consumo de energia, taxa de coleta seletiva, treinamentos de inclusão ou conformidade ambiental. O projeto aplica práticas completas de DevOps: pipeline CI/CD, containerização com Docker e orquestração com Docker Compose, com deploy automatizado em **staging** e **produção**.

**Integrantes (Grupo 112):**

| Nome | RM |
|------|----|
| Sarah Schulze Bittar Bonafé | RM563780 |

## Estrutura do projeto

```
cidades-esg-inteligentes/
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── pom.xml
├── scripts/
│   ├── deploy.sh                  # pull da imagem + compose up + espera "healthy"
│   └── smoke-test.sh              # testes pós-deploy (health, ambiente, API) + evidências
├── docs/                          # documentação técnica (PDF)
├── prints/                        # evidências de execução
├── src/
│   ├── main/java/br/com/fiap/esg/ # controller, service, repository, model
│   ├── main/resources/application.properties
│   └── test/java/br/com/fiap/esg/ # testes automatizados
└── .github/workflows/
    ├── ci-cd.yml                  # pipeline principal
    └── deploy.yml                 # deploy reutilizável (staging/produção)
```

## API

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/indicadores` | Lista indicadores (filtro opcional `?categoria=AMBIENTAL\|SOCIAL\|GOVERNANCA`) |
| GET | `/api/indicadores/{id}` | Busca um indicador |
| POST | `/api/indicadores` | Cria um indicador |
| PUT | `/api/indicadores/{id}` | Atualiza um indicador |
| DELETE | `/api/indicadores/{id}` | Remove um indicador |
| GET | `/actuator/health` | Estado da aplicação (usado no healthcheck) |
| GET | `/actuator/info` | Mostra em qual ambiente a aplicação está (`local`, `staging`, `production`) |

Exemplo de corpo para `POST`/`PUT`:

```json
{
  "cidade": "São Paulo",
  "categoria": "AMBIENTAL",
  "nome": "Consumo de energia",
  "valor": 1250.5,
  "unidade": "MWh",
  "dataMedicao": "2026-09-01"
}
```

## Como executar localmente com Docker

Pré-requisitos: Docker e Docker Compose instalados.

```bash
# 1. Crie o arquivo de variáveis de ambiente
cp .env.example .env

# 2. Suba aplicação + banco (constrói a imagem na primeira vez)
docker compose up -d --build

# 3. Acompanhe os logs (opcional)
docker compose logs -f app

# 4. Teste
curl http://localhost:8080/actuator/health
curl -X POST http://localhost:8080/api/indicadores \
  -H "Content-Type: application/json" \
  -d '{"cidade":"São Paulo","categoria":"AMBIENTAL","nome":"Consumo de energia","valor":1250.5,"unidade":"MWh","dataMedicao":"2026-09-01"}'
curl http://localhost:8080/api/indicadores

# 5. Para encerrar (use -v para apagar também os volumes)
docker compose down
```

Para rodar apenas os testes sem Docker (requer JDK 17 e Maven): `mvn verify`.

## Pipeline CI/CD

**Ferramenta:** GitHub Actions (`.github/workflows/`).

| # | Job | Quando roda | O que faz |
|---|-----|-------------|-----------|
| 1 | `build-test` | push e pull request | Configura JDK 17, compila e executa os testes (`mvn verify`); publica relatórios de teste e o JAR como artefatos |
| 2 | `container-smoke` | após o job 1 | Sobe app + banco com `docker compose`, valida `/actuator/health` e cria/lista um indicador pela API |
| 3 | `docker-publish` | push na `main` | Constrói a imagem e publica no GitHub Container Registry (`ghcr.io`) com as tags `sha-<commit>` e `latest` |
| 4 | `deploy-staging` | após o job 3 | Deploy automático no environment **staging** + smoke test |
| 5 | `deploy-production` | após staging OK | Deploy no environment **production** (mesma imagem) + smoke test; aceita aprovação manual |

**Lógica do pipeline:** cada job só roda se o anterior passou. Um build ou teste quebrado nunca gera imagem; uma imagem que falha no teste de container nunca é publicada; uma versão que falha em staging nunca chega à produção.

**Funcionamento do deploy** (`deploy.yml`, workflow reutilizável chamado uma vez para cada ambiente):

1. O job declara o **GitHub Environment** do ambiente (`staging` ou `production`) — o GitHub registra cada deploy na aba *Deployments* do repositório e permite exigir aprovação manual em produção (*Required reviewers*).
2. Gera um `.env` específico do ambiente: `APP_ENV`, `COMPOSE_PROJECT_NAME=cidades-esg-<ambiente>`, porta (8081 staging / 8082 produção), banco `esg_<ambiente>`, senha vinda do secret `DB_PASSWORD` do environment (ou gerada aleatoriamente) e a imagem `sha-<commit>` publicada no job 3.
3. `scripts/deploy.sh`: faz login no GHCR (token via stdin), `docker compose pull`, `docker compose up -d --no-build` e espera o container ficar `healthy`.
4. `scripts/smoke-test.sh`: valida `/actuator/health` = UP, confere em `/actuator/info` que a aplicação está no ambiente certo, cria (POST) e lista (GET) um indicador ESG. As respostas são gravadas no **resumo da execução** (evidências).
5. Registra `docker compose ps` e os logs da aplicação no resumo e encerra o ambiente.

**Promoção de artefato:** staging e produção usam **a mesma imagem** (tag `sha-<commit>`); só as variáveis de ambiente mudam. **Rollback:** reexecutar o deploy com uma tag `sha-...` anterior.

**Onde os ambientes rodam:** os ambientes staging e produção são provisionados pelo próprio pipeline em runners do GitHub Actions (hosts Linux com Docker), de forma efêmera — cada deploy sobe app + PostgreSQL, valida e registra as evidências. Os scripts são independentes do host: o mesmo `deploy.sh` pode ser executado em uma VM na nuvem (Azure, AWS etc.) via SSH para manter os ambientes permanentemente no ar.

### Configuração no GitHub (opcional)

Os environments `staging` e `production` são criados automaticamente na primeira execução. Opcionalmente, em **Settings → Environments**:

| Environment | Secret | Variable | Proteção |
|-------------|--------|----------|----------|
| `staging` | `DB_PASSWORD` | `APP_PORT` | — |
| `production` | `DB_PASSWORD` | `APP_PORT` | **Required reviewers** (aprovação manual antes do deploy) |

## Containerização

**Dockerfile** (multi-stage):

```dockerfile
# ---------- Estágio 1: build ----------
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Estágio 2: runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S esg && adduser -S esg -G esg \
    && mkdir -p /app/logs && chown -R esg:esg /app
USER esg
COPY --from=build --chown=esg:esg /app/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Estratégias adotadas**

- **Multi-stage build:** o Maven e o código-fonte ficam só no primeiro estágio; a imagem final tem apenas o JRE e o JAR (menor e mais segura).
- **Cache de dependências:** o `pom.xml` é copiado antes do `src`, então as dependências só são baixadas de novo quando o `pom.xml` muda.
- **Usuário não-root:** a aplicação roda com o usuário `esg`.
- **Healthcheck:** usa o Actuator; o compose só inicia a aplicação depois que o banco está saudável (`depends_on` com `service_healthy`).

**Orquestração (`docker-compose.yml`)**

- **Serviços:** `app` (Spring Boot) e `db` (PostgreSQL 16).
- **Rede:** `esg-net` (bridge) — a aplicação acessa o banco pelo nome `db`; o banco não é publicado no host.
- **Volumes:** `pgdata` (dados do PostgreSQL) e `applogs` (logs da aplicação) — os dados sobrevivem a recriações dos containers.
- **Variáveis de ambiente:** definidas no `.env` (modelo em `.env.example`): `APP_ENV`, `APP_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `APP_IMAGE`, `COMPOSE_PROJECT_NAME`.

## Prints do funcionamento

> Substitua os itens abaixo pelas imagens/links reais (salve as imagens em uma pasta `prints/` e referencie com `![descrição](prints/arquivo.png)`).

- [ ] Pipeline executado com sucesso (aba **Actions**: build, testes, container-smoke, publish)
- [ ] Relatório/logs dos testes automatizados
- [ ] Imagem publicada no GitHub Container Registry
- [ ] Aprovação manual do deploy em produção
- [ ] Deploy em **staging** concluído + `/actuator/info` mostrando `"environment":"staging"`
- [ ] Deploy em **produção** concluído + `/actuator/info` mostrando `"environment":"production"`
- [ ] API funcionando (POST/GET em `/api/indicadores`) em staging e em produção
- [ ] `docker compose ps` / `docker ps` com os containers `healthy`

**URLs dos ambientes:** staging: _preencher_ · produção: _preencher_

## Tecnologias utilizadas

- **Linguagem/framework:** Java 17, Spring Boot 3.3 (Web, Data JPA, Validation, Actuator)
- **Banco de dados:** PostgreSQL 16
- **Testes:** JUnit 5, Mockito, Spring MockMvc (`@WebMvcTest`)
- **Build:** Maven
- **Containerização/orquestração:** Docker (multi-stage), Docker Compose
- **CI/CD:** GitHub Actions, GitHub Container Registry, GitHub Environments (aprovação manual em produção)
- **Deploy:** GitHub Environments (staging/production) + Docker Compose; scripts Bash de deploy e smoke test

## Checklist de entrega

| Item | OK |
|------|----|
| Projeto compactado em .ZIP com estrutura organizada | ☑ |
| Dockerfile funcional | ☑ |
| docker-compose.yml ou arquivos Kubernetes | ☑ |
| Pipeline com etapas de build, teste e deploy | ☑ |
| README.md com instruções e prints | ☐ (inserir os prints) |
| Documentação técnica com evidências (PDF ou PPT) | ☐ (inserir os prints e os integrantes) |
| Deploy realizado nos ambientes staging e produção | ☐ (executar o pipeline) |
