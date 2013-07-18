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

package org.drugis.addis.gui.components;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import org.drugis.addis.gui.AddisWindow;
import org.drugis.common.gui.table.EnhancedTable;

public class EntityTableDeleteListener extends KeyAdapter {

	private AddisWindow d_main;

	public EntityTableDeleteListener(AddisWindow main) {
		d_main = main;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			EnhancedTable studyTable = (EnhancedTable)e.getComponent();
			if (!studyTable.isFocusOwner() || studyTable.getSelectedRowCount() != 1) {
				return;
			}

			int row = studyTable.convertRowIndexToModel(studyTable.getSelectedRow());
			d_main.deleteEntity(EntityTablePanel.getEntityAt(studyTable, row), true);

			e.consume();
		}
	}
}
