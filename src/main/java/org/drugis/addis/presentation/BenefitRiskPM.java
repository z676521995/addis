package org.drugis.addis.presentation;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

import org.drugis.addis.entities.analysis.BenefitRiskAnalysis;
import org.drugis.addis.entities.analysis.MetaAnalysis;
import org.drugis.addis.entities.analysis.NetworkMetaAnalysis;
import org.drugis.addis.util.JSMAAintegration.BRSMAASimulationBuilder;
import org.drugis.addis.util.JSMAAintegration.SMAAEntityFactory;
import org.drugis.mtc.ConsistencyModel;
import org.drugis.mtc.MixedTreatmentComparison;
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
import fi.smaa.jsmaa.model.ModelChangeEvent;
import fi.smaa.jsmaa.model.SMAAModel;
import fi.smaa.jsmaa.model.SMAAModelListener;
import fi.smaa.jsmaa.simulator.BuildQueue;
import fi.smaa.jsmaa.simulator.SMAA2Results;

@SuppressWarnings("serial")
public class BenefitRiskPM extends PresentationModel<BenefitRiskAnalysis>{

	private class AnalysisProgressListener implements ProgressListener {
		JProgressBar d_progBar;
		private MixedTreatmentComparison d_networkModel;

		public AnalysisProgressListener(MixedTreatmentComparison networkModel) {
			networkModel.addProgressListener(this);
			d_networkModel = networkModel;
		}
		
		public void attachBar(JProgressBar bar) {
			d_progBar = bar;
			bar.setVisible(!d_networkModel.isReady());
		}

		public void update(MixedTreatmentComparison mtc, ProgressEvent event) {
			if(event.getType() == EventType.SIMULATION_PROGRESS && d_progBar != null){
				d_progBar.setString("Simulating: " + event.getIteration()/(event.getTotalIterations()/100) + "%");
				d_progBar.setValue(event.getIteration()/(event.getTotalIterations()/100));
			} else if(event.getType() == EventType.BURNIN_PROGRESS && d_progBar != null){
				d_progBar.setString("Burn in: " + event.getIteration()/(event.getTotalIterations()/100) + "%");
				d_progBar.setValue(event.getIteration()/(event.getTotalIterations()/100));
			} else if(event.getType() == EventType.SIMULATION_FINISHED) {
				d_progBar.setVisible(false);
			}
		}
	}
	
	private class AllModelsReadyListener implements ProgressListener {
		private List<ConsistencyModel> d_models = new ArrayList<ConsistencyModel>();
		
		public void addModel(ConsistencyModel model) {
			model.addProgressListener(this);
			d_models.add(model);
		}

		public void update(MixedTreatmentComparison mtc, ProgressEvent event) {
			if (event.getType() == ProgressEvent.EventType.SIMULATION_FINISHED){
				if(allModelsReady()) {
					startSmaa();					
					firePropertyChange(PROPERTY_ALLMODELSREADY, false, true);
				}
			}
		}

		public boolean allModelsReady() {
			for (ConsistencyModel model : d_models)
				if (!model.isReady())
					return false;
			return true;
		}
	}
	
	public static final String PROPERTY_ALLMODELSREADY = "allModelsReady";
	
	private PresentationModelFactory d_pmf;
	private AllModelsReadyListener d_allModelsReadyListener;
	private List<AnalysisProgressListener> d_analysisProgressListeners;

	private RankAcceptabilityTableModel d_rankAccepTM;
	private RankAcceptabilitiesDataset d_rankAccepDS;	
	private BuildQueue d_buildQueue;

	private CentralWeightsDataset d_cwDS;

	private CentralWeightTableModel d_cwTM;

	private PreferencePresentationModel d_prefPresModel;

	private SMAAModel d_model;

	private SimulationProgressBar d_progressBar;
	
	public boolean allModelsReady() {
		return d_allModelsReadyListener.allModelsReady();
	}
	
	public BenefitRiskPM(BenefitRiskAnalysis bean, PresentationModelFactory pmf) {
		super(bean);
		
		d_pmf = pmf;
		d_allModelsReadyListener = new AllModelsReadyListener();
		d_analysisProgressListeners = new ArrayList<AnalysisProgressListener>();
		d_buildQueue = new BuildQueue();
		d_progressBar = new SimulationProgressBar();
		
		if (!startAllNetworkAnalyses())
			startSmaa();
	}
	
	private void startSmaa() {
		SMAAEntityFactory smaaf = new SMAAEntityFactory();
		d_model = smaaf.createSmaaModel(getBean());
		SMAA2Results emptyResults = new SMAA2Results(d_model.getAlternatives(), d_model.getCriteria(), 10);
		d_rankAccepDS = new RankAcceptabilitiesDataset(emptyResults);
		d_rankAccepTM = new RankAcceptabilityTableModel(emptyResults);
		d_cwTM = new CentralWeightTableModel(emptyResults);
		d_cwDS = new CentralWeightsDataset(emptyResults);
		d_prefPresModel = new PreferencePresentationModel(d_model);

		d_model.addModelListener(new SMAAModelListener() {
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
		d_buildQueue.add(new BRSMAASimulationBuilder(d_model,
				d_rankAccepTM, d_rankAccepDS, d_cwTM, d_cwDS, d_progressBar));
	}

	public int getNumProgBars() {
		return d_analysisProgressListeners.size();
	}
	
	public void attachProgBar(JProgressBar bar, int progBarNum) {
		if (progBarNum >= d_analysisProgressListeners.size() )
			throw new IllegalArgumentException();
		d_analysisProgressListeners.get(progBarNum).attachBar(bar);
	}
	
	public PreferencePresentationModel getSmaaPreferenceModel() {
		return null;
	}
	
	
	public List<PresentationModel<MetaAnalysis>> getAnalysesPMList() {
		List<PresentationModel<MetaAnalysis>> entitiesPMs = new ArrayList<PresentationModel<MetaAnalysis>>();
		for (MetaAnalysis a : getBean().getMetaAnalyses())
			entitiesPMs.add(d_pmf.getModel(a));
		return entitiesPMs;
	}

	public BenefitRiskMeasurementTableModel getMeasurementTableModel() {
		return new BenefitRiskMeasurementTableModel(getBean(), d_pmf);
	}
	
	public PreferenceScaleTableModel getPreferenceScaleTableModel() {
		return new PreferenceScaleTableModel(getBean(), d_pmf);
	}
	
	private boolean startAllNetworkAnalyses() {
		getBean().runAllConsistencyModels();
		boolean hasNetworks = false;
		for (MetaAnalysis ma : getBean().getMetaAnalyses() ){
			if (ma instanceof NetworkMetaAnalysis) {
				hasNetworks = true;
				ConsistencyModel consistencyModel = ((NetworkMetaAnalysis) ma).getConsistencyModel();
				d_allModelsReadyListener.addModel(consistencyModel);
				d_analysisProgressListeners.add(new AnalysisProgressListener(consistencyModel));
			}
		}
		return hasNetworks;
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
}
