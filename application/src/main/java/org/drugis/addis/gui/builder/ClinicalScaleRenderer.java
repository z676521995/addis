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
 * Copyright © 2013 Gert van Valkenhoef, Joël Kuiper.
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

import java.awt.FlowLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drugis.addis.entities.Drug;
import org.drugis.addis.entities.OutcomeMeasure;
import org.drugis.addis.entities.RateVariableType;
import org.drugis.addis.entities.analysis.BenefitRiskAnalysis;
import org.drugis.addis.entities.analysis.MetaBenefitRiskAnalysis;
import org.drugis.addis.entities.treatment.TreatmentDefinition;
import org.drugis.addis.presentation.AbstractBenefitRiskPresentation;
import org.drugis.addis.presentation.OddsRatioScalePresentation;
import org.drugis.addis.presentation.RiskScalePresentation;
import org.drugis.addis.presentation.SMAAPresentation;
import org.drugis.common.gui.NumberAndIntervalFormat;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;

import fi.smaa.jsmaa.gui.views.ScaleRenderer;
import fi.smaa.jsmaa.model.Criterion;
import fi.smaa.jsmaa.model.ScaleCriterion;

public class ClinicalScaleRenderer implements ScaleRenderer {
	private SMAAPresentation<Drug, BenefitRiskAnalysis<Drug>> d_smaapm;

	public ClinicalScaleRenderer(AbstractBenefitRiskPresentation<TreatmentDefinition, MetaBenefitRiskAnalysis> pm, SMAAPresentation<Drug, BenefitRiskAnalysis<Drug>> smaapm) {
		d_smaapm = smaapm;
	}

	public JComponent getScaleComponent(Criterion c) {
		if (c instanceof ScaleCriterion) {
			ScaleCriterion criterion = (ScaleCriterion)c;
			OutcomeMeasure outcome = d_smaapm.getOutcomeMeasureForCriterion(criterion);
			if (outcome.getVariableType() instanceof RateVariableType) {
				RiskScalePresentation cpm = new RiskScalePresentation(criterion);
				return buildRiskClinicalView(cpm);
			} else {
				PresentationModel<ScaleCriterion> cpm = new PresentationModel<ScaleCriterion>((ScaleCriterion) c);
				JLabel orLabel = new JLabel("RMD: ");
				JLabel scaleLabel = BasicComponentFactory.createLabel(cpm.getModel(ScaleCriterion.PROPERTY_SCALE),
						new fi.smaa.jsmaa.gui.IntervalFormat());
				JPanel panel = new JPanel(new FlowLayout());
				panel.add(orLabel);
				panel.add(scaleLabel);
				return panel;
			}
		}
		return new JLabel("NA");
	}

	private JPanel buildRiskClinicalView(RiskScalePresentation cpm) {
		JPanel panel = new JPanel(new FlowLayout());
		addPropertyToPanel(cpm, panel, "Risk: ", OddsRatioScalePresentation.PROPERTY_RISK);
		addPropertyToPanel(cpm, panel, "RD: ", OddsRatioScalePresentation.PROPERTY_RISK_DIFFERENCE);

		JLabel label = BasicComponentFactory.createLabel(cpm.getModel(OddsRatioScalePresentation.PROPERTY_NNT_LABEL));
		JLabel valueLabel = BasicComponentFactory.createLabel(cpm.getModel(OddsRatioScalePresentation.PROPERTY_NNT),
				new NumberAndIntervalFormat());
		panel.add(label);
		panel.add(valueLabel);

		return panel;
	}

	private void addPropertyToPanel(PresentationModel<?> cpm, JPanel panel, String text, String property) {
		JLabel label = new JLabel(text);
		JLabel valueLabel = BasicComponentFactory.createLabel(cpm.getModel(property),
				new NumberAndIntervalFormat());
		panel.add(label);
		panel.add(valueLabel);
	}
}