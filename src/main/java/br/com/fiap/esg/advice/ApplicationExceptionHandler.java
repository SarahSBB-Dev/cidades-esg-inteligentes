package br.com.fiap.esg.advice;

import br.com.fiap.esg.exception.IndicadorNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> manusearArgumentosInvalidos(MethodArgumentNotValidException erro) {
        Map<String, String> mapaDeErros = new HashMap<>();
        List<FieldError> campos = erro.getBindingResult().getFieldErrors();
        campos.forEach(campo -> mapaDeErros.put(campo.getField(), campo.getDefaultMessage()));
        return mapaDeErros;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(IndicadorNaoEncontradoException.class)
    public Map<String, String> manusearIndicadorNaoEncontrado(IndicadorNaoEncontradoException erro) {
        Map<String, String> mapaDeErro = new HashMap<>();
        mapaDeErro.put("erro", erro.getMessage());
        return mapaDeErro;
    }
}
