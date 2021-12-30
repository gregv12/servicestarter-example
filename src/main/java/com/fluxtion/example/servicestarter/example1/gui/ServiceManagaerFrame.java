package com.fluxtion.example.servicestarter.example1.gui;

import com.fluxtion.example.servicestater.Service;
import com.fluxtion.example.servicestater.ServiceManager;
import com.fluxtion.example.servicestater.ServiceStatusRecord;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fluxtion.example.servicestarter.example1.Main.*;

public class ServiceManagaerFrame {
    public static final Color GREEN = new Color(0, 150, 90);
    private JTable statusTable;
    private final List<ServiceStatusRecord> statusUpdate = new ArrayList<>();
    private final Map<String, JLabel> nodeMap = new HashMap<>();
    private DefaultTableModel tableModel;
    private JPanel buttonPanel;
    private final ServiceManager serviceManager;
    private JPanel graphPanel;

    public ServiceManagaerFrame(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
        JFrame mainFrame = new JFrame("Service starter example");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        mainFrame.setLayout(new BorderLayout());
        buildGraphPanel();
        buildTableModel();
        buildButtonBar();
        mainFrame.add(new JScrollPane(statusTable), BorderLayout.NORTH);
        mainFrame.add(graphPanel, BorderLayout.CENTER);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);
        mainFrame.setPreferredSize(new Dimension(900, 700));
        mainFrame.pack();
        mainFrame.setVisible(true);
    }

    private void buildGraphPanel() {
        graphPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawLine(425, 95, 425, 150);
                g.drawLine(425, 195, 425, 250);
                g.drawLine(250, 175, 325, 175);
                g.drawLine(525, 175, 600, 175);
                g.drawLine(525, 275, 600, 275);
                g.drawLine(425, 295, 300, 350);
                g.drawLine(425, 295, 550, 350);
            }
        };
        graphPanel.setBackground(new Color(225, 225, 225));
        graphPanel.setLayout(null);
        graphPanel.setPreferredSize(new Dimension(800, 500));
        graphPanel.setMinimumSize(new Dimension(800, 500));
        addNode(ORDER_GATEWAY, 325, 50);
        addNode(PNL_CHECK, 325, 150);
        addNode(LIMIT_READER, 50, 150);
        addNode(MARKET_DATA_GATEWAY, 600, 150);
        addNode(ORDER_PROCESSOR, 325, 250);
        addNode(INTERNAL_ORDER_SOURCE, 600, 250);
        addNode(ORDER_AUDIT, 200, 350);
        addNode(RISK_MANAGER, 450, 350);
    }

    private void addNode(String name, int horizontal, int vertical) {
        JLabel node = new JLabel(name);
        node.setBackground(Color.lightGray);
        node.setForeground(Color.BLUE);
        node.setBorder(new LineBorder(Color.BLACK, 1));
        node.setHorizontalAlignment(SwingConstants.CENTER);
        node.setOpaque(true);
        final int width = 200;
        final int height = 46;
        graphPanel.add(node);
        node.setBounds(horizontal, vertical, width, height);
        nodeMap.put(name, node);
        //start/stopbutton
        JButton startButton = new JButton(">");
        startButton.setBackground(GREEN);
        startButton.setMargin(new Insets(0, 0, 0, 0));
        startButton.setBounds(horizontal + width, vertical, 35, 23);
        startButton.addActionListener(a -> serviceManager.startService(name));
        startButton.setFocusPainted(false);
        graphPanel.add(startButton);
        //stop button
        JButton stopButton = new JButton("X");
        stopButton.setBackground(new Color(180, 20, 20));
        stopButton.setMargin(new Insets(0, 0, 0, 0));
        stopButton.setBounds(horizontal + width, vertical + 23, 35, 23);
        stopButton.addActionListener(a -> serviceManager.stopService(name));
        stopButton.setFocusPainted(false);
        graphPanel.add(stopButton);
    }

    private void buildButtonBar() {
        buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setLayout(new FlowLayout());
        JButton start_all = new JButton("start all");
        JButton stop_all = new JButton("stop all");
        start_all.addActionListener(e -> serviceManager.startAllServices());
        stop_all.addActionListener(e -> serviceManager.stopAllServices());

        JCheckBox triggerOnTaskComplete = new JCheckBox("trigger on task complete", true);
        triggerOnTaskComplete.setOpaque(false);
        triggerOnTaskComplete.addItemListener(i ->
                serviceManager.triggerNotificationOnSuccessfulTaskExecution(i.getStateChange() == ItemEvent.SELECTED)
        );

        JCheckBox triggerOnNotification = new JCheckBox("trigger on status update", true);
        triggerOnNotification.setOpaque(false);
        triggerOnNotification.addItemListener(i ->
                serviceManager.triggerDependentsOnNotification(i.getStateChange() == ItemEvent.SELECTED)
        );
        buttonPanel.add(start_all);
        buttonPanel.add(stop_all);
        buttonPanel.add(triggerOnTaskComplete);
        buttonPanel.add(triggerOnNotification);
    }

    public void logStatus(List<ServiceStatusRecord> statusUpdate) {
        SwingUtilities.invokeLater(() -> {
            this.statusUpdate.clear();
            this.statusUpdate.addAll(statusUpdate);
            tableModel.fireTableDataChanged();
        });
    }

    private void buildTableModel() {
        tableModel = new DefaultTableModel() {
            @Override
            public int getColumnCount() {
                return 6;
            }

            @Override
            public int getRowCount() {
                return statusUpdate.size();
            }

            @Override
            public Object getValueAt(int row, int column) {
                ServiceStatusRecord serviceStatusRecord = statusUpdate.get(row);
                return switch (column) {
                    case 0 -> serviceStatusRecord.getServiceName();
                    case 1 -> serviceStatusRecord.getStatus();
                    case 2 -> "start service";
                    case 3 -> "stop service";
                    case 4 -> "notify started";
                    case 5 -> "notify stopped";
                    default -> throw new IllegalStateException("Unexpected value: " + column);
                };
            }

            @Override
            public String getColumnName(int column) {
                return switch (column) {
                    case 0 -> "Service name";
                    case 1 -> "Status";
                    case 2 -> "Start service";
                    case 3 -> "Stop service";
                    case 4 -> "Notify started";
                    case 5 -> "Notify stopped";
                    default -> throw new IllegalStateException("Unexpected value: " + column);
                };
            }

            @Override
            public void setValueAt(Object aValue, int row, int column) {
                String svcName = statusUpdate.get(row).getServiceName();
                System.out.println(aValue + "(" + svcName + ")");
                switch ((String) aValue) {
                    case "start service" -> serviceManager.startService(svcName);
                    case "stop service" -> serviceManager.stopService(svcName);
                    case "notify started" -> serviceManager.serviceStarted(svcName);
                    case "notify stopped" -> serviceManager.serviceStopped(svcName);
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 1;
            }
        };
        statusTable = new JTable(tableModel);
        statusTable.setPreferredSize(new Dimension(800, 150));
        statusTable.setPreferredScrollableViewportSize(new Dimension(800, 150));
        statusTable.setRowSelectionAllowed(false);
        statusTable.setColumnSelectionAllowed(false);
        TableColumnModel columnModel = statusTable.getColumnModel();
        columnModel.getColumn(0).setMinWidth(150);
        columnModel.getColumn(0).setMaxWidth(300);
        columnModel.getColumn(1).setMinWidth(130);
        columnModel.getColumn(1).setCellRenderer(new MyRenderer(Color.lightGray, Color.blue));
        new ButtonColumn(statusTable, 2);
        new ButtonColumn(statusTable, 3);
        new ButtonColumn(statusTable, 4);
        new ButtonColumn(statusTable, 5);
    }

    class MyRenderer extends DefaultTableCellRenderer {
        Color bg, fg;

        public MyRenderer(Color bg, Color fg) {
            super();
            this.bg = bg;
            this.fg = fg;
        }

        public Component getTableCellRendererComponent(JTable table, Object
                value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value,
                    isSelected, hasFocus, row, column);
            ServiceStatusRecord serviceStatusRecord = statusUpdate.get(row);
            JLabel jComponent = nodeMap.get(serviceStatusRecord.getServiceName());
            jComponent.setText("<html>" + serviceStatusRecord.getServiceName() + "<br>" + serviceStatusRecord.getStatus() + "</html>");
            if (serviceStatusRecord.getStatus() == Service.Status.STARTED) {
                cell.setForeground(GREEN);
                jComponent.setForeground(GREEN);
            } else if (serviceStatusRecord.getStatus() == Service.Status.STOPPED) {
                cell.setForeground(Color.RED);
                jComponent.setForeground(Color.RED);
            } else {
                cell.setForeground(fg);
                jComponent.setForeground(fg);
            }
            cell.setBackground(bg);
            return cell;
        }
    }
}
