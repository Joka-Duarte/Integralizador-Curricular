
package com.mycompany.integralizacao;

import java.util.*;

public class DisciplinaUtils {
    public static List<Disciplina> verificarDisciplinasDisponiveis(List<Disciplina> todas, Set<String> cursadas) {
        List<Disciplina> disponiveis = new ArrayList<>();

        for (Disciplina d : todas) {
            boolean podeCursar = true;
            for (String pre : d.getPreRequisitos()) {
                if (!pre.equalsIgnoreCase("Não Possui") && !cursadas.contains(pre.trim())) {
                    podeCursar = false;
                    break;
                }
            }
            if (!cursadas.contains(d.getNome()) && podeCursar) {
                disponiveis.add(d);
            }
        }

        return disponiveis;
    }
}