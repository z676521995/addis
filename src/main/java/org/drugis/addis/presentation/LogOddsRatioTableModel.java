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

import org.drugis.addis.entities.Measurement;
import org.drugis.addis.entities.OutcomeMeasure;
import org.drugis.addis.entities.RateMeasurement;
import org.drugis.addis.entities.Study;
import org.drugis.addis.entities.relativeeffect.AbstractRelativeEffect;
import org.drugis.addis.entities.relativeeffect.LogOddsRatio;
import org.drugis.addis.entities.relativeeffect.RelativeEffect;

@SuppressWarnings("serial")
public class LogOddsRatioTableModel extends OddsRatioTableModel {
	
	public LogOddsRatioTableModel(Study s, OutcomeMeasure om, PresentationModelFactory pmf) {
		super(s, om, pmf);
	}

	@Override
	protected AbstractRelativeEffect<RateMeasurement> getRelativeEffect(Measurement denominator, Measurement numerator) {
		return new LogOddsRatio((RateMeasurement)denominator,
		(RateMeasurement)numerator);
	}

	@Override
	public String getTitle() {
		return "log Odds-Ratio Table";
	}

	@Override
	protected Class<? extends RelativeEffect<?>> getRelativeEffectType() {
		return LogOddsRatio.class;
	}
}
