package br.com.fiap.esg.controller;

import br.com.fiap.esg.dto.StatusDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/** Mostra se a aplicação está no ar e em qual ambiente (dev, stg ou prd) ela está rodando. */
@RestController
@RequestMapping("/api/status")
public class StatusController {

    @Value("${spring.profiles.active:local}")
    private String ambiente;

    @GetMapping
    public StatusDTO status() {
        return new StatusDTO("Cidades ESG Inteligentes", ambiente, "UP", LocalDateTime.now().toString());
    }
}
