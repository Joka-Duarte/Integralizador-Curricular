package com.mycompany.integralizacao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.table.DefaultTableCellRenderer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class MainFrame extends JFrame {
    private JTable tabela;
    private JButton salvar;

    private class DisciplinaCellRenderer extends DefaultTableCellRenderer {
        private List<Disciplina> disciplinas;
        private Set<String> cursadas;

        public DisciplinaCellRenderer(List<Disciplina> disciplinas, Set<String> cursadas) {
            this.disciplinas = disciplinas;
            this.cursadas = cursadas;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);

            String nomeDisciplina = (String) table.getValueAt(row, 2); // Nome agora na coluna 2
            Disciplina d = disciplinas.stream()
                    .filter(disc -> disc.getNome().equals(nomeDisciplina))
                    .findFirst()
                    .orElse(null);

            if (d != null && !d.getPreRequisitos().isEmpty()) {
                boolean liberada = d.getPreRequisitos().stream().allMatch(cursadas::contains);
                if (liberada) {
                    c.setBackground(new Color(200, 255, 200)); // Verde claro
                } else {
                    c.setBackground(new Color(255, 200, 200)); // Vermelho claro
                }
            } else {
                c.setBackground(new Color(200, 255, 200)); // Sem pré-requisitos = verde
            }

            if (isSelected) c.setBackground(c.getBackground().darker());

            return c;
        }
    }

    private void atualizarCoresDinamicamente(Curso curso, DefaultTableModel modelo) {
        Set<String> cursadas = new HashSet<>();

        for (int i = 0; i < modelo.getRowCount(); i++) {
            boolean marcada = Boolean.TRUE.equals(modelo.getValueAt(i, 6));
            if (marcada) {
                cursadas.add((String) modelo.getValueAt(i, 2)); // Nome agora na coluna 2
            }
        }

        List<Disciplina> todasDisciplinas = curso.getDisciplinas();
        DisciplinaCellRenderer renderer = new DisciplinaCellRenderer(todasDisciplinas, cursadas);

        for (int i = 0; i < tabela.getColumnCount(); i++) {
            tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        tabela.repaint();
    }

    private void mostrarProgresso() {
        int cargaTotal = 0;
        int cargaCursada = 0;

        for (int i = 0; i < tabela.getRowCount(); i++) {
            int carga = Integer.parseInt((String) tabela.getValueAt(i, 3));
            cargaTotal += carga;

            boolean cursada = Boolean.TRUE.equals(tabela.getValueAt(i, 6));
            if (cursada) {
                cargaCursada += carga;
            }
        }

        double percentual = (cargaCursada * 100.0) / cargaTotal;
        String msg = String.format("Carga Horária Concluída: %d h\nCarga Total do Curso: %d h\nPercentual de Conclusão: %.2f%%",
                cargaCursada, cargaTotal, percentual);

        JOptionPane.showMessageDialog(this, msg, "Progresso do Aluno", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarGrafico() {
        int total = tabela.getRowCount();
        int cursadas = 0;

        for (int i = 0; i < total; i++) {
            if (Boolean.TRUE.equals(tabela.getValueAt(i, 6))) {
                cursadas++;
            }
        }

        int naoCursadas = total - cursadas;

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Cursadas", cursadas);
        dataset.setValue("Não Cursadas", naoCursadas);

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribuição das Disciplinas",
                dataset,
                true, true, false
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 400));

        JDialog dialogo = new JDialog(this, "Gráfico de Progresso", true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogo.getContentPane().add(chartPanel);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    public MainFrame(Curso curso) {
        setTitle("Integralização Curricular - " + curso.getNome());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);

        String[] colunas = {"Semestre", "Ordem", "Disciplina", "Carga Horaria", "Créditos", "Pré-Requisitos", "Cursada?"};
        DefaultTableModel modelo = new DefaultTableModel(colunas, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return (column == 6) ? Boolean.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                if (column != 6) return false;

                String nomeDisciplina = (String) getValueAt(row, 2);
                Disciplina d = curso.getDisciplinas().stream()
                        .filter(disc -> disc.getNome().equals(nomeDisciplina))
                        .findFirst()
                        .orElse(null);

                if (d == null || d.getPreRequisitos().isEmpty()) return true;

                Set<String> cursadas = new HashSet<>();
                for (int i = 0; i < getRowCount(); i++) {
                    if (Boolean.TRUE.equals(getValueAt(i, 6))) {
                        cursadas.add((String) getValueAt(i, 2));
                    }
                }

                return d.getPreRequisitos().stream().allMatch(cursadas::contains);
            }
        };

        for (Disciplina d : curso.getDisciplinas()) {
            modelo.addRow(new Object[]{
                    d.getSemestre(),
                    d.getOrdem(),
                    d.getNome(),
                    String.valueOf(d.getCargaHoraria()),
                    String.valueOf(d.getCreditos()),
                    String.join(", ", d.getPreRequisitos()),
                    false
            });
        }

        tabela = new JTable(modelo);
        tabela.setDefaultEditor(Boolean.class, new DefaultCellEditor(new JCheckBox()));
        tabela.setDefaultRenderer(Boolean.class, tabela.getDefaultRenderer(Boolean.class));

        JScrollPane scroll = new JScrollPane(tabela);

        salvar = new JButton("Salvar");
        salvar.addActionListener(e -> {
            Set<String> cursadas = new HashSet<>();
            DefaultTableModel model = (DefaultTableModel) tabela.getModel();

            for (int i = 0; i < model.getRowCount(); i++) {
                boolean marcada = Boolean.TRUE.equals(model.getValueAt(i, 6));
                if (marcada) {
                    cursadas.add((String) model.getValueAt(i, 2));
                }
            }

            List<Disciplina> todasDisciplinas = curso.getDisciplinas();
            List<Disciplina> disponiveis = DisciplinaUtils.verificarDisciplinasDisponiveis(todasDisciplinas, cursadas);

            DisciplinaCellRenderer renderer = new DisciplinaCellRenderer(todasDisciplinas, cursadas);
            for (int i = 0; i < tabela.getColumnCount(); i++) {
                tabela.getColumnModel().getColumn(i).setCellRenderer(renderer);
            }

            tabela.repaint();

            StringBuilder msg = new StringBuilder("Disciplinas disponíveis para cursar:\n\n");
            for (Disciplina d : disponiveis) {
                msg.append("- ").append(d.getNome()).append("\n");
            }

            JOptionPane.showMessageDialog(this, msg.toString());
        });

        add(scroll, BorderLayout.CENTER);
        JPanel botoesPanel = new JPanel();
        botoesPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));

        JButton progresso = new JButton("Ver Progresso");
        progresso.addActionListener(e -> mostrarProgresso());

        JButton grafico = new JButton("Ver Gráfico");
        grafico.addActionListener(e -> mostrarGrafico());

        botoesPanel.add(progresso);
        botoesPanel.add(salvar);
        botoesPanel.add(grafico);

        add(botoesPanel, BorderLayout.SOUTH);

        modelo.addTableModelListener(e -> atualizarCoresDinamicamente(curso, modelo));

        setVisible(true);
    }
}
