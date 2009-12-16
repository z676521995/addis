/**
 * 
 */
package org.drugis.addis.presentation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.drugis.addis.entities.BasicArm;
import org.drugis.addis.entities.FixedDose;
import org.drugis.addis.entities.FlexibleDose;
import org.drugis.addis.entities.SIUnit;
import org.drugis.common.Interval;

import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueHolder;

class DosePresentationImpl implements DosePresentationModel {
	private BasicArm d_pg;
	private ValueHolder d_min;
	private ValueHolder d_max;
	private ValueHolder d_unit;
	
	public DosePresentationImpl(
			BasicArmPresentation basicArmPresentation) {
		d_pg = basicArmPresentation.getBean();
		d_min = new ValueHolder(getMinDose(d_pg));
		d_max = new ValueHolder(getMaxDose(d_pg));
		d_unit = new ValueHolder(d_pg.getDose().getUnit());
		
		d_min.addPropertyChangeListener(new DoseChangeListener());
		d_max.addPropertyChangeListener(new DoseChangeListener());
		d_unit.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				d_pg.getDose().setUnit((SIUnit) arg0.getNewValue());
			}
		});
	}

	private double getMaxDose(BasicArm pg) {
		if (d_pg.getDose() instanceof FlexibleDose) {
			return ((FlexibleDose)d_pg.getDose()).getFlexibleDose().getUpperBound();
		} else if (d_pg.getDose() instanceof FixedDose) {
			return ((FixedDose)d_pg.getDose()).getQuantity();
		}
		return 0.0;
	}

	private double getMinDose(BasicArm pg) {
		if (d_pg.getDose() instanceof FlexibleDose) {
			return ((FlexibleDose)d_pg.getDose()).getFlexibleDose().getLowerBound();
		} else if (d_pg.getDose() instanceof FixedDose) {
			return ((FixedDose)d_pg.getDose()).getQuantity();
		}
		return 0.0;
	}

	public AbstractValueModel getMaxModel() {
		return d_max;
	}

	public AbstractValueModel getMinModel() {
		return d_min;
	}

	public AbstractValueModel getUnitModel() {
		return d_unit;
	}
	
	private class DoseChangeListener implements PropertyChangeListener {
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() == d_min) {
				double newMin = (Double)evt.getNewValue();
				if (newMin > d_max.doubleValue()) {
					d_max.setValue(newMin);
					return;
				}
			}
			if (evt.getSource() == d_max) {
				double newMax = (Double)evt.getNewValue();
				if (newMax < d_min.doubleValue()) {
					d_min.setValue(newMax);
					return;
				}
			}
			if (d_min.doubleValue() == d_max.doubleValue()) {
				d_pg.setDose(new FixedDose(d_min.doubleValue(), d_pg.getDose().getUnit()));
			} else if (d_min.doubleValue() < d_max.doubleValue()) {
				Interval<Double> interval = new Interval<Double>(d_min.doubleValue(), d_max.doubleValue());
				d_pg.setDose(new FlexibleDose(interval , d_pg.getDose().getUnit()));
			} else {
				throw new RuntimeException("Should not be reached");
			}
		}
	}
}