# FIAP Bank - Plataforma de Crédito 💳

Microsserviço de **Esteira e Motor de Decisão de Crédito** do FIAP Bank. Recebe propostas, consulta SCR (Banco Central), Bureau de Crédito, CRM e Core Bancário, aplica o motor de regras, notifica o cliente e encaminha casos discricionários para a Mesa de Operações.

Refatorado de um projeto monolítico "tudo POST" para **DDD em Maven multi-módulo**, com **API RESTful `/api/v1`** e um **Controller Adaptador** que mantém o contrato legado `/api/credito/*` funcionando para os front-ends em produção.

---

## 🛠️ Tecnologias

- Java 21 · Spring Boot 4.1 · Maven multi-módulo
- Spring Data JPA + H2 (em memória)
- Spring Cloud OpenFeign **2025.1.x (Oakwood)**
- Jakarta Bean Validation · Lombok · Actuator
- `maven-enforcer-plugin` para blindar as fronteiras entre camadas

---

## 🧱 Arquitetura

```
fiap-bank-plataforma-credito/                    (pom agregador, packaging pom)
├── fiap-bank-plataforma-credito-domain          Java puro: SolicitacaoCredito, MotorRegrasCredito,
│                                                VOs (Cpf, AnaliseRisco...), portas (repository/gateway)
├── fiap-bank-plataforma-credito-infrastructure  Entidade JPA, Spring Data, adapters de repositório,
│                                                clients Core Bancário (Feign), SCR, Bureau, CRM, Notificações
├── fiap-bank-plataforma-credito-application     DTOs (records + Bean Validation) e casos de uso
└── fiap-bank-plataforma-credito-presentation    Controllers RESTful, Controller Adaptador legado,
                                                 GlobalExceptionHandler, @SpringBootApplication
```

Hierarquia de dependências: `presentation → application → infrastructure → domain`.

| Módulo | Enxerga | Garantia automatizada (enforcer) |
| :--- | :--- | :--- |
| `domain` | nada | nenhuma dependência de compile/runtime (sem Spring, JPA, Feign) |
| `infrastructure` | `domain` | não pode depender de `application`/`presentation` |
| `application` | `infrastructure`, `domain` | não pode depender de `presentation` |
| `presentation` | `application` | `domain`/`infrastructure` só em escopo `runtime`: fora do classpath de compilação |

---

## 🚀 Como executar

```bash
cd fiap-bank-plataforma-credito
mvn clean install
mvn -pl fiap-bank-plataforma-credito-presentation spring-boot:run
```

Disponível em **`http://localhost:8081`**. Suba o Core Bancário (8080) antes, para que a análise use os dados reais da conta.

| Propriedade | Variável | Padrão |
| :--- | :--- | :--- |
| `server.port` | `SERVER_PORT` | `8081` |
| `core.bancario.url` | `CORE_BANCARIO_URL` | `http://localhost:8080/api/v1` |

H2 Console: `http://localhost:8081/h2-console` · JDBC URL `jdbc:h2:mem:plataformacreditodb` · user `sa`.

---

## 🌐 API RESTful (`/api/v1`)

| Verbo | URI | Sucesso | Erros | Descrição |
| :--- | :--- | :---: | :---: | :--- |
| `POST` | `/api/v1/solicitacoes` | `201` + `Location` | 400 | Cria uma proposta e executa o motor de regras |
| `GET` | `/api/v1/solicitacoes` | `200` | — | Lista todas as propostas (mais recentes primeiro) |
| `GET` | `/api/v1/solicitacoes?cpf={cpf}` | `200` | 400 | Filtra as propostas de um cliente |
| `GET` | `/api/v1/solicitacoes/{id}` | `200` | 400, 404 | Detalha uma proposta |
| `PUT` | `/api/v1/solicitacoes/{id}/decisao` | `200` | 400, 404, 409 | Decisão da Mesa (`APROVADO`/`REPROVADO`) |
| `DELETE` | `/api/v1/solicitacoes/{id}` | `204` | 404, 409 | Cancelamento lógico de proposta pendente (idempotente) |
| `GET` | `/api/v1/clientes/{cpf}/conta` | `200` | 400, 404, 503 | Conta do cliente no Core Bancário |

Exemplo de decisão:

```json
PUT /api/v1/solicitacoes/4/decisao
{ "decisao": "APROVADO", "parecer": "Renda comprovada por holerite." }
```

Erros seguem o padrão **Problem Details (RFC 9457)**, com a lista `erros` campo a campo nas falhas de validação.

---

## ♻️ Controller Adaptador (contrato legado, *deprecated*)

Mantido para o app Mobile (Angular) e qualquer consumidor antigo. Mesmas rotas, verbos, payloads e status do contrato histórico, delegando para os mesmos casos de uso da API nova. Toda resposta traz `Deprecation: true` e `Link: <rota-nova>; rel="successor-version"`.

| Rota legada (POST) | Equivalente RESTful |
| :--- | :--- |
| `/api/credito/solicitarCredito` | `POST /api/v1/solicitacoes` |
| `/api/credito/consultarStatusCredito` | `GET /api/v1/solicitacoes?cpf={cpf}` |
| `/api/credito/listarTodasSolicitacoes` | `GET /api/v1/solicitacoes` |
| `/api/credito/buscarSolicitacaoPorId` | `GET /api/v1/solicitacoes/{id}` |
| `/api/credito/aprovarSolicitacao` | `PUT /api/v1/solicitacoes/{id}/decisao` |
| `/api/credito/reprovarSolicitacao` | `PUT /api/v1/solicitacoes/{id}/decisao` |
| `/api/credito/consultarClienteCore` | `GET /api/v1/clientes/{cpf}/conta` |

Os payloads legados aceitam `solicitacaoId` e também `id` (nome que o README antigo documentava).

---

## ⚙️ Motor de regras

1. Restrição no SCR → **REPROVADO**
2. Score do Bureau < 500 → **REPROVADO**
3. Pendência interna no CRM → **EM_ANALISE** (Mesa)
4. Parcela estimada > 30% da renda → **EM_ANALISE** (Mesa)
5. Core Bancário indisponível ou cliente sem conta ativa → **EM_ANALISE** (Mesa)
6. Caso contrário → **APROVADO**

## 🧪 Propostas pré-carregadas

| Cliente | Proposta | Resultado esperado (com o Core no ar) |
| :--- | :--- | :--- |
| Ana Silva `12345678900` | R$ 15.000 em 24x | APROVADO |
| Carlos Eduardo Oliveira `98765432100` | R$ 350.000 em 180x | APROVADO |
| Mariana Souza `55544433399` | R$ 45.000 em 48x | REPROVADO (SCR) |
| Ana Silva `12345678900` | R$ 60.000 em 12x | EM_ANALISE (vai para a Mesa) |
