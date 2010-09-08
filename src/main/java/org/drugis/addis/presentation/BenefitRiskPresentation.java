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

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

import org.drugis.addis.entities.OutcomeMeasure;
import org.drugis.addis.entities.analysis.BenefitRiskAnalysis;
import org.drugis.addis.entities.analysis.MetaAnalysis;
import org.drugis.addis.entities.analysis.NetworkMetaAnalysis;
import org.drugis.addis.mcmcmodel.AbstractBaselineModel;
import org.drugis.addis.util.JSMAAintegration.BRSMAASimulationBuilder;
import org.drugis.addis.util.JSMAAintegration.SMAAEntityFactory;
import org.drugis.addis.util.threading.ThreadHandler;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.MCMCModel;
import org.drugis.mtc.ProgressEvent;
import org.drugis.mtc.ProgressListener;
import org.drugis.mtc.ProgressEvent.EventType;

import com.jgoodies.binding.PresentationModel;

import fi.smaa.jsmaa.gui.components.SimulationProgressBar;
import fi.smaa.jsmaa.gui.jfreechart.CentralWeightsDataset;
import fi.smaa.jsmaa.gui.jfreechart.RankAcceptabilitiesDataset;
import fi.smaa.jsmaa.gui.presentation.CentralWeightTableModel;
import fi.smaa.jsmaa.gui.presentation.PreferencePresentationModel;
import fi.smaa.jsmaa.gui.presentation.RankAcceptabilityTableModel;
import fi.smaa.jsmaa.gui.presentation.SMAA2ResultsTableModel;
import fi.smaa.jsmaa.model.CardinalCriterion;
import fi.smaa.jsmaa.model.ModelChangeEvent;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.SMAAModelListener;
import fi.smaa.jsmaa.model.xml.JSMAABinding;
import fi.smaa.jsmaa.simulator.BuildQueue;
import fi.smaa.jsmaa.simulator.SMAA2Results;

@SuppressWarnings("serial")
public class BenefitRiskPresentation extends PresentationModel<BenefitRiskAnalysis>{
	
	private class AnalysisProgressListener implements ProgressListener {
		JProgressBar d_progBar;
		private MCMCModel d_networkModel;

		public AnalysisProgressListener(MCMCModel networkModel) {
			networkModel.addProgressListener(this);
			d_networkModel = networkModel;
		}
		
		public void attachBar(JProgressBar bar) {
			d_progBar = bar;
			bar.setVisible(!d_networkModel.isReady());
		}

		public void update(MCMCModel mtc, ProgressEvent event) {
			if(event.getType() == EventType.SIMULATION_PROGRESS && d_progBar != null){
				d_progBar.setString("Simulating: " + event.getIteration()/(event.getTotalIterations()/100) + "%");
				d_progBar.setValue(event.getIteration()/(event.getTotalIterations()/100));
			} else if(event.getType() == EventType.BURNIN_PROGRESS && d_progBar != null){
				d_progBar.setString("Burn in: " + event.getIteration()/(event.getTotalIterations()/100) + "%");
				d_progBar.setValue(event.getIteration()/(event.getTotalIterations()/100));
			} else if(event.getType() == EventType.SIMULATION_FINISHED && d_progBar != null) {
				d_progBar.setVisible(false);
			}
		}
	}
	
	private static class AllModelsReadyListener extends UnmodifiableHolder<Boolean> implements ProgressListener {
		private List<MCMCModel> d_models = new ArrayList<MCMCModel>();
		
		public AllModelsReadyListener() {
			super(true);
		}
		
		public void addModel(MCMCModel model) {
			model.addProgressListener(this);
			d_models.add(model);
		}
		
		@Override 
		public Boolean getValue() {
			return allModelsReady();
		}

		public void update(MCMCModel mtc, ProgressEvent event) {
			if (event.getType() == ProgressEvent.EventType.SIMULATION_FINISHED){
				if(allModelsReady()) {
					fireValueChange(false, true);
				}
			}
		}

		public boolean allModelsReady() {
			for (MCMCModel model : d_models){
				if (!model.isReady())
					return false;
			}
			return true;
		}
	}
	
	public static final String PROPERTY_ALLMODELSREADY = "allModelsReady";
	
	private PresentationModelFactory d_pmf;
	private AllModelsReadyListener d_allNetworkModelsReadyListener;
	private List<AnalysisProgressListener> d_NMAnalysisProgressListeners;
	private List<AnalysisProgressListener> d_baselineProgressListeners;

	private RankAcceptabilityTableModel d_rankAccepTM;
	private RankAcceptabilitiesDataset d_rankAccepDS;	
	private BuildQueue d_buildQueue;

	private CentralWeightsDataset d_cwDS;

	private CentralWeightTableModel d_cwTM;

	private PreferencePresentationModel d_prefPresModel;

	private SMAAModel d_smaaModel;

	private SimulationProgressBar d_progressBar;

	private SMAAEntityFactory d_smaaf;

	private List<MCMCModel> d_baselineModels;

	private boolean d_allSimulationsStarted = false;
	
	public boolean allNMAModelsReady() {
		return d_allNetworkModelsReadyListener.allModelsReady();
	}
	
	public BenefitRiskPresentation(BenefitRiskAnalysis bean, PresentationModelFactory pmf) {
		super(bean);
		
		d_pmf = pmf;
		d_allNetworkModelsReadyListener = new AllModelsReadyListener();
		d_NMAnalysisProgressListeners = new ArrayList<AnalysisProgressListener>();
		d_baselineProgressListeners = new ArrayList<AnalysisProgressListener>();
		d_buildQueue = new BuildQueue();
		d_progressBar = new SimulationProgressBar();
		
		/* 
		 * Only start SMAA if all networks are already done calculating when running this constructor.
		 * If not, the 'ready' event of the networks will trigger the creation of the SMAA model.
		 */
		d_baselineModels = new ArrayList<MCMCModel>();
		initAllBaselineModels();
		initAllNetworkAnalyses();
	}
	
	public void startSMAA() {
		d_smaaf = new SMAAEntityFactory();
		d_smaaModel = d_smaaf.createSmaaModel(getBean());
		SMAA2Results emptyResults = new SMAA2Results(d_smaaModel.getAlternatives(), d_smaaModel.getCriteria(), 10);
		d_rankAccepDS = new RankAcceptabilitiesDataset(emptyResults);
		d_rankAccepTM = new RankAcceptabilityTableModel(emptyResults);
		d_cwTM = new CentralWeightTableModel(emptyResults);
		d_cwDS = new CentralWeightsDataset(emptyResults);
		d_prefPresModel = new PreferencePresentationModel(d_smaaModel, false);

		d_smaaModel.addModelListener(new SMAAModelListener() {
			public void modelChanged(ModelChangeEvent type) {
				startSimulation();
			}			
		});
		startSimulation();
	}
	
	public SimulationProgressBar getSmaaSimulationProgressBar() {
		return d_progressBar;
	}

	private void startSimulation() {
		d_buildQueue.add(new BRSMAASimulationBuilder(d_smaaModel,
				d_rankAccepTM, d_rankAccepDS, d_cwTM, d_cwDS, d_progressBar));
	}

	public int getNumNMAProgBars() {
		return d_NMAnalysisProgressListeners.size();
	}
	
	public int getNumBaselineProgBars() {
		return d_baselineProgressListeners.size();
	}
	
	public void attachNMAProgBar(JProgressBar bar, int progBarNum) {
		if (progBarNum >= d_NMAnalysisProgressListeners.size() )
			throw new IllegalArgumentException();
		d_NMAnalysisProgressListeners.get(progBarNum).attachBar(bar);
	}
	
	public void attachBaselineProgBar(JProgressBar bar, int progBarNum) {
		if (progBarNum >= d_baselineProgressListeners.size() )
			throw new IllegalArgumentException();
		d_baselineProgressListeners.get(progBarNum).attachBar(bar);
	}
	
	public PreferencePresentationModel getSmaaPreferenceModel() {
		return null;
	}
	
	
	public ListHolder<MetaAnalysis> getAnalysesModel() {
		// FIXME: By the time it's possible the edit BR-analyses, this listholder should be hooked up.
		return new DefaultListHolder<MetaAnalysis>(getBean().getMetaAnalyses());
	}

	public BenefitRiskMeasurementTableModel getMeasurementTableModel(boolean relative) {
		return new BenefitRiskMeasurementTableModel(getBean(), d_pmf, relative);
	}

	public OutcomeMeasure getOutcomeMeasureForCriterion(CardinalCriterion crit) {
		return d_smaaf.getOutcomeMeasure(crit);
	}
	
	public PreferencePresentationModel getPreferencePresentationModel() {
		return d_prefPresModel;
	}

	public SMAA2ResultsTableModel getRankAcceptabilitiesTableModel() {
		return d_rankAccepTM;
	}

	public RankAcceptabilitiesDataset getRankAcceptabilityDataSet() {
		return d_rankAccepDS;
	}

	public CentralWeightsDataset getCentralWeightsDataSet() {
		return d_cwDS;
	}

	public CentralWeightTableModel getCentralWeightsTableModel() {
		return d_cwTM;
	}	
	
	private void initAllNetworkAnalyses() {
		for (MetaAnalysis ma : getBean().getMetaAnalyses() ){
			if (ma instanceof NetworkMetaAnalysis) {
				ConsistencyModel consistencyModel = ((NetworkMetaAnalysis) ma).getConsistencyModel();
				d_allNetworkModelsReadyListener.addModel(consistencyModel);
				d_NMAnalysisProgressListeners.add(new AnalysisProgressListener(consistencyModel));
			}
		}
	}
	
	public ValueHolder<Boolean> getAllModelsReadyModel() {
		return d_allNetworkModelsReadyListener;
	}
	
	public synchronized void startAllSimulations() {
		if (d_allSimulationsStarted) {
			return;
		}

		getBean().runAllConsistencyModels();
		List<Runnable> models = new ArrayList<Runnable>();
		for (MCMCModel model : d_baselineModels) {
			if (!model.isReady()) {
				models.add(model);
			}
		}
		ThreadHandler.getInstance().scheduleTasks(models);
		
		d_allSimulationsStarted  = true;
	}
	
	public void saveSmaa(String filename) {
		try {
			FileOutputStream os = new FileOutputStream(filename);
			JSMAABinding.writeModel(d_smaaModel, os);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void initAllBaselineModels() {
		AbstractBaselineModel<?> model;
		for (OutcomeMeasure om : getBean().getOutcomeMeasures()) {
			model = getBean().getBaselineModel(om);
			d_baselineModels.add(model);
			d_allNetworkModelsReadyListener.addModel(model);
			d_baselineProgressListeners.add(new AnalysisProgressListener(model));
		}
	}

	public PresentationModelFactory getFactory() {
		return d_pmf;
	}
}
