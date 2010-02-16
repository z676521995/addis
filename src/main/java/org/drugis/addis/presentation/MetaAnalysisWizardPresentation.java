package org.drugis.addis.presentation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.drugis.addis.entities.Arm;
import org.drugis.addis.entities.Domain;
import org.drugis.addis.entities.Drug;
import org.drugis.addis.entities.EntityIdExistsException;
import org.drugis.addis.entities.Indication;
import org.drugis.addis.entities.OutcomeMeasure;
import org.drugis.addis.entities.Study;
import org.drugis.addis.entities.StudyArmsEntry;
import org.drugis.addis.entities.metaanalysis.RandomEffectsMetaAnalysis;

import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

public class MetaAnalysisWizardPresentation {
	
	@SuppressWarnings("serial") class IndicationHolder extends AbstractHolder<Indication> {
		@Override
		protected void cascade() {
			d_endpointHolder.unSet();
		}
		
		@Override
		protected void checkArgument(Object newValue) {
			if (!d_domain.getIndications().contains(newValue))
				throw new IllegalArgumentException("Indication not in the actual set!");
		}
	}
	
	@SuppressWarnings("serial")
	private class OutcomeMeasureHolder extends AbstractHolder<OutcomeMeasure> {
		@Override
		protected void checkArgument(Object newValue) {
			if (newValue != null)
				if (!getEndpointSet().contains(newValue))
					throw new IllegalArgumentException("Endpoint not in the actual set!");
		}

		@Override
		protected void cascade() {
			d_firstDrugHolder.unSet();
			d_secondDrugHolder.unSet();
		}
	}
	
	@SuppressWarnings("serial")
	private class DrugHolder extends AbstractHolder<Drug> {
		@Override
		protected void checkArgument(Object newValue) {
			if (newValue != null)
				if (!getDrugSet().contains(newValue))
					throw new IllegalArgumentException("Drug not in the actual set!");
		}

		protected void cascade() {
		}
	}
	
	@SuppressWarnings("serial")
	class ArmHolder extends AbstractHolder<Arm> {

		public ArmHolder(Arm arm) {
			setValue(arm);
		}
		
		@Override
		protected void cascade() {
		}

		@Override
		protected void checkArgument(Object newValue) {
		}
			
	}
	
	@SuppressWarnings("serial")
	private class IndicationListHolder extends AbstractListHolder<Indication> {
		@Override
		public List<Indication> getValue() {
			return new ArrayList<Indication>(d_domain.getIndications());
		}
	}
	
	@SuppressWarnings("serial")
	private class OutcomeListHolder extends AbstractListHolder<OutcomeMeasure> implements PropertyChangeListener {
		public OutcomeListHolder() {
			getIndicationModel().addValueChangeListener(this);
		}
		
		@Override
		public List<OutcomeMeasure> getValue() {
			return new ArrayList<OutcomeMeasure>(getEndpointSet());
		}

		public void propertyChange(PropertyChangeEvent event) {
			fireValueChange(null, getValue());
		}
	}
	
	@SuppressWarnings("serial")
	private class DrugListHolder extends AbstractListHolder<Drug> implements PropertyChangeListener {
		public DrugListHolder() {
			getEndpointModel().addValueChangeListener(this);
		}
		
		@Override
		public List<Drug> getValue() {
			return new ArrayList<Drug>(getDrugSet());
		}

		public void propertyChange(PropertyChangeEvent evt) {
			fireValueChange(null, getValue());
		}
	}
	
	@SuppressWarnings("serial")
	private class ArmListHolder extends AbstractListHolder<Arm> implements PropertyChangeListener {
		Study d_study;
		Drug d_drug;
		
		public ArmListHolder(Study s, Drug d) {
			d_study = s;
			d_drug = d;
			
			d_study.addPropertyChangeListener(this);
			d_drug.addPropertyChangeListener(this);
		}

		@Override
		public List<Arm> getValue() {
			return getArmPerStudyDrug(d_study,d_drug);
		}

		public void propertyChange(PropertyChangeEvent arg0) {
			fireValueChange(null,getValue());			
		}
	}
	
	@SuppressWarnings("serial")
	private class StudyListHolder extends AbstractListHolder<Study> implements PropertyChangeListener {
		public StudyListHolder() {
			getFirstDrugModel().addValueChangeListener(this);
			getSecondDrugModel().addValueChangeListener(this);
		}
		
		@Override
		public List<Study> getValue() {
			return d_studyList;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			updateStudyList();
			fireValueChange(null, getValue());
		}
	}
	
	@SuppressWarnings("serial")
	public class DrugsSelectedCompleteListener extends AbstractValueModel implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange(PROPERTYNAME_VALUE, null, getValue());
		}

		public Object getValue() {
			boolean val = d_firstDrugHolder.getValue() != null && d_secondDrugHolder.getValue() != null;
			return new Boolean(val);
		}

		public void setValue(Object newValue) {			
		}		
	}
		
	private Domain d_domain;
	private AbstractHolder<Indication> d_indicationHolder;
	private AbstractHolder<OutcomeMeasure> d_endpointHolder;
	private StudiesMeasuringValueModel d_studiesMeasuringValueModel;	
	private DrugHolder d_firstDrugHolder;
	private DrugHolder d_secondDrugHolder;
	private OutcomeListHolder d_outcomeListHolder;
	private DrugListHolder d_drugListHolder;
	private DefaultSelectableStudyListPresentationModel d_studyListPm;
	private MetaAnalysisCompleteListener d_metaAnalysisCompleteListener;
	private List<Study> d_studyList = new ArrayList<Study>();
	private PresentationModelFactory d_pmm;
	private Map<Study, ArmHolder> d_firstArms;
	private Map<Study, ArmHolder> d_secondArms;
	private DrugsSelectedCompleteListener d_drugsSelectedCompleteModel;
	private StudyGraphPresentation d_studyGraphPresentationModel;
	
	public MetaAnalysisWizardPresentation(Domain d, PresentationModelFactory pmm) {
		d_domain = d;
		d_pmm = pmm;
		d_indicationHolder = new IndicationHolder();
		d_endpointHolder = new OutcomeMeasureHolder();
		d_firstDrugHolder = new DrugHolder();
		d_studiesMeasuringValueModel = new StudiesMeasuringValueModel();		
		d_secondDrugHolder = new DrugHolder();
		d_outcomeListHolder = new OutcomeListHolder();
		d_drugListHolder = new DrugListHolder();
		
		d_firstDrugHolder.addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() != null && evt.getNewValue().equals(getSecondDrug())) {
					d_secondDrugHolder.unSet();
				}									
			}
		});
		d_secondDrugHolder.addValueChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getNewValue() != null && evt.getNewValue().equals(getFirstDrug())) {
					d_firstDrugHolder.unSet();
				}									
			}			
		});
		d_studyListPm = new DefaultSelectableStudyListPresentationModel(new StudyListHolder());
				
		d_metaAnalysisCompleteListener = new MetaAnalysisCompleteListener();		
		d_studyListPm.getSelectedStudiesModel().addValueChangeListener(d_metaAnalysisCompleteListener);
		
		d_drugsSelectedCompleteModel = new DrugsSelectedCompleteListener();
		d_firstDrugHolder.addValueChangeListener(d_drugsSelectedCompleteModel);
		d_secondDrugHolder.addValueChangeListener(d_drugsSelectedCompleteModel);		
				
		d_studyListPm.getSelectedStudiesModel().addValueChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent arg0) {
				updateArmHolders();
			}
		});
		d_firstArms = new HashMap <Study, ArmHolder>() ;
		d_secondArms = new HashMap <Study, ArmHolder>() ;
		
		d_studyGraphPresentationModel = new StudyGraphPresentation(d_indicationHolder,
				d_endpointHolder, d_drugListHolder, d_domain);		
	}
		
	public ListHolder<Indication> getIndicationListModel() {
		return new IndicationListHolder();
	}
	
	public ValueModel getIndicationModel() {
		return d_indicationHolder; 
	}
	
	public AbstractListHolder<OutcomeMeasure> getOutcomeMeasureListModel() {
		return d_outcomeListHolder;
	}
	
	private SortedSet<OutcomeMeasure> getEndpointSet() {
		TreeSet<OutcomeMeasure> endpoints = new TreeSet<OutcomeMeasure>();
		if (getIndication() != null) {
			for (Study s : d_domain.getStudies(getIndication()).getValue()) {
				endpoints.addAll(s.getOutcomeMeasures());
			}			
		}	
		return endpoints;
	}
	
	public ValueModel getEndpointModel() {
		return d_endpointHolder;
	}
	
	public AbstractListHolder<Drug> getDrugListModel() {
		return d_drugListHolder;
	}
	
	private SortedSet<Drug> getDrugSet() {
		SortedSet<Drug> drugs = new TreeSet<Drug>();
		if (getIndication() != null && getEndpoint() != null) {
			List<Study> studies = getStudiesEndpointAndIndication();
			for (Study s : studies) {
				drugs.addAll(s.getDrugs());
			}
		}
		return drugs;
	}
	


	private List<Study> getStudiesEndpointAndIndication() {
		List<Study> studies = new ArrayList<Study>(d_domain.getStudies(getEndpoint()).getValue());
		studies.retainAll(d_domain.getStudies(getIndication()).getValue());
		return studies;
	}

	private Indication getIndication() {
		return d_indicationHolder.getValue();
	}

	private OutcomeMeasure getEndpoint() {
		return d_endpointHolder.getValue();
	}
	
	public ValueModel getFirstDrugModel() {
		return d_firstDrugHolder;
	}
	
	public ValueModel getSecondDrugModel() {
		return d_secondDrugHolder;
	}
	
	private void updateStudyList() {
		List<Study> studies = new ArrayList<Study>();
		if (getSecondDrug() != null && getFirstDrug() != null) {
			studies = getStudiesEndpointAndIndication();
			studies.retainAll(d_domain.getStudies(getFirstDrug()).getValue());
			studies.retainAll(d_domain.getStudies(getSecondDrug()).getValue());
		}
		d_studyList = studies;
	}
	
	private void updateArmHolders() {
		d_firstArms.clear();
		d_secondArms.clear();
		for (Study s : getStudyListModel().getSelectedStudiesModel().getValue()) {
			d_firstArms.put(s, new ArmHolder(getArmsPerStudyPerDrug(s, getFirstDrug()).getValue().get(0)));
			d_secondArms.put(s, new ArmHolder(getArmsPerStudyPerDrug(s, getSecondDrug()).getValue().get(0)));
		}
	}
	
	private Drug getFirstDrug() {
		return d_firstDrugHolder.getValue();
	}

	private Drug getSecondDrug() {
		return d_secondDrugHolder.getValue();
	}
	
	public ValueModel getStudiesMeasuringLabelModel() {
		return d_studiesMeasuringValueModel;
	}
	
	@SuppressWarnings("serial")
	public class StudiesMeasuringValueModel extends AbstractValueModel implements PropertyChangeListener {
		
		public StudiesMeasuringValueModel() {
			// NB indication listening automatically via endpoint cascade
			d_endpointHolder.addValueChangeListener(this);			
		}

		public Object getValue() {
			return constructString();
		}

		private Object constructString() {
			String indVal = d_indicationHolder.getValue() != null ? d_indicationHolder.getValue().toString() : "";
			String endpVal = d_endpointHolder.getValue() != null ? d_endpointHolder.getValue().toString() : "";
			return "Studies measuring " + indVal + " on " + endpVal;
		}
		
		public void setValue(Object newValue) {
			throw new RuntimeException("value set not allowed");
		}

		public void propertyChange(PropertyChangeEvent arg0) {
			fireValueChange(null, constructString());
		}		
	}

	public SelectableStudyListPresentationModel getStudyListModel() {
		return d_studyListPm;
	}
	
	private RandomEffectsMetaAnalysis createMetaAnalysis() {
		List<StudyArmsEntry> studyArms = new ArrayList <StudyArmsEntry>();
		
		for(Entry<Study,ArmHolder> entry : d_firstArms.entrySet()) {
			studyArms.add(new StudyArmsEntry(entry.getKey(), entry.getValue().getValue(), d_secondArms.get(entry.getKey()).getValue() ) );
		}
		
		return new RandomEffectsMetaAnalysis("", (OutcomeMeasure) getEndpointModel().getValue(), studyArms);
	}
	
	public RandomEffectsMetaAnalysisPresentation saveMetaAnalysis(String name) throws EntityIdExistsException {
		RandomEffectsMetaAnalysis ma = createMetaAnalysis();		
		ma.setName(name);
		d_domain.addMetaAnalysis(ma);
		return (RandomEffectsMetaAnalysisPresentation) d_pmm.getModel(ma);
	}
	
	public ValueModel getMetaAnalysisCompleteModel() {
		return d_metaAnalysisCompleteListener;
	}
	
	@SuppressWarnings("serial")
	public class MetaAnalysisCompleteListener extends AbstractValueModel implements PropertyChangeListener {

		public void propertyChange(PropertyChangeEvent evt) {
			firePropertyChange(PROPERTYNAME_VALUE, null, getValue());
		}

		public Object getValue() {
			return new Boolean(!d_studyListPm.getSelectedStudiesModel().getValue().isEmpty());
		}

		public void setValue(Object newValue) {			
		}		
	}

	public RandomEffectsMetaAnalysisPresentation getMetaAnalysisModel() {
		return (RandomEffectsMetaAnalysisPresentation) d_pmm.getModel(createMetaAnalysis());
	}

	private List<Arm> getArmPerStudyDrug(Study study, Drug drug) {
		ArrayList<Arm> armList = new ArrayList<Arm>();
		for (Arm curArm : study.getArms()) {
			if (curArm.getDrug().equals(drug)) {
				armList.add(curArm);
			}
		}
		return armList;
	}
	
	public ListHolder<Arm> getArmsPerStudyPerDrug(Study study, Drug drug) {
		return new ArmListHolder(study, drug);
	}
	
	public ValueModel getLeftArmPerStudyPerDrug(Study study) {
		return d_firstArms.get(study);
	}
	
	public ValueModel getRightArmPerStudyPerDrug(Study study) {
		return d_secondArms.get(study);
	}

	public ValueModel getDrugsSelectedCompleteModel() {
		return d_drugsSelectedCompleteModel;
	}

	public StudyGraphPresentation getStudyGraphModel() {
		return d_studyGraphPresentationModel;
	}
}
