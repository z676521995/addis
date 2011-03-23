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

import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drugis.addis.entities.Arm;
import org.drugis.addis.entities.Study;
import org.drugis.addis.gui.NoteViewButton;
import org.drugis.addis.presentation.BasicArmPresentation;
import org.drugis.addis.presentation.PresentationModelFactory;
import org.drugis.common.gui.LayoutUtil;
import org.drugis.common.gui.OneWayObjectFormat;
import org.drugis.common.gui.ViewBuilder;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

public class StudyArmsView implements ViewBuilder {
	
	private PresentationModel<? extends Study> d_model;
	private PresentationModelFactory d_pm;
	private JFrame d_parent;

	public StudyArmsView(JFrame parent, PresentationModel<? extends Study> model, PresentationModelFactory pm) {
		d_parent = parent;
		d_model = model;
		d_pm = pm;
	}

	public JPanel buildPanel() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout( 
				"left:pref, 5dlu, left:pref, 5dlu, left:pref, 5dlu, center:pref, 5dlu, center:pref",
				"p"
				);
		
		int fullWidth = 7;
		for (int i = 1; i < d_model.getBean().getOutcomeMeasures().size(); ++i) {			
			layout.appendColumn(ColumnSpec.decode("3dlu"));
			layout.appendColumn(ColumnSpec.decode("center:pref"));			
			fullWidth += 2;
		}
		PanelBuilder builder = new PanelBuilder(layout);
		
		int row = 1;

		builder.addLabel("Size", cc.xy(7, row, "center, center"));		
		row += 2;

		for (Arm g : d_model.getBean().getArms()) {
			row = buildArm(layout, builder, cc, row, g);
		}
		return builder.getPanel();
	}

	private int buildArm(FormLayout layout, PanelBuilder builder, CellConstraints cc, int row, Arm g) {
		BasicArmPresentation armModel = (BasicArmPresentation)d_pm.getModel(g);
		LayoutUtil.addRow(layout);
		final JLabel armLabel = BasicComponentFactory.createLabel(d_pm.getLabeledModel(g).getLabelModel()); 
		//armLabel.setToolTipText(GUIFactory.createToolTip(d_model.getBean().getNote(armModel.getBean())));
		JButton button = new NoteViewButton(d_parent, "Arm: " + g.toString(), g.getNotes());
		builder.add(button, cc.xy(1, row));
		builder.add(armLabel, cc.xy(3, row));
		
		builder.add(
				BasicComponentFactory.createLabel(
						armModel.getModel(Arm.PROPERTY_DOSE),
						new OneWayObjectFormat()),
						cc.xy(5, row, "right, center"));
		
		builder.add(
				BasicComponentFactory.createLabel(
						armModel.getModel(Arm.PROPERTY_SIZE),
						NumberFormat.getInstance()),
				cc.xy(7, row, "center, center"));
		
		row += 2;
		return row;
	}
}
