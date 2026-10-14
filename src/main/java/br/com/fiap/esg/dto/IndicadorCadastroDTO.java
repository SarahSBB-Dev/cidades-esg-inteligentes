package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.model.IndicadorEsg;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Dados recebidos no cadastro/atualização de um indicador ESG. */
public record IndicadorCadastroDTO(

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String cidade,

        @NotNull(message = "A categoria é obrigatória (AMBIENTAL, SOCIAL ou GOVERNANCA)")
        CategoriaEsg categoria,

        @NotBlank(message = "O nome do indicador é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @NotNull(message = "O valor é obrigatório")
        @PositiveOrZero(message = "O valor não pode ser negativo")
        Double valor,

        @NotBlank(message = "A unidade é obrigatória")
        @Size(max = 30, message = "A unidade deve ter no máximo 30 caracteres")
        String unidade,

        @NotNull(message = "A data da medição é obrigatória")
        LocalDate dataMedicao
) {

    public IndicadorEsg toEntity() {
        return new IndicadorEsg(cidade, categoria, nome, valor, unidade, dataMedicao);
    }
}
