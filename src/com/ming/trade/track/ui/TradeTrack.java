package com.ming.trade.track.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTextField;

import com.ming.trade.track.engine.*;
import com.ming.trade.track.model.Callback;
import com.ming.trade.track.model.GroupTrade;
import com.ming.trade.track.model.Stock;
import com.ming.trade.track.model.Trade;
import com.ming.trade.track.util.FileUtil;
import com.ming.trade.track.util.StringHelper;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JProgressBar;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class TradeTrack {
	private static final Log LOG = LogFactory.getLog(TradeTrack.class);
	
	private TradeFetcherService service=null;
	
	private JFrame frame;
	private JLabel label;
	private JTextField textFieldCode;
	private JLabel label_1;
	private JTextField textFieldDate;
	private JButton button;
	
	private JTable tableTrades;
	private JScrollPane scrollPaneTrades;
	private Vector rowsTrades=null;
	private Vector colsTrades=null;
	
	private JTable tableStocks;
	private JScrollPane scrollPaneStocks;
	private Vector rowsStocks=null;
	private Vector colsStocks=null;
	
	private Thread thread = null;
	private List<GroupTrade> groupTrades=new ArrayList<GroupTrade>();
	
	private static final DateFormat df = new SimpleDateFormat("HH:mm:ss"); 
	
	private static final DateFormat datedf=new SimpleDateFormat("yyyy-MM-dd");
	
	private JProgressBar progressBar;
	private JMenuBar menuBar;
	private JMenu menu;
	private JMenuItem menuItem;
	private JMenuItem menuItem_1;
	private JMenu menu_1;
	private JMenuItem menuItem_2;
	private JMenuItem menuItem_3;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TradeTrack window = new TradeTrack();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public TradeTrack() {
		initialize();
		service=new TradeFetcherService(new Callback<GroupTrade>(){
			public synchronized void invoke(GroupTrade gt) {
				if(gt!=null && gt.getStock().isBulkGap()){
					groupTrades.add(gt);
					updateStocks(gt.getStock());
				}
				progressBar.setValue(progressBar.getValue()+1);
			}
		},new Callback<Object>(){
			public void invoke(Object o) {
				compReset();
			}
		});
	}
	
	private void compReset(){
		textFieldCode.setEditable(true);
		textFieldDate.setEditable(true);
		button.setText("查询");
	}
	private void compLock(){
		textFieldCode.setEditable(false);
		textFieldDate.setEditable(false);
		button.setText("停止");
	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JToolBar toolBar = new JToolBar();
		frame.getContentPane().add(toolBar, BorderLayout.NORTH);
		
		button = new JButton("查询");
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Date date = datedf.parse(textFieldDate.getText());
					if(date.after(new Date())){
						JOptionPane.showMessageDialog(frame, "日期超过当天");
						return ;
					}
				} catch (ParseException e1) {
					LOG.error(e1);
					JOptionPane.showMessageDialog(frame, "日期输入错误");
					return ;
				}
				
				if(!service.isPause()){
					int choice = JOptionPane.showConfirmDialog(frame, "确认关闭?", "提示", JOptionPane.OK_CANCEL_OPTION );
					if(choice == 0){
						service.pause();
						compReset();
					}
				}else{
					compLock();
					String code = textFieldCode.getText();
					String date = textFieldDate.getText();
					rowsStocks.clear();
					tableStocks.updateUI();
					groupTrades.clear();
					
					List<Stock> stocks = StockFetcher.getStocks2();
					
					progressBar.setMaximum(stocks.size());
					progressBar.setValue(0);
					progressBar.setIndeterminate(false);
					
					if(StringHelper.isNotEmpty(code)){
						service.add(date, Stock.getStockByCode(stocks, code));
					}else{
						service.addAll(date, stocks);
					}
				}
				
			}
		});
		
		label = new JLabel("\u4EE3\u7801");
		toolBar.add(label);
		
		textFieldCode = new JTextField("");
		toolBar.add(textFieldCode);
		textFieldCode.setColumns(10);
		
		label_1 = new JLabel("\u65E5\u671F");
		toolBar.add(label_1);
		
		
		textFieldDate = new JTextField(datedf.format(new Date()));
		toolBar.add(textFieldDate);
		textFieldDate.setColumns(10);
		toolBar.add(button);
		
		progressBar = new JProgressBar();
		toolBar.add(progressBar);
		
		menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		menu = new JMenu("\u6587\u4EF6");
		menuBar.add(menu);
		
		menuItem = new JMenuItem("\u4FDD\u5B58");
		menuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(groupTrades.size()==0) return;
				JFileChooser   chooser   =   new   JFileChooser(FileUtil.saveDir());
				chooser.setSelectedFile(new File(FileUtil.saveFileName()));
				chooser.setAcceptAllFileFilterUsed(false);
				int result = chooser.showSaveDialog(frame);
				if(result == JFileChooser.APPROVE_OPTION)
				{
					File savefile = chooser.getSelectedFile();
					AnalysisHis.save(groupTrades,savefile);
				}
			}
		});
		menu.add(menuItem);
		
		menuItem_1 = new JMenuItem("\u6253\u5F00");
		menuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser   chooser   =   new   JFileChooser(FileUtil.saveDir());
				chooser.setAcceptAllFileFilterUsed(false);
				int   result   =   chooser.showOpenDialog(frame); 
				if(result == JFileChooser.APPROVE_OPTION)
				{
					File savefile = chooser.getSelectedFile();
					//groupTrades.clear();
					groupTrades = (AnalysisHis.open(savefile));
					rowsStocks.clear();
					tableStocks.updateUI();
					for(int i=0;i<groupTrades.size();i++){
						updateStocks(groupTrades.get(i).getStock());
					}
					
				}
			}
		});
		menu.add(menuItem_1);
		
		menuItem_3 = new JMenuItem("\u6253\u5F00\u5E76\u91CD\u5206\u6790");
		menuItem_3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser   chooser   =   new   JFileChooser(FileUtil.saveDir());
				chooser.setAcceptAllFileFilterUsed(false);
				int   result   =   chooser.showOpenDialog(frame); 
				if(result == JFileChooser.APPROVE_OPTION)
				{
					File savefile = chooser.getSelectedFile();
					//groupTrades.clear();
					groupTrades = (AnalysisHis.openReanaly(savefile));
					rowsStocks.clear();
					tableStocks.updateUI();
					for(int i=0;i<groupTrades.size();i++){
						updateStocks(groupTrades.get(i).getStock());
					}
					
				}
			}
		});
		menu.add(menuItem_3);
		
		menu_1 = new JMenu("\u9009\u9879");
		menuBar.add(menu_1);
		
		menuItem_2 = new JMenuItem("\u53C2\u6570\u8BBE\u7F6E");
		menuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
		menu_1.add(menuItem_2);
		
		initTableStocks();
		initTableTrades();
	}
	private void initTableTrades(){
		tableTrades = new JTable(){
			public   boolean   isCellEditable(int   iRow,   int   iCol)   { 
				return false;
            } 
		};
		//tableTrades.setSize(frame.getWidth()/3*2, frame.getHeight());
		scrollPaneTrades = new JScrollPane(tableTrades);
		//frame.getContentPane().add(scrollPane, BorderLayout.WEST);
		rowsTrades=new Vector();
		colsTrades=new Vector();
		String [] colsname = new String[] {
				"\u65F6\u95F4", "\u4EF7\u683C", "\u6570\u91CF(\u624B)", "\u91D1\u989D", "\u7C7B\u578B","groupid"
			};
		for(int i=0;i<colsname.length;i++){
			colsTrades.add(colsname[i]);
		}
		tableTrades.setModel(new DefaultTableModel(
			rowsTrades,
			colsTrades
		) {
			Class[] columnTypes = new Class[] {
				String.class, Double.class, Integer.class, Double.class, String.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		tableTrades.getColumnModel().getColumn(0).setPreferredWidth(91);
		tableTrades.getColumnModel().getColumn(1).setPreferredWidth(112);
		tableTrades.getColumnModel().getColumn(2).setPreferredWidth(117);
		tableTrades.getColumnModel().getColumn(3).setPreferredWidth(169);
		tableTrades.getColumnModel().getColumn(5).setMaxWidth(0);
		tableTrades.getColumnModel().getColumn(5).setMinWidth(0);

		DefaultTableCellRenderer cellrender = new DefaultTableCellRenderer(){

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				// TODO Auto-generated method stub
				int groupid =(Integer) table.getModel().getValueAt(row, 5);
				
				if(groupid%2==0){
					setBackground(Color.WHITE);
				}else{
					setBackground(Color.GRAY);
				}
				return super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
			}
			
		};
		//cellrender.setBackground(Color.RED);
		for(int i=0;i<tableTrades.getColumnModel().getColumnCount();i++){
			tableTrades.getColumnModel().getColumn(i).setCellRenderer(cellrender);
		}
		
		//tableTrades.setEnabled(false);
		tableTrades.setRowSelectionAllowed(true);
		//tableTrades.
		frame.getContentPane().add(scrollPaneTrades, BorderLayout.CENTER);
	}
	private void initTableStocks(){
		tableStocks = new JTable(){
			public   boolean   isCellEditable(int   iRow,   int   iCol)   { 
				String colname = this.getModel().getColumnName(iCol);
				//System.out.println(colname);
				if(colname.equals("价差") || colname.equals("大量")){ 
					return  true ; 
				}else {
					return false;
				}
            } 
		};
		//tableStocks.setDragEnabled(false);
		//tableStocks.setSize(frame.getWidth()/3, frame.getHeight());
		scrollPaneStocks = new JScrollPane(tableStocks);
		//frame.getContentPane().add(scrollPane, BorderLayout.WEST);
		rowsStocks=new Vector();
		colsStocks=new Vector();
		String [] colsname = new String[] {
				"代码", "名称","市盈","价格","价差","大量"
			};
		for(int i=0;i<colsname.length;i++){
			colsStocks.add(colsname[i]);
		}
		tableStocks.setModel(new DefaultTableModel(
			rowsStocks,
			colsStocks
		) {
			Class[] columnTypes = new Class[] {
				String.class,  String.class,Double.class,Double.class,Double.class,Double.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
		tableStocks.setRowSelectionAllowed(true);
		tableStocks.getModel().addTableModelListener(new TableModelListener(){
			public void tableChanged(TableModelEvent e) {
				int row = tableStocks.getSelectedRow();
		        String code = (String)tableStocks.getModel().getValueAt(row, 0);
		        double bg=(Double)tableStocks.getModel().getValueAt(row, 4);
		        double bq=(Double)tableStocks.getModel().getValueAt(row, 5);
		        for(int i=0;i<groupTrades.size();i++){
		        	if(groupTrades.get(i).getStock().getCode().equals(code)){
		        		groupTrades.get(i).getStock().setBulkGap(bg);
		        		groupTrades.get(i).getStock().setBulkQty(bq);
		        		Analysis.analysisByPrice(groupTrades.get(i));
		        		updateTrades(groupTrades.get(i));
		        		break;
		        	}
		        }
			}	
		});
		tableStocks.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
			public void valueChanged(ListSelectionEvent e) {
				int lastRow = tableStocks.getSelectedRow();
				Object o =tableStocks.getModel().getValueAt(lastRow, 0);
				for(int i=0;i<groupTrades.size();i++){
					if(groupTrades.get(i).getStock().getCode().equals(o)){
						updateTrades(groupTrades.get(i));
						break;
					}
				}
				//updateTrades();
			}
		});
		tableStocks.getColumnModel().getColumn(0).setPreferredWidth(91);
		tableStocks.getColumnModel().getColumn(1).setPreferredWidth(112);
		//tableStocks.getColumnModel().getColumn(2).setPreferredWidth(117);
		//tableStocks.getColumnModel().getColumn(3).setPreferredWidth(169);
		frame.getContentPane().add(scrollPaneStocks, BorderLayout.WEST);
	}
	private void updateTrades(GroupTrade gt){
		rowsTrades.clear();
		Vector v=null;
		List<List<Trade>>  bt = gt.getBulkTrades();
		Trade trade=null;
		for(int i=0;i<bt.size();i++){
			for(int j=0;j<bt.get(i).size();j++){
				trade = bt.get(i).get(j);
				v=new Vector();
				v.add(df.format(trade.getTime()));
				v.add(trade.getPrice());
				v.add(trade.getQty());
				v.add(trade.getAmount());
				v.add(trade.getTypeName());
				v.add(i);
				rowsTrades.add(v);
			}
			
		}
		tableTrades.updateUI();
	}
	private void updateStocks(Stock stock){
		Vector v=new Vector();
		v.add(stock.getCode());
		v.add(stock.getName());
		v.add(stock.getPe());
		v.add(stock.getPrice());
		v.add(stock.getBulkGap());
		v.add(stock.getBulkQty());
		if(stock.isIntQty()){//整数关口，前列
			rowsStocks.add(0,v);
		}else{
			rowsStocks.add(v);
		}
		tableStocks.updateUI();
	}
	/*private class  DataRunnable implements Runnable{
		private String code;
		private String date;
		public DataRunnable(String date,String code){
			this.date=date;
			this.code=code;
		}
		private void loadData(Stock stock){
			
			GroupTrade gt = TradeFetcher.getTrades(date,stock);
			Analysis.analysisByPrice(gt);
			if(gt.getStock().isBulkGap()){
				groupTrades.add(gt);
				updateStocks(gt.getStock());
			}
		}
		private void process(){
			//JOptionPane.showMessageDialog(frame, "查询线程已开始");
			List<Stock> stocks = StockFetcher.getStocks();
			progressBar.setMaximum(stocks.size());
			progressBar.setValue(0);
			progressBar.setIndeterminate(false);
			if(StringHelper.isNotEmpty(code)) {
				for(int i=0;i<stocks.size();i++){
					if(stocks.get(i).getCode().equals(code)){
						loadData(stocks.get(i));
						break;
					}
				}
			}else{
				for(int i=0;i<stocks.size();i++){
					loadData(stocks.get(i));
					progressBar.setValue(i);
				}
			}
		}
		public void run() {
			button.setText("停止");
			try {
				process();
			} catch (RuntimeException e) {
				
			}finally{
				button.setText("查询");
			}
		}
		
	}*/
}
