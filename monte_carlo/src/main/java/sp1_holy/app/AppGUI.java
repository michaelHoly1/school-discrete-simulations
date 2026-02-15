package sp1_holy.app;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.axis.NumberAxis;

public class AppGUI {

    private BusinessmanJohnApplication app;
    private JFrame frame;
    private JTextField replicationCountField;
    private JCheckBox strategyA, strategyB, strategyC, strategyD, strategyCustom;
    private JButton runButton, stopButton;
    private XYSeries series;
    private JFreeChart chart;
    private XYSeriesCollection dataset;

    public AppGUI() {
        this.app = new BusinessmanJohnApplication(System.currentTimeMillis());
        initializeGUI();

        app.setGraphUpdater((x, y) -> {
            SwingUtilities.invokeLater(() -> {
                series.add(x, y);
            });
        });
    }

    private void initializeGUI() {
        frame = new JFrame("BusinessmanJohn Simulation");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 750);
        frame.setLayout(new BorderLayout());
        frame.setResizable(true);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel simulationTypeLabel = new JLabel("Simulation Type:");
        strategyA = new JCheckBox("Strategy A");
        strategyB = new JCheckBox("Strategy B");
        strategyC = new JCheckBox("Strategy C");
        strategyD = new JCheckBox("Strategy D");
        strategyCustom = new JCheckBox("Strategy Custom");

        topPanel.add(simulationTypeLabel);
        topPanel.add(strategyA);
        topPanel.add(strategyB);
        topPanel.add(strategyC);
        topPanel.add(strategyD);
        topPanel.add(strategyCustom);

        JPanel middlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel replicationCountLabel = new JLabel("Number of Replications:");
        replicationCountField = new JTextField(12);
        runButton = new JButton("Run Simulation");
        stopButton = new JButton("Stop Simulation");
        stopButton.setEnabled(false);

        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runSimulation();
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopSimulation();
            }
        });

        middlePanel.add(replicationCountLabel);
        middlePanel.add(replicationCountField);
        middlePanel.add(runButton);
        middlePanel.add(stopButton);

        
        series = new XYSeries("Total costs");
        dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart("Simulation Results", "X", "Total costs", dataset);
        NumberAxis yAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(false);
        
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(650, 300));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        chartPanel.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.add(topPanel);
        topContainer.add(middlePanel);

        mainPanel.add(topContainer, BorderLayout.NORTH);
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    private void runSimulation() {
        SwingUtilities.invokeLater(() -> series.clear());
        
        try {
            int replicationCount = Integer.parseInt(replicationCountField.getText());
            int selectedStrategy = getSelectedStrategy();
            if (selectedStrategy == -1) {
                JOptionPane.showMessageDialog(frame, "Please select one strategy", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            runButton.setEnabled(false);
            stopButton.setEnabled(true);

            if (replicationCount == 1) {
                this.chart.getXYPlot().getDomainAxis().setLabel("Day");

            } else {
                this.chart.getXYPlot().getDomainAxis().setLabel("Replication");
            }

            new Thread(() -> {

                app.runSimulation(selectedStrategy, replicationCount);
                SwingUtilities.invokeLater(() -> {
                    runButton.setEnabled(true);
                    stopButton.setEnabled(false);
                });

            }).start();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Please enter a valid number", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void stopSimulation() {
        app.stopSimulation();
        stopButton.setEnabled(false);
        JOptionPane.showMessageDialog(frame, "Simulation stopped", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private int getSelectedStrategy() {
        if (strategyA.isSelected()) return 1;
        if (strategyB.isSelected()) return 2;
        if (strategyC.isSelected()) return 3;
        if (strategyD.isSelected()) return 4;
        if (strategyCustom.isSelected()) return 5;
        return -1;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AppGUI::new);
    }
}