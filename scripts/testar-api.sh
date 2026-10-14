#!/usr/bin/env bash
# Testa as principais operações da API em um ambiente: cadastro, listagem, filtro e validação.
# Uso: bash scripts/testar-api.sh <url-da-aplicacao>
# Exemplo: bash scripts/testar-api.sh http://localhost:8080

URL="$1"
ERROS=0

cadastrar() {
  echo ""
  echo ">> POST $URL/api/indicadores"
  echo "   $1"
  STATUS=$(curl -s -o resposta.json -w "%{http_code}" -X POST "$URL/api/indicadores" \
    -H "Content-Type: application/json" -d "$1")
  echo "   HTTP $STATUS - $(cat resposta.json)"
  if [ "$STATUS" != "$2" ]; then
    echo "   ERRO: era esperado HTTP $2"
    ERROS=$((ERROS + 1))
  fi
}

echo "=== Cadastrando indicadores ESG (um de cada pilar) ==="
cadastrar '{"cidade":"São Paulo","categoria":"AMBIENTAL","nome":"Energia renovável na iluminação pública","valor":78.5,"unidade":"%","dataMedicao":"2026-10-01"}' 201
cadastrar '{"cidade":"Recife","categoria":"SOCIAL","nome":"Moradores atendidos por saneamento","valor":92.3,"unidade":"%","dataMedicao":"2026-09-15"}' 201
cadastrar '{"cidade":"Curitiba","categoria":"GOVERNANCA","nome":"Índice de transparência nos gastos públicos","valor":87.0,"unidade":"pontos","dataMedicao":"2026-09-30"}' 201

echo ""
echo "=== Validação: cadastro com dados inválidos deve retornar 400 ==="
cadastrar '{"cidade":"","categoria":"AMBIENTAL","nome":"Coleta seletiva","valor":-5,"unidade":"%","dataMedicao":"2026-10-01"}' 400

echo ""
echo "=== GET $URL/api/indicadores ==="
curl -s "$URL/api/indicadores"
echo ""

echo ""
echo "=== GET $URL/api/indicadores?categoria=AMBIENTAL ==="
curl -s "$URL/api/indicadores?categoria=AMBIENTAL"
echo ""

rm -f resposta.json

if [ "$ERROS" -gt 0 ]; then
  echo ""
  echo "Teste da API falhou ($ERROS erro(s))"
  exit 1
fi

echo ""
echo "Teste da API concluído com sucesso!"
