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

package org.drugis.addis.gui.builder;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.drugis.addis.entities.relativeeffect.Distribution;
import org.drugis.addis.presentation.SummaryCellRenderer;

@SuppressWarnings("serial")
public class DistributionQuantileCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value, 
			boolean isSelected, boolean hasFocus, int row, int column) {
		if (value instanceof Distribution) {
			Distribution d = (Distribution)value;
			String str = SummaryCellRenderer.format(d.getQuantile(0.5)) + " (" + 
				SummaryCellRenderer.format(d.getQuantile(0.025)) + ", " + 
				SummaryCellRenderer.format(d.getQuantile(0.975)) + ")";
			return super.getTableCellRendererComponent(table, str, isSelected, hasFocus, row, column);
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}
}
