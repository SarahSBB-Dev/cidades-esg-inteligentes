package br.com.fiap.esg.repository;

import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.model.IndicadorEsg;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndicadorEsgRepository extends JpaRepository<IndicadorEsg, Long> {

    List<IndicadorEsg> findByCategoria(CategoriaEsg categoria);
}
