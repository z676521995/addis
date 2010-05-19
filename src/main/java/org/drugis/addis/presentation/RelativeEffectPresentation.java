/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009  Gert van Valkenhoef and Tommi Tervonen.
 * Copyright (C) 2010  Gert van Valkenhoef, Tommi Tervonen, Tijs Zwinkels,
 * Maarten Jacobs and Hanno Koeslag.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.drugis.addis.presentation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;

import org.drugis.addis.entities.Measurement;
import org.drugis.addis.entities.RelativeEffect;
import org.drugis.common.Interval;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.value.AbstractValueModel;

@SuppressWarnings("serial")
public class RelativeEffectPresentation extends PresentationModel<RelativeEffect<? extends Measurement>> implements LabeledPresentationModel {
	
	public class LabelModel extends AbstractValueModel implements PropertyChangeListener {
		public LabelModel() {
			getBean().addPropertyChangeListener(this);
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange("value", null, getValue());
		}

		public Object getValue() {
			if (!getBean().isDefined()) {
				return "N/A";
			}			
			DecimalFormat format = new DecimalFormat("###0.00");
			Interval<Double> ci = getBean().getConfidenceInterval();			
			return format.format(getBean().getMedian()) + " (" + format.format(ci.getLowerBound()) + ", " + 
				format.format(ci.getUpperBound()) + ")";
		}

		public void setValue(Object arg0) {
			throw new RuntimeException();
		}
	}

	public RelativeEffectPresentation(RelativeEffect<? extends Measurement> bean) {
		super(bean);
	}
	
	public AbstractValueModel getLabelModel() {
		return new LabelModel();
	}
}
