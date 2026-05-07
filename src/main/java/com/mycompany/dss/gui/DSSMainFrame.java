package com.mycompany.dss.gui;

import com.mycompany.dss.logic.DecisionEngine;
import com.mycompany.dss.logic.RiskAnalyzer;
import com.mycompany.dss.model.Product;
import com.mycompany.dss.model.Scenario;
import com.mycompany.dss.model.ScenarioResult;
import com.mycompany.dss.database.DatabaseManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DSSMainFrame extends JFrame {

    // Data (synchronised with database)
    private ArrayList<Scenario> scenarios = new ArrayList<>();
    private ArrayList<ScenarioResult> results = new ArrayList<>();
    private DecisionEngine engine = new DecisionEngine();
    private RiskAnalyzer riskAnalyzer = new RiskAnalyzer();
    private DecimalFormat currencyFormat = new DecimalFormat("€##,##0.00");

    // Table models
    private DefaultTableModel productModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Price (€)", "Cost (€)", "Demand", "Profit (€)", "Margin" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column >= 1 && column <= 4; // allow edit Name, Price, Cost, Demand
        }
    };
    private JTable productTable = new JTable(productModel);
    private DefaultTableModel scenarioModel = new DefaultTableModel(
            new String[] { "ID", "Name", "Price Change", "Cost Change", "Demand Change", "Profit Impact" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 1; // only scenario name editable
        }
    };
    private JTable scenarioTable = new JTable(scenarioModel);
    private DefaultTableModel resultsModel;

    // Input fields (for products)
    private JTextField pName = new JTextField(15);
    private JTextField pPrice = new JTextField(15);
    private JTextField pCost = new JTextField(15);
    private JTextField pDemand = new JTextField(15);

    // Input fields (for scenarios)
    private JTextField sName = new JTextField(15);
    private JTextField sPrice = new JTextField(15);
    private JTextField sDemand = new JTextField(15);
    private JSpinner sCostSpinner;

    // Labels
    private JLabel selectedProductLabel = new JLabel("Selected Product: --");
    private JLabel bestScenarioLabel = new JLabel("🏆 Best Scenario: --");
    private JLabel riskLabel = new JLabel("⚠️ Risk Level: --");
    private JLabel avgProfitLabel = new JLabel("💰 Avg Profit: --");
    private JLabel recommendationLabel = new JLabel("💡 Recommendation: --");
    private JLabel totalProfitLabel = new JLabel("Total Profit: €0.00");
    private JLabel avgMarginLabel = new JLabel("Avg Margin: 0.00%");
    private JLabel bestProductLabel = new JLabel("Best Product: --");
    private JTextArea recommendationTextArea = new JTextArea();

    // Tabs and UI
    private JTabbedPane tabs = new JTabbedPane();
    private JProgressBar analysisProgress;
    private JPanel comparePanel;

    public DSSMainFrame() {
        initUI();
        refreshProductsFromDB(); // load products from database
        refreshScenariosFromDB(); // load scenarios from database
        updateProductStats();
    }

    private void initUI() {
        setTitle("Product Profit Scenario");
        setSize(1400, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton darkModeBtn = new JButton("🌙 Dark Mode");
        JButton runBtn = new JButton("▶ Run Analysis");
        JButton helpBtn = new JButton("❓ Help");
        JButton compareBtn = new JButton("📊 Compare Scenarios");
        toolBar.add(darkModeBtn);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(compareBtn);
        toolBar.add(runBtn);
        toolBar.add(helpBtn);
        add(toolBar, BorderLayout.NORTH);

        // ================= PRODUCTS TAB =================
        JPanel productPanel = new JPanel(new BorderLayout(10, 10));
        productPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Product Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Product Name:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(pName, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Price (€):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(pPrice, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Cost (€):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(pCost, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        inputPanel.add(new JLabel("Demand (units):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(pDemand, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addProductBtn = new JButton("Add Product");
        JButton updateProductBtn = new JButton("Update Selected");
        JButton deleteProductBtn = new JButton("Delete Selected");
        buttonPanel.add(addProductBtn);
        buttonPanel.add(updateProductBtn);
        buttonPanel.add(deleteProductBtn);
        addProductBtn.addActionListener(e -> addProduct());
        updateProductBtn.addActionListener(e -> updateProduct());
        deleteProductBtn.addActionListener(e -> deleteProduct());

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        inputPanel.add(buttonPanel, gbc);
        productPanel.add(inputPanel, BorderLayout.NORTH);

        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(new TitledBorder("Product List"));
        setupProductTable();
        tablePanel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        footerPanel.add(selectedProductLabel);
        tablePanel.add(footerPanel, BorderLayout.SOUTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        statsPanel.setBorder(new TitledBorder("Statistics"));
        statsPanel.add(totalProfitLabel);
        statsPanel.add(avgMarginLabel);
        statsPanel.add(bestProductLabel);

        productPanel.add(tablePanel, BorderLayout.CENTER);
        productPanel.add(statsPanel, BorderLayout.SOUTH);

        // Table selection listener
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = productTable.getSelectedRow();
                if (row >= 0) {
                    selectedProductLabel.setText("Selected Product: " + productModel.getValueAt(row, 1));
                } else {
                    selectedProductLabel.setText("Selected Product: --");
                }
            }
        });

        // ================= SCENARIOS TAB =================
        JPanel scenarioPanel = new JPanel(new BorderLayout(10, 10));
        scenarioPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel scenarioInputPanel = new JPanel(new GridBagLayout());
        scenarioInputPanel.setBorder(new TitledBorder("Scenario Configuration"));
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.insets = new Insets(5, 5, 5, 5);
        sgbc.fill = GridBagConstraints.HORIZONTAL;

        sgbc.gridx = 0;
        sgbc.gridy = 0;
        scenarioInputPanel.add(new JLabel("Scenario Name:"), sgbc);
        sgbc.gridx = 1;
        scenarioInputPanel.add(sName, sgbc);

        sgbc.gridx = 0;
        sgbc.gridy = 1;
        scenarioInputPanel.add(new JLabel("Price Change (%):"), sgbc);
        sgbc.gridx = 1;
        scenarioInputPanel.add(sPrice, sgbc);

        sgbc.gridx = 0;
        sgbc.gridy = 2;
        scenarioInputPanel.add(new JLabel("Cost Change (%):"), sgbc);
        sgbc.gridx = 1;
        sCostSpinner = new JSpinner(new SpinnerNumberModel(0, -100, 100, 5));
        scenarioInputPanel.add(sCostSpinner, sgbc);

        sgbc.gridx = 0;
        sgbc.gridy = 3;
        scenarioInputPanel.add(new JLabel("Demand Change (%):"), sgbc);
        sgbc.gridx = 1;
        scenarioInputPanel.add(sDemand, sgbc);

        JPanel scenarioButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addScenarioBtn = new JButton("Add Scenario");
        JButton deleteSelectedScenariosBtn = new JButton("Delete Selected"); // renamed & changed action
        scenarioButtonPanel.add(addScenarioBtn);
        scenarioButtonPanel.add(deleteSelectedScenariosBtn);
        sgbc.gridx = 0;
        sgbc.gridy = 4;
        sgbc.gridwidth = 2;
        scenarioInputPanel.add(scenarioButtonPanel, sgbc);

        scenarioPanel.add(scenarioInputPanel, BorderLayout.NORTH);

        JScrollPane scenarioScroll = new JScrollPane(scenarioTable);
        scenarioScroll.setBorder(new TitledBorder("Scenario List"));
        scenarioPanel.add(scenarioScroll, BorderLayout.CENTER);

        addScenarioBtn.addActionListener(e -> addScenario());
        deleteSelectedScenariosBtn.addActionListener(e -> deleteSelectedScenarios()); // new action

        // ================= RESULTS TAB =================
        JPanel resultPanel = new JPanel(new BorderLayout(10, 10));
        resultPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        resultsModel = new DefaultTableModel(new String[] { "Scenario", "Profit (€)", "Change (%)", "Risk" }, 0);
        JTable resultsTable = new JTable(resultsModel);
        analysisProgress = new JProgressBar();
        analysisProgress.setStringPainted(true);
        analysisProgress.setVisible(false);
        JPanel progressPanel = new JPanel(new BorderLayout());
        progressPanel.add(analysisProgress, BorderLayout.CENTER);
        resultPanel.add(progressPanel, BorderLayout.NORTH);
        resultPanel.add(new JScrollPane(resultsTable), BorderLayout.CENTER);

        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        summaryPanel.setBorder(new TitledBorder("Analysis Summary"));
        summaryPanel.add(bestScenarioLabel);
        summaryPanel.add(riskLabel);
        summaryPanel.add(avgProfitLabel);
        summaryPanel.add(recommendationLabel);
        resultPanel.add(summaryPanel, BorderLayout.SOUTH);

        // Recommendation engine panel
        recommendationTextArea = new JTextArea(10, 40);
        recommendationTextArea.setEditable(false);
        recommendationTextArea.setFont(new Font("Arial", Font.PLAIN, 12));
        recommendationTextArea.setBackground(new Color(240, 248, 255));
        recommendationTextArea.setLineWrap(true);
        recommendationTextArea.setWrapStyleWord(true);
        JScrollPane recScroll = new JScrollPane(recommendationTextArea);
        recScroll.setBorder(new TitledBorder("🤖 Recommendation Engine"));
        recScroll.setPreferredSize(new Dimension(800, 200));
        resultPanel.add(recScroll, BorderLayout.NORTH);

        // ================= COMPARE TAB =================
        comparePanel = new JPanel(new BorderLayout(10, 10));
        comparePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        comparePanel.add(new JLabel("Select two scenarios from the table and click 'Compare'", SwingConstants.CENTER),
                BorderLayout.NORTH);
        JTextArea compareArea = new JTextArea();
        compareArea.setEditable(false);
        compareArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        comparePanel.add(new JScrollPane(compareArea), BorderLayout.CENTER);

        // ================= TABS =================
        tabs.addTab("📦 Products", productPanel);
        tabs.addTab("🎯 Scenarios", scenarioPanel);
        tabs.addTab("📊 Results", resultPanel);
        tabs.addTab("🔍 Compare Scenarios", comparePanel);
        add(tabs, BorderLayout.CENTER);

        // Listeners
        darkModeBtn.addActionListener(e -> DarkModeManager.toggle(this));
        runBtn.addActionListener(e -> runAnalysis(resultsTable, compareArea));
        helpBtn.addActionListener(e -> showHelp());
        compareBtn.addActionListener(e -> compareScenarios(compareArea));

        // Table editing listeners - to update DB when user edits directly
        productModel.addTableModelListener(e -> {
            if (e.getColumn() >= 1 && e.getColumn() <= 4 && e.getFirstRow() >= 0) {
                int row = e.getFirstRow();
                int id = (int) productModel.getValueAt(row, 0);
                String name = (String) productModel.getValueAt(row, 1);
                double price = Double.parseDouble(productModel.getValueAt(row, 2).toString());
                double cost = Double.parseDouble(productModel.getValueAt(row, 3).toString());
                int demand = Integer.parseInt(productModel.getValueAt(row, 4).toString());
                Product p = new Product(id, name, price, cost, demand);
                DatabaseManager.getInstance().updateProduct(p);
                updateProductStats();
                // Refresh profit/margin in table
                productModel.setValueAt(currencyFormat.format(p.getProfit()), row, 5);
                productModel.setValueAt(String.format("%.2f%%", p.getMargin()), row, 6);
            }
        });

        scenarioModel.addTableModelListener(e -> {
            if (e.getColumn() == 1 && e.getFirstRow() >= 0) {
                int row = e.getFirstRow();
                int id = (int) scenarioModel.getValueAt(row, 0);
                String newName = (String) scenarioModel.getValueAt(row, 1);
                for (Scenario s : scenarios) {
                    if (s.getId() == id) {
                        s.setName(newName);
                        DatabaseManager.getInstance().updateScenario(s);
                        break;
                    }
                }
                refreshScenariosFromDB(); // reload to keep impact column consistent
            }
        });

        setupProductTable();
        setupScenarioTable();
    }

    private void setupProductTable() {
        productTable.setRowHeight(25);
        productTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        productTable.getColumnModel().getColumn(0).setMinWidth(0);
        productTable.getColumnModel().getColumn(0).setMaxWidth(0); // hide ID column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i <= 6; i++) {
            productTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
    }

    private void setupScenarioTable() {
        scenarioTable.setRowHeight(25);
        scenarioTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        scenarioTable.getColumnModel().getColumn(0).setMinWidth(0);
        scenarioTable.getColumnModel().getColumn(0).setMaxWidth(0); // hide ID column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 2; i <= 4; i++) {
            scenarioTable.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        }
        scenarioTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    }

    private void refreshProductsFromDB() {
        productModel.setRowCount(0);
        List<Product> products = DatabaseManager.getInstance().loadProducts();
        for (Product p : products) {
            productModel.addRow(new Object[] {
                    p.getId(),
                    p.getName(),
                    p.getPrice(),
                    p.getCost(),
                    p.getDemand(),
                    currencyFormat.format(p.getProfit()),
                    String.format("%.2f%%", p.getMargin())
            });
        }
        updateProductStats();
    }

    private void refreshScenariosFromDB() {
        scenarioModel.setRowCount(0);
        scenarios.clear();
        List<Scenario> scenarioList = DatabaseManager.getInstance().loadScenarios();
        for (Scenario s : scenarioList) {
            scenarios.add(s);
            double impact = (s.getPriceChange() - s.getCostChange() + s.getDemandChange()) / 2.0;
            scenarioModel.addRow(new Object[] {
                    s.getId(),
                    s.getName(),
                    String.format("%+.1f%%", s.getPriceChange()),
                    String.format("%+.1f%%", s.getCostChange()),
                    String.format("%+.1f%%", s.getDemandChange()),
                    String.format("%+.1f%%", impact)
            });
        }
    }

    private void updateProductStats() {
        if (productModel.getRowCount() == 0) {
            totalProfitLabel.setText("Total Profit: €0.00");
            avgMarginLabel.setText("Avg Margin: 0.00%");
            bestProductLabel.setText("Best Product: --");
            return;
        }
        double totalProfit = 0;
        double totalMargin = 0;
        String bestName = "";
        double bestProfit = 0;
        for (int i = 0; i < productModel.getRowCount(); i++) {
            try {
                String profitStr = productModel.getValueAt(i, 5).toString().replace("€", "").replace(",", "");
                double profit = Double.parseDouble(profitStr);
                String marginStr = productModel.getValueAt(i, 6).toString().replace("%", "");
                double margin = Double.parseDouble(marginStr);
                totalProfit += profit;
                totalMargin += margin;
                if (profit > bestProfit) {
                    bestProfit = profit;
                    bestName = productModel.getValueAt(i, 1).toString();
                }
            } catch (Exception e) {
            }
        }
        totalProfitLabel.setText("Total Profit: " + currencyFormat.format(totalProfit));
        avgMarginLabel.setText(String.format("Avg Margin: %.2f%%", totalMargin / productModel.getRowCount()));
        bestProductLabel.setText("Best Product: " + bestName);
    }

    private void addProduct() {
        try {
            String name = pName.getText().trim();
            if (name.isEmpty())
                throw new Exception();
            double price = Double.parseDouble(pPrice.getText().trim());
            double cost = Double.parseDouble(pCost.getText().trim());
            int demand = Integer.parseInt(pDemand.getText().trim());
            Product product = new Product(name, price, cost, demand);
            DatabaseManager.getInstance().insertProduct(product);
            refreshProductsFromDB();
            clearProductInputs();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please check values.");
        }
    }

    private void updateProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to update.");
            return;
        }
        try {
            int id = (int) productModel.getValueAt(row, 0);
            String name = pName.getText().trim();
            if (name.isEmpty())
                name = (String) productModel.getValueAt(row, 1);
            double price = pPrice.getText().trim().isEmpty()
                    ? Double.parseDouble(productModel.getValueAt(row, 2).toString())
                    : Double.parseDouble(pPrice.getText().trim());
            double cost = pCost.getText().trim().isEmpty()
                    ? Double.parseDouble(productModel.getValueAt(row, 3).toString())
                    : Double.parseDouble(pCost.getText().trim());
            int demand = pDemand.getText().trim().isEmpty()
                    ? Integer.parseInt(productModel.getValueAt(row, 4).toString())
                    : Integer.parseInt(pDemand.getText().trim());
            Product product = new Product(id, name, price, cost, demand);
            DatabaseManager.getInstance().updateProduct(product);
            refreshProductsFromDB();
            clearProductInputs();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating product.");
        }
    }

    private void deleteProduct() {
        int row = productTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a product to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Delete selected product?", "Confirm",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            int id = (int) productModel.getValueAt(row, 0);
            DatabaseManager.getInstance().deleteProduct(id);
            refreshProductsFromDB();
        }
    }

    private void clearProductInputs() {
        pName.setText("");
        pPrice.setText("");
        pCost.setText("");
        pDemand.setText("");
    }

    private void addScenario() {
        try {
            String name = sName.getText().trim();
            double priceChange = Double.parseDouble(sPrice.getText().trim());
            double costChange = ((Number) sCostSpinner.getValue()).doubleValue();
            double demandChange = Double.parseDouble(sDemand.getText().trim());
            Scenario scenario = new Scenario(name, priceChange, costChange, demandChange);
            DatabaseManager.getInstance().insertScenario(scenario);
            refreshScenariosFromDB();
            clearScenarioInputs();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid scenario input.");
        }
    }

    // NEW: Delete only selected scenarios (multiple allowed)
    private void deleteSelectedScenarios() {
        int[] selectedRows = scenarioTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Select one or more scenarios to delete.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete " + selectedRows.length + " selected scenario(s)?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            for (int row : selectedRows) {
                int id = (int) scenarioModel.getValueAt(row, 0);
                DatabaseManager.getInstance().deleteScenario(id);
            }
            refreshScenariosFromDB();
        }
    }

    private void clearScenarioInputs() {
        sName.setText("");
        sPrice.setText("");
        sDemand.setText("");
        sCostSpinner.setValue(0);
    }

    private void compareScenarios(JTextArea compareArea) {
        int[] selectedRows = scenarioTable.getSelectedRows();
        if (selectedRows.length < 2) {
            JOptionPane.showMessageDialog(this, "Please select at least two scenarios from the Scenarios tab.",
                    "Compare", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        if (selectedRows.length > 2) {
            JOptionPane.showMessageDialog(this, "Please select only two scenarios for side‑by‑side comparison.",
                    "Compare", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Scenario s1 = scenarios.get(selectedRows[0]);
        Scenario s2 = scenarios.get(selectedRows[1]);

        int prodRow = productTable.getSelectedRow();
        if (prodRow < 0 && productModel.getRowCount() > 0)
            prodRow = 0;
        if (prodRow < 0) {
            JOptionPane.showMessageDialog(this, "No product available. Please add a product first.");
            return;
        }
        String prodName = (String) productModel.getValueAt(prodRow, 1);
        double price = Double.parseDouble(productModel.getValueAt(prodRow, 2).toString());
        double cost = Double.parseDouble(productModel.getValueAt(prodRow, 3).toString());
        int demand = Integer.parseInt(productModel.getValueAt(prodRow, 4).toString());
        Product product = new Product(prodName, price, cost, demand);

        double profitBase = engine.calculateBaseProfit(product);
        double profit1 = engine.calculateScenarioProfit(product, s1);
        double profit2 = engine.calculateScenarioProfit(product, s2);
        double change1 = (profit1 - profitBase) / profitBase * 100;
        double change2 = (profit2 - profitBase) / profitBase * 100;
        String risk1 = profit1 < 50000 ? "High" : (profit1 < 150000 ? "Medium" : "Low");
        String risk2 = profit2 < 50000 ? "High" : (profit2 < 150000 ? "Medium" : "Low");

        StringBuilder sb = new StringBuilder();
        sb.append("Side‑by‑Side Scenario Comparison\n");
        sb.append("Product: ").append(prodName).append("\n\n");
        sb.append(String.format("%-20s %-30s %-30s\n", "Metric", s1.getName(), s2.getName()));
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-20s %-30s %-30s\n", "Profit", currencyFormat.format(profit1),
                currencyFormat.format(profit2)));
        sb.append(String.format("%-20s %-30s %-30s\n", "Change from Base", String.format("%+.1f%%", change1),
                String.format("%+.1f%%", change2)));
        sb.append(String.format("%-20s %-30s %-30s\n", "Risk Level", risk1, risk2));
        sb.append(String.format("%-20s %-30s %-30s\n", "Price Change", String.format("%+.1f%%", s1.getPriceChange()),
                String.format("%+.1f%%", s2.getPriceChange())));
        sb.append(String.format("%-20s %-30s %-30s\n", "Cost Change", String.format("%+.1f%%", s1.getCostChange()),
                String.format("%+.1f%%", s2.getCostChange())));
        sb.append(String.format("%-20s %-30s %-30s\n", "Demand Change", String.format("%+.1f%%", s1.getDemandChange()),
                String.format("%+.1f%%", s2.getDemandChange())));
        sb.append("\n💡 Recommendation: ");
        if (profit1 > profit2) {
            sb.append("Scenario '").append(s1.getName()).append("' yields higher profit. ");
            if (risk1.equals("Low") && risk2.equals("High"))
                sb.append("It is also much safer.");
            else if (risk1.equals("High"))
                sb.append("But it carries higher risk – consider mitigating actions.");
        } else {
            sb.append("Scenario '").append(s2.getName()).append("' yields higher profit. ");
            if (risk2.equals("Low") && risk1.equals("High"))
                sb.append("It is also much safer.");
            else if (risk2.equals("High"))
                sb.append("But it carries higher risk – consider mitigating actions.");
        }
        compareArea.setText(sb.toString());
        tabs.setSelectedIndex(3);
    }

    private void runAnalysis(JTable resultsTable, JTextArea compareArea) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a product first.");
            return;
        }
        if (scenarios.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No scenarios to analyze.");
            return;
        }

        analysisProgress.setVisible(true);
        analysisProgress.setValue(0);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                analysisProgress.setValue(20);
                Thread.sleep(300);

                resultsModel.setRowCount(0);
                results.clear();

                String prodName = (String) productModel.getValueAt(selectedRow, 1);
                double price = Double.parseDouble(productModel.getValueAt(selectedRow, 2).toString());
                double cost = Double.parseDouble(productModel.getValueAt(selectedRow, 3).toString());
                int demand = Integer.parseInt(productModel.getValueAt(selectedRow, 4).toString());
                double baseProfit = (price - cost) * demand;
                Product currentProduct = new Product(prodName, price, cost, demand);

                analysisProgress.setValue(40);
                ScenarioResult best = null;

                for (int i = 0; i < scenarios.size(); i++) {
                    Scenario s = scenarios.get(i);
                    double profit = engine.calculateScenarioProfit(currentProduct, s);
                    double change = ((profit - baseProfit) / baseProfit) * 100;
                    double ratio = profit / baseProfit;
                    String risk;
                    if (ratio < 1.0) {
                        risk = "High Risk";
                    } else if (ratio <= 1.2) {
                        risk = "Medium Risk";
                    } else {
                        risk = "Low Risk";
                    }
                    resultsModel.addRow(new Object[] { s.getName(),
                            currencyFormat.format(profit),
                            String.format("%+.1f%%", change),
                            risk });
                    results.add(new ScenarioResult(s.getName(), profit));
                    if (best == null || profit > best.getProfit())
                        best = new ScenarioResult(s.getName(), profit);
                    analysisProgress.setValue(40 + (int) ((i + 1) * 30.0 / scenarios.size()));
                }

                analysisProgress.setValue(80);
                if (best != null) {
                    bestScenarioLabel.setText("🏆 Best Scenario: " + best.getName());
                    double avgProfit = results.stream().mapToDouble(ScenarioResult::getProfit).average().orElse(0);
                    String overallRisk = riskAnalyzer.getRiskLevel(baseProfit, results);
                    riskLabel.setText("⚠️ Risk Level: " + overallRisk);
                    avgProfitLabel.setText("💰 Avg Profit: " + currencyFormat.format(avgProfit));

                    // Recommendation engine
                    StringBuilder recBuilder = new StringBuilder();
                    recBuilder.append("Based on the analysis:\n\n");
                    recBuilder.append("✓ Best scenario: ").append(best.getName()).append("\n");

                    Scenario bestScenario = null;
                    for (Scenario s : scenarios) {
                        if (s.getName().equals(best.getName())) {
                            bestScenario = s;
                            break;
                        }
                    }
                    if (bestScenario != null) {
                        double profitBest = best.getProfit();
                        if (profitBest > baseProfit) {
                            recBuilder.append("✓ Profit improvement of ")
                                    .append(String.format("%.1f%%", (profitBest - baseProfit) / baseProfit * 100))
                                    .append(" is achievable.\n");
                        } else {
                            recBuilder.append("⚠️ This scenario reduces profit. Consider revising.\n");
                        }
                        double priceEffect = bestScenario.getPriceChange();
                        double costEffect = -bestScenario.getCostChange();
                        double demandEffect = bestScenario.getDemandChange();
                        double maxEffect = Math.max(Math.abs(priceEffect),
                                Math.max(Math.abs(costEffect), Math.abs(demandEffect)));
                        String mainDriver = "";
                        if (Math.abs(priceEffect) == maxEffect)
                            mainDriver = "price";
                        else if (Math.abs(costEffect) == maxEffect)
                            mainDriver = "cost";
                        else
                            mainDriver = "demand";
                        recBuilder.append("✓ The most influential factor is ").append(mainDriver)
                                .append(" (change of ").append(String.format("%+.1f%%", maxEffect)).append(").\n");

                        if (priceEffect > 0 && profitBest > baseProfit) {
                            recBuilder.append("💰 Suggestion: Increase price by ")
                                    .append(String.format("%.0f%%", priceEffect)).append(" to boost profit.\n");
                        } else if (priceEffect < 0 && profitBest > baseProfit) {
                            recBuilder.append(
                                    "💰 Suggestion: Lower price to drive demand – this scenario shows profit increase.\n");
                        }
                        if (costEffect > 0 && profitBest > baseProfit) {
                            recBuilder.append("🛠️ Reducing cost by ").append(String.format("%.0f%%", costEffect))
                                    .append(" improves profit.\n");
                        }
                        if (demandEffect > 0 && profitBest > baseProfit) {
                            recBuilder.append("📈 Increasing demand by ").append(String.format("%.0f%%", demandEffect))
                                    .append(" would be beneficial.\n");
                        }
                    }
                    if (overallRisk.equals("High")) {
                        recBuilder.append("\n⚠️ OVERALL RISK IS HIGH. Consider diversifying or reducing exposure.\n");
                    } else if (overallRisk.equals("Medium")) {
                        recBuilder.append("\n⚖️ Medium risk – acceptable, but monitor closely.\n");
                    } else {
                        recBuilder.append("\n✅ Low risk – safe to proceed with the best scenario.\n");
                    }
                    recBuilder.append("\n✨ Recommendation: ")
                            .append(recommendationLabel.getText().replace("💡 Recommendation: ", ""));
                    recommendationTextArea.setText(recBuilder.toString());
                }

                analysisProgress.setValue(100);
                Thread.sleep(300);
                return null;
            }

            @Override
            protected void done() {
                analysisProgress.setVisible(false);
                tabs.setSelectedIndex(2);
            }
        };
        worker.execute();
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this,
                "Decision Support System Help\n\n" +
                        "1. Products Tab: Add, edit, delete products. Select a product.\n" +
                        "2. Scenarios Tab: Create what-if scenarios with % changes.\n" +
                        "   - Delete Selected: Delete only the scenarios you highlight.\n" +
                        "3. Results Tab: Run analysis, compare profits, see risk.\n" +
                        "4. Compare Scenarios: Select two scenarios (Ctrl+click) and click the toolbar button.\n" +
                        "5. Dark mode toggle available.\n" +
                        "6. Recommendation engine gives actionable advice.\n\n" +
                        "All data is automatically saved to an embedded H2 database (no manual save needed).",
                "Help", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);
            if (login.isSucceeded()) {
                new DSSMainFrame().setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}