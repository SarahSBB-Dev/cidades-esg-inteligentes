package br.com.fiap.esg.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

/** Medição de um indicador ESG de uma cidade (ex.: consumo de energia, coleta seletiva). */
@Entity
@Table(name = "tbl_indicadores_esg")
public class IndicadorEsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaEsg categoria;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(nullable = false)
    private Double valor;

    @Column(nullable = false, length = 30)
    private String unidade;

    @Column(name = "data_medicao", nullable = false)
    private LocalDate dataMedicao;

    public IndicadorEsg() {
    }

    public IndicadorEsg(String cidade, CategoriaEsg categoria, String nome,
                        Double valor, String unidade, LocalDate dataMedicao) {
        this.cidade = cidade;
        this.categoria = categoria;
        this.nome = nome;
        this.valor = valor;
        this.unidade = unidade;
        this.dataMedicao = dataMedicao;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }

    public CategoriaEsg getCategoria() { return categoria; }
    public void setCategoria(CategoriaEsg categoria) { this.categoria = categoria; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getUnidade() { return unidade; }
    public void setUnidade(String unidade) { this.unidade = unidade; }

    public LocalDate getDataMedicao() { return dataMedicao; }
    public void setDataMedicao(LocalDate dataMedicao) { this.dataMedicao = dataMedicao; }
}
