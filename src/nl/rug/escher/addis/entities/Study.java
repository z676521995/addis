package nl.rug.escher.addis.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.annotations.Element;
import javax.jdo.annotations.FetchGroup;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import com.jgoodies.binding.beans.Model;

@PersistenceCapable(identityType=IdentityType.APPLICATION,detachable="true")
@FetchGroup(name="default",members={@Persistent(name="endpoints"),@Persistent(name="patientGroups")})
public class Study extends Model {
	private String d_id;
	private List<Endpoint> d_endpoints;
	private List<PatientGroup> d_patientGroups;

	public final static String PROPERTY_ID = "id";
	public final static String PROPERTY_ENDPOINTS = "endpoints";
	public final static String PROPERTY_PATIENTGROUPS = "patientGroups";

	public Study() {
		d_endpoints = new ArrayList<Endpoint>();
		d_patientGroups = new ArrayList<PatientGroup>();
	}
	
	public Study(Endpoint e) {
		this();
		d_endpoints.add(e);
	}
	
	@Persistent(primaryKey="true")
	public String getId() {
		return d_id;
	}

	public void setId(String id) {
		String oldVal = d_id;
		d_id = id;
		firePropertyChange(PROPERTY_ID, oldVal, d_id);
	}
	
	@Persistent
	public List<Endpoint> getEndpoints() {
		return d_endpoints;
	}

	public void setEndpoints(List<Endpoint> endpoints) {
		List<Endpoint> oldVal = d_endpoints;
		d_endpoints = endpoints;
		firePropertyChange(PROPERTY_ENDPOINTS, oldVal, d_endpoints);
	}

	@Element
	public List<PatientGroup> getPatientGroups() {
		return d_patientGroups;
	}

	public void setPatientGroups(List<PatientGroup> patientGroups) {
		List<PatientGroup> oldVal = d_patientGroups;
		d_patientGroups = patientGroups;
		firePropertyChange(PROPERTY_PATIENTGROUPS, oldVal, d_patientGroups);
	}
	
	public void addPatientGroup(PatientGroup group) {
		List<PatientGroup> newVal = new ArrayList<PatientGroup>(d_patientGroups);
		newVal.add(group);
		setPatientGroups(newVal);
	}
	
	public String toString() {
		return getId();
	}
	
	public void addEndpoint(Endpoint endpoint) {
		List<Endpoint> newVal = new ArrayList<Endpoint>(d_endpoints);
		newVal.add(endpoint);
		setEndpoints(newVal);
	}

	public Set<Drug> getDrugs() {
		Set<Drug> drugs = new HashSet<Drug>();
		for (PatientGroup g : getPatientGroups()) {
			drugs.add(g.getDrug());
		}
		return drugs;
	}
}
