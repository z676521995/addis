/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009  Gert van Valkenhoef and Tommi Tervonen.
 * Copyright (C) 2010  Gert van Valkenhoef, Tommi Tervonen, Tijs Zwinkels,
 * Maarten Jacobs and Hanno Koeslag.
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

package org.drugis.addis.presentation;

import org.drugis.addis.entities.analysis.RandomEffectsMetaAnalysis;
import org.drugis.addis.entities.relativeeffect.RelativeEffect;

@SuppressWarnings("serial")
public class RandomEffectsMetaAnalysisPresentation
extends AbstractMetaAnalysisPresentation<RandomEffectsMetaAnalysis>
implements StudyListPresentationModel {

	public RandomEffectsMetaAnalysisPresentation(RandomEffectsMetaAnalysis bean, PresentationModelFactory mgr) {
		super(bean, mgr);
	}
	
	public LabeledPresentationModel getFirstDrugModel() {
		return d_mgr.getLabeledModel(getBean().getFirstDrug());
	}
	
	public LabeledPresentationModel getSecondDrugModel() {
		return d_mgr.getLabeledModel(getBean().getSecondDrug());		
	}

	public ForestPlotPresentation getForestPlotPresentation(Class<? extends RelativeEffect<?>> type) {
		ForestPlotPresentation pm = new ForestPlotPresentation(getBean(), type, d_mgr);
		return pm;
	}
}
