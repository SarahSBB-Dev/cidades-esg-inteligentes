package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.model.IndicadorEsg;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Testes unitários da conversão da entidade para o DTO de exibição. */
class IndicadorExibicaoDTOTest {

    @Test
    void copiaOsDadosDaEntidade() {
        IndicadorEsg indicador = new IndicadorEsg("Belo Horizonte", CategoriaEsg.SOCIAL,
                "Moradores atendidos por saneamento", 92.3, "%", LocalDate.of(2026, 8, 15));
        indicador.setId(10L);

        IndicadorExibicaoDTO dto = new IndicadorExibicaoDTO(indicador);

        assertEquals(10L, dto.id());
        assertEquals("Belo Horizonte", dto.cidade());
        assertEquals(CategoriaEsg.SOCIAL, dto.categoria());
        assertEquals("Moradores atendidos por saneamento", dto.nome());
        assertEquals(92.3, dto.valor());
        assertEquals("%", dto.unidade());
        assertEquals(LocalDate.of(2026, 8, 15), dto.dataMedicao());
    }

    @Test
    void categoriasEsgDisponiveis() {
        assertEquals(3, CategoriaEsg.values().length);
        assertEquals(CategoriaEsg.GOVERNANCA, CategoriaEsg.valueOf("GOVERNANCA"));
    }
}
