
package com.mycompany.integralizacao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.DefaultTableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class MainFrame extends JFrame {

    // Constantes para os índices das colunas
    private static final int COLUNA_SEMESTRE = 0;
    private static final int COLUNA_ORDEM = 1;
    private static final int COLUNA_NOME_DISCIPLINA = 2;
    private static final int COLUNA_CARGA_HORARIA = 3;
    private static final int COLUNA_CREDITOS = 4;
    private static final int COLUNA_PRE_REQUISITOS = 5;
    private static final int COLUNA_CURSADA = 6;
    
    // Constantes para os separadores
    private static final String SEPARADOR_OBRIGATORIAS = "--- Disciplinas Obrigatórias ---";
    private static final String SEPARADOR_CCCG = "--- Disciplinas CCCG ---";
    private static final String SEPARADOR_OUTRAS = "--- Outras Atividades ---";

    private JTable tabela;
    private Curso cursoAtual; 
    private Set<String> disciplinasCursadas = new HashSet<>();
    
    // --- MUDANÇA 1: Variável de controle para o carregamento ---
    private boolean isLoadingProgress = false;
    
    // Referências para os botões que precisam ser ativados/desativados
    private JButton carregarProgressoBtn;
    private JButton salvarProgressoBtn;
    private JButton progressoBtn;
    private JButton graficoBtn;
    private JButton disponiveisBtn;
    private JButton salvarRelatorioBtn;

    public MainFrame() {
        super("Integralização Curricular");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);

        // ... (código do construtor sem alterações)
        JPanel painelInicial = new JPanel(new BorderLayout());
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton carregarPPC = new JButton("Carregar PPC do Curso");
        carregarProgressoBtn = new JButton("Carregar Progresso");
        carregarProgressoBtn.addActionListener(e -> carregarProgresso());

        carregarPPC.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Selecione o arquivo PPC (XML)");
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File arquivo = fileChooser.getSelectedFile();
                try {
                    Curso novoCurso = PPCParser.carregarCursoDeXML(arquivo.getAbsolutePath());
                    if (novoCurso != null && !novoCurso.getDisciplinas().isEmpty()) {
                        carregarCurso(novoCurso);
                    } else {
                        JOptionPane.showMessageDialog(this, "O arquivo PPC parece estar vazio ou não contém disciplinas.", "Aviso", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage(), "Erro de Leitura", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        painelSuperior.add(carregarPPC);
        painelSuperior.add(carregarProgressoBtn);
        painelInicial.add(painelSuperior, BorderLayout.NORTH);
        
        JLabel labelBemVindo = new JLabel("Bem-vindo! Carregue o arquivo PPC de um curso para começar.", SwingConstants.CENTER);
        labelBemVindo.setFont(new Font("Arial", Font.PLAIN, 18));
        painelInicial.add(labelBemVindo, BorderLayout.CENTER);

        setContentPane(painelInicial);
        setBotoesAcaoEnabled(false);
        setVisible(true);
    }
    
    private void atualizarEstadoDisciplinasCursadas() {
        // ... (código sem alterações)
        disciplinasCursadas.clear();
        if (tabela != null) {
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (isLinhaSeparadora(i)) continue;
                if (Boolean.TRUE.equals(modelo.getValueAt(i, COLUNA_CURSADA))) {
                    disciplinasCursadas.add((String) modelo.getValueAt(i, COLUNA_NOME_DISCIPLINA));
                }
            }
        }
    }

    private void carregarCurso(Curso curso) {
        this.cursoAtual = curso;
        this.disciplinasCursadas.clear();
        setTitle("Integralização Curricular - " + curso.getNome());
        
        String[] colunas = {"Semestre", "Ordem", "Disciplina", "Carga Horária", "Créditos", "Pré-Requisitos", "Cursada?"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return (columnIndex == COLUNA_CURSADA) ? Boolean.class : String.class;
            }
            @Override
            public boolean isCellEditable(int row, int column) {
                // --- MUDANÇA 2: A validação é ignorada durante o carregamento ---
                if (isLoadingProgress) return true;
                
                if (isLinhaSeparadora(row) || column != COLUNA_CURSADA) return false;
                
                String nomeDisciplina = (String) getValueAt(row, COLUNA_NOME_DISCIPLINA);
                Disciplina d = cursoAtual.getDisciplinas().stream()
                        .filter(disc -> disc.getNome().equals(nomeDisciplina))
                        .findFirst().orElse(null);

                if (d == null || d.isOutraAtividade()) return true;
                if (d.getPreRequisitos().isEmpty() || d.getPreRequisitos().get(0).equalsIgnoreCase("Não Possui")) return true;
                
                return d.getPreRequisitos().stream().allMatch(disciplinasCursadas::contains);
            }
        };
        
        // ... (lógica de ordenação e preenchimento da tabela sem alterações)
        List<Disciplina> disciplinasOrdenadas = curso.getDisciplinas();
        disciplinasOrdenadas.sort(Comparator
                .comparingInt((Disciplina d) -> d.isOutraAtividade() ? 2 : (d.isCCCG() ? 1 : 0))
                .thenComparingInt(Disciplina::getSemestre)
                .thenComparingInt(Disciplina::getOrdem));

        modelo.addRow(new Object[]{SEPARADOR_OBRIGATORIAS, null, null, null, null, null, null});

        boolean separadorCCCGAdicionado = false;
        boolean separadorOutrasAdicionado = false;
        for (Disciplina d : disciplinasOrdenadas) {
            if (d.isCCCG() && !separadorCCCGAdicionado) {
                modelo.addRow(new Object[]{SEPARADOR_CCCG, null, null, null, null, null, null});
                separadorCCCGAdicionado = true;
            }
            if (d.isOutraAtividade() && !separadorOutrasAdicionado) {
                modelo.addRow(new Object[]{SEPARADOR_OUTRAS, null, null, null, null, null, null});
                separadorOutrasAdicionado = true;
            }
            
            String semestreDisplay = d.isOutraAtividade() ? "N/A" : (d.isCCCG() ? "CCCG" : String.valueOf(d.getSemestre()));
            modelo.addRow(new Object[]{
                    semestreDisplay, String.valueOf(d.getOrdem()), d.getNome(),
                    String.valueOf(d.getCargaHoraria()), String.valueOf(d.getCreditos()),
                    String.join(", ", d.getPreRequisitos()), false
            });
        }

        tabela = new JTable(modelo);
        tabela.setRowHeight(25);
        tabela.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        tabela.setDefaultRenderer(Object.class, new DisciplinaCellRenderer());
        
        modelo.addTableModelListener(e -> {
            if (e.getColumn() == COLUNA_CURSADA && !isLoadingProgress) { // Só reage a cliques do usuário
                atualizarEstadoDisciplinasCursadas();
                tabela.repaint();
            }
        });

        // ... (código dos botões e painéis sem alterações)
        JPanel botoesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        progressoBtn = new JButton("Ver Progresso");
        progressoBtn.addActionListener(e -> mostrarProgresso());
        graficoBtn = new JButton("Ver Gráfico");
        graficoBtn.addActionListener(e -> mostrarGrafico());
        disponiveisBtn = new JButton("Ver Disciplinas Disponíveis");
        disponiveisBtn.addActionListener(e -> mostrarDisponiveis());
        salvarRelatorioBtn = new JButton("Salvar Relatório");
        salvarRelatorioBtn.addActionListener(e -> salvarRelatorio());
        salvarProgressoBtn = new JButton("Salvar Progresso");
        salvarProgressoBtn.addActionListener(e -> salvarProgresso());

        botoesPanel.add(progressoBtn);
        botoesPanel.add(disponiveisBtn);
        botoesPanel.add(graficoBtn);
        botoesPanel.add(salvarRelatorioBtn);
        botoesPanel.add(salvarProgressoBtn);
        
        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.add(new JScrollPane(tabela), BorderLayout.CENTER);
        painelPrincipal.add(botoesPanel, BorderLayout.SOUTH);
        
        setContentPane(painelPrincipal);
        setBotoesAcaoEnabled(true);
        revalidate();
        repaint();
    }
    
    private void setBotoesAcaoEnabled(boolean enabled) { /* ...código sem alterações... */ }
    private void salvarProgresso() { /* ...código sem alterações... */ }
    
    private void carregarProgresso() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Carregar Progresso Salvo");
        int userSelection = fileChooser.showOpenDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoParaCarregar = fileChooser.getSelectedFile();
            Set<String> progressoCarregado = new HashSet<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(arquivoParaCarregar))) {
                String linha;
                while ((linha = reader.readLine()) != null) {
                    if (!linha.trim().isEmpty()) {
                        progressoCarregado.add(linha.trim());
                    }
                }
                aplicarProgressoNaTabela(progressoCarregado); // <-- Chamada ao método corrigido
                JOptionPane.showMessageDialog(this, "Progresso carregado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Erro ao carregar o progresso: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- MUDANÇA 3: Método atualizado com o "interruptor" ---
    private void aplicarProgressoNaTabela(Set<String> progressoCarregado) {
        this.isLoadingProgress = true; // LIGA o interruptor (desativa validações)
        try {
            DefaultTableModel modelo = (DefaultTableModel) tabela.getModel();
            // Primeiro, desmarca tudo para garantir um estado limpo
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (!isLinhaSeparadora(i)) {
                    modelo.setValueAt(false, i, COLUNA_CURSADA);
                }
            }
            // Depois, marca apenas as que estão no arquivo de progresso
            for (int i = 0; i < modelo.getRowCount(); i++) {
                if (isLinhaSeparadora(i)) continue;
                
                String nomeDisciplinaNaTabela = (String) modelo.getValueAt(i, COLUNA_NOME_DISCIPLINA);
                if (progressoCarregado.contains(nomeDisciplinaNaTabela)) {
                    modelo.setValueAt(true, i, COLUNA_CURSADA);
                }
            }
        } finally {
            this.isLoadingProgress = false; // DESLIGA o interruptor em qualquer circunstância
        }
        
        // Após todas as mudanças, atualiza o estado e a interface de uma só vez
        atualizarEstadoDisciplinasCursadas();
        tabela.repaint();
    }

    private boolean isLinhaSeparadora(int row) {
        if (tabela == null || row < 0 || row >= tabela.getRowCount()) return false;
        Object valor = tabela.getValueAt(row, 0);
        return valor != null && (valor.toString().equals(SEPARADOR_OBRIGATORIAS) || valor.toString().equals(SEPARADOR_CCCG) || valor.toString().equals(SEPARADOR_OUTRAS));
    }
    
    private void mostrarProgresso() {
        int cargaTotal = cursoAtual.getDisciplinas().stream()
                .filter(d -> !d.isCCCG() && !d.isOutraAtividade())
                .mapToInt(Disciplina::getCargaHoraria).sum();
        int cargaCursada = cursoAtual.getDisciplinas().stream()
                .filter(d -> !d.isCCCG() && !d.isOutraAtividade() && disciplinasCursadas.contains(d.getNome()))
                .mapToInt(Disciplina::getCargaHoraria).sum();
        double percentual = (cargaTotal == 0) ? 0 : (cargaCursada * 100.0) / cargaTotal;
        String msg = String.format(
            "Carga Horária das Disciplinas Obrigatórias: %d h\n" +
            "Carga Horária Concluída (Obrigatórias): %d h\n" +
            "Percentual de Conclusão (Obrigatórias): %.2f%%",
            cargaTotal, cargaCursada, percentual);
        JOptionPane.showMessageDialog(this, msg, "Progresso do Aluno", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarGrafico() {
        long totalDisciplinasObrigatorias = cursoAtual.getDisciplinas().stream()
                .filter(d -> !d.isCCCG() && !d.isOutraAtividade()).count();
        long cursadasObrigatorias = disciplinasCursadas.stream()
                .map(nome -> cursoAtual.getDisciplinas().stream().filter(d -> d.getNome().equals(nome)).findFirst().orElse(null))
                .filter(d -> d != null && !d.isCCCG() && !d.isOutraAtividade()).count();

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Cursadas (Obrigatórias)", cursadasObrigatorias);
        dataset.setValue("Não Cursadas (Obrigatórias)", totalDisciplinasObrigatorias - cursadasObrigatorias);
        JFreeChart chart = ChartFactory.createPieChart("Distribuição das Disciplinas Obrigatórias", dataset, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        JDialog dialogo = new JDialog(this, "Gráfico de Progresso", true);
        dialogo.setContentPane(chartPanel);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }
    
    private void mostrarDisponiveis() {
        List<Disciplina> disponiveis = DisciplinaUtils.verificarDisciplinasDisponiveis(cursoAtual.getDisciplinas(), disciplinasCursadas);
        if (disponiveis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Parabéns, não há mais disciplinas disponíveis ou você concluiu o curso!");
            return;
        }
        StringBuilder msg = new StringBuilder("Disciplinas disponíveis para cursar:\n\n");
        disponiveis.forEach(d -> msg.append("- ").append(d.getNome()).append("\n"));
        JOptionPane.showMessageDialog(this, msg.toString());
    }
    
    private void salvarRelatorio() {
        String nomeAluno = JOptionPane.showInputDialog(this, "Digite o nome do(a) aluno(a):", "Salvar Relatório", JOptionPane.PLAIN_MESSAGE);
        if (nomeAluno == null || nomeAluno.trim().isEmpty()) {
            return;
        }
        List<Disciplina> disponiveis = DisciplinaUtils.verificarDisciplinasDisponiveis(cursoAtual.getDisciplinas(), disciplinasCursadas);
        StringBuilder conteudoArquivo = new StringBuilder();
        conteudoArquivo.append("Aluno(a): ").append(nomeAluno.trim()).append("\n");
        conteudoArquivo.append("Curso: ").append(cursoAtual.getNome()).append("\n\n");
        conteudoArquivo.append("Disciplinas disponíveis para cursar:\n");
        conteudoArquivo.append("-------------------------------------\n");
        if (disponiveis.isEmpty()) {
            conteudoArquivo.append("Nenhuma disciplina disponível no momento ou curso concluído.\n");
        } else {
            for (Disciplina d : disponiveis) {
                conteudoArquivo.append("- ").append(d.getNome()).append("\n");
            }
        }
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Relatório de Disciplinas");
        fileChooser.setSelectedFile(new File(nomeAluno.trim().replace(" ", "_") + "_disciplinas.txt"));
        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File arquivoParaSalvar = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(arquivoParaSalvar)) {
                writer.write(conteudoArquivo.toString());
                JOptionPane.showMessageDialog(this, "Relatório salvo com sucesso em:\n" + arquivoParaSalvar.getAbsolutePath(), "Sucesso", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Erro ao salvar o arquivo: " + ex.getMessage(), "Erro de Salvamento", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class DisciplinaCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            if (isLinhaSeparadora(row)) {
                String textoSeparador = table.getValueAt(row, 0).toString();
                JLabel labelSeparador = new JLabel(textoSeparador, SwingConstants.CENTER);
                labelSeparador.setFont(new Font("Arial", Font.BOLD, 14));
                labelSeparador.setOpaque(true);
                labelSeparador.setBackground(Color.LIGHT_GRAY);
                labelSeparador.setForeground(Color.BLACK);
                return labelSeparador;
            }
            
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            String nomeDisciplina = (String) table.getValueAt(row, COLUNA_NOME_DISCIPLINA);
            Disciplina d = cursoAtual.getDisciplinas().stream()
                    .filter(disc -> disc.getNome().equals(nomeDisciplina))
                    .findFirst().orElse(null);

            if (d != null) {
                if (disciplinasCursadas.contains(d.getNome())) {
                    c.setBackground(new Color(210, 210, 210));
                    c.setForeground(Color.DARK_GRAY);
                } else {
                    boolean liberada = d.isOutraAtividade() || d.getPreRequisitos().stream()
                                        .allMatch(pre -> pre.isEmpty() || pre.equalsIgnoreCase("Não Possui") || disciplinasCursadas.contains(pre.trim()));
                    c.setBackground(liberada ? new Color(220, 255, 220) : new Color(255, 220, 220));
                    c.setForeground(Color.BLACK);
                }
            }
            
            if (isSelected) {
                c.setBackground(c.getBackground().darker());
            }
            return c;
        }
    }
}