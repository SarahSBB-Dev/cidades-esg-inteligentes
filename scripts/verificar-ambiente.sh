#!/usr/bin/env bash
# Verifica se a aplicação publicada está no ar e no ambiente correto.
# Usado pelo pipeline depois de cada deploy, mas também pode ser rodado manualmente.
# Uso: bash scripts/verificar-ambiente.sh <url-da-aplicacao> <profile-esperado>
# Exemplo: bash scripts/verificar-ambiente.sh https://cidades-esg-staging.azurewebsites.net stg

URL="$1"
PROFILE_ESPERADO="$2"

echo "Verificando $URL/api/status (esperado: ambiente $PROFILE_ESPERADO)"

# O Azure leva alguns minutos para baixar a nova imagem e reiniciar o container
for tentativa in $(seq 1 40); do
  RESPOSTA=$(curl -s "$URL/api/status")
  echo "Tentativa $tentativa: $RESPOSTA"

  if echo "$RESPOSTA" | grep -q "\"ambiente\":\"$PROFILE_ESPERADO\""; then
    echo "Aplicação no ar no ambiente $PROFILE_ESPERADO!"
    echo ""
    echo "Indicadores ESG cadastrados ($URL/api/indicadores):"
    curl -s "$URL/api/indicadores"
    echo ""
    exit 0
  fi

  sleep 15
done

echo "A aplicação não respondeu como esperado em $URL"
exit 1
