package org.drugis.addis.presentation.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.drugis.addis.entities.Domain;
import org.drugis.addis.entities.Drug;
import org.drugis.addis.presentation.ListHolder;
import org.drugis.addis.presentation.ModifiableHolder;
import org.drugis.addis.presentation.PresentationModelFactory;
import org.drugis.addis.presentation.SelectableStudyGraphModel;
import org.drugis.addis.presentation.StudyGraphModel;
import org.drugis.addis.presentation.ValueHolder;
import org.drugis.addis.presentation.StudyGraphModel.Edge;
import org.drugis.addis.presentation.StudyGraphModel.Vertex;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.event.GraphEdgeChangeEvent;
import org.jgrapht.event.GraphListener;
import org.jgrapht.event.GraphVertexChangeEvent;

import com.jgoodies.binding.value.AbstractValueModel;
import com.jgoodies.binding.value.ValueModel;

public class NetworkMetaAnalysisWizardPM extends AbstractMetaAnalysisWizardPM<SelectableStudyGraphModel>{
	private DrugSelectionCompleteListener d_connectedDrugsSelectedModel;
	private StudyGraphModel d_selectedStudyGraph;
	private ValueHolder<Boolean> d_studySelectionCompleteModel;

	public NetworkMetaAnalysisWizardPM(Domain d, PresentationModelFactory pmm) {
		super(d, pmm);
		d_selectedStudyGraph = new StudyGraphModel(getStudyListModel().getSelectedStudiesModel(), 
				getSelectedDrugsModel());
		d_studySelectionCompleteModel = new StudySelectionCompleteListener();
	}

	@Override
	protected void buildDrugHolders() {
		d_connectedDrugsSelectedModel = new DrugSelectionCompleteListener();
		d_studyGraphPresentationModel.getSelectedDrugsModel().addValueChangeListener(d_connectedDrugsSelectedModel);
	}

	public ListHolder<Drug> getSelectedDrugsModel() {
		return d_studyGraphPresentationModel.getSelectedDrugsModel();
	}
	
	public StudyGraphModel getSelectedStudyGraphModel(){
		return d_selectedStudyGraph;
	}

	@Override
	protected SelectableStudyGraphModel buildStudyGraphPresentation() {
		return new SelectableStudyGraphModel(d_indicationHolder, d_endpointHolder, d_drugListHolder, d_domain);
	}
	
	public ValueModel getConnectedDrugsSelectedModel() {
		return d_connectedDrugsSelectedModel;
	}
	
	public ValueHolder<Boolean> getStudySelectionCompleteModel() {
		return d_studySelectionCompleteModel;
	}
	
	@SuppressWarnings("serial")
	public class StudySelectionCompleteListener extends AbstractValueModel 
	implements ValueHolder<Boolean> {
		private boolean d_value;
		
		public StudySelectionCompleteListener() {
			update();
			getSelectedStudyGraphModel().addGraphListener(new GraphListener<Vertex, Edge>() {
				
				public void vertexRemoved(GraphVertexChangeEvent<Vertex> e) {
					update();
				}
				
				public void vertexAdded(GraphVertexChangeEvent<Vertex> e) {
					update();
				}
				
				public void edgeRemoved(GraphEdgeChangeEvent<Vertex, Edge> e) {
					update();
				}
				
				public void edgeAdded(GraphEdgeChangeEvent<Vertex, Edge> e) {
					update();
				}
			});
		}
		
		private void update() {
			Boolean oldValue = d_value;
			Boolean newValue = selectedStudiesConnected();
			if (oldValue != newValue) {
				d_value = newValue;
				fireValueChange(oldValue, newValue);
			}
		}

		public Boolean getValue() {
			return d_value;
		}

		public void setValue(Object newValue) {
			throw new RuntimeException();
		}
	}
	
	private boolean selectedStudiesConnected() {
		ConnectivityInspector<Vertex, Edge> inspectorGadget = 
			new ConnectivityInspector<Vertex, Edge>(getSelectedStudyGraphModel());
		return inspectorGadget.isGraphConnected();
	}
	
	@SuppressWarnings("serial")
	private class DrugSelectionCompleteListener extends ModifiableHolder<Boolean> implements PropertyChangeListener {
		public DrugSelectionCompleteListener() {
			setValue(false);
		}
		
		@SuppressWarnings("unchecked")
		public void propertyChange(PropertyChangeEvent evt) {
			List<Drug> selectedDrugs = (List<Drug>) evt.getNewValue();	
			setValue(selectedDrugs.size() > 1 && d_studyGraphPresentationModel.isSelectionConnected());			
		}
	}
}
