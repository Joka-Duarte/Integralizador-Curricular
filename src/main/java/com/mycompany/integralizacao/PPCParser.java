
package com.mycompany.integralizacao;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.util.*;

public class PPCParser {

    public static Curso carregarCursoDeXML(String caminho) throws Exception {
        Curso curso = new Curso();
        try {
            File arquivo = new File(caminho);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(arquivo);
            doc.getDocumentElement().normalize();

            NodeList cursoNodeList = doc.getElementsByTagName("curso");
            if (cursoNodeList.getLength() > 0) {
                Element cursoElement = (Element) cursoNodeList.item(0);
                curso.setNome(cursoElement.getAttribute("nome"));
            } else {
                curso.setNome("Nome do Curso não encontrado");
            }

            NodeList listaDisciplinas = doc.getElementsByTagName("disciplina");

            for (int i = 0; i < listaDisciplinas.getLength(); i++) {
                Node node = listaDisciplinas.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    
                    String semestreStr = elem.getElementsByTagName("semestre").item(0).getTextContent();
                    int semestre;
                    if (semestreStr.equalsIgnoreCase("CCCG")) {
                        semestre = 0;
                    } else {
                        semestre = Integer.parseInt(semestreStr);
                    }
                    
                    int ordem = Integer.parseInt(elem.getElementsByTagName("ordem").item(0).getTextContent());
                    String nome = elem.getElementsByTagName("nome").item(0).getTextContent();
                    int carga = Integer.parseInt(elem.getElementsByTagName("cargaHoraria").item(0).getTextContent());
                    int creditos = Integer.parseInt(elem.getElementsByTagName("creditos").item(0).getTextContent());

                    String preReqStr = elem.getElementsByTagName("preRequisitos").item(0).getTextContent();
                    List<String> preReqs = preReqStr.isEmpty() ? Collections.emptyList() : Arrays.asList(preReqStr.split(" - "));

                    boolean isCCCG = false;
                    NodeList ccNodes = elem.getElementsByTagName("isCCCG");
                    if (ccNodes.getLength() > 0) {
                        isCCCG = Boolean.parseBoolean(ccNodes.item(0).getTextContent());
                    }

                    boolean isOutraAtividade = false;
                    NodeList oaNodes = elem.getElementsByTagName("isOutraAtividade");
                    if (oaNodes.getLength() > 0) {
                        isOutraAtividade = Boolean.parseBoolean(oaNodes.item(0).getTextContent());
                    }
                    
                    Disciplina d = new Disciplina(semestre, ordem, nome, carga, creditos, preReqs, isCCCG, isOutraAtividade);
                    curso.adicionarDisciplina(d);
                }
            }
        } catch (Exception e) {
            throw new Exception("Falha ao ler o arquivo PPC. Verifique se o formato do XML está correto.", e);
        }
        return curso;
    }
}