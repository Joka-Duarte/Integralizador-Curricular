
package com.mycompany.integralizacao;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import org.xml.sax.SAXException;

public class PPCParser {

    public static Curso carregarCursoDeXML(String caminho) throws Exception {
        Curso curso = new Curso();
        try {
            File arquivo = new File(caminho);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(arquivo);
            doc.getDocumentElement().normalize();

            // --- MUDANÇA 1: Lendo o nome do curso da tag <curso> ---
            // Procura pela tag <curso> no documento
            NodeList cursoNodeList = doc.getElementsByTagName("curso");
            if (cursoNodeList.getLength() > 0) {
                // Pega o primeiro elemento <curso> encontrado
                Element cursoElement = (Element) cursoNodeList.item(0);
                // Pega o atributo "nome" deste elemento
                curso.setNome(cursoElement.getAttribute("nome"));
            } else {
                // Se não encontrar a tag <curso>, define um nome padrão ou lança um erro
                curso.setNome("Nome do Curso não encontrado");
            }

            // A busca por todas as disciplinas continua igual, pois getElementsByTagName
            // pega todas as tags "disciplina" não importa onde estejam no arquivo.
            NodeList listaDisciplinas = doc.getElementsByTagName("disciplina");

            for (int i = 0; i < listaDisciplinas.getLength(); i++) {
                Node node = listaDisciplinas.item(i);

                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;
                    
                    // --- MUDANÇA 2: Tratamento especial para o semestre ---
                    String semestreStr = elem.getElementsByTagName("semestre").item(0).getTextContent();
                    int semestre;
                    if (semestreStr.equalsIgnoreCase("CCCG")) {
                        // Atribui um valor numérico (ex: 0) para representar o semestre CCCG
                        semestre = 0;
                    } else {
                        // Se não for "CCCG", converte para número como antes
                        semestre = Integer.parseInt(semestreStr);
                    }
                    
                    int ordem = Integer.parseInt(elem.getElementsByTagName("ordem").item(0).getTextContent());
                    String nome = elem.getElementsByTagName("nome").item(0).getTextContent();
                    int carga = Integer.parseInt(elem.getElementsByTagName("cargaHoraria").item(0).getTextContent());
                    int creditos = Integer.parseInt(elem.getElementsByTagName("creditos").item(0).getTextContent());

                    String preReqStr = elem.getElementsByTagName("preRequisitos").item(0).getTextContent();
                    // Garante que a lista não seja nula mesmo se a tag estiver vazia
                    List<String> preReqs = preReqStr.isEmpty() ? Collections.emptyList() : Arrays.asList(preReqStr.split(" - "));

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
            // A lógica de lançar um erro claro para a interface continua a mesma
            throw new Exception("Falha ao ler o arquivo PPC. Verifique se o formato do XML está correto.", e);
        }
        return curso;
    }
}