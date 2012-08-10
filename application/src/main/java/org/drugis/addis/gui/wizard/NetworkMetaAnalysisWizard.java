/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
 * Copyright (C) 2012 Gert van Valkenhoef, Daniel Reid, 
 * Joël Kuiper, Wouter Reckman.
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

package org.drugis.addis.gui.wizard;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import org.drugis.addis.gui.AddisWindow;
import org.drugis.addis.gui.StudyGraph;
import org.drugis.addis.presentation.SelectableTreatmentDefinitionsGraphModel;
import org.drugis.addis.presentation.wizard.AbstractMetaAnalysisWizardPM;
import org.drugis.addis.presentation.wizard.NetworkMetaAnalysisWizardPM;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardModel;
import org.pietschy.wizard.models.StaticModel;

import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

@SuppressWarnings("serial")
public class NetworkMetaAnalysisWizard extends Wizard {

	public NetworkMetaAnalysisWizard(AddisWindow mainWindow, NetworkMetaAnalysisWizardPM model) {
		super(buildModel(model, mainWindow));
		setDefaultExitMode(Wizard.EXIT_ON_FINISH);
		getTitleComponent().setPreferredSize(new Dimension(550, 100));
	}

	private static WizardModel buildModel(NetworkMetaAnalysisWizardPM pm, AddisWindow main) {
		StaticModel wizardModel = new StaticModel();
		wizardModel.add(new SelectIndicationAndNameWizardStep(pm, main));
		wizardModel.add(new SelectEndpointWizardStep(pm));
		wizardModel.add(new SelectDrugsWizardStep(pm));
		wizardModel.add(new RefineDrugSelectionWizardStep(pm));
		wizardModel.add(new SelectTreatmentDefinitionsWizardStep(pm));
		SelectStudiesWizardStep selectStudiesStep = new SelectStudiesWizardStep(pm, main);
		selectStudiesStep.setComplete(true);
		wizardModel.add(selectStudiesStep);
		wizardModel.add(new SelectArmsWizardStep(pm));
		OverviewWizardStep overviewStep = new OverviewWizardStep(pm, main);
		Bindings.bind(overviewStep, "complete", pm.getOverviewGraphConnectedModel());
		wizardModel.add(overviewStep);
		return wizardModel;
	}
	

	public static class OverviewWizardStep extends AbstractOverviewWizardStep<SelectableTreatmentDefinitionsGraphModel> {
		private StudyGraph d_studyGraph;

		public OverviewWizardStep(AbstractMetaAnalysisWizardPM<SelectableTreatmentDefinitionsGraphModel> pm, AddisWindow main) {
			super(pm, main);

			setLayout(new BorderLayout());
			    
			FormLayout layout = new FormLayout(
					"center:pref:grow",
					"p"
					);	
			
			PanelBuilder builder = new PanelBuilder(layout);
			CellConstraints cc = new CellConstraints();
			
			builder.add(buildStudiesGraph(), cc.xy(1, 1));
			
			JScrollPane sp = new JScrollPane(builder.getPanel());
			sp.getVerticalScrollBar().setUnitIncrement(16);
			add(sp, BorderLayout.CENTER);

			setComplete(true);
		}

		protected Component buildStudiesGraph() {
			d_studyGraph = new StudyGraph(((NetworkMetaAnalysisWizardPM)d_pm).getOverviewGraph());
			d_studyGraph.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
			return d_studyGraph;
		}
		
		@Override
		public void prepare() {
			((NetworkMetaAnalysisWizardPM) d_pm).rebuildOverviewGraph();
			d_studyGraph.layoutGraph();
		}
	}
}
