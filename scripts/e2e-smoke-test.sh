#!/usr/bin/env bash
# =============================================================================
# Smoke test ponta a ponta do ecossistema FIAP Bank (Checkpoint 5)
#
# Pre-requisito: Core Bancario (8080), Plataforma de Credito (8081) e
# Mesa de Operacoes (8082) em execucao. Requer curl e jq.
#
#   ./scripts/e2e-smoke-test.sh
#
# Valida: API RESTful do Core, API RESTful da Plataforma, contrato legado
# usado pelo app Mobile (Controller Adaptador) e as telas da Mesa de Operacoes.
# =============================================================================
set -u

CORE=${CORE_URL:-http://localhost:8080}
PLAT=${PLATAFORMA_URL:-http://localhost:8081}
MESA=${MESA_URL:-http://localhost:8082}

PASS=0
FAIL=0
TMP=$(mktemp -d)
BODY=$TMP/body
HDRS=$TMP/headers

req() { # metodo url [json]
  local metodo=$1 url=$2 dados=${3:-}
  if [ -n "$dados" ]; then
    curl -s -o "$BODY" -D "$HDRS" -w '%{http_code}' -X "$metodo" -H 'Content-Type: application/json' -d "$dados" "$url"
  else
    curl -s -o "$BODY" -D "$HDRS" -w '%{http_code}' -X "$metodo" "$url"
  fi
}

ok()   { PASS=$((PASS + 1)); echo "PASS [$1] $2"; }
erro() { FAIL=$((FAIL + 1)); echo "FAIL [esperado $1, obtido $2] $3 :: $(head -c 250 "$BODY" | tr '\n' ' ')"; }

check() { # descricao status_esperado status_obtido [expressao_jq]
  local desc=$1 esperado=$2 obtido=$3 cond=${4:-}
  if [ "$obtido" != "$esperado" ]; then erro "$esperado" "$obtido" "$desc"; return; fi
  if [ -n "$cond" ] && ! jq -e "$cond" "$BODY" >/dev/null 2>&1; then erro "$esperado + ($cond)" "$obtido" "$desc"; return; fi
  ok "$obtido" "$desc"
}

header_presente() { grep -qi "^$1:" "$HDRS"; }
json() { jq -r "$1" "$BODY"; }

echo "================ CORE BANCARIO (RESTful /api/v1) ================"

check "GET /clientes lista os clientes pre-carregados" 200 "$(req GET "$CORE/api/v1/clientes")" 'length >= 2'
check "GET /clientes/cpf/{cpf} aceita CPF com mascara" 200 "$(req GET "$CORE/api/v1/clientes/cpf/123.456.789-00")" '.nome == "Ana Silva" and .numeroConta == "12345-6"'
check "GET /clientes/cpf/{cpf} inexistente -> 404" 404 "$(req GET "$CORE/api/v1/clientes/cpf/00000000000")"
check "GET /clientes/{id} com UUID invalido -> 400" 400 "$(req GET "$CORE/api/v1/clientes/nao-e-uuid")"
check "GET /clientes/{id} inexistente -> 404" 404 "$(req GET "$CORE/api/v1/clientes/00000000-0000-0000-0000-000000000000")"

NOVO_CLIENTE='{"cpf":"111.444.777-35","nome":"Joao Pereira","email":"joao.pereira@email.com","dataNascimento":"1995-03-20","rendaMensal":5200.00}'
check "POST /clientes cria cliente -> 201" 201 "$(req POST "$CORE/api/v1/clientes" "$NOVO_CLIENTE")" '.cpf == "11144477735"'
CLIENTE_ID=$(json '.id')
if header_presente "Location"; then ok "Location" "POST /clientes devolve cabecalho Location"; else erro "Location" "-" "POST /clientes devolve cabecalho Location"; fi
check "POST /clientes com CPF duplicado -> 409" 409 "$(req POST "$CORE/api/v1/clientes" "$NOVO_CLIENTE")"
check "POST /clientes invalido -> 400 com erros por campo" 400 \
  "$(req POST "$CORE/api/v1/clientes" '{"cpf":"123","nome":"Jo","email":"x@","dataNascimento":"2999-01-01","rendaMensal":-1}')" '.erros | length >= 5'
check "GET /clientes/{id} -> 200" 200 "$(req GET "$CORE/api/v1/clientes/$CLIENTE_ID")"
check "PUT /clientes/{id} substitui o cadastro -> 200" 200 \
  "$(req PUT "$CORE/api/v1/clientes/$CLIENTE_ID" '{"nome":"Joao Pereira Lima","email":"joao.lima@email.com","dataNascimento":"1995-03-20","rendaMensal":6100.00}')" '.nome == "Joao Pereira Lima"'
check "PUT /clientes/{id} tentando trocar o CPF -> 400" 400 \
  "$(req PUT "$CORE/api/v1/clientes/$CLIENTE_ID" '{"cpf":"98765432100","nome":"Joao","email":"joao@email.com","dataNascimento":"1995-03-20","rendaMensal":1}')"

check "POST /contas abre conta (agencia default 0001) -> 201" 201 \
  "$(req POST "$CORE/api/v1/contas" '{"cpfCliente":"11144477735","tipoConta":"CORRENTE","saldoInicial":100.00}')" '.agencia == "0001" and .statusConta == "ATIVA"'
NUMERO_CONTA=$(json '.numeroConta')
check "POST /contas segunda conta ativa para o mesmo CPF -> 409" 409 \
  "$(req POST "$CORE/api/v1/contas" '{"cpfCliente":"11144477735","tipoConta":"POUPANCA","saldoInicial":0}')"
check "POST /contas com tipoConta invalido -> 400" 400 \
  "$(req POST "$CORE/api/v1/contas" '{"cpfCliente":"11144477735","tipoConta":"INVESTIMENTO","saldoInicial":0}')"
check "POST /contas com agencia de 3 digitos -> 400" 400 \
  "$(req POST "$CORE/api/v1/contas" '{"cpfCliente":"11144477735","agencia":"123","tipoConta":"CORRENTE","saldoInicial":0}')"
check "POST /contas para CPF sem cadastro -> 404" 404 \
  "$(req POST "$CORE/api/v1/contas" '{"cpfCliente":"22233344455","tipoConta":"CORRENTE","saldoInicial":0}')"
check "GET /contas -> 200" 200 "$(req GET "$CORE/api/v1/contas")" 'length >= 3'
check "GET /contas/{numeroConta} -> 200" 200 "$(req GET "$CORE/api/v1/contas/$NUMERO_CONTA")"
check "GET /contas/cliente/{cpf} -> 200" 200 "$(req GET "$CORE/api/v1/contas/cliente/11144477735")" ".numeroConta == \"$NUMERO_CONTA\""
check "PUT /contas/{n}/saldo DEPOSITO (case-insensitive) -> 200" 200 \
  "$(req PUT "$CORE/api/v1/contas/$NUMERO_CONTA/saldo" '{"operacao":"deposito","valor":50.00}')" '.saldo == 150'
check "PUT /contas/{n}/saldo SAQUE maior que o saldo -> 409" 409 \
  "$(req PUT "$CORE/api/v1/contas/$NUMERO_CONTA/saldo" '{"operacao":"SAQUE","valor":1000.00}')"
check "PUT /contas/{n}/saldo operacao invalida -> 400" 400 \
  "$(req PUT "$CORE/api/v1/contas/$NUMERO_CONTA/saldo" '{"operacao":"PIX","valor":10}')"
check "PUT /contas/{n}/saldo valor zero -> 400" 400 \
  "$(req PUT "$CORE/api/v1/contas/$NUMERO_CONTA/saldo" '{"operacao":"DEPOSITO","valor":0}')"
check "DELETE /clientes/{id} com conta ativa -> 409" 409 "$(req DELETE "$CORE/api/v1/clientes/$CLIENTE_ID")"
check "DELETE /contas/{n} encerra a conta -> 204" 204 "$(req DELETE "$CORE/api/v1/contas/$NUMERO_CONTA")"
check "DELETE /contas/{n} de novo (idempotente) -> 204" 204 "$(req DELETE "$CORE/api/v1/contas/$NUMERO_CONTA")"
check "PUT /contas/{n}/saldo em conta encerrada -> 409" 409 \
  "$(req PUT "$CORE/api/v1/contas/$NUMERO_CONTA/saldo" '{"operacao":"DEPOSITO","valor":10}')"
check "DELETE /clientes/{id} -> 204" 204 "$(req DELETE "$CORE/api/v1/clientes/$CLIENTE_ID")"
check "GET /clientes/{id} removido -> 404" 404 "$(req GET "$CORE/api/v1/clientes/$CLIENTE_ID")"

echo "================ PLATAFORMA DE CREDITO (RESTful /api/v1) ================"

check "GET /solicitacoes: carga inicial com 2 APROVADO, 1 REPROVADO e 1 EM_ANALISE" 200 "$(req GET "$PLAT/api/v1/solicitacoes")" \
  '([.[] | select(.status=="APROVADO")] | length) == 2 and ([.[] | select(.status=="REPROVADO")] | length) == 1 and ([.[] | select(.status=="EM_ANALISE")] | length) == 1'
check "POST /solicitacoes aprovacao automatica -> 201" 201 \
  "$(req POST "$PLAT/api/v1/solicitacoes" '{"cpfCliente":"98765432100","nomeCliente":"Carlos Eduardo Oliveira","tipoCredito":"CONSIGNADO","valorSolicitado":12000,"quantidadeParcelas":12,"rendaMensal":12000}')" '.status == "APROVADO" and .scoreBureau > 0'
if header_presente "Location"; then ok "Location" "POST /solicitacoes devolve cabecalho Location"; else erro "Location" "-" "POST /solicitacoes devolve cabecalho Location"; fi
check "POST /solicitacoes cliente sem conta no Core vai para a Mesa (sem dado ficticio)" 201 \
  "$(req POST "$PLAT/api/v1/solicitacoes" '{"cpfCliente":"22233344455","nomeCliente":"Cliente Sem Conta","tipoCredito":"PESSOAL","valorSolicitado":1000,"quantidadeParcelas":10,"rendaMensal":5000}')" '.status == "EM_ANALISE"'
PENDENTE_ID=$(json '.id')
check "POST /solicitacoes com 0 parcelas -> 400 (antes: divisao por zero / 500)" 400 \
  "$(req POST "$PLAT/api/v1/solicitacoes" '{"cpfCliente":"12345678900","nomeCliente":"Ana Silva","tipoCredito":"PESSOAL","valorSolicitado":1000,"quantidadeParcelas":0,"rendaMensal":5000}')"
check "GET /solicitacoes?cpf={cpf} filtra por cliente" 200 "$(req GET "$PLAT/api/v1/solicitacoes?cpf=12345678900")" 'length >= 2 and all(.[]; .cpfCliente == "12345678900")'
check "GET /solicitacoes/{id} -> 200" 200 "$(req GET "$PLAT/api/v1/solicitacoes/$PENDENTE_ID")"
check "GET /solicitacoes/{id} inexistente -> 404 (antes: 500)" 404 "$(req GET "$PLAT/api/v1/solicitacoes/999999")"
check "GET /solicitacoes/{id} nao numerico -> 400" 400 "$(req GET "$PLAT/api/v1/solicitacoes/abc")"
check "PUT /solicitacoes/{id}/decisao APROVADO -> 200" 200 \
  "$(req PUT "$PLAT/api/v1/solicitacoes/$PENDENTE_ID/decisao" '{"decisao":"APROVADO","parecer":"Renda comprovada"}')" '.status == "APROVADO"'
check "PUT /solicitacoes/{id}/decisao em proposta ja decidida -> 409" 409 \
  "$(req PUT "$PLAT/api/v1/solicitacoes/$PENDENTE_ID/decisao" '{"decisao":"REPROVADO","parecer":"tarde demais"}')"
check "PUT /solicitacoes/{id}/decisao invalida -> 400" 400 \
  "$(req PUT "$PLAT/api/v1/solicitacoes/$PENDENTE_ID/decisao" '{"decisao":"TALVEZ"}')"
check "POST /solicitacoes nova proposta para cancelar" 201 \
  "$(req POST "$PLAT/api/v1/solicitacoes" '{"cpfCliente":"12345678900","nomeCliente":"Ana Silva","tipoCredito":"PESSOAL","valorSolicitado":90000,"quantidadeParcelas":10,"rendaMensal":7500}')" '.status == "EM_ANALISE"'
CANCELAR_ID=$(json '.id')
check "DELETE /solicitacoes/{id} cancela proposta pendente -> 204" 204 "$(req DELETE "$PLAT/api/v1/solicitacoes/$CANCELAR_ID")"
check "GET /solicitacoes/{id} cancelada" 200 "$(req GET "$PLAT/api/v1/solicitacoes/$CANCELAR_ID")" '.status == "CANCELADO"'
check "GET /clientes/{cpf}/conta consulta o Core via Feign" 200 "$(req GET "$PLAT/api/v1/clientes/12345678900/conta")" \
  '.numeroConta == "12345-6" and .agencia == "0001" and .statusConta == "ATIVA" and .saldo == 12500'
check "GET /clientes/{cpf}/conta sem conta no Core -> 404" 404 "$(req GET "$PLAT/api/v1/clientes/22233344455/conta")"

echo "================ CONTRATO LEGADO /api/credito (app Mobile Angular) ================"

check "POST /api/credito/solicitarCredito (payload do Mobile) -> 200" 200 \
  "$(req POST "$PLAT/api/credito/solicitarCredito" '{"cpfCliente":"12345678900","nomeCliente":"Ana Silva","tipoCredito":"EMPRÉSTIMO PESSOAL","valorSolicitado":5000,"quantidadeParcelas":12,"rendaMensal":7500}')" \
  '.status == "APROVADO" and .tipoCredito == "EMPRÉSTIMO PESSOAL" and (.id | type) == "number"'
if header_presente "Deprecation"; then ok "Deprecation" "Rotas legadas sinalizam o cabecalho Deprecation"; else erro "Deprecation" "-" "Rotas legadas sinalizam o cabecalho Deprecation"; fi
check "POST /api/credito/consultarStatusCredito -> 200" 200 \
  "$(req POST "$PLAT/api/credito/consultarStatusCredito" '{"cpfCliente":"12345678900"}')" 'type == "array" and length >= 3'
check "POST /api/credito/consultarClienteCore -> 200 (formato ClienteCoreInfo)" 200 \
  "$(req POST "$PLAT/api/credito/consultarClienteCore" '{"cpfCliente":"12345678900"}')" 'has("statusConta") and has("saldo") and has("agencia") and has("numeroConta")'
check "POST /api/credito/listarTodasSolicitacoes sem corpo -> 200" 200 "$(req POST "$PLAT/api/credito/listarTodasSolicitacoes")" 'type == "array"'
check "POST /api/credito/buscarSolicitacaoPorId com {\"id\":1} (README legado) -> 200" 200 \
  "$(req POST "$PLAT/api/credito/buscarSolicitacaoPorId" '{"id":1}')" '.id == 1'
check "POST /api/credito/solicitarCredito proposta para a Mesa" 200 \
  "$(req POST "$PLAT/api/credito/solicitarCredito" '{"cpfCliente":"98765432100","nomeCliente":"Carlos Eduardo Oliveira","tipoCredito":"CRÉDITO IMOBILIÁRIO","valorSolicitado":500000,"quantidadeParcelas":60,"rendaMensal":12000}')" '.status == "EM_ANALISE"'
LEGADO_ID=$(json '.id')
check "POST /api/credito/reprovarSolicitacao -> 200" 200 \
  "$(req POST "$PLAT/api/credito/reprovarSolicitacao" "{\"solicitacaoId\":$LEGADO_ID,\"motivoReprovacao\":\"Comprometimento de renda\"}")" '.status == "REPROVADO"'
CORS=$(curl -s -o /dev/null -D - -X OPTIONS "$PLAT/api/credito/solicitarCredito" \
  -H "Origin: http://localhost:4200" -H "Access-Control-Request-Method: POST" -H "Access-Control-Request-Headers: content-type")
if echo "$CORS" | grep -qi "access-control-allow-origin"; then ok "CORS" "Preflight CORS do Angular (localhost:4200) liberado"; else erro "CORS" "-" "Preflight CORS do Angular (localhost:4200) liberado"; fi

echo "================ MESA DE OPERACOES (Thymeleaf, consumindo a API RESTful) ================"

check "POST /api/v1/solicitacoes proposta para a Mesa decidir" 201 \
  "$(req POST "$PLAT/api/v1/solicitacoes" '{"cpfCliente":"12345678900","nomeCliente":"Ana Silva","tipoCredito":"CREDITO_AUTOMOTIVO","valorSolicitado":80000,"quantidadeParcelas":12,"rendaMensal":7500}')" '.status == "EM_ANALISE"'
MESA_ID=$(json '.id')
DASH=$(curl -s -w '\n%{http_code}' "$MESA/dashboard")
if [ "$(echo "$DASH" | tail -1)" == "200" ] && ! echo "$DASH" | grep -q "modo offline" && echo "$DASH" | grep -q "Ana Silva"; then
  ok 200 "Mesa: dashboard carrega as propostas da Plataforma"
else
  erro 200 "$(echo "$DASH" | tail -1)" "Mesa: dashboard carrega as propostas da Plataforma"
fi
ANALISE=$(curl -s -o /dev/null -w '%{http_code}' "$MESA/solicitacao/$MESA_ID")
if [ "$ANALISE" == "200" ]; then ok 200 "Mesa: tela de analise da proposta #$MESA_ID"; else erro 200 "$ANALISE" "Mesa: tela de analise da proposta #$MESA_ID"; fi
APROVAR=$(curl -s -o /dev/null -w '%{http_code} %{redirect_url}' -X POST "$MESA/aprovar" \
  --data-urlencode "solicitacaoId=$MESA_ID" --data-urlencode "parecerOperador=Aprovado no smoke test")
if [[ "$APROVAR" == 302*success* ]]; then ok 302 "Mesa: aprovar proposta pelo formulario"; else erro "302 success" "$APROVAR" "Mesa: aprovar proposta pelo formulario"; fi
check "Proposta aprovada pela Mesa refletida na Plataforma" 200 "$(req GET "$PLAT/api/v1/solicitacoes/$MESA_ID")" \
  '.status == "APROVADO" and (.parecerOperador | contains("Aprovado no smoke test"))'

echo "=================================================================="
echo "RESULTADO: $PASS passaram, $FAIL falharam"
rm -rf "$TMP"
[ "$FAIL" -eq 0 ]
