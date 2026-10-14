package br.com.fiap.esg.dto;

/** Resposta do endpoint de status, usada para conferir em qual ambiente a aplicação está rodando. */
public record StatusDTO(
        String aplicacao,
        String ambiente,
        String status,
        String dataHora
) {
}
