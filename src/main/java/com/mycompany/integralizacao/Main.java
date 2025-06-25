
package com.mycompany.integralizacao;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        Curso curso = PPCParser.carregarCursoDeXML("src/main/resources/ppc.xml");
        SwingUtilities.invokeLater(() -> new MainFrame(curso));
    }
}
