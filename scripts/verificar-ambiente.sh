#!/usr/bin/env bash
# Verifica se a aplicação está no ar e no ambiente (profile) correto.
# Usado pelo pipeline depois de cada deploy, mas também pode ser rodado manualmente.
# Uso: bash scripts/verificar-ambiente.sh <url-da-aplicacao> <profile-esperado>
# Exemplo: bash scripts/verificar-ambiente.sh http://localhost:8080 stg

URL="$1"
PROFILE_ESPERADO="$2"

echo "Verificando $URL/api/status (esperado: ambiente $PROFILE_ESPERADO)"

# A aplicação leva alguns segundos para subir depois que o banco fica pronto
for tentativa in $(seq 1 40); do
  RESPOSTA=$(curl -s "$URL/api/status")
  echo "Tentativa $tentativa: $RESPOSTA"

  if echo "$RESPOSTA" | grep -q "\"ambiente\":\"$PROFILE_ESPERADO\""; then
    echo ""
    echo "Aplicação no ar no ambiente $PROFILE_ESPERADO!"
    exit 0
  fi

  sleep 5
done

echo "A aplicação não respondeu como esperado em $URL"
exit 1
