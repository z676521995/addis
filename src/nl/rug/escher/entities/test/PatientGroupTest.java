package nl.rug.escher.entities.test;

import static org.junit.Assert.*;
import static org.easymock.EasyMock.*;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import nl.rug.escher.entities.Dose;
import nl.rug.escher.entities.Drug;
import nl.rug.escher.entities.Endpoint;
import nl.rug.escher.entities.ContinuousMeasurement;
import nl.rug.escher.entities.PatientGroup;
import nl.rug.escher.entities.SIUnit;
import nl.rug.escher.entities.Study;

import org.junit.Test;

public class PatientGroupTest {
	@Test
	public void testSetStudy() {
		Helper.testSetter(new PatientGroup(), PatientGroup.PROPERTY_STUDY, null, new Study());
	}
	
	@Test
	public void testSetSize() {
		Helper.testSetter(new PatientGroup(), PatientGroup.PROPERTY_SIZE, null, 1);
	}
	
	@Test
	public void testSetDrug() {
		Helper.testSetter(new PatientGroup(), PatientGroup.PROPERTY_DRUG, null, new Drug());
	}
	
	@Test
	public void testSetDose() {
		Helper.testSetter(new PatientGroup(), PatientGroup.PROPERTY_DOSE, null, new Dose());
	}
	
	@Test
	public void testInitialMeasurements() {
		PatientGroup p = new PatientGroup();
		assertNotNull(p.getMeasurements());
		assertTrue(p.getMeasurements().isEmpty());
	}
	
	@Test
	public void testSetMeasurements() {
		List<ContinuousMeasurement> list = Collections.singletonList(new ContinuousMeasurement());
		Helper.testSetter(new PatientGroup(), PatientGroup.PROPERTY_MEASUREMENTS, Collections.EMPTY_LIST, 
				list);
	}
	
	@Test
	public void testAddMeasurement() {
		Helper.testAdder(new PatientGroup(), PatientGroup.PROPERTY_MEASUREMENTS,
				"addMeasurement", new ContinuousMeasurement());
	}
	
	@Test
	public void testToString() {
		PatientGroup group = new PatientGroup();
		assertEquals("INCOMPLETE", group.toString());
		
		Dose dose = new Dose();
		dose.setQuantity(25.5);
		dose.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		group.setDose(dose);
		Drug drug = new Drug();
		drug.setName("Fluoxetine");
		group.setDrug(drug);
		assertEquals("Fluoxetine " + dose.toString(), group.toString());
	}
	
	@Test
	public void testFireLabelChanged() {
		PatientGroup group;
		PropertyChangeListener l;
		Drug drug = new Drug();
		drug.setName("Fluoxetine");
		
		group = new PatientGroup();
		group.setDrug(drug);
		Dose dose = new Dose();
		dose.setQuantity(25.5);
		dose.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		group.setDose(dose);
		String expect = group.toString();
		group.setDose(null);
		l = Helper.mockListener(group, PatientGroup.PROPERTY_LABEL, "INCOMPLETE", expect);
		group.addPropertyChangeListener(l);
		group.setDose(dose);
		verify(l);
		
		group = new PatientGroup();
		group.setDose(dose);
		Drug drug2 = new Drug();
		drug2.setName("Paroxetine");
		group.setDrug(drug2);
		l = Helper.mockListener(group, PatientGroup.PROPERTY_LABEL, group.toString(), expect);
		group.addPropertyChangeListener(l);
		group.setDrug(drug);
		verify(l);
	}
	
	@Test
	public void testGetMeasurementByEndpoint() {
		Endpoint e1 = new Endpoint();
		e1.setName("e1");
		Endpoint e2 = new Endpoint();
		e2.setName("e2");
		Endpoint e3 = new Endpoint();
		e3.setName("e3");
		
		ContinuousMeasurement m1 = new ContinuousMeasurement();
		m1.setEndpoint(e1);
		ContinuousMeasurement m2 = new ContinuousMeasurement();
		m2.setEndpoint(e2);
		
		PatientGroup g = new PatientGroup();
		g.addMeasurement(m2);
		g.addMeasurement(m1);
		
		assertEquals(m2, g.getMeasurement(e2));
		assertEquals(null, g.getMeasurement(e3));
	}
}
