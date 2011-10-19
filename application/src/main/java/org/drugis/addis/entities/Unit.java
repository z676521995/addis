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

package org.drugis.addis.entities;

import java.util.Collections;
import java.util.Set;

import org.drugis.common.EqualsUtil;

public class Unit extends AbstractNamedEntity<Unit> {
	public static final String PROPERTY_SYMBOL = "symbol";
	private String d_symbol;
	
	public Unit(String name, String symbol) {
		super(name);
		d_symbol = symbol;
	}
	
	public String getSymbol() {
		return d_symbol;
	}
	
	public void setSymbol(String symbol) {
		String oldValue = d_symbol;
		d_symbol = symbol;
		firePropertyChange(PROPERTY_SYMBOL, oldValue, d_symbol);
	}
	
	@Override
	public Set<? extends Entity> getDependencies() {
		return Collections.emptySet();
	}
	
	@Override
	public boolean deepEquals(Entity other) {
		if (!super.deepEquals(other)) {
			return false;
		}
		Unit o = (Unit) other;
		return EqualsUtil.equal(d_symbol, o.d_symbol);
	}
	
	@Override
	public String toString() {
		return getLabel();
	}
	
}