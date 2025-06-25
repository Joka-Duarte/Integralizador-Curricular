
package com.mycompany.integralizacao;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class PPCParser {
    public static Curso carregarCursoDeXML(String caminho) {
        Curso curso = new Curso();
        try {
            File arquivo = new File(caminho);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(arquivo);
            doc.getDocumentElement().normalize();

            curso.setNome(doc.getDocumentElement().getAttribute("nome"));
            NodeList lista = doc.getElementsByTagName("disciplina");

            for (int i = 0; i < lista.getLength(); i++) {
                Node node = lista.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    int semestre = Integer.parseInt(elem.getElementsByTagName("semestre").item(0).getTextContent());
                    int ordem = Integer.parseInt(elem.getElementsByTagName("ordem").item(0).getTextContent());
                    String nome = elem.getElementsByTagName("nome").item(0).getTextContent();
                    int carga = Integer.parseInt(elem.getElementsByTagName("cargaHoraria").item(0).getTextContent());
                    int creditos = Integer.parseInt(elem.getElementsByTagName("creditos").item(0).getTextContent());

                    String preReqStr = elem.getElementsByTagName("preRequisitos").item(0).getTextContent();
                    List<String> preReqs = preReqStr.isEmpty() ? new ArrayList<>() : Arrays.asList(preReqStr.split(" - "));

                    boolean isCCCG = false;
                    NodeList ccNodes = elem.getElementsByTagName("isCCCG");
                    if (ccNodes.getLength() > 0) {
                        isCCCG = Boolean.parseBoolean(ccNodes.item(0).getTextContent());
                    }

                    Disciplina d = new Disciplina(semestre, ordem, nome, carga, creditos, preReqs, isCCCG);
                    curso.adicionarDisciplina(d);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return curso;
    }
}