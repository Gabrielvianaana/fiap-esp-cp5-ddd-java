# FIAP Bank - Core Bancário 🏦

Microsserviço de **Core Bancário**: gestão de correntistas e contas, consulta de saldo e movimentações (depósito/saque). Construído do zero com **Domain-Driven Design** em **Maven multi-módulo** e API **RESTful** sob `/api/v1`.

---

## 🛠️ Tecnologias

- Java 21 · Spring Boot 4.1 · Maven multi-módulo
- Spring Data JPA + H2 (em memória)
- Jakarta Bean Validation · Lombok
- `maven-enforcer-plugin` para blindar as fronteiras entre camadas

---

## 🧱 Arquitetura

```
fiap-bank-core-bancario/                     (pom agregador, packaging pom)
├── fiap-bank-core-bancario-domain           Java puro: Cliente, ContaBancaria, VOs Cpf/Email/Saldo,
│                                            enums TipoConta/StatusConta, portas ClienteRepository/ContaBancariaRepository
├── fiap-bank-core-bancario-infrastructure   Entidades JPA (@Entity/@Table), Spring Data e Repository Adapters
├── fiap-bank-core-bancario-application      DTOs (Java Records + Bean Validation) e serviços de aplicação
└── fiap-bank-core-bancario-presentation     Controllers RESTful, GlobalExceptionHandler, @SpringBootApplication
```

`presentation → application → infrastructure → domain`, verificado em todo build pelo `maven-enforcer-plugin`:

- **domain**: nenhuma dependência de compile/runtime é aceita (Lombok é só de compilação, escopo `provided`).
- **infrastructure**: proibido depender de `application`, `presentation` ou da camada web.
- **application**: proibido depender de `presentation` ou da camada web.
- **presentation**: `domain` e `infrastructure` são redeclarados com escopo `runtime`. Eles ficam fora do classpath de compilação (qualquer `import` do domínio ou da infraestrutura nessa camada quebra o build), mas continuam disponíveis em runtime para o Spring.

Detalhes de DDD:

- Entidades puras com fábricas (`novo/abrir` e `reconstituir`) e métodos de negócio (`depositar`, `sacar`, `encerrar`, `atualizarCadastro`). Não há setters públicos.
- Value Objects imutáveis (`record`) que se auto-validam: `Cpf`, `Email`, `Saldo` (que nunca fica negativo).
- Movimentação de saldo atômica: lock pessimista (`SELECT ... FOR UPDATE`) mais `@Version` na entidade JPA.

---

## 🚀 Como executar

```bash
cd fiap-bank-core-bancario
mvn clean install
mvn -pl fiap-bank-core-bancario-presentation spring-boot:run
```

Ou pelo JAR: `java -jar fiap-bank-core-bancario-presentation/target/fiap-bank-core-bancario-presentation-1.0.0-SNAPSHOT.jar`

Disponível em **`http://localhost:8080`**.

H2 Console: `http://localhost:8080/h2-console` · JDBC URL `jdbc:h2:mem:corebancariodb` · user `sa` · senha em branco.

---

## 📌 Dados iniciais

| Cliente | CPF | Agência | Conta | Saldo |
| :--- | :--- | :---: | :---: | ---: |
| Ana Silva | `12345678900` | 0001 | 12345-6 | R$ 12.500,00 |
| Carlos Eduardo Oliveira | `98765432100` | 0001 | 78901-2 | R$ 45.800,50 |

---

## 🌐 Endpoints RESTful

| Verbo | URI | Sucesso | Erros | Descrição |
| :--- | :--- | :---: | :---: | :--- |
| `GET` | `/api/v1/clientes` | 200 | — | Lista os clientes |
| `GET` | `/api/v1/clientes/{id}` | 200 | 400, 404 | Cliente pelo UUID |
| `GET` | `/api/v1/clientes/cpf/{cpf}` | 200 | 400, 404 | Cliente pelo CPF (com ou sem máscara) |
| `POST` | `/api/v1/clientes` | 201 + `Location` | 400, 409 | Cadastra cliente (CPF único) |
| `PUT` | `/api/v1/clientes/{id}` | 200 | 400, 404 | Substitui os dados cadastrais |
| `DELETE` | `/api/v1/clientes/{id}` | 204 | 400, 404, 409 | Remove o cliente (409 se tiver conta ativa) |
| `GET` | `/api/v1/contas` | 200 | 500 | Lista as contas |
| `GET` | `/api/v1/contas/{numeroConta}` | 200 | 400, 404 | Conta pelo número (ex.: `12345-6`) |
| `GET` | `/api/v1/contas/cliente/{cpf}` | 200 | 400, 404 | Conta vigente do titular |
| `POST` | `/api/v1/contas` | 201 + `Location` | 400, 404, 409 | Abre conta para cliente existente |
| `PUT` | `/api/v1/contas/{numeroConta}/saldo` | 200 | 400, 404, 409 | Depósito/saque atômico (409 = saldo insuficiente ou conta inativa) |
| `DELETE` | `/api/v1/contas/{numeroConta}` | 204 | 400, 404 | Encerramento lógico, idempotente |

> As listagens retornam `200 OK` com `[]` quando vazias: uma coleção vazia ainda é um recurso existente.

### Exemplos

```json
POST /api/v1/clientes
{
  "cpf": "111.444.777-35",
  "nome": "João Pereira",
  "email": "joao.pereira@email.com",
  "dataNascimento": "1995-03-20",
  "rendaMensal": 5200.00
}
```

```json
POST /api/v1/contas
{ "cpfCliente": "11144477735", "tipoConta": "CORRENTE", "saldoInicial": 100.00 }
```

```json
PUT /api/v1/contas/12345-6/saldo
{ "operacao": "saque", "valor": 250.00 }
```

Erros seguem o padrão **Problem Details (RFC 9457)**:

```json
{
  "type": "about:blank",
  "title": "Dados de entrada invalidos",
  "status": 400,
  "detail": "Um ou mais campos nao passaram na validacao.",
  "instance": "/api/v1/clientes",
  "erros": [ { "campo": "cpf", "mensagem": "O CPF deve conter 11 digitos numericos ou o formato XXX.XXX.XXX-XX." } ]
}
```
