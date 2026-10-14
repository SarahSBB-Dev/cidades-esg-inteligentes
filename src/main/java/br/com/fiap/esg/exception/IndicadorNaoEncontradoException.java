package br.com.fiap.esg.exception;

public class IndicadorNaoEncontradoException extends RuntimeException {

    public IndicadorNaoEncontradoException(Long id) {
        super("Indicador ESG não encontrado: " + id);
    }
}
