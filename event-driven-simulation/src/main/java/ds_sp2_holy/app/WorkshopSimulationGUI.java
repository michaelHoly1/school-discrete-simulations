package ds_sp2_holy.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import ds_sp2_holy.app.eventdriven.simulation.GeneralEventDrivenSimulation;
import ds_sp2_holy.app.eventdriven.simulation.ISimDelegate;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.FurnitureWorkshopSimulation;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Order;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Worker;
import ds_sp2_holy.app.eventdriven.simulation.furnitureworkshop.entities.Workplace;
import ds_sp2_holy.app.generators.SeedGenerator;
import ds_sp2_holy.app.eventdriven.simulation.Statistics;
import ds_sp2_holy.app.eventdriven.simulation.WeightedStatistics;

import java.util.ArrayList;


public class WorkshopSimulationGUI extends JFrame implements ISimDelegate {

    private JTextField replicationCountField;
    private JTextField workersAField;
    private JTextField workersBField;
    private JTextField workersCField;
    private JTextField pointsDisplayedField;
    private JLabel timeLabel;
    private JButton runButton;
    private JButton stopButton;
    private JButton pauseButton;
    private JButton continueButton;
    private XYSeries series;
    private JFreeChart chart;

    private JTextField animWorkersAField;
    private JTextField animWorkersBField;
    private JTextField animWorkersCField;
    private JLabel animTimeLabel;
    private JButton animRunButton;
    private JButton animStopButton;
    private JButton animPauseButton;
    private JButton animContinueButton;
    private JComboBox<String> speedComboBox;
    private JButton changeSpeedButton;
    private JLabel currentSpeedLabel;
    private boolean isAnimationRunning = false;
    private double finalSlowdown;

    private JTable tableOrders, tableWorkplaces, tableA, tableB, tableC, tableQueues;
    private DefaultTableModel modelOrders, modelWorkplaces, modelA, modelB, modelC, modelQueues;

    private JTable globalStatisticsTable;
    private JTable globalStatisticsWorkersTable;
    private JTable ongoingStatisticsTable;
    private JTable ongoingStatisticsWorkersTable;
    private DefaultTableModel modelGlobalStatistics;
    private DefaultTableModel modelGlobalStatisticsWorkers;
    private DefaultTableModel modelOngoingStatistics;
    private DefaultTableModel modelOngoingStatisticsWorkers;

    private Thread simulationThread;
    private FurnitureWorkshopSimulation simulation;
    private int graphUpdateInterval = 1;
    private int lastIgnoredIndex = 0;
    

    public WorkshopSimulationGUI() {
        setTitle("Furniture Workshop Simulation");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            System.out.println("Failed to apply FlatDarkLaf look and feel. Using default.");
        }

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Full-speed", createFullSpeedPanel());
        tabbedPane.addTab("Animation", createAnimationPanel());
        tabbedPane.addTab("Statistics", createStatisticsPanel());

        add(tabbedPane);
    }

    private JPanel createFullSpeedPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel topControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topControlPanel.add(new JLabel("Number of Replications:"));
        replicationCountField = new JTextField(5);
        topControlPanel.add(replicationCountField);

        topControlPanel.add(new JLabel("Workers A:"));
        workersAField = new JTextField(3);
        topControlPanel.add(workersAField);

        topControlPanel.add(new JLabel("Workers B:"));
        workersBField = new JTextField(3);
        topControlPanel.add(workersBField);

        topControlPanel.add(new JLabel("Workers C:"));
        workersCField = new JTextField(3);
        topControlPanel.add(workersCField);

        topControlPanel.add(new JLabel("Points in Graph:"));
        pointsDisplayedField = new JTextField(5);
        topControlPanel.add(pointsDisplayedField);

        timeLabel = new JLabel("Time: 0.00 h");
        topControlPanel.add(Box.createHorizontalStrut(20));
        topControlPanel.add(timeLabel);

        runButton = new JButton("Run Simulation");
        stopButton = new JButton("Stop Simulation");
        pauseButton = new JButton("Pause Simulation");
        continueButton = new JButton("Continue Simulation");

        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        continueButton.setEnabled(false);

        runButton.addActionListener(e -> {
            runButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            continueButton.setEnabled(false);
            series.clear();
            timeLabel.setText("Time: 0.00 h");

            int replications = Integer.parseInt(replicationCountField.getText());
            int workersA = Integer.parseInt(workersAField.getText());
            int workersB = Integer.parseInt(workersBField.getText());
            int workersC = Integer.parseInt(workersCField.getText());
            int pointsDisplayed = Integer.parseInt(pointsDisplayedField.getText());

            graphUpdateInterval = Math.max(1, (int)Math.floor((double)replications / pointsDisplayed));
            lastIgnoredIndex = (int)Math.floor(replications * 0.15);

            simulationThread = new Thread(() -> {
                simulation = new FurnitureWorkshopSimulation(new SeedGenerator(System.currentTimeMillis()), 7171200, workersA, workersB, workersC);
                simulation.registerDelegate(this);
                simulation.simulate(replications);

                SwingUtilities.invokeLater(() -> {
                    runButton.setEnabled(true);
                    stopButton.setEnabled(false);
                    pauseButton.setEnabled(false);
                    continueButton.setEnabled(false);
                });
            });

            simulationThread.start();
        });

        stopButton.addActionListener(e -> {
            if (simulation != null) simulation.stopSimulation();
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
            continueButton.setEnabled(false);
            runButton.setEnabled(true);
        });

        pauseButton.addActionListener(e -> {
            if (simulation != null) simulation.pauseSimulation();
            pauseButton.setEnabled(false);
            continueButton.setEnabled(true);
        });

        continueButton.addActionListener(e -> {
            if (simulation != null) simulation.resumeSimulation();
            continueButton.setEnabled(false);
            pauseButton.setEnabled(true);
        });

        topControlPanel.add(runButton);
        topControlPanel.add(pauseButton);
        topControlPanel.add(continueButton);
        topControlPanel.add(stopButton);

        series = new XYSeries("Average Finish Time");
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        chart = ChartFactory.createXYLineChart("Simulation Results", "Replication", "Average Finish Time", dataset);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(50, 50, 50));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        chart.setBackgroundPaint(new Color(45, 45, 45));
        chart.getTitle().setPaint(Color.WHITE);

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.CYAN);
        renderer.setSeriesShapesVisible(0, true);
        renderer.setSeriesShape(0, new java.awt.geom.Ellipse2D.Double(-1.5, -1.5, 3, 3));
        plot.setRenderer(renderer);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRange(true);
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLabelPaint(Color.WHITE);
        yAxis.setTickLabelPaint(Color.WHITE);

        NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
        xAxis.setLabelPaint(Color.WHITE);
        xAxis.setTickLabelPaint(Color.WHITE);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(800, 600));

        panel.add(topControlPanel, BorderLayout.NORTH);
        panel.add(chartPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAnimationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        controlPanel.add(new JLabel("Workers A:"));
        animWorkersAField = new JTextField(3);
        controlPanel.add(animWorkersAField);

        controlPanel.add(new JLabel("Workers B:"));
        animWorkersBField = new JTextField(3);
        controlPanel.add(animWorkersBField);

        controlPanel.add(new JLabel("Workers C:"));
        animWorkersCField = new JTextField(3);
        controlPanel.add(animWorkersCField);

        controlPanel.add(new JLabel("Speed:"));
        speedComboBox = new JComboBox<>(new String[]{"0.5x", "1x", "2x", "5x", "10x", "20x", "50x", "100x", "200x", "500x", "1000x"});
        controlPanel.add(speedComboBox);

        changeSpeedButton = new JButton("Change Speed");
        currentSpeedLabel = new JLabel("Current: 1x");
        controlPanel.add(changeSpeedButton);
        controlPanel.add(currentSpeedLabel);

        animTimeLabel = new JLabel("W: 0 D: X Time: 00:00");
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(animTimeLabel);

        animRunButton = new JButton("Run Animated Simulation");
        animStopButton = new JButton("Stop Simulation");
        animPauseButton = new JButton("Pause Simulation");
        animContinueButton = new JButton("Continue Simulation");

        animStopButton.setEnabled(false);
        animPauseButton.setEnabled(false);
        animContinueButton.setEnabled(false);

        controlPanel.add(animRunButton);
        controlPanel.add(animPauseButton);
        controlPanel.add(animContinueButton);
        controlPanel.add(animStopButton);

        animRunButton.addActionListener(e -> {
            isAnimationRunning = true;
            animRunButton.setEnabled(false);
            animStopButton.setEnabled(true);
            animPauseButton.setEnabled(true);
            animContinueButton.setEnabled(false);
            animTimeLabel.setText("Time: 0.00 h");

            int workersA = Integer.parseInt(animWorkersAField.getText());
            int workersB = Integer.parseInt(animWorkersBField.getText());
            int workersC = Integer.parseInt(animWorkersCField.getText());
            currentSpeedLabel.setText("Current: " + speedComboBox.getSelectedItem());

            this.finalSlowdown = getSelectedSlowdownRatio();
            simulationThread = new Thread(() -> {
                simulation = new FurnitureWorkshopSimulation(new SeedGenerator(System.currentTimeMillis()), 7171200, workersA, workersB, workersC);
                simulation.registerDelegate(this);
                simulation.turnOnSlowdown(finalSlowdown);
                simulation.simulate(1);
                
                SwingUtilities.invokeLater(() -> {
                    isAnimationRunning = false;
                    animRunButton.setEnabled(true);
                    animStopButton.setEnabled(false);
                    animPauseButton.setEnabled(false);
                    animContinueButton.setEnabled(false);
                });
            });
            simulationThread.start();
        });
    

        animPauseButton.addActionListener(e -> {
            if (simulation != null) simulation.pauseSimulation();
            animPauseButton.setEnabled(false);
            animContinueButton.setEnabled(true);
        });

        animContinueButton.addActionListener(e -> {
            if (simulation != null) simulation.resumeSimulation();
            animContinueButton.setEnabled(false);
            animPauseButton.setEnabled(true);
        });

        animStopButton.addActionListener(e -> {
            if (simulation != null) simulation.stopSimulation();
            isAnimationRunning = false;
            animRunButton.setEnabled(true);
            animStopButton.setEnabled(false);
            animPauseButton.setEnabled(false);
            animContinueButton.setEnabled(false);
        });

        changeSpeedButton.addActionListener(e -> {
            if (simulation != null) {
                this.finalSlowdown = getSelectedSlowdownRatio();
                simulation.turnOnSlowdown(finalSlowdown);
                currentSpeedLabel.setText("Current: " + speedComboBox.getSelectedItem());
            }
        });

        panel.add(controlPanel, BorderLayout.NORTH);

        JPanel tablePanel = new JPanel(new GridLayout(2, 3));

        modelOrders = new DefaultTableModel(new Object[]{"ID", "Order Type", "Order State", "Worker", "Workplace"}, 0);
        tableOrders = new JTable(modelOrders);
        tablePanel.add(wrapWithTitle("Active Orders", tableOrders));

        modelWorkplaces = new DefaultTableModel(new Object[]{"ID", "State"}, 0);
        tableWorkplaces = new JTable(modelWorkplaces);
        tablePanel.add(wrapWithTitle("Workplaces", tableWorkplaces));

        modelQueues = new DefaultTableModel(new Object[]{"Sim information", "Value"}, 0);
        tableQueues = new JTable(modelQueues);
        tablePanel.add(wrapWithTitle("Queue Information", tableQueues));

        modelA = new DefaultTableModel(new Object[]{"ID", "State", "Last Workplace"}, 0);
        tableA = new JTable(modelA);
        tablePanel.add(wrapWithTitle("Workers A", tableA));

        modelB = new DefaultTableModel(new Object[]{"ID", "State", "Last Workplace"}, 0);
        tableB = new JTable(modelB);
        tablePanel.add(wrapWithTitle("Workers B", tableB));

        modelC = new DefaultTableModel(new Object[]{"ID", "State", "Last Workplace"}, 0);
        tableC = new JTable(modelC);
        tablePanel.add(wrapWithTitle("Workers C", tableC));

        panel.add(tablePanel, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane wrapWithTitle(String title, JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createTitledBorder(title));
        return scroll;
    }

    private double getSelectedSlowdownRatio() {
        String selected = (String) speedComboBox.getSelectedItem();
        if ("0.5x".equals(selected)) return 0.5;
        if ("2x".equals(selected)) return 2.0;
        if ("5x".equals(selected)) return 5.0;
        if ("10x".equals(selected)) return 10.0;
        if ("20x".equals(selected)) return 20.0;
        if ("50x".equals(selected)) return 50.0;
        if ("100x".equals(selected)) return 100.0;
        if ("200x".equals(selected)) return 200.0;
        if ("500x".equals(selected)) return 500.0;
        if ("1000x".equals(selected)) return 1000.0;

        return 1.0; 
    }

    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1));

        // Global Statistics
        JPanel globalPanel = new JPanel(new GridLayout(1, 2));

        modelGlobalStatistics = new DefaultTableModel(new Object[]{"Type of Statistic", "Value", "95% CI"}, 0);
        globalStatisticsTable = new JTable(modelGlobalStatistics);
        JScrollPane globalScrollPane = new JScrollPane(globalStatisticsTable);
        globalScrollPane.setBorder(BorderFactory.createTitledBorder("Fullspeed - Global Statistics"));

        modelGlobalStatisticsWorkers = new DefaultTableModel(new Object[]{"Statistic Type", "Workload (%)", "95% CI"}, 0);
        globalStatisticsWorkersTable = new JTable(modelGlobalStatisticsWorkers);
        JScrollPane globalWorkersScrollPane = new JScrollPane(globalStatisticsWorkersTable);
        globalWorkersScrollPane.setBorder(BorderFactory.createTitledBorder("Fullspeed - Global Statistics Workers"));

        globalPanel.add(globalScrollPane);
        globalPanel.add(globalWorkersScrollPane);

        // Ongoing Statistics
        JPanel ongoingPanel = new JPanel(new GridLayout(1, 2));

        modelOngoingStatistics = new DefaultTableModel(new Object[]{"Type of Statistic", "Value"}, 0);
        ongoingStatisticsTable = new JTable(modelOngoingStatistics);
        JScrollPane ongoingScrollPane = new JScrollPane(ongoingStatisticsTable);
        ongoingScrollPane.setBorder(BorderFactory.createTitledBorder("Animation - Ongoing Statistics"));

        modelOngoingStatisticsWorkers = new DefaultTableModel(new Object[]{"Statistic Type", "Workload (%)"}, 0);
        ongoingStatisticsWorkersTable = new JTable(modelOngoingStatisticsWorkers);
        JScrollPane ongoingWorkersScrollPane = new JScrollPane(ongoingStatisticsWorkersTable);
        ongoingWorkersScrollPane.setBorder(BorderFactory.createTitledBorder("Animation - Ongoing Statistics Workers"));

        ongoingPanel.add(ongoingScrollPane);
        ongoingPanel.add(ongoingWorkersScrollPane);

        panel.add(globalPanel);
        panel.add(ongoingPanel);

        return panel;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
        } catch (Exception e) {
            System.out.println("Failed to apply FlatDarkLaf. Using default look and feel.");
        }

        SwingUtilities.invokeLater(() -> {
            WorkshopSimulationGUI gui = new WorkshopSimulationGUI();
            gui.setVisible(true);
        });
    }

    @Override
    public void refresh(GeneralEventDrivenSimulation simulation, RefreshType type) {
        FurnitureWorkshopSimulation workshopSim = (FurnitureWorkshopSimulation) simulation;

        if (type == RefreshType.REFRESH_GRAPH) {
            int index = workshopSim.getReplicationIndex();
            if (index >= lastIgnoredIndex && index % graphUpdateInterval == 0) {
                double averageOrderFinishTime = workshopSim.getAverageOrderFinishTimeGlobalStatistics().getMean();
                series.add(index, averageOrderFinishTime);
                double hours = averageOrderFinishTime / 3600.0;
                SwingUtilities.invokeLater(() -> timeLabel.setText(String.format("Time: %.2f h", hours)));
            }
        } else if (type == RefreshType.REFRESH_ENTITIES && isAnimationRunning) {
            double currentSimulationTime = workshopSim.getCurrentSimulationTime();
            SwingUtilities.invokeLater(() -> {
                animTimeLabel.setText(formatSimulationTime(currentSimulationTime));

                modelQueues.setRowCount(0);
                modelQueues.addRow(new Object[]{"Unstarted Orders", workshopSim.getUnstartedOrdersSize()});
                modelQueues.addRow(new Object[]{"Varnishing Queue", workshopSim.getOrdersWaitingForVarnishingSize()});
                modelQueues.addRow(new Object[]{"Assembly Queue", workshopSim.getOrdersWaitingForAssemblySize()});
                modelQueues.addRow(new Object[]{"Forging Queue", workshopSim.getOrdersWaitingForForgingSize()});
                modelQueues.addRow(new Object[]{"Expedition Queue", workshopSim.getOrdersWaitingForExpeditionSize()});
                modelQueues.addRow(new Object[]{"Free Workers A", workshopSim.getFreeWorkersASize()});
                modelQueues.addRow(new Object[]{"Free Workers B", workshopSim.getFreeWorkersBSize()});
                modelQueues.addRow(new Object[]{"Free Workers C", workshopSim.getFreeWorkersCSize()});
                modelQueues.addRow(new Object[]{"Free Workplaces", workshopSim.getFreeWorkplacesSize()});
                modelQueues.addRow(new Object[]{"Finished Orders", workshopSim.getNumberOfFinishedOrders()});

                modelOrders.setRowCount(0);
                for (Order order : workshopSim.getActiveOrders()) {

                    if (order.getCurrentWorker() == null) {

                        if (order.getAssignedWorkplace() == null) {
                            modelOrders.addRow(new Object[]{order.getOrderID(), order.getOrderType(), order.getState(), "None", "None"});
                        } else {
                            modelOrders.addRow(new Object[]{order.getOrderID(), order.getOrderType(), order.getState(), "None", order.getAssignedWorkplace().getId()});
                        }

                        
                    } else {
                        modelOrders.addRow(new Object[]{order.getOrderID(), order.getOrderType(), order.getState(), order.getCurrentWorker(), order.getAssignedWorkplace().getId()});
                    }
                    
                }

                modelWorkplaces.setRowCount(0);
                for (Workplace wp : workshopSim.getAllWorkplaces()) {
                    modelWorkplaces.addRow(new Object[]{wp.getId(), wp.getState()});
                }

                modelA.setRowCount(0);
                for (Worker w : workshopSim.getAllWorkersA()) {
                    modelA.addRow(new Object[]{w.getId(), w.getState(), w.getLastOccupiedWorkplace()});
                }

                modelB.setRowCount(0);
                for (Worker w : workshopSim.getAllWorkersB()) {
                    modelB.addRow(new Object[]{w.getId(), w.getState(), w.getLastOccupiedWorkplace()});
                }

                modelC.setRowCount(0);
                for (Worker w : workshopSim.getAllWorkersC()) {
                    modelC.addRow(new Object[]{w.getId(), w.getState(), w.getLastOccupiedWorkplace()});
                }
            });
        } else if (type == RefreshType.REFRESH_TIME && isAnimationRunning) {
            double currentSimulationTime = workshopSim.getCurrentSimulationTime();
            SwingUtilities.invokeLater(() -> animTimeLabel.setText(formatSimulationTime(currentSimulationTime)));

        } else if (type == RefreshType.REFRESH_FULLSPEED_STATISTICS) {
            
            SwingUtilities.invokeLater(() -> {
                modelGlobalStatistics.setRowCount(0);
            
                double[] averageOrderFinishTimeCI = workshopSim.getAverageOrderFinishTimeGlobalStatistics().getConfidenceInterval(95);
                double[] averageCountOfUnstartedOrdersCI = workshopSim.getAverageCountOfUnstartedOrdersGlobalStatistics().getConfidenceInterval(95);
            
                modelGlobalStatistics.addRow(new Object[]{
                    "Average Order Finish Time",
                    String.format("%.4f", averageOrderFinishTimeCI[1] / 3600.0),
                    String.format("<%.4f;%.4f>", averageOrderFinishTimeCI[0] / 3600.0, averageOrderFinishTimeCI[2] / 3600.0)
                });
            
                modelGlobalStatistics.addRow(new Object[]{
                    "Average Amount Of Unstarted Orders",
                    String.format("%.4f", averageCountOfUnstartedOrdersCI[1]),
                    String.format("<%.4f;%.4f>", averageCountOfUnstartedOrdersCI[0], averageCountOfUnstartedOrdersCI[2])
                });
            
                modelGlobalStatisticsWorkers.setRowCount(0);
            
                double[] averageWorkloadGroupACI = workshopSim.getAverageWorkloadGroupAGlobalStatistics().getConfidenceInterval(95);
                double[] averageWorkloadGroupBCI = workshopSim.getAverageWorkloadGroupBGlobalStatistics().getConfidenceInterval(95);
                double[] averageWorkloadGroupCCI = workshopSim.getAverageWorkloadGroupCGlobalStatistics().getConfidenceInterval(95);
            
                modelGlobalStatisticsWorkers.addRow(new Object[]{
                    "Group A",
                    String.format("%.4f", averageWorkloadGroupACI[1]),
                    String.format("<%.4f;%.4f>", averageWorkloadGroupACI[0], averageWorkloadGroupACI[2])
                });
                modelGlobalStatisticsWorkers.addRow(new Object[]{
                    "Group B",
                    String.format("%.4f", averageWorkloadGroupBCI[1]),
                    String.format("<%.4f;%.4f>", averageWorkloadGroupBCI[0], averageWorkloadGroupBCI[2])
                });
                modelGlobalStatisticsWorkers.addRow(new Object[]{
                    "Group C",
                    String.format("%.4f", averageWorkloadGroupCCI[1]),
                    String.format("<%.4f;%.4f>", averageWorkloadGroupCCI[0], averageWorkloadGroupCCI[2])
                });
            
                ArrayList<Statistics> workloadWorkersA = workshopSim.getAverageWorkloadWorkersAGlobalStatistics();
                ArrayList<Statistics> workloadWorkersB = workshopSim.getAverageWorkloadWorkersBGlobalStatistics();
                ArrayList<Statistics> workloadWorkersC = workshopSim.getAverageWorkloadWorkersCGlobalStatistics();
            
                for (int i = 0; i < workloadWorkersA.size(); i++) {
                    double[] workloadWorkersACI = workloadWorkersA.get(i).getConfidenceInterval(95);
                    modelGlobalStatisticsWorkers.addRow(new Object[]{
                        "Worker A" + i,
                        String.format("%.4f", workloadWorkersACI[1]),
                        String.format("<%.4f;%.4f>", workloadWorkersACI[0], workloadWorkersACI[2])
                    });
                }
            
                for (int i = 0; i < workloadWorkersB.size(); i++) {
                    double[] workloadWorkersBCI = workloadWorkersB.get(i).getConfidenceInterval(95);
                    modelGlobalStatisticsWorkers.addRow(new Object[]{
                        "Worker B" + i,
                        String.format("%.4f", workloadWorkersBCI[1]),
                        String.format("<%.4f;%.4f>", workloadWorkersBCI[0], workloadWorkersBCI[2])
                    });
                }
            
                for (int i = 0; i < workloadWorkersC.size(); i++) {
                    double[] workloadWorkersCCI = workloadWorkersC.get(i).getConfidenceInterval(95);
                    modelGlobalStatisticsWorkers.addRow(new Object[]{
                        "Worker C" + i,
                        String.format("%.4f", workloadWorkersCCI[1]),
                        String.format("<%.4f;%.4f>", workloadWorkersCCI[0], workloadWorkersCCI[2])
                    });
                }
            });

        } else if (type == RefreshType.REFRESH_ANIMATION_STATISTICS && isAnimationRunning) {

            double currentSimulationTime = workshopSim.getCurrentSimulationTime();
            
            SwingUtilities.invokeLater(() -> {
                modelOngoingStatistics.setRowCount(0);
                modelOngoingStatisticsWorkers.setRowCount(0);
                Statistics averageOrderFinishTime = workshopSim.getAverageOrderFinishTimeReplicationStatistics();
                WeightedStatistics averageCountOfUnstartedOrders = workshopSim.getAverageCountOfUnstartedOrdersReplicationStatistics();

                if (averageOrderFinishTime.getDataCount() > 0 && averageCountOfUnstartedOrders.getDataCount() > 0)  {

                    // If there are data points, set the values to the mean of the statistics
                    modelOngoingStatistics.addRow(new Object[]{"Average Order Finish Time", workshopSim.getAverageOrderFinishTimeReplicationStatistics().getMean()});
                    modelOngoingStatistics.addRow(new Object[]{"Average Amount Of Unstarted Orders", workshopSim.getAverageCountOfUnstartedOrdersReplicationStatistics().getMean()});

                    
                } else {

                    // If there are no data points = "N/A"
                    modelOngoingStatistics.addRow(new Object[]{"Average Order Finish Time", "N/A"});
                    modelOngoingStatistics.addRow(new Object[]{"Average Amount Of Unstarted Orders", "N/A"});
                }
                

                modelOngoingStatisticsWorkers.addRow(new Object[]{"Group A", String.format("%.4f", workshopSim.getAverageWorkloadPercentageForWholeGroup("A"))});
                modelOngoingStatisticsWorkers.addRow(new Object[]{"Group B", String.format("%.4f", workshopSim.getAverageWorkloadPercentageForWholeGroup("B"))});
                modelOngoingStatisticsWorkers.addRow(new Object[]{"Group C", String.format("%.4f", workshopSim.getAverageWorkloadPercentageForWholeGroup("C"))});

                ArrayList<Worker> workersA = workshopSim.getAllWorkersA();
                ArrayList<Worker> workersB = workshopSim.getAllWorkersB();
                ArrayList<Worker> workersC = workshopSim.getAllWorkersC();

                for (int i = 0; i < workersA.size(); i++) {
                    double workloadPercentage = workersA.get(i).getWorkloadPercentage(currentSimulationTime);
                    modelOngoingStatisticsWorkers.addRow(new Object[]{"Worker A" + i , String.format("%.4f", workloadPercentage)});
                }

                for (int i = 0; i < workersB.size(); i++) {
                    double workloadPercentage = workersB.get(i).getWorkloadPercentage(currentSimulationTime);
                    modelOngoingStatisticsWorkers.addRow(new Object[]{"Worker B" + i , String.format("%.4f", workloadPercentage)});

                }

                for (int i = 0; i < workersC.size(); i++) {
                    double workloadPercentage = workersC.get(i).getWorkloadPercentage(currentSimulationTime);
                    modelOngoingStatisticsWorkers.addRow(new Object[]{"Worker C" + i , String.format("%.4f", workloadPercentage)});

                }

            });


        }
    }

    private String formatSimulationTime(double timeInSeconds) {
        int secondsPerDay = 28800;
        int secondsPerWeek = 144000;
    
        int totalWeeks = (int) (timeInSeconds / secondsPerWeek) + 1;
        int remainingSeconds = (int) (timeInSeconds % secondsPerWeek);
    
        int currentDayIndex = remainingSeconds / secondsPerDay;
        String[] days = {"MON", "TUE", "WED", "THU", "FRI"};
        String dayName = days[currentDayIndex];
    
        int timeOfDaySeconds = remainingSeconds % secondsPerDay;
        int hours = 6 + (timeOfDaySeconds / 3600); 
        int minutes = (timeOfDaySeconds % 3600) / 60;
        int seconds = timeOfDaySeconds % 60;
    
        return String.format("Week: %d  D: %s  Time: %02d:%02d:%02d", totalWeeks, dayName, hours, minutes, seconds);
    }
    
    
}
