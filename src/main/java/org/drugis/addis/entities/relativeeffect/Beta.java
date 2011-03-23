/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
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

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.BetaDistribution;
import org.apache.commons.math.distribution.BetaDistributionImpl;
import org.drugis.common.beans.AbstractObservable;

public class Beta extends AbstractObservable implements Distribution {
	
	private final double d_alpha;
	private final double d_beta;
	private BetaDistribution d_dist;

	public Beta(double alpha, double beta) {
		// FIXME: Should alpha, beta also be tested for being int?
		if (Double.isNaN(alpha)) throw new IllegalArgumentException("alpha may not be NaN");
		if (Double.isNaN(beta)) throw new IllegalArgumentException("beta may not be NaN");
		if (alpha <= 0) throw new IllegalArgumentException("alpha must be > 0"); 
		if (beta <= 0) throw new IllegalArgumentException("beta must be > 0"); 
		d_alpha = alpha;
		d_beta = beta;
		d_dist = new BetaDistributionImpl(d_alpha, d_beta);
	}

	public AxisType getAxisType() {
		return AxisType.LINEAR;
	}

	public double getAlpha() {
		return d_alpha;
	}
	
	public double getBeta() {
		return d_beta;
	}

	public double getQuantile(double p) {
		try {
			return d_dist.inverseCumulativeProbability(p);
		} catch (MathException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean equals(Object o){
		if(o instanceof Beta) {
			Beta other = (Beta) o;
			return (other.d_alpha == d_alpha) && (other.d_beta == d_beta);
		}
		return false;
	}
	
}
