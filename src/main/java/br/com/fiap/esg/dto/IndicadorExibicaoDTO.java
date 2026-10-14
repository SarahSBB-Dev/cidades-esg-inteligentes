package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.model.IndicadorEsg;

import java.time.LocalDate;

/** Dados devolvidos pela API ao exibir um indicador ESG. */
public record IndicadorExibicaoDTO(
        Long id,
        String cidade,
        CategoriaEsg categoria,
        String nome,
        Double valor,
        String unidade,
        LocalDate dataMedicao
) {

    public IndicadorExibicaoDTO(IndicadorEsg indicador) {
        this(
                indicador.getId(),
                indicador.getCidade(),
                indicador.getCategoria(),
                indicador.getNome(),
                indicador.getValor(),
                indicador.getUnidade(),
                indicador.getDataMedicao()
        );
    }
}
