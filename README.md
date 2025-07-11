# Integralizador Curricular

Aplicação de desktop em Java/Swing para auxiliar estudantes no planejamento da sua grade curricular. O programa visualiza o PPC de um curso, valida pré-requisitos em tempo real e acompanha o progresso do aluno.

## Principais Funcionalidades

    Carrega a grade curricular de um arquivo XML customizado.

    Exibe a grade em uma tabela interativa com feedback visual (disciplinas liberadas, bloqueadas e cursadas).

    Valida pré-requisitos automaticamente ao marcar uma disciplina.

    Calcula o progresso em carga horária e gera um gráfico de conclusão.

    Permite salvar e carregar o progresso do aluno em arquivos de texto.

    Exporta um relatório .txt com as disciplinas disponíveis para cursar.

## Como Executar

### Opção 1: Arquivo Executável (JAR)

    Faça o build do projeto com Maven (no NetBeans, use a opção "Limpar e Construir").

    Navegue até a pasta target/.

    Encontre o arquivo .jar principal: IntegralizacaoCurricular-1.0-SNAPSHOT.jar
    ![IntegralizacaoCurricular-1.0-SNAPSHOT.jar](./target/IntegralizacaoCurricular-1.0-SNAPSHOT.jar)

    Execute o arquivo com um duplo-clique ou pelo terminal:
    *Bash*

    java -jar nome-do-arquivo.jar

### Opção 2: Pelo Código-Fonte

    Clone o repositório.

    Abra o projeto em uma IDE com suporte a Maven (NetBeans, IntelliJ, etc.).

    Execute a classe principal com.mycompany.integralizacao.Main.

## Formato do Arquivo de Dados
 O programa depende de um arquivo ppc.xml localizado na pasta resources
 ![Resources](./resources/ppc.xml)

## Tecnologias

 *Java | Swing (UI) | Maven | JFreeChart*

## Autores

 *João Oliveira*
 *Jansen Avila*
 *Renan Antunes*

## Documentação

 A documenttação completa se encontra na pasta Documentation.
 ![Documentation](./Documentation/Documentação%20de%20Projeto%20de%20Software.pdf)
