package org.drugis.addis.presentation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.drugis.common.JUnitUtil;
import org.drugis.common.beans.AbstractObservable;
import org.drugis.mtc.summary.Summary;
import org.junit.Test;
import static org.easymock.EasyMock.*;
public class AllSummariesDefinedModelTest {
	
	public class MySummary extends AbstractObservable implements Summary {
		private boolean d_defined;

		public MySummary(boolean defined) {
			d_defined = defined;
		}
		
		public boolean getDefined() {
			return d_defined;
		}
		
		public void setDefined(boolean defined) {
			boolean oldValue = d_defined;
			d_defined = defined;
			firePropertyChange(PROPERTY_DEFINED, oldValue, d_defined);
		}
	}

	@Test	
	public void testEvaluateSingleSummary() {
		MySummary trueSummary = new MySummary(true);
		AllSummariesDefinedModel trueModel = new AllSummariesDefinedModel(Collections.singletonList(trueSummary));
		assertTrue(trueModel.getValue());
		
		MySummary falseSummary = new MySummary(false);
		AllSummariesDefinedModel falseModel = new AllSummariesDefinedModel(Collections.singletonList(falseSummary));
		assertFalse(falseModel.getValue());
	}
	
	@Test 
	public void testEvaluateMultipleSummaries() {
		MySummary summary1 = new MySummary(true);
		MySummary summary2 = new MySummary(false);
		List<Summary> summaries = Arrays.asList(new Summary[] { summary1, summary2 });
		
		AllSummariesDefinedModel allValuesModel = new AllSummariesDefinedModel(summaries);

		assertEquals(false, allValuesModel.getValue());
		
		summary2.setDefined(true);
		allValuesModel = new AllSummariesDefinedModel(summaries);
		assertEquals(true, allValuesModel.getValue());
	}
	
	@Test
	public void testListenMultipleSummaries() {
		MySummary summary1 = new MySummary(true);
		MySummary summary2 = new MySummary(false);
		List<Summary> summaries = Arrays.asList(new Summary[] { summary1, summary2 });
		
		AllSummariesDefinedModel allValuesModel = new AllSummariesDefinedModel(summaries);

		assertEquals(false, allValuesModel.getValue());
		
		summary2.setDefined(true);
		assertEquals(true, allValuesModel.getValue());
	}
	
	@Test
	public void testFiresChanges() {
		MySummary summary1 = new MySummary(true);
		MySummary summary2 = new MySummary(false);
		List<Summary> summaries = Arrays.asList(new Summary[] { summary1, summary2 });
		
		AllSummariesDefinedModel allValuesModel = new AllSummariesDefinedModel(summaries);

		assertEquals(false, allValuesModel.getValue());

		PropertyChangeListener mock = JUnitUtil.mockListener(allValuesModel, "value", false, true);
		allValuesModel.addPropertyChangeListener(mock);
		
		summary2.setDefined(true);
		assertEquals(true, allValuesModel.getValue());
		verify(mock);
	}
	
	@Test
	public void testFiresChangesTrueToFalse() {
		MySummary summary1 = new MySummary(true);
		MySummary summary2 = new MySummary(true);
		List<Summary> summaries = Arrays.asList(new Summary[] { summary1, summary2 });
		
		AllSummariesDefinedModel allValuesModel = new AllSummariesDefinedModel(summaries);

		assertEquals(true, allValuesModel.getValue());

		PropertyChangeListener mock = JUnitUtil.mockListener(allValuesModel, "value", true, false);
		allValuesModel.addPropertyChangeListener(mock);
		
		summary2.setDefined(false);
		assertEquals(false, allValuesModel.getValue());
		verify(mock);
	}

	@Test
	public void testOnlyFireWhenShould() {
		MySummary summary1 = new MySummary(false);
		MySummary summary2 = new MySummary(false);
		List<Summary> summaries = Arrays.asList(new Summary[] { summary1, summary2 });
		
		AllSummariesDefinedModel allValuesModel = new AllSummariesDefinedModel(summaries);

		assertEquals(false, allValuesModel.getValue());

		PropertyChangeListener m = createStrictMock(PropertyChangeListener.class);
		replay(m);
		allValuesModel.addPropertyChangeListener(m);
		
		summary2.setDefined(true);
		assertEquals(false, allValuesModel.getValue());
		verify(m);
	}
	
}
