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

package org.drugis.addis.gui.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import org.drugis.addis.entities.BasicMeasurement;
import org.drugis.addis.entities.Domain;
import org.drugis.addis.entities.Endpoint;
import org.drugis.addis.entities.MutableStudy;
import org.drugis.addis.entities.Arm;
import org.drugis.addis.entities.Study;
import org.drugis.addis.gui.EndpointHolder;
import org.drugis.addis.gui.MeasurementInputHelper;
import org.drugis.addis.gui.components.NotEmptyValidator;
import org.drugis.addis.presentation.PresentationModelFactory;
import org.drugis.common.gui.LayoutUtil;
import org.drugis.common.gui.ViewBuilder;


import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

public class StudyAddEndpointView implements ViewBuilder {
	private Domain d_domain;
	private MutableStudy d_study;
	private PresentationModel<EndpointHolder> d_endpointModel;
	private Map<Arm, BasicMeasurement> d_measurements;
	
	private JComboBox d_endpointSelect;
	private SelectionInList<Endpoint> d_endpointSelectionInList;
	private NotEmptyValidator d_validator;
	private PresentationModelFactory d_presMan;
	
	public StudyAddEndpointView(Domain domain, MutableStudy study,
			PresentationModel<EndpointHolder> endpointModel,
			Map<Arm,BasicMeasurement> measurements,
			JButton okButton, PresentationModelFactory presModelMan) {
		d_validator = new NotEmptyValidator(okButton);
		d_domain = domain;
		d_study = study;
		d_endpointModel = endpointModel;
		d_measurements = measurements;
		d_presMan = presModelMan;
	}

	private void initializeMeasurements() {
		for (Arm g : d_study.getArms()) {
			if (getEndpoint() != null) {
				BasicMeasurement m = getEndpoint().buildMeasurement(g);
				d_measurements.put(g, m);
			}
		}
	}
	
	private void initComponents() {
		d_endpointSelectionInList = new SelectionInList<Endpoint>(getEndpoints(), 
				d_endpointModel.getModel(EndpointHolder.PROPERTY_ENDPOINT));
		d_endpointSelect = BasicComponentFactory.createComboBox(d_endpointSelectionInList);
		d_validator.add(d_endpointSelect);
	}

	private List<Endpoint> getEndpoints() {
		List<Endpoint> list = new ArrayList<Endpoint>(d_domain.getEndpoints());
		list.removeAll(d_study.getEndpoints());
		return list;
	}

	public JComponent buildPanel() {
		initializeMeasurements();
		initComponents();
		
		String colSpec = "right:pref, 3dlu, " +
			(getNumComponents() > 0 ? "pref" : "pref:grow");
		FormLayout layout = new FormLayout(colSpec,
				"p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
		int fullWidth = 3;
		for (int i = 1; i < getNumComponents(); ++i) {
			layout.appendColumn(ColumnSpec.decode("3dlu"));
			if (i < getNumComponents() - 1) {
				layout.appendColumn(ColumnSpec.decode("pref"));				
			} else {
				layout.appendColumn(ColumnSpec.decode("pref:grow"));				
			}
			fullWidth += 2;
		}
		
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		
		CellConstraints cc = new CellConstraints();
		
		builder.addSeparator("Study", cc.xyw(1, 1, fullWidth));
		builder.addLabel("ID:", cc.xy(1, 3));
		builder.add(BasicComponentFactory.createLabel(
				new PresentationModel<MutableStudy>(d_study).getModel(Study.PROPERTY_ID)
				), cc.xyw(3, 3, fullWidth - 2));
		
		builder.addSeparator("Endpoint", cc.xyw(1, 5, fullWidth));
		builder.addLabel("Endpoint:", cc.xy(1, 7));
		builder.add(d_endpointSelect, cc.xyw(3, 7, fullWidth - 2));
		
		builder.addSeparator("Data", cc.xyw(1, 9, fullWidth));
		
		if (getEndpoint() != null) {
			int col = 3;
			for (String header : MeasurementInputHelper.getHeaders(getEndpoint())) {
				builder.addLabel(header, cc.xy(col, 11));
				col += 2;
			}
		}
		
		buildMeasurementsPart(builder, cc, 13, layout);
		
		return builder.getPanel();
	}

	private int getNumComponents() {
		Endpoint e = getEndpoint();
		if (e == null) {
			return 0;
		}
		return MeasurementInputHelper.numComponents(e);
	}

	private Endpoint getEndpoint() {
		return d_endpointModel.getBean().getEndpoint();
	}

	private void buildMeasurementsPart(PanelBuilder builder,
			CellConstraints cc, int row, FormLayout layout) {
		for (Map.Entry<Arm, BasicMeasurement> e: d_measurements.entrySet()) {
			Arm g = e.getKey();
			BasicMeasurement m = e.getValue();
			LayoutUtil.addRow(layout);
			builder.add(BasicComponentFactory.createLabel(d_presMan.getLabeledModel(g).getLabelModel()),
					cc.xy(1, row));
			int col = 3;
			for (JTextField component : MeasurementInputHelper.getComponents(m)) {
				d_validator.add(component);
				builder.add(component, cc.xy(col, row));
				col += 2;
			}
			row += 2;
		}
	}
}
