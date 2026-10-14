# Projeto - Cidades ESG Inteligentes

API REST em **Java 21 + Spring Boot** para cadastrar e consultar **indicadores ESG** (Ambiental, Social e Governança) de cidades, como consumo de energia, coleta seletiva, acesso a saneamento ou transparência nos gastos públicos.

O foco do projeto é aplicar as práticas de **DevOps** vistas na fase *"Navegando pelo mundo DevOps"*: pipeline de CI/CD com **GitHub Actions**, containerização com **Docker**, orquestração com **Docker Compose** e deploy automatizado em dois ambientes na nuvem **Microsoft Azure** (**staging** e **produção**).

**Integrantes (Grupo 112):**

| Nome | RM |
|------|----|
| Sarah Schulze Bittar Bonafé | RM563780 |

**Repositório:** https://github.com/SarahSBB-Dev/cidades-esg-inteligentes

## Estrutura do projeto

```
cidades-esg-inteligentes/
├── Dockerfile                         # imagem da aplicação (build com Maven + runtime com JRE 21)
├── docker-compose.yml                 # orquestração local: API + MySQL
├── .env.example                       # modelo das variáveis de ambiente do compose
├── pom.xml                            # dependências e build (Maven)
├── scripts/
│   └── verificar-ambiente.sh          # verifica se o ambiente publicado está no ar
├── src/
│   ├── main/java/br/com/fiap/esg/
│   │   ├── controller/                # IndicadorEsgController e StatusController
│   │   ├── service/                   # regras de negócio
│   │   ├── repository/                # Spring Data JPA
│   │   ├── model/                     # entidade IndicadorEsg e enum CategoriaEsg
│   │   ├── dto/                       # records de cadastro e exibição
│   │   ├── exception/                 # exceção de indicador não encontrado
│   │   └── advice/                    # tratamento global de erros
│   ├── main/resources/
│   │   ├── application.properties     # configurações comuns
│   │   ├── application-dev.properties # profile dev (local, MySQL do compose)
│   │   ├── application-stg.properties # profile stg (staging no Azure)
│   │   └── application-prd.properties # profile prd (produção no Azure)
│   └── test/java/br/com/fiap/esg/     # testes automatizados (JUnit 5)
├── prints/                            # evidências de funcionamento
├── docs/                              # documentação técnica (PDF)
└── .github/workflows/
    └── ci-cd.yml                      # pipeline de CI/CD (GitHub Actions)
```

## Endpoints da API

| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/status` | Mostra se a aplicação está no ar e em qual ambiente (`dev`, `stg` ou `prd`) |
| GET | `/api/indicadores` | Lista os indicadores (filtro opcional `?categoria=AMBIENTAL`, `SOCIAL` ou `GOVERNANCA`) |
| GET | `/api/indicadores/{id}` | Busca um indicador pelo id |
| POST | `/api/indicadores` | Cadastra um indicador |
| PUT | `/api/indicadores/{id}` | Atualiza um indicador |
| DELETE | `/api/indicadores/{id}` | Exclui um indicador |

Exemplo de corpo para `POST` e `PUT`:

```json
{
  "cidade": "São Paulo",
  "categoria": "AMBIENTAL",
  "nome": "Energia renovável na iluminação pública",
  "valor": 78.5,
  "unidade": "%",
  "dataMedicao": "2026-10-01"
}
```

## Como executar localmente com Docker

Pré-requisito: **Docker Desktop** instalado e em execução.

1. Clone o repositório e entre na pasta do projeto:
   ```bash
   git clone https://github.com/SarahSBB-Dev/cidades-esg-inteligentes.git
   cd cidades-esg-inteligentes
   ```
2. Crie o arquivo `.env` a partir do modelo (no Windows use `copy .env.example .env`):
   ```bash
   cp .env.example .env
   ```
3. Construa a imagem e suba a API e o banco MySQL:
   ```bash
   docker compose up --build
   ```
4. Teste a aplicação (navegador, Insomnia ou curl):
   - Status: http://localhost:8080/api/status → `"ambiente":"dev"`
   - Indicadores: http://localhost:8080/api/indicadores
5. Para parar os containers: `docker compose down` (use `docker compose down -v` para apagar também o volume do banco).

Para rodar só os testes automatizados (precisa do JDK 21 e do Maven): `mvn test`.

## Pipeline CI/CD

**Ferramenta:** GitHub Actions, arquivo `.github/workflows/ci-cd.yml`.

**Fluxo de branches:** o desenvolvimento acontece em branches `feature/*`, que são integradas na `develop` por pull request. A `develop` é publicada em **staging**. Depois de validada, a `develop` é mesclada na `main`, que é publicada em **produção**.

| Etapa (job) | Quando roda | O que faz |
|-------------|-------------|-----------|
| 1. `build-test` - Build e testes | push e pull request em `develop` e `main` | Configura o Java 21, executa os **testes automatizados** (`mvn test`) e gera o `app.jar` (`mvn package`) |
| 2. `docker` - Build e push da imagem | push em `develop` e `main` | Constrói a imagem a partir do `Dockerfile` e envia ao **Docker Hub** com as tags `<sha do commit>` e `latest` |
| 3. `deploy-staging` - Deploy em staging | após a etapa 2 | Publica a imagem no Aplicativo Web de **staging** no Azure e roda `scripts/verificar-ambiente.sh` |
| 4. `deploy-producao` - Deploy em produção | após a etapa 3, **somente na `main`** | Publica **a mesma imagem** no Aplicativo Web de **produção** e verifica se está no ar |

**Funcionamento:**

- Cada etapa depende da anterior (`needs`). Se os testes falharem, nenhuma imagem é gerada. Se o deploy em staging falhar, a produção não é atualizada.
- O deploy usa a action `azure/webapps-deploy` com o **perfil de publicação** de cada Aplicativo Web, guardado nos *secrets* do GitHub.
- Os jobs de deploy usam os **environments** `staging` e `production` do GitHub, que registram cada implantação e mostram o link do ambiente.
- A diferença entre os ambientes está só nas **variáveis de ambiente** configuradas em cada Aplicativo Web do Azure: `PROFILE=stg` ou `PROFILE=prd`.
- Depois de cada deploy, o script `verificar-ambiente.sh` acessa `/api/status` até a aplicação responder com o ambiente correto. Isso confirma que a nova versão subiu.

**Secrets configurados no repositório** (Settings → Secrets and variables → Actions):

| Secret | Uso |
|--------|-----|
| `DOCKERHUB_USERNAME` | usuário do Docker Hub |
| `DOCKERHUB_TOKEN` | token de acesso do Docker Hub |
| `AZURE_PUBLISH_PROFILE_STAGING` | perfil de publicação do Aplicativo Web de staging |
| `AZURE_PUBLISH_PROFILE_PRODUCAO` | perfil de publicação do Aplicativo Web de produção |

**Ambientes no Azure** (Serviços de Aplicativos, plano Básico B1, Linux, contêiner Docker):

| Ambiente | Aplicativo Web | Variáveis de ambiente |
|----------|----------------|-----------------------|
| Staging | `cidades-esg-stg-rm563780` | `PROFILE=stg`, `WEBSITES_PORT=8080` |
| Produção | `cidades-esg-prd-rm563780` | `PROFILE=prd`, `WEBSITES_PORT=8080` |

## Containerização

**Dockerfile:**

```dockerfile
# Etapa de build: usa uma imagem com o Maven e Java 21 para compilar o projeto
FROM maven:3.9.8-eclipse-temurin-21 AS build
RUN mkdir /opt/app
COPY . /opt/app
WORKDIR /opt/app
RUN mvn clean package -DskipTests

# Etapa de runtime: usa uma imagem leve com apenas o JRE para rodar o app
FROM eclipse-temurin:21-jre-alpine
RUN mkdir /opt/app
COPY --from=build /opt/app/target/app.jar /opt/app/app.jar
WORKDIR /opt/app
ENV PROFILE=prd
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=$PROFILE -jar app.jar"]
```

**Estratégias adotadas:**

- **Build em duas etapas (multi-stage):** a primeira etapa usa a imagem do Maven para compilar e gerar o `app.jar`. A imagem final usa só o JRE 21 em Alpine, então fica menor e não leva o código-fonte nem o Maven.
- **Profile por variável de ambiente:** a variável `PROFILE` define o profile do Spring (`dev`, `stg` ou `prd`). A mesma imagem roda em qualquer ambiente, mudando apenas a configuração.
- **`.dockerignore`:** evita copiar para a imagem arquivos desnecessários, como `target/`, `.git/`, `.env`, prints e documentação.
- **Testes fora da imagem:** os testes rodam no job de CI. Por isso o build da imagem usa `-DskipTests`.

**Orquestração com Docker Compose (`docker-compose.yml`):**

- **Serviços:** `api` (aplicação Spring Boot, construída pelo Dockerfile) e `db` (MySQL 8.0).
- **Volume:** `db_data`, que guarda os dados do MySQL mesmo se o container for recriado.
- **Rede:** `esg-network` (bridge). A API acessa o banco pelo nome do serviço: `jdbc:mysql://db:3306/...`.
- **Variáveis de ambiente:** vêm do arquivo `.env` (modelo em `.env.example`): `PROFILE`, `DB_NAME` e `DB_ROOT_PASSWORD`. A API recebe `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PWD`.
- **Healthcheck:** o MySQL é verificado com `mysqladmin ping`. A API só sobe depois que o banco está saudável (`depends_on` com `condition: service_healthy`).

**Banco de dados por ambiente:**

| Profile | Onde roda | Banco |
|---------|-----------|-------|
| `dev` | Docker Compose local | MySQL 8.0 (container `db`) |
| `stg` | Azure - staging | H2 em memória (ou o banco informado na variável `DATABASE_URL`) |
| `prd` | Azure - produção | H2 em memória (ou o banco informado na variável `DATABASE_URL`) |

Nos ambientes do Azure usamos um banco em memória para não gerar custo extra na conta de estudante. Com isso, os dados são apagados quando o Aplicativo Web reinicia. Para manter os dados, basta criar um banco gerenciado e configurar `DATABASE_URL`, `DATABASE_USER` e `DATABASE_PWD` no Azure, sem alterar o código.

## Prints do funcionamento

As evidências ficam na pasta [`prints/`](prints/) e também estão na documentação em PDF (pasta [`docs/`](docs/)).

_(prints adicionados após a execução do pipeline)_

## Tecnologias utilizadas

- **Linguagem e framework:** Java 21, Spring Boot 3.3 (Spring Web, Spring Data JPA, Bean Validation)
- **Banco de dados:** MySQL 8.0 (local, via Docker Compose) e H2 (ambientes no Azure)
- **Testes:** JUnit 5 (`spring-boot-starter-test`)
- **Build:** Maven
- **Containerização e orquestração:** Docker e Docker Compose
- **Registro de imagens:** Docker Hub
- **CI/CD:** GitHub Actions (com environments `staging` e `production`)
- **Nuvem:** Microsoft Azure - Serviços de Aplicativos (Aplicativo Web para contêineres)
- **Versionamento:** Git e GitHub

## Checklist de entrega

| Item | OK |
|------|----|
| Projeto compactado em .ZIP com estrutura organizada | ☐ |
| Dockerfile funcional | ☐ |
| docker-compose.yml ou arquivos Kubernetes | ☐ |
| Pipeline com etapas de build, teste e deploy | ☐ |
| README.md com instruções e prints | ☐ |
| Documentação técnica com evidências (PDF ou PPT) | ☐ |
| Deploy realizado nos ambientes staging e produção | ☐ |
