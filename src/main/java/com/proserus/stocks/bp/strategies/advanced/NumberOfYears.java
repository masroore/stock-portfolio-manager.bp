package com.proserus.stocks.bp.strategies.advanced;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.proserus.stocks.bo.analysis.Analysis;
import com.proserus.stocks.bo.analysis.ViewableAnalysis;
import com.proserus.stocks.bp.strategies.fw.AdvancedStrategy;
import com.proserus.stocks.bp.utils.DateUtils;

public class NumberOfYears extends AdvancedStrategy {
	protected static Logger calculsLog = LoggerFactory.getLogger("calculs." + NumberOfYears.class.getName());

	@Override
	public BigDecimal process(ViewableAnalysis analysis) {

		if (calculsLog.isInfoEnabled()) {
			calculsLog.info("--------------------------------------");
			calculsLog.info("setNumberOfYears = Days.daysBetween(getStartOfPeriod,getEndOfPeriod) / 365");
			calculsLog.info("getStartOfPeriod: {}", new Object[] { analysis.getStartOfPeriod() });
			calculsLog.info("getEndOfPeriod: {}", new Object[] { analysis.getEndOfPeriod() });
		}

		double years = DateUtils.getYearsBetween(analysis.getStartOfPeriod(), analysis.getEndOfPeriod());
		calculsLog.info("Calculated NumberOfYears successfully!");

		calculsLog.info("setNumberOfYears = {} years", new Object[] { years });
		return new BigDecimal(years);
	}

	@Override
	public void setAnalysisValue(Analysis analysis, BigDecimal value) {
		analysis.setNumberOfYears(value);
	}
}
