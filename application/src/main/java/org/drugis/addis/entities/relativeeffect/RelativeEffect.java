/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright © 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright © 2010 Gert van Valkenhoef, Tommi Tervonen, Tijs Zwinkels,
 * Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, Ahmad Kamal, Daniel
 * Reid.
 * Copyright © 2011 Gert van Valkenhoef, Ahmad Kamal, Daniel Reid, Florin
 * Schimbinschi.
 * Copyright © 2012 Gert van Valkenhoef, Daniel Reid, Joël Kuiper, Wouter
 * Reckman.
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

package org.drugis.addis.entities.relativeeffect;

import org.drugis.addis.entities.Measurement;

import com.jgoodies.binding.beans.Observable;

public interface RelativeEffect<T extends Measurement> extends Observable {

	/**
	 * Create a probability distribution for this relative effect.
	 */
	public Distribution getDistribution();
	
	/**
	 * Get the 95% confidence interval.
	 * @return The confidence interval.
	 */
	public ConfidenceInterval getConfidenceInterval();
	
	public AxisType getAxisType();
	
	public boolean isDefined();
	
	public String getName();
	
	
	/**
	 * @return the p-value of this distribution being different from "no effect".
	 */
	public abstract double getTwoSidedPValue();

	/**
	 * @return the value representing "no effect".
	 */
	public abstract double getNeutralValue();
}
