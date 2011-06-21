package com.proserus.stocks.controllers;

import java.util.Collection;
import java.util.Date;
import java.util.Observer;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.jfree.data.time.Year;

import com.proserus.stocks.bp.AnalysisBp;
import com.proserus.stocks.bp.DateUtil;
import com.proserus.stocks.bp.FilterBp;
import com.proserus.stocks.bp.LabelsBp;
import com.proserus.stocks.bp.SharedFilter;
import com.proserus.stocks.bp.SymbolsBp;
import com.proserus.stocks.bp.TransactionsBp;
import com.proserus.stocks.controllers.iface.PortfolioController;
import com.proserus.stocks.dao.PersistenceManager;
import com.proserus.stocks.exceptions.InvalidLabelsTransactionException;
import com.proserus.stocks.exceptions.InvalidTransactionException;
import com.proserus.stocks.model.analysis.CurrencyAnalysis;
import com.proserus.stocks.model.analysis.SymbolAnalysis;
import com.proserus.stocks.model.symbols.CurrencyEnum;
import com.proserus.stocks.model.symbols.HistoricalPrice;
import com.proserus.stocks.model.symbols.Symbol;
import com.proserus.stocks.model.transactions.Label;
import com.proserus.stocks.model.transactions.Transaction;

public class PortfolioControllerImpl implements PortfolioController {
	private SymbolsBp symbolsBp = SymbolsBp.getInstance();
	private LabelsBp labelsBp = LabelsBp.getInstance();
	private TransactionsBp transactionsBp = TransactionsBp.getInstance();
	private AnalysisBp analysisBp = AnalysisBp.getInstance();
	private FilterBp filterBp = SharedFilter.getInstance();

	private static PortfolioController controller = new PortfolioControllerImpl();

	private PortfolioControllerImpl() {
	}

	static public PortfolioController getInstance() {
		return controller;
	}

	@Override
	public boolean updateSymbol(Symbol symbol) {
		boolean val = symbolsBp.updateSymbol(symbol);
		analysisBp.recalculate(filterBp);
		return val;
	}

	@Override
	public Collection<Symbol> getSymbols() {
		return symbolsBp.get();
	}

	@Override
	public void addAnalysisObserver(Observer o) {
		analysisBp.addObserver(o);
	}

	@Override
	public boolean remove(Symbol s) {
		if(transactionsBp.contains(s)){
			return false;
		}
		symbolsBp.remove(s);
		return true;
	}

	@Override
	public Collection<? extends CurrencyAnalysis> getCurrencyAnalysis(FilterBp filter) {
		return analysisBp.getCurrencyAnalysis();
	}

	@Override
	public Collection<? extends SymbolAnalysis> getSymbolAnalysis(FilterBp filter) {
		return analysisBp.getSymbolAnalysis();
	}

	@Override
	public Year getFirstYear() {
		EntityManager em = PersistenceManager.getEntityManager();
		Query query = em.createNamedQuery("transaction.findMinDate");
		Date val = (Date) query.getSingleResult();
		//TODO Manage Date better
		if(val!=null){
		return new Year(val);
		}else{
			return DateUtil.getCurrentYear();
		}
	}

	@Override
	public Symbol addSymbol(Symbol symbol) {
		CurrencyControllerImpl.getInstance().setDefaultCurrency(((CurrencyEnum) symbol.getCurrency()));
		return symbolsBp.add(symbol);
	}
	
	public Symbol getSymbol(String ticker){
		return symbolsBp.getSymbol(ticker);
	}

	@Override
	public void addSymbolsObserver(Observer o) {
		symbolsBp.addObserver(o);
		o.update(symbolsBp, null);
	}

	@Override
	public void addTransactionsObserver(Observer o) {
		transactionsBp.addObserver(o);
	}
	
	@Override
	public void addFilterObserver(Observer o) {
		filterBp.addObserver(o);
	}

	@Override
	public void setCustomFilter(String custom) {
		throw new AssertionError();
	}

	@Override
	public Collection<Transaction> getTransactions(FilterBp filter) {
		return transactionsBp.getTransactions(filter, true);
	}

	@Override
	public Transaction addTransaction(Transaction t) {
		CurrencyControllerImpl.getInstance().setDefaultCurrency(((CurrencyEnum) t.getSymbol().getCurrency()));
		Symbol s = addSymbol(t.getSymbol());
		t.setSymbol(s);
		t = transactionsBp.add(t);
		analysisBp.recalculate(SharedFilter.getInstance());
		return t;
	}

	@Override
	public void addTransactionObserver(Observer o) {
		transactionsBp.addObserver(o);
		o.update(transactionsBp, null);
	}

	@Override
	public void remove(Transaction t) {
		for(Object o: t.getLabelsValues().toArray()){
			t.removeLabel((Label)o);
		}
		transactionsBp.remove(t);
		analysisBp.recalculate(SharedFilter.getInstance());
	}

	@Override
	public void remove(Label label) {
		Collection<Transaction> transactions = transactionsBp.getTransactionsByLabel(label);
		for(Transaction t: transactions){
			t.removeLabel(label);
		}
		labelsBp.remove(label);
		analysisBp.recalculate(SharedFilter.getInstance());
	}

	@Override
	public void addLabelsObserver(Observer o) {
		labelsBp.addObserver(o);
		o.update(labelsBp, null);
	}

	@Override
	public void updateTransaction(Transaction t) throws InvalidLabelsTransactionException, InvalidTransactionException {
		transactionsBp.updateTransaction(t);
		analysisBp.recalculate(SharedFilter.getInstance());
	}

	@Override
	public Label addLabel(Label label) {
		Label l = labelsBp.add(label);
		analysisBp.recalculate(SharedFilter.getInstance());
		return l;
	}

	@Override
	public Collection<Label> getLabels() {
		return labelsBp.get();
	}

	@Override
	public void updatePrices() {
		symbolsBp.updatePrices();
		analysisBp.recalculate(SharedFilter.getInstance());
	}

	@Override
	public void updateHistoricalPrices() {
		symbolsBp.updateHistoricalPrices();
		analysisBp.recalculate(SharedFilter.getInstance());
	}

	@Override
	public void update(HistoricalPrice hPrice){
		symbolsBp.update(hPrice);
		analysisBp.recalculate(SharedFilter.getInstance());
	}
}