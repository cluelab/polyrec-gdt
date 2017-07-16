package it.unisa.di.weblab.polyrec.gdt;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultRowSorter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.*;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.*;

public class Chart extends JFrame implements ItemListener {

	private JTable rankingTable;
	private String title;
	private int columnIndex;
	private int secondaryColumnIndex;
	private int type;
	private SortOrder sortOrder;
	private Choice featureChoice = new Choice();

	private ChartPanel chartPanel;
	private Choice orderChoice;
	private Choice typeChoice;
	private int columnLabels;
	private Choice secondaryFeatureChoice= new Choice(); 

	static final int LINE = 1;
	static final int BAR = 0;

	public Chart(JTable rankingTable, String title, int columnIndex, int secondaryColumnIndex, int columnLabels,
			int type, SortOrder sortOrder) {
		System.out.println("column labels"+columnLabels);
		this.rankingTable = rankingTable;
		this.title = title;
		this.columnIndex = columnIndex;
		this.secondaryColumnIndex = secondaryColumnIndex;
		this.type = type;
		this.sortOrder = sortOrder;
		this.columnLabels = columnLabels;
		initUI();
		

	}

	private void initUI() {

		setLayout(new BorderLayout());
		CategoryDataset dataset = createDataset(this.columnIndex, true);
	
		JFreeChart chart;
	
		if (secondaryFeatureChoice.getSelectedItem()!=null && secondaryFeatureChoice.getSelectedItem() != " - ") {
			CategoryDataset dataset2 = createDataset(this.secondaryColumnIndex, false);

			System.out.println("righe del dataset" + dataset2.getRowCount());

			chart = createChart(dataset, dataset2);
		} else
			chart = createChart(dataset, null);

	

		chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);
		add(chartPanel, BorderLayout.CENTER);

		JPanel northPanel = new JPanel(new FlowLayout());
	

		for (int i = columnLabels; i < rankingTable.getModel().getColumnCount(); i++) {
			featureChoice.add(rankingTable.getModel().getColumnName(i));

		}
		featureChoice.select(this.columnIndex - columnLabels);
		featureChoice.addItemListener(this);
		northPanel.add(new JLabel("Primary:"));
		northPanel.add(featureChoice);

		typeChoice = new Choice();
		typeChoice.add("Bar Chart");
		typeChoice.add("Line Chart");
		typeChoice.select(this.type);

		typeChoice.addItemListener(this);
		northPanel.add(typeChoice);

		orderChoice = new Choice();

		orderChoice.add("Discending");
		orderChoice.add("Ascending");
		orderChoice.add("Unsorted");
		if (sortOrder == SortOrder.DESCENDING)

			orderChoice.select(0);
		if (sortOrder == SortOrder.ASCENDING)

			orderChoice.select(1);
		if (sortOrder == SortOrder.UNSORTED)

			orderChoice.select(2);
		orderChoice.addItemListener(this);
		northPanel.add(orderChoice);

	

		
		for (int i = columnLabels; i < rankingTable.getModel().getColumnCount(); i++) {
			secondaryFeatureChoice.add(rankingTable.getModel().getColumnName(i));

		}
		secondaryFeatureChoice.add(" - ");

		secondaryFeatureChoice.select(this.secondaryColumnIndex - columnLabels);
		secondaryFeatureChoice.addItemListener(this);
		
		northPanel.add(new JLabel("Secondary:"));
		northPanel.add(secondaryFeatureChoice);

		add(northPanel, BorderLayout.NORTH);

		pack();
		setTitle("Chart");
		setLocationRelativeTo(null);

	}

	private CategoryDataset createDataset(int columnIndex, boolean setOrder) {

		// ordina dati
		if (setOrder) {
			System.out.println("ordina dati");
			rankingTable.setAutoCreateRowSorter(true);
			DefaultRowSorter sorter = ((DefaultRowSorter) rankingTable.getRowSorter());
			ArrayList list = new ArrayList();

			list.add(new RowSorter.SortKey(columnIndex, this.sortOrder));
			sorter.setSortKeys(list);
			sorter.sort();
		}
		System.out.println("create dataset (column index: " + columnIndex + ")");
		final DefaultCategoryDataset dataset = new DefaultCategoryDataset();

		for (int i = 0; i < rankingTable.getRowCount(); i++) {

			if (rankingTable.getValueAt(i, columnIndex) instanceof Double)
				dataset.addValue((Double) rankingTable.getValueAt(i, columnIndex),
						rankingTable.getModel().getColumnName(columnIndex),
						(String) rankingTable.getValueAt(i, 0) + rankingTable.getValueAt(i, 1));
			else if (rankingTable.getValueAt(i, columnIndex) instanceof Integer)
				dataset.addValue((Integer) rankingTable.getValueAt(i, columnIndex),
						rankingTable.getModel().getColumnName(columnIndex),
						(String) rankingTable.getValueAt(i, 0) + rankingTable.getValueAt(i, 1));
			else

				dataset.addValue(Double.valueOf((String) rankingTable.getValueAt(i, columnIndex)),
						rankingTable.getModel().getColumnName(columnIndex), (String) rankingTable.getValueAt(i, 0));

		}

		return dataset;
	}

	private JFreeChart createChart(final CategoryDataset dataset1, final CategoryDataset dataset2) {
		System.out.println("dataset 2="+dataset2);
		JFreeChart chart;
		if (this.type == LINE)
			chart = ChartFactory.createLineChart(title, "Template", rankingTable.getModel().getColumnName(columnIndex),
					dataset1, PlotOrientation.VERTICAL, true, true, false);
		else
			chart = ChartFactory.createBarChart(title, "Template", rankingTable.getModel().getColumnName(columnIndex),
					dataset1, PlotOrientation.VERTICAL, true, true, false);

		// NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
		chart.setBackgroundPaint(Color.white);
		// chart.getLegend().setAnchor(Legend.SOUTH);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		((CategoryAxis) plot.getDomainAxis()).setTickLabelFont(new Font("Arial", Font.PLAIN, 16));
		((CategoryAxis) plot.getDomainAxis()).setLabelFont(new Font("Arial", Font.PLAIN, 20));
		((ValueAxis) plot.getRangeAxis()).setTickLabelFont(new Font("Arial", Font.PLAIN, 14));
		((ValueAxis) plot.getRangeAxis()).setLabelFont(new Font("Arial", Font.PLAIN, 20));

		if (this.type == BAR)
			((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
		else {
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setBaseShapesVisible(true);
		}

		plot.setDataset(1, dataset2);
		plot.mapDatasetToRangeAxis(1, 1);

		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_45);
		if (dataset2 != null) {

			final ValueAxis axis2 = new NumberAxis(rankingTable.getModel().getColumnName(this.secondaryColumnIndex));
			axis2.setTickLabelFont(new Font("Arial", Font.PLAIN, 14));
			axis2.setLabelFont(new Font("Arial", Font.PLAIN, 20));
			plot.setRangeAxis(1, axis2);

			final LineAndShapeRenderer renderer2 = new LineAndShapeRenderer();
			renderer2.setSeriesShapesVisible(1, true);
			// renderer2.setToolTipGenerator(new
			// StandardCategoryToolTipGenerator());

			plot.setRenderer(1, renderer2);
			plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
		}
		return chart;
	}

	// con singolo dataset (non usato)
	private JFreeChart createChart(final CategoryDataset dataset) {
		JFreeChart chart;
		if (this.type == LINE)
			chart = ChartFactory.createLineChart(title, "Template", rankingTable.getModel().getColumnName(columnIndex),
					dataset, PlotOrientation.VERTICAL, true, true, false);
		else
			chart = ChartFactory.createBarChart(title, "Template", rankingTable.getModel().getColumnName(columnIndex),
					dataset, PlotOrientation.VERTICAL, true, true, false);

		chart.setBackgroundPaint(Color.white);

		// get a reference to the plot for further customisation...
		final CategoryPlot plot = chart.getCategoryPlot();
		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		((CategoryAxis) plot.getDomainAxis()).setTickLabelFont(new Font("Arial", Font.PLAIN, 16));
		((CategoryAxis) plot.getDomainAxis()).setLabelFont(new Font("Arial", Font.PLAIN, 20));
		((ValueAxis) plot.getRangeAxis()).setTickLabelFont(new Font("Arial", Font.PLAIN, 16));
		((ValueAxis) plot.getRangeAxis()).setLabelFont(new Font("Arial", Font.PLAIN, 20));

		if (this.type == BAR)
			((BarRenderer) plot.getRenderer()).setBarPainter(new StandardBarPainter());
		else {
			LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
			renderer.setBaseShapesVisible(true);
		}
		final CategoryAxis domainAxis = plot.getDomainAxis();
		domainAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0));

		return chart;
	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		if (e.getSource() == featureChoice) {

			this.columnIndex = ((Choice) e.getSource()).getSelectedIndex() + columnLabels;

		}

		if (e.getSource() == secondaryFeatureChoice) {

			this.secondaryColumnIndex = ((Choice) e.getSource()).getSelectedIndex() + columnLabels;
			System.out.println("seconday" + this.secondaryColumnIndex);
		}

		if (e.getSource() == orderChoice) {
			if (orderChoice.getSelectedIndex() == 0)
				this.sortOrder = SortOrder.DESCENDING;
			if (orderChoice.getSelectedIndex() == 1)
				this.sortOrder = SortOrder.ASCENDING;
			if (orderChoice.getSelectedIndex() == 2)
				this.sortOrder = SortOrder.UNSORTED;

		}
		if (e.getSource() == typeChoice) {
			this.type = typeChoice.getSelectedIndex();

		}
		remove(chartPanel);
		CategoryDataset dataset = createDataset(this.columnIndex, true);
	
		JFreeChart chart;
		System.out.println("secondary index"+this.secondaryColumnIndex);
		if (secondaryFeatureChoice.getSelectedItem() != " - ") {
			CategoryDataset dataset2 = createDataset(this.secondaryColumnIndex, false);

			System.out.println("righe del dataset" + dataset2.getRowCount());

			chart = createChart(dataset, dataset2);
		} else
			chart = createChart(dataset, null);

		chartPanel = new ChartPanel(chart);
		chartPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		chartPanel.setBackground(Color.white);
		add(chartPanel, BorderLayout.CENTER);
		validate();
		repaint();

	}

}