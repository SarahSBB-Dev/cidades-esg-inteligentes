package br.com.fiap.esg.dto;

import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.model.IndicadorEsg;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Testes unitários das regras de validação do cadastro de indicadores ESG. */
class IndicadorCadastroDTOTest {

    private static Validator validator;

    @BeforeAll
    static void configurarValidador() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    private IndicadorCadastroDTO indicadorValido() {
        return new IndicadorCadastroDTO("São Paulo", CategoriaEsg.AMBIENTAL,
                "Consumo de energia", 1250.5, "MWh", LocalDate.of(2026, 9, 1));
    }

    private boolean temErroNoCampo(Set<ConstraintViolation<IndicadorCadastroDTO>> erros, String campo) {
        return erros.stream().anyMatch(erro -> erro.getPropertyPath().toString().equals(campo));
    }

    @Test
    void indicadorValidoNaoTemErros() {
        Set<ConstraintViolation<IndicadorCadastroDTO>> erros = validator.validate(indicadorValido());

        assertTrue(erros.isEmpty());
    }

    @Test
    void cidadeEmBrancoDeveSerRejeitada() {
        IndicadorCadastroDTO dto = new IndicadorCadastroDTO("", CategoriaEsg.SOCIAL,
                "Vagas em creches", 120.0, "vagas", LocalDate.of(2026, 9, 1));

        Set<ConstraintViolation<IndicadorCadastroDTO>> erros = validator.validate(dto);

        assertTrue(temErroNoCampo(erros, "cidade"));
    }

    @Test
    void valorNegativoDeveSerRejeitado() {
        IndicadorCadastroDTO dto = new IndicadorCadastroDTO("Curitiba", CategoriaEsg.AMBIENTAL,
                "Coleta seletiva", -5.0, "%", LocalDate.of(2026, 9, 1));

        Set<ConstraintViolation<IndicadorCadastroDTO>> erros = validator.validate(dto);

        assertTrue(temErroNoCampo(erros, "valor"));
    }

    @Test
    void categoriaObrigatoria() {
        IndicadorCadastroDTO dto = new IndicadorCadastroDTO("Recife", null,
                "Transparência nos gastos", 87.0, "pontos", LocalDate.of(2026, 9, 1));

        Set<ConstraintViolation<IndicadorCadastroDTO>> erros = validator.validate(dto);

        assertTrue(temErroNoCampo(erros, "categoria"));
    }

    @Test
    void dataDaMedicaoObrigatoria() {
        IndicadorCadastroDTO dto = new IndicadorCadastroDTO("Recife", CategoriaEsg.GOVERNANCA,
                "Transparência nos gastos", 87.0, "pontos", null);

        Set<ConstraintViolation<IndicadorCadastroDTO>> erros = validator.validate(dto);

        assertFalse(erros.isEmpty());
        assertTrue(temErroNoCampo(erros, "dataMedicao"));
    }

    @Test
    void converteParaEntidadeComTodosOsCampos() {
        IndicadorEsg entidade = indicadorValido().toEntity();

        assertNull(entidade.getId());
        assertEquals("São Paulo", entidade.getCidade());
        assertEquals(CategoriaEsg.AMBIENTAL, entidade.getCategoria());
        assertEquals("Consumo de energia", entidade.getNome());
        assertEquals(1250.5, entidade.getValor());
        assertEquals("MWh", entidade.getUnidade());
        assertEquals(LocalDate.of(2026, 9, 1), entidade.getDataMedicao());
    }
}
