package org.drugis.addis.entities;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Set;

import org.drugis.common.Interval;
import org.drugis.common.StudentTTable;

public abstract class AbstractRelativeEffect<T extends Measurement> extends AbstractEntity implements RelativeEffect<T>{

	private static final long serialVersionUID = 1863294156273299358L;
	protected T d_subject;
	protected T d_baseline; 

	protected AbstractRelativeEffect(T subject, T baseline) {
		d_subject  = subject;
		d_baseline = baseline;
		connectListeners();
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		in.defaultReadObject();
		connectListeners();
	}	
	
	private void connectListeners() {
		MemberListener listener = new MemberListener();
		getSubject().addPropertyChangeListener(listener);
		getBaseline().addPropertyChangeListener(listener);
	}

	public Integer getSampleSize() {
		return getSubject().getSampleSize() + getBaseline().getSampleSize();
	}
	
	public T getSubject() {
		return d_subject;
	}

	public T getBaseline() {
		return d_baseline;
	}

	public Set<Entity> getDependencies() {
		return Collections.emptySet();
	}

	private class MemberListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange(PROPERTY_SAMPLESIZE, null, getSampleSize());
		}		
	}
	
	protected abstract Integer getDegreesOfFreedom();
	
	public boolean isDefined() {
		return getDegreesOfFreedom() > 0;
	}
	
	public Interval<Double> getDefaultConfidenceInterval() {
		if (!isDefined()) {
			return new Interval<Double>(Double.NaN, Double.NaN);
		}
		double t = StudentTTable.getT(getDegreesOfFreedom());

		return new Interval<Double>(getRelativeEffect() - t * getError(), getRelativeEffect() + t * getError());
	}
}