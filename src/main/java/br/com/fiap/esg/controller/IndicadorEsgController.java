package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.IndicadorCadastroDTO;
import br.com.fiap.esg.dto.IndicadorExibicaoDTO;
import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.service.IndicadorEsgService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/indicadores")
public class IndicadorEsgController {

    @Autowired
    private IndicadorEsgService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<IndicadorExibicaoDTO> listar(@RequestParam(required = false) CategoriaEsg categoria) {
        return service.listar(categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<IndicadorExibicaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IndicadorExibicaoDTO cadastrar(@RequestBody @Valid IndicadorCadastroDTO dados) {
        return service.cadastrar(dados);
    }

    @PutMapping("/{id}")
    public ResponseEntity<IndicadorExibicaoDTO> atualizar(@PathVariable Long id,
                                                          @RequestBody @Valid IndicadorCadastroDTO dados) {
        return ResponseEntity.ok(service.atualizar(id, dados));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        service.excluir(id);
    }
}
