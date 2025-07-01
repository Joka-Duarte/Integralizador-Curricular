
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

    public Disciplina(int semestre, int ordem, String nome, int cargaHoraria, int creditos, List<String> preRequisitos) {
        this(semestre, ordem, nome, cargaHoraria, creditos, preRequisitos, false);
        this.isCCCG = false;
    }

    public Disciplina(int semestre, int ordem, String nome, int cargaHoraria, int creditos, List<String> preRequisitos, boolean isCCCG) {
        this.isCCCG = false;
        this.semestre = semestre;
        this.ordem = ordem;
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.creditos = creditos;
        this.preRequisitos = preRequisitos;
        this.isCCCG = isCCCG;
    }

    public int getSemestre() { return semestre; }
    public int getOrdem() { return ordem; }
    public String getNome() { return nome; }
    public int getCargaHoraria() { return cargaHoraria; }
    public int getCreditos() { return creditos; }
    public List<String> getPreRequisitos() { return preRequisitos; }
    public boolean isCCCG() { return isCCCG; }

    @Override
    public String toString() {
        return nome + " (" + ordem + ")";
    }
}