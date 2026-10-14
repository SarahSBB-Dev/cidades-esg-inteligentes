package br.com.fiap.esg.service;

import br.com.fiap.esg.dto.IndicadorCadastroDTO;
import br.com.fiap.esg.dto.IndicadorExibicaoDTO;
import br.com.fiap.esg.exception.IndicadorNaoEncontradoException;
import br.com.fiap.esg.model.CategoriaEsg;
import br.com.fiap.esg.model.IndicadorEsg;
import br.com.fiap.esg.repository.IndicadorEsgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndicadorEsgService {

    @Autowired
    private IndicadorEsgRepository indicadorRepository;

    public List<IndicadorExibicaoDTO> listar(CategoriaEsg categoria) {
        List<IndicadorEsg> indicadores = (categoria == null)
                ? indicadorRepository.findAll()
                : indicadorRepository.findByCategoria(categoria);

        return indicadores.stream()
                .map(IndicadorExibicaoDTO::new)
                .toList();
    }

    public IndicadorExibicaoDTO buscarPorId(Long id) {
        return new IndicadorExibicaoDTO(buscarEntidade(id));
    }

    public IndicadorExibicaoDTO cadastrar(IndicadorCadastroDTO dados) {
        IndicadorEsg indicador = indicadorRepository.save(dados.toEntity());
        return new IndicadorExibicaoDTO(indicador);
    }

    public IndicadorExibicaoDTO atualizar(Long id, IndicadorCadastroDTO dados) {
        IndicadorEsg indicador = buscarEntidade(id);
        indicador.setCidade(dados.cidade());
        indicador.setCategoria(dados.categoria());
        indicador.setNome(dados.nome());
        indicador.setValor(dados.valor());
        indicador.setUnidade(dados.unidade());
        indicador.setDataMedicao(dados.dataMedicao());
        return new IndicadorExibicaoDTO(indicadorRepository.save(indicador));
    }

    public void excluir(Long id) {
        indicadorRepository.delete(buscarEntidade(id));
    }

    private IndicadorEsg buscarEntidade(Long id) {
        return indicadorRepository.findById(id)
                .orElseThrow(() -> new IndicadorNaoEncontradoException(id));
    }
}
