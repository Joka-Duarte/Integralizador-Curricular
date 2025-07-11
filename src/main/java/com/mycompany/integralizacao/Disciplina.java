
package com.mycompany.integralizacao;

import java.util.List;

public class Disciplina {
    private int semestre;
    private int ordem;
    private String nome;
    private int cargaHoraria;
    private int creditos;
    private List<String> preRequisitos;
    private boolean isCCCG;
    private boolean isOutraAtividade; // <-- Novo atributo

    // Construtor principal atualizado
    public Disciplina(int semestre, int ordem, String nome, int cargaHoraria, int creditos, List<String> preRequisitos, boolean isCCCG, boolean isOutraAtividade) {
        this.semestre = semestre;
        this.ordem = ordem;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.creditos = creditos;
        this.preRequisitos = preRequisitos;
        this.isCCCG = isCCCG;
        this.isOutraAtividade = isOutraAtividade;
    }
    
    // Construtor antigo para manter a compatibilidade
    public Disciplina(int semestre, int ordem, String nome, int cargaHoraria, int creditos, List<String> preRequisitos, boolean isCCCG) {
        this(semestre, ordem, nome, cargaHoraria, creditos, preRequisitos, isCCCG, false);
    }
    
    // Getters
    public int getSemestre() { return semestre; }
    public int getOrdem() { return ordem; }
    public String getNome() { return nome; }
    public int getCargaHoraria() { return cargaHoraria; }
    public int getCreditos() { return creditos; }
    public List<String> getPreRequisitos() { return preRequisitos; }
    public boolean isCCCG() { return isCCCG; }
    public boolean isOutraAtividade() { return isOutraAtividade; } // <-- Getter para o novo campo

    @Override
    public String toString() {
        return nome + " (" + ordem + ")";
    }
}