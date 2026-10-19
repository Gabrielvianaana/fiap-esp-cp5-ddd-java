# FIAP Bank - Esteira de Crédito 🏦💳

**Checkpoint 5 · Domain Driven Design - Java · Turma 2ESPG · Prof. Eduardo dos Santos Ramos**

Monorepo com o ecossistema da **Esteira de Crédito** do FIAP Bank: Core Bancário, Plataforma de Crédito, Mesa de Operações e Internet Banking Mobile.

## 👥 Integrantes

| Nome | RM |
| :--- | :--- |
| Gabriel Viana de Souza | RM 564382 |
| Rafael Falaguasta Ferraz | RM 561714 |

---

## 📁 Estrutura do Workspace

| Projeto | Tecnologia | Porta | Situação no CP5 |
| :--- | :--- | :---: | :--- |
| [`fiap-bank-core-bancario`](./fiap-bank-core-bancario/README.md) | Java 21, Spring Boot 4.1, JPA, DDD multi-módulo | `8080` | **Construído do zero** (Fase 1) |
| [`fiap-bank-plataforma-credito`](./fiap-bank-plataforma-credito/README.md) | Java 21, Spring Boot 4.1, OpenFeign, DDD multi-módulo | `8081` | **Refatorado** para DDD + RESTful + Adapter legado (Fase 2) |
| [`fiap-bank-mesa-operacoes-credito`](./fiap-bank-mesa-operacoes-credito/README.md) | Java 21, Spring Boot 4.1, Thymeleaf, OpenFeign | `8082` | Ajustado: Spring Cloud corrigido e client migrado para a API RESTful |
| [`fiap-bank-internet-banking-mobile`](./fiap-bank-internet-banking-mobile/README.md) | Angular 17+ | `4200` | **Sem alteração**: continua usando o contrato legado via Controller Adaptador |

Documentos da entrega:

- [`docs/avaliacao-teorica.md`](./docs/avaliacao-teorica.md): respostas da Avaliação Teórica (Seção 5) e diagnóstico das falhas do estado inicial.

---

## 🧱 Arquitetura alvo (Core Bancário e Plataforma de Crédito)

```
<projeto>/                         pom agregador (packaging pom)
├── <projeto>-domain               Java puro: entidades, value objects, domain services, portas
├── <projeto>-infrastructure       JPA (@Entity), Spring Data, Repository Adapters, clients externos
├── <projeto>-application          DTOs (records + Bean Validation) e casos de uso
└── <projeto>-presentation         Controllers RESTful, @RestControllerAdvice, @SpringBootApplication
```

Dependências permitidas: `presentation → application → infrastructure → domain`. O `maven-enforcer-plugin` falha o build em qualquer violação:

- o `domain` não aceita nenhuma dependência de compile/runtime;
- a `presentation` só recebe `domain`/`infrastructure` em escopo `runtime`, então um `import` direto não compila.

O workflow [`.github/workflows/build.yml`](./.github/workflows/build.yml) roda `mvn verify` (compilação, testes e enforcer) em todo push.

---

## 🚦 Ordem recomendada de execução

### 1. Core Bancário (`8080`)
```bash
cd fiap-bank-core-bancario
mvn clean install
mvn -pl fiap-bank-core-bancario-presentation spring-boot:run
```
API: `http://localhost:8080/api/v1` · H2: `http://localhost:8080/h2-console`

### 2. Plataforma de Crédito (`8081`)
```bash
cd fiap-bank-plataforma-credito
mvn clean install
mvn -pl fiap-bank-plataforma-credito-presentation spring-boot:run
```
API RESTful: `http://localhost:8081/api/v1` · Contrato legado: `http://localhost:8081/api/credito` · H2: `http://localhost:8081/h2-console`

### 3. Mesa de Operações (`8082`)
```bash
cd fiap-bank-mesa-operacoes-credito
mvn clean compile
mvn spring-boot:run
```
Painel do operador: `http://localhost:8082`

### 4. Internet Banking Mobile (`4200`)
```bash
cd fiap-bank-internet-banking-mobile
npm install
npm run start
```
App: `http://localhost:4200`

---

## ✅ Testes

- **Unitários do domínio** (JUnit puro, sem Spring): `mvn verify` em cada projeto.
- **Smoke test ponta a ponta** ([`scripts/e2e-smoke-test.sh`](./scripts/e2e-smoke-test.sh)): com Core, Plataforma e Mesa no ar, roda `./scripts/e2e-smoke-test.sh` (requer `curl` e `jq`). São 61 verificações:
  - todos os endpoints RESTful e os status de erro (400/404/409);
  - as 7 rotas legadas que o app Mobile usa;
  - CORS para o Angular;
  - o fluxo da Mesa de Operações: dashboard, análise e aprovação.
- **CI**: os workflows [`build.yml`](./.github/workflows/build.yml) e [`e2e.yml`](./.github/workflows/e2e.yml) rodam a cada push. O `e2e.yml` sobe os três serviços Java e executa o smoke test.

---

## 🧪 Dados pré-carregados

- **Ana Silva** · CPF `12345678900` · Renda R$ 7.500,00 · Conta 0001 / 12345-6 · Saldo R$ 12.500,00
- **Carlos Eduardo Oliveira** · CPF `98765432100` · Renda R$ 12.000,00 · Conta 0001 / 78901-2 · Saldo R$ 45.800,50
- **Mariana Souza** · CPF `55544433399` · restrição ativa no SCR (sem conta no Core)

A Plataforma sobe com 4 propostas: duas aprovadas automaticamente, uma reprovada pelo SCR e uma **em análise**, para a Mesa decidir.
