/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009  Gert van Valkenhoef and Tommi Tervonen.
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

package org.drugis.addis.gui;

import org.drugis.addis.entities.MutableStudy;
import org.drugis.addis.entities.StudyCharacteristic;
import org.drugis.addis.presentation.CharacteristicHolder;

@SuppressWarnings("serial")
public class MutableCharacteristicHolder extends CharacteristicHolder {
	public MutableCharacteristicHolder(MutableStudy bean, StudyCharacteristic characteristic) {
		super(bean, characteristic);
	}

	public void setValue(Object newValue) {
		Object oldValue = d_study.getCharacteristics().get(d_char);
		((MutableStudy)d_study).setCharacteristic(d_char, newValue);
		firePropertyChange("value", oldValue, newValue);
	}
}
