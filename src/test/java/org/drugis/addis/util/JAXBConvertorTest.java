package org.drugis.addis.util;

import static org.drugis.addis.entities.AssertEntityEquals.assertDomainEquals;
import static org.drugis.addis.entities.AssertEntityEquals.assertEntityEquals;
import static org.drugis.addis.util.JAXBConvertor.nameReference;
import static org.drugis.common.JUnitUtil.assertAllAndOnly;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import javolution.text.CharArray;
import javolution.xml.XMLFormat;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLReferenceResolver;
import javolution.xml.stream.XMLStreamException;

import org.drugis.addis.ExampleData;
import org.drugis.addis.entities.AdverseEvent;
import org.drugis.addis.entities.Arm;
import org.drugis.addis.entities.BasicContinuousMeasurement;
import org.drugis.addis.entities.BasicRateMeasurement;
import org.drugis.addis.entities.BasicStudyCharacteristic;
import org.drugis.addis.entities.CategoricalPopulationCharacteristic;
import org.drugis.addis.entities.CharacteristicsMap;
import org.drugis.addis.entities.ContinuousPopulationCharacteristic;
import org.drugis.addis.entities.Domain;
import org.drugis.addis.entities.DomainData;
import org.drugis.addis.entities.DomainImpl;
import org.drugis.addis.entities.Drug;
import org.drugis.addis.entities.Endpoint;
import org.drugis.addis.entities.EntityIdExistsException;
import org.drugis.addis.entities.FixedDose;
import org.drugis.addis.entities.FlexibleDose;
import org.drugis.addis.entities.FrequencyMeasurement;
import org.drugis.addis.entities.Indication;
import org.drugis.addis.entities.Measurement;
import org.drugis.addis.entities.Note;
import org.drugis.addis.entities.ObjectWithNotes;
import org.drugis.addis.entities.PopulationCharacteristic;
import org.drugis.addis.entities.PubMedId;
import org.drugis.addis.entities.PubMedIdList;
import org.drugis.addis.entities.SIUnit;
import org.drugis.addis.entities.Source;
import org.drugis.addis.entities.Study;
import org.drugis.addis.entities.StudyArmsEntry;
import org.drugis.addis.entities.Variable;
import org.drugis.addis.entities.BasicStudyCharacteristic.Allocation;
import org.drugis.addis.entities.BasicStudyCharacteristic.Blinding;
import org.drugis.addis.entities.BasicStudyCharacteristic.Status;
import org.drugis.addis.entities.OutcomeMeasure.Direction;
import org.drugis.addis.entities.Study.MeasurementKey;
import org.drugis.addis.entities.Variable.Type;
import org.drugis.addis.entities.analysis.BenefitRiskAnalysis;
import org.drugis.addis.entities.analysis.MetaAnalysis;
import org.drugis.addis.entities.analysis.MetaBenefitRiskAnalysis;
import org.drugis.addis.entities.analysis.NetworkMetaAnalysis;
import org.drugis.addis.entities.analysis.RandomEffectsMetaAnalysis;
import org.drugis.addis.entities.analysis.StudyBenefitRiskAnalysis;
import org.drugis.addis.entities.analysis.BenefitRiskAnalysis.AnalysisType;
import org.drugis.addis.entities.data.AddisData;
import org.drugis.addis.entities.data.Alternative;
import org.drugis.addis.entities.data.AnalysisArms;
import org.drugis.addis.entities.data.ArmReference;
import org.drugis.addis.entities.data.ArmReferences;
import org.drugis.addis.entities.data.Arms;
import org.drugis.addis.entities.data.BenefitRiskAnalyses;
import org.drugis.addis.entities.data.CategoricalVariable;
import org.drugis.addis.entities.data.CategoryMeasurement;
import org.drugis.addis.entities.data.Characteristics;
import org.drugis.addis.entities.data.ContinuousMeasurement;
import org.drugis.addis.entities.data.ContinuousVariable;
import org.drugis.addis.entities.data.DateWithNotes;
import org.drugis.addis.entities.data.DrugReferences;
import org.drugis.addis.entities.data.Measurements;
import org.drugis.addis.entities.data.MetaAnalyses;
import org.drugis.addis.entities.data.MetaAnalysisReferences;
import org.drugis.addis.entities.data.NameReferenceWithNotes;
import org.drugis.addis.entities.data.Notes;
import org.drugis.addis.entities.data.OutcomeMeasure;
import org.drugis.addis.entities.data.OutcomeMeasuresReferences;
import org.drugis.addis.entities.data.RateMeasurement;
import org.drugis.addis.entities.data.RateVariable;
import org.drugis.addis.entities.data.References;
import org.drugis.addis.entities.data.StudyOutcomeMeasure;
import org.drugis.addis.entities.data.StudyOutcomeMeasures;
import org.drugis.addis.imports.PubMedDataBankRetriever;
import org.drugis.addis.util.JAXBConvertor.ConversionException;
import org.drugis.common.Interval;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.org.apache.xerces.internal.jaxp.datatype.XMLGregorianCalendarImpl;

public class JAXBConvertorTest {
	private JAXBContext d_jaxb;
	private Unmarshaller d_unmarshaller;
	
	@Before
	public void setup() throws JAXBException{
		d_jaxb = JAXBContext.newInstance("org.drugis.addis.entities.data" );
		d_unmarshaller = d_jaxb.createUnmarshaller();
	}
	
	@Test
	public void testConvertContinuousEndpoint() throws ConversionException {
		String name = "Onset of erection";
		String desc = "Time to onset of erection of >= 60 % rigidity";
		String unit = "Minutes";
		Direction dir = Direction.LOWER_IS_BETTER;
		
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName(name);
		m.setDescription(desc);
		ContinuousVariable value = new ContinuousVariable();
		value.setDirection(dir);
		value.setUnitOfMeasurement(unit);
		m.setContinuous(value);
		
		Endpoint e = new Endpoint(name, Type.CONTINUOUS, dir);
		e.setDescription(desc);
		e.setUnitOfMeasurement(unit);
		
		assertEntityEquals(e, JAXBConvertor.convertEndpoint(m));
		assertEquals(m, JAXBConvertor.convertEndpoint(e));
	}
	
	@Test
	public void testConvertRateEndpoint() throws ConversionException {
		String name = "Efficacy";
		String desc = "Erection of >= 60% rigidity within 1 hr of medication";
		Direction dir = Direction.HIGHER_IS_BETTER;
		
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName(name);
		m.setDescription(desc);
		RateVariable value = new RateVariable();
		value.setDirection(dir);
		m.setRate(value);
		
		Endpoint e = new Endpoint(name, Type.RATE, dir);
		e.setDescription(desc);
		
		assertEntityEquals(e, JAXBConvertor.convertEndpoint(m));
		
		value.setDirection(Direction.LOWER_IS_BETTER);
		e.setDirection(org.drugis.addis.entities.OutcomeMeasure.Direction.LOWER_IS_BETTER);
		
		assertEntityEquals(e, JAXBConvertor.convertEndpoint(m));
		assertEquals(m, JAXBConvertor.convertEndpoint(e));
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertCategoricalEndpointThrows() throws ConversionException {
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName("Gender");
		m.setDescription("Which gender you turn out to be after taking medication");
		CategoricalVariable value = new CategoricalVariable();
		m.setCategorical(value);
		
		JAXBConvertor.convertEndpoint(m);
	}

	@Test
	public void testConvertContinuousAdverseEvent() throws ConversionException {
		String name = "Onset of erection";
		String desc = "Time to onset of erection of >= 60 % rigidity";
		String unit = "Minutes";
		
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName(name);
		m.setDescription(desc);
		ContinuousVariable value = new ContinuousVariable();
		value.setDirection(Direction.LOWER_IS_BETTER);
		value.setUnitOfMeasurement(unit);
		m.setContinuous(value);
		
		AdverseEvent e = new AdverseEvent(name, Type.CONTINUOUS);
		e.setDescription(desc);
		e.setUnitOfMeasurement(unit);
		
		assertEntityEquals(e, JAXBConvertor.convertAdverseEvent(m));
		assertEquals(m, JAXBConvertor.convertAdverseEvent(e));
	}
	
	@Test
	public void testConvertRateAdverseEvent() throws ConversionException {
		String name = "Seizure";
		String desc = "Its bad hmmkay";
		
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName(name);
		m.setDescription(desc);
		RateVariable value = new RateVariable();
		value.setDirection(Direction.LOWER_IS_BETTER);
		m.setRate(value);
		
		AdverseEvent e = new AdverseEvent(name, Type.RATE);
		e.setDescription(desc);
		
		assertEntityEquals(e, JAXBConvertor.convertAdverseEvent(m));
		
		value.setDirection(Direction.HIGHER_IS_BETTER);
		e.setDirection(org.drugis.addis.entities.OutcomeMeasure.Direction.HIGHER_IS_BETTER);
		
		assertEntityEquals(e, JAXBConvertor.convertAdverseEvent(m));
		assertEquals(m, JAXBConvertor.convertAdverseEvent(e));
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertCategoricalAdverseEventThrows() throws ConversionException {
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName("Efficacy");
		m.setDescription("Erection of >= 60% rigidity within 1 hr of medication");
		CategoricalVariable value = new CategoricalVariable();
		m.setCategorical(value);
		
		JAXBConvertor.convertAdverseEvent(m);
	}

	@Test
	public void testConvertContinuousPopChar() throws ConversionException {
		String name = "Age";
		String desc = "Age (years since birth)";
		String unit = "Years";
		
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName(name);
		m.setDescription(desc);
		ContinuousVariable value = new ContinuousVariable();
		value.setUnitOfMeasurement(unit);
		m.setContinuous(value);
		
		ContinuousPopulationCharacteristic p = new ContinuousPopulationCharacteristic(name);
		p.setUnitOfMeasurement(unit);
		p.setDescription(desc);
		
		assertEntityEquals(p, JAXBConvertor.convertPopulationCharacteristic(m));
		assertEquals(m, JAXBConvertor.convertPopulationCharacteristic(p));
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertRatePopCharThrows() throws ConversionException {
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName("Seizure");
		m.setDescription("Its bad hmmkay");
		RateVariable value = new RateVariable();
		value.setDirection(Direction.LOWER_IS_BETTER);
		m.setRate(value);
		
		JAXBConvertor.convertPopulationCharacteristic(m);
	}
	
	@Test
	public void testConvertCategoricalPopChar() throws ConversionException {
		String name = "Smoking habits";
		String desc = "Classification of smoking habits";
		String[] categories = new String[] {"Non-smoker", "Smoker", "Ex-smoker"};
		
		OutcomeMeasure m = new OutcomeMeasure();
		m.setName(name);
		m.setDescription(desc);
		CategoricalVariable var = new CategoricalVariable();
		for (String s : categories) {
			var.getCategory().add(s);
		}
		m.setCategorical(var);
		
		CategoricalPopulationCharacteristic catChar = new CategoricalPopulationCharacteristic(name, categories);
		catChar.setDescription(desc);
		
		assertEntityEquals(catChar, JAXBConvertor.convertPopulationCharacteristic(m));
		assertEquals(m, JAXBConvertor.convertPopulationCharacteristic(catChar));
	}

	@Test
	public void testConvertIndication() {
		String name = "Erectile Dysfunction";
		long code = 12;
		
		org.drugis.addis.entities.data.Indication i1 = new org.drugis.addis.entities.data.Indication(); 
		i1.setCode(code);
		i1.setName(name);
	
		Indication i2 = new Indication(code, name);
		
		assertEntityEquals(i2, JAXBConvertor.convertIndication(i1));
		assertEquals(i1, JAXBConvertor.convertIndication(i2));
	}
	
	@Test
	public void testConvertDrug() {
		String name = "Sildenafil";
		String code = "G04BE03";
		
		org.drugis.addis.entities.data.Drug d1 = new org.drugis.addis.entities.data.Drug(); 
		d1.setAtcCode(code);
		d1.setName(name);
	
		Drug d2 = new Drug(name, code);
		
		assertEntityEquals(d2, JAXBConvertor.convertDrug(d1));
		assertEquals(d1, JAXBConvertor.convertDrug(d2));
	}
	
	@Test
	public void testConvertArm() throws ConversionException {
		int size = 99;
		String name = "Sildenafil";
		String code = "G04BE03";
		double quantity = 12.5;
		double maxQuantity = 34.5;
		
		Domain domain = new DomainImpl();
		Drug drug = new Drug(name, code);
		domain.addDrug(drug);
		
		org.drugis.addis.entities.data.Arm arm1 = new org.drugis.addis.entities.data.Arm();
		arm1.setId(null);
		arm1.setSize(size);
		arm1.setNotes(new Notes());
		org.drugis.addis.entities.data.FixedDose fixDose = new org.drugis.addis.entities.data.FixedDose();
		fixDose.setQuantity(quantity);
		fixDose.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		arm1.setFixedDose(fixDose);
		arm1.setDrug(nameReference(name));
		
		Arm arm2 = buildFixedDoseArm(size, drug, quantity);
		
		assertEntityEquals(arm2, JAXBConvertor.convertArm(arm1, domain));
		assertEquals(arm1, JAXBConvertor.convertArm(arm2));
		
		arm1.setFixedDose(null);
		org.drugis.addis.entities.data.FlexibleDose flexDose = new org.drugis.addis.entities.data.FlexibleDose();
		flexDose.setMinDose(quantity);
		flexDose.setMaxDose(maxQuantity);
		flexDose.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		arm1.setFlexibleDose(flexDose);
		
		Arm arm3 = buildFlexibleDoseArm(size, drug, quantity, maxQuantity);
		
		assertEntityEquals(arm3, JAXBConvertor.convertArm(arm1, domain));
		assertEquals(arm1, JAXBConvertor.convertArm(arm3));
	}

	private Arm buildFixedDoseArm(int size, Drug drug, double quantity) {
		return new Arm(drug, new FixedDose(quantity, SIUnit.MILLIGRAMS_A_DAY), size);
	}

	private Arm buildFlexibleDoseArm(int size, Drug drug, double minQuantity, double maxQuantity) {
		return new Arm(drug, new FlexibleDose(new Interval<Double> (minQuantity, maxQuantity), SIUnit.MILLIGRAMS_A_DAY), size);
	}
	
	@Test
	public void testConvertArms() throws ConversionException {
		int size1 = 99;
		int size2 = 101;
		String name = "Sildenafil";
		String code = "G04BE03";
		double quantity = 10.0;
		double minQuantity = 12.5;
		double maxQuantity = 34.5;
		
		Domain domain = new DomainImpl();
		Drug drug = new Drug(name, code);
		domain.addDrug(drug);
		
		org.drugis.addis.entities.data.Arm arm1 = buildFixedDoseArmData(1, size1, name, quantity);
		
		org.drugis.addis.entities.data.Arm arm2 = buildFlexibleDoseArmData(2, size2, name, minQuantity, maxQuantity);
		
		Arms arms = new Arms();
		arms.getArm().add(arm1);
		arms.getArm().add(arm2);
		
		LinkedHashMap<Integer, Arm> convertedArms = JAXBConvertor.convertStudyArms(arms, domain);
		Set<Integer> keys = new HashSet<Integer>();
		keys.add(1);
		keys.add(2);
		
		assertEquals(keys , convertedArms.keySet());		
		assertEquals(size1, (int)convertedArms.get(1).getSize());		
		assertEquals(size2, (int)convertedArms.get(2).getSize());
		assertEquals(FixedDose.class, convertedArms.get(1).getDose().getClass());
		assertEquals(FlexibleDose.class, convertedArms.get(2).getDose().getClass());
		
		assertEquals(arms, JAXBConvertor.convertStudyArms(convertedArms));
	}

	private org.drugis.addis.entities.data.Arm buildFlexibleDoseArmData(
			Integer id, int size2, String name, double minQuantity, double maxQuantity) {
		org.drugis.addis.entities.data.Arm newArm = new org.drugis.addis.entities.data.Arm();
		newArm.setId(id);
		newArm.setSize(size2);
		newArm.setNotes(new Notes());
		org.drugis.addis.entities.data.FlexibleDose flexDose = new org.drugis.addis.entities.data.FlexibleDose();
		flexDose.setMinDose(minQuantity);
		flexDose.setMaxDose(maxQuantity);
		flexDose.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		newArm.setFlexibleDose(flexDose);
		newArm.setDrug(nameReference(name));
		return newArm;
	}

	private org.drugis.addis.entities.data.Arm buildFixedDoseArmData(
			Integer id, int size1, String name, double quantity) {
		org.drugis.addis.entities.data.Arm newArm = new org.drugis.addis.entities.data.Arm();
		newArm.setId(id);
		newArm.setSize(size1);
		newArm.setNotes(new Notes());
		org.drugis.addis.entities.data.FixedDose fixDose = new org.drugis.addis.entities.data.FixedDose();
		fixDose.setQuantity(quantity);
		fixDose.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		newArm.setFixedDose(fixDose);
		newArm.setDrug(nameReference(name));
		return newArm;
	}
	
	@Test
	public void testConvertStudyChars() {
		Allocation alloc = Allocation.RANDOMIZED;
		Blinding blind = Blinding.UNKNOWN;
		String title = "MyStudy";
		int centers = 5;
		String objective = "The loftiest of goals";
		String incl = "Obesity";
		String excl = "Diabetes";
		Status status = Status.ENROLLING;
		Source source = Source.MANUAL;
		GregorianCalendar studyStart = new GregorianCalendar(2008, 8, 12);
		GregorianCalendar studyEnd = new GregorianCalendar(2010, 1, 18);
		GregorianCalendar created = new GregorianCalendar(2011, 2, 15);
		PubMedIdList pmids = new PubMedIdList();
		pmids.add(new PubMedId("1"));
		pmids.add(new PubMedId("12345"));
		List<BigInteger> pmints = new ArrayList<BigInteger>();
		for (PubMedId id : pmids) {
			pmints.add(new BigInteger(id.getId()));
		}
		
		org.drugis.addis.entities.data.Characteristics chars1 = new org.drugis.addis.entities.data.Characteristics();
		chars1.setTitle(JAXBConvertor.stringWithNotes(title));
		chars1.setAllocation(JAXBConvertor.allocationWithNotes(alloc));
		chars1.setBlinding(JAXBConvertor.blindingWithNotes(blind));
		chars1.setCenters(JAXBConvertor.intWithNotes(centers));
		chars1.setObjective(JAXBConvertor.stringWithNotes(objective));
		chars1.setStudyStart(JAXBConvertor.dateWithNotes(studyStart.getTime()));
		chars1.setStudyEnd(JAXBConvertor.dateWithNotes(studyEnd.getTime()));
		chars1.setStatus(JAXBConvertor.statusWithNotes(status));
		chars1.setInclusion(JAXBConvertor.stringWithNotes(incl));
		chars1.setExclusion(JAXBConvertor.stringWithNotes(excl));
		References refs = new References();
		refs.getPubMedId().addAll(pmints);
		chars1.setReferences(refs);
		chars1.setSource(JAXBConvertor.sourceWithNotes(source));
		chars1.setCreationDate(JAXBConvertor.dateWithNotes(created.getTime()));

		
		CharacteristicsMap chars2 = new CharacteristicsMap();
		chars2.put(BasicStudyCharacteristic.TITLE, new ObjectWithNotes<Object>(title));
		chars2.put(BasicStudyCharacteristic.ALLOCATION, new ObjectWithNotes<Object>(alloc));
		chars2.put(BasicStudyCharacteristic.BLINDING, new ObjectWithNotes<Object>(blind));
		chars2.put(BasicStudyCharacteristic.CENTERS, new ObjectWithNotes<Object>(centers));
		chars2.put(BasicStudyCharacteristic.OBJECTIVE, new ObjectWithNotes<Object>(objective));
		chars2.put(BasicStudyCharacteristic.STUDY_START, new ObjectWithNotes<Object>(studyStart.getTime()));
		chars2.put(BasicStudyCharacteristic.STUDY_END, new ObjectWithNotes<Object>(studyEnd.getTime()));
		chars2.put(BasicStudyCharacteristic.INCLUSION, new ObjectWithNotes<Object>(incl));
		chars2.put(BasicStudyCharacteristic.EXCLUSION, new ObjectWithNotes<Object>(excl));
		chars2.put(BasicStudyCharacteristic.PUBMED, new ObjectWithNotes<Object>(pmids)); // References
		chars2.put(BasicStudyCharacteristic.STATUS, new ObjectWithNotes<Object>(status));
		chars2.put(BasicStudyCharacteristic.SOURCE, new ObjectWithNotes<Object>(source));
		chars2.put(BasicStudyCharacteristic.CREATION_DATE, new ObjectWithNotes<Object>(created.getTime()));
		
		assertEntityEquals(chars2, JAXBConvertor.convertStudyCharacteristics(chars1));
		assertEquals(chars1, JAXBConvertor.convertStudyCharacteristics(chars2));
	}
	
	@Test
	public void testConvertStudyOutcomeMeasure() throws ConversionException {
		Domain domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		
		Endpoint ep = ExampleData.buildEndpointHamd();
		StudyOutcomeMeasure om = new StudyOutcomeMeasure();
		om.setNotes(new Notes());
		om.setEndpoint(nameReference(ep.getName()));
		
		assertEntityEquals(ep, JAXBConvertor.convertStudyOutcomeMeasure(om, domain).getValue());
		assertEquals(JAXBConvertor.convertStudyOutcomeMeasure(new Study.StudyOutcomeMeasure<Variable>(ep)), om);
		
		AdverseEvent ade = ExampleData.buildAdverseEventDiarrhea();
		domain.addAdverseEvent(ade);
		om.setEndpoint(null);
		om.setAdverseEvent(nameReference(ade.getName()));
		
		assertEntityEquals(ade, JAXBConvertor.convertStudyOutcomeMeasure(om, domain).getValue());
		assertEquals(JAXBConvertor.convertStudyOutcomeMeasure(new Study.StudyOutcomeMeasure<Variable>(ade)), om);
		
		PopulationCharacteristic pc = ExampleData.buildGenderVariable();
		domain.addPopulationCharacteristic(pc);
		om.setAdverseEvent(null);
		om.setPopulationCharacteristic(nameReference(pc.getName()));
		
		assertEntityEquals(pc, JAXBConvertor.convertStudyOutcomeMeasure(om, domain).getValue());
		assertEquals(JAXBConvertor.convertStudyOutcomeMeasure(new Study.StudyOutcomeMeasure<Variable>(pc)), om);
	}
	
	@Test(expected=ConversionException.class)
	public void testConvertStudyOutcomeMeasureThrows() throws ConversionException {
		Domain domain = new DomainImpl();
		StudyOutcomeMeasure om = new StudyOutcomeMeasure();
		JAXBConvertor.convertStudyOutcomeMeasure(om, domain);
	}
	
	@Test
	public void testConvertStudyOutcomeMeasures() throws ConversionException {
		Domain domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		Endpoint ep = ExampleData.buildEndpointHamd();
		domain.addAdverseEvent(ExampleData.buildAdverseEventDiarrhea());
		
		LinkedHashMap<String, Study.StudyOutcomeMeasure<?>> vars = new LinkedHashMap<String, Study.StudyOutcomeMeasure<?>>();
		vars.put("X", new Study.StudyOutcomeMeasure<Variable>(ep));
		vars.put("Y", new Study.StudyOutcomeMeasure<Variable>(ExampleData.buildAdverseEventDiarrhea()));

		StudyOutcomeMeasure epRef = new StudyOutcomeMeasure();
		epRef.setNotes(new Notes());
		epRef.setId("X");
		epRef.setEndpoint(nameReference(ep.getName()));
		StudyOutcomeMeasure adeRef = new StudyOutcomeMeasure();
		adeRef.setNotes(new Notes());
		adeRef.setId("Y");
		adeRef.setAdverseEvent(nameReference(ExampleData.buildAdverseEventDiarrhea().getName()));
		StudyOutcomeMeasures oms = new StudyOutcomeMeasures();
		oms.getStudyOutcomeMeasure().add(epRef);
		oms.getStudyOutcomeMeasure().add(adeRef);
		
		assertEquals(vars, JAXBConvertor.convertStudyOutcomeMeasures(oms, domain));
		assertEquals(JAXBConvertor.convertStudyOutcomeMeasures(vars), oms);
	}
	
	@Test
	public void testConvertMeasurement() throws ConversionException {
		org.drugis.addis.entities.data.RateMeasurement rm = new org.drugis.addis.entities.data.RateMeasurement();
		int c = 12;
		int s = 42;
		rm.setRate(c);
		rm.setSampleSize(s);
		org.drugis.addis.entities.data.Measurement meas = new org.drugis.addis.entities.data.Measurement();
		meas.setRateMeasurement(rm);
		BasicRateMeasurement expected1 = new BasicRateMeasurement(c, s);
		assertEntityEquals(expected1, JAXBConvertor.convertMeasurement(meas));
		assertEquals(meas, JAXBConvertor.convertMeasurement(expected1));
		
		org.drugis.addis.entities.data.ContinuousMeasurement cm = new org.drugis.addis.entities.data.ContinuousMeasurement();
		double m = 3.14;
		double e = 2.71;
		cm.setMean(m);
		cm.setStdDev(e);
		cm.setSampleSize(s);
		meas = new org.drugis.addis.entities.data.Measurement();
		meas.setRateMeasurement(null);
		meas.setContinuousMeasurement(cm);
		BasicContinuousMeasurement expected2 = new BasicContinuousMeasurement(m, e, s);
		assertEntityEquals(expected2, JAXBConvertor.convertMeasurement(meas));
		assertEquals(meas, JAXBConvertor.convertMeasurement(expected2));
		
		org.drugis.addis.entities.data.CategoricalMeasurement fm = new org.drugis.addis.entities.data.CategoricalMeasurement();
		CategoryMeasurement c1 = new CategoryMeasurement();
		c1.setName("Cats");
		c1.setRate(18);
		CategoryMeasurement c2 = new CategoryMeasurement();
		c2.setName("Dogs");
		c2.setRate(2145);
		fm.getCategory().add(c1);
		fm.getCategory().add(c2);
		meas = new org.drugis.addis.entities.data.Measurement();
		meas.setCategoricalMeasurement(fm);
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		map.put("Dogs", 2145);
		map.put("Cats", 18);
		FrequencyMeasurement expected3 = new FrequencyMeasurement(new String[] {"Cats", "Dogs"}, map);	
		assertEntityEquals(expected3, JAXBConvertor.convertMeasurement(meas));
		assertEquals(meas, JAXBConvertor.convertMeasurement(expected3));
	}
	
	@Test
	public void testConvertMeasurements() throws ConversionException {
		Map<Integer, Arm> arms = new HashMap<Integer, Arm>();
		Arm arm5 = new Arm(new Drug("Opium", "OPIUM4TW"), new FixedDose(100.0, SIUnit.MILLIGRAMS_A_DAY), 42);
		arms.put(5, arm5);
		Arm arm8 = new Arm(new Drug("LSD", "UFO"), new FixedDose(100.0, SIUnit.MILLIGRAMS_A_DAY), 42);
		arms.put(8, arm8);
		Map<String, Study.StudyOutcomeMeasure<?>> oms = new HashMap<String, Study.StudyOutcomeMeasure<?>>();
		String pcName = "popChar-hair";
		ContinuousPopulationCharacteristic pc = new ContinuousPopulationCharacteristic("Hair Length");
		oms.put(pcName, new Study.StudyOutcomeMeasure<Variable>(pc));
		String epName = "endpoint-tripping";
		Endpoint ep = new Endpoint("Tripping achieved", Type.RATE, Direction.HIGHER_IS_BETTER);
		oms.put(epName, new Study.StudyOutcomeMeasure<Variable>(ep));
		String aeName = "ade-nojob";
		AdverseEvent ae = new AdverseEvent("Job loss", Type.RATE);
		oms.put(aeName, new Study.StudyOutcomeMeasure<Variable>(ae));
		
		org.drugis.addis.entities.data.RateMeasurement rm1 = new org.drugis.addis.entities.data.RateMeasurement();
		rm1.setRate(10);
		rm1.setSampleSize(100);
		BasicRateMeasurement crm1 = new BasicRateMeasurement(10, 100);
		
		org.drugis.addis.entities.data.RateMeasurement rm2 = new org.drugis.addis.entities.data.RateMeasurement();
		rm2.setRate(20);
		rm2.setSampleSize(100);
		BasicRateMeasurement crm2 = new BasicRateMeasurement(20, 100);
		
		org.drugis.addis.entities.data.ContinuousMeasurement cm1 = new org.drugis.addis.entities.data.ContinuousMeasurement();
		cm1.setMean(1.5);
		cm1.setStdDev(1.0);
		cm1.setSampleSize(100);
		BasicContinuousMeasurement ccm1 = new BasicContinuousMeasurement(1.5, 1.0, 100);
		
		Measurements measurements = new Measurements();
		List<org.drugis.addis.entities.data.Measurement> list = measurements.getMeasurement();
		org.drugis.addis.entities.data.Measurement m1 = new org.drugis.addis.entities.data.Measurement();
		m1.setArm(JAXBConvertor.idReference(5));
		m1.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(epName));
		m1.setRateMeasurement(rm1);
		org.drugis.addis.entities.data.Measurement m2 = new org.drugis.addis.entities.data.Measurement();
		m2.setArm(JAXBConvertor.idReference(8));
		m2.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(epName));
		m2.setRateMeasurement(rm2);
		org.drugis.addis.entities.data.Measurement m3 = new org.drugis.addis.entities.data.Measurement();
		m3.setArm(JAXBConvertor.idReference(5));
		m3.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(aeName));
		m3.setRateMeasurement(rm2);
		org.drugis.addis.entities.data.Measurement m4 = new org.drugis.addis.entities.data.Measurement();
		m4.setArm(JAXBConvertor.idReference(8));
		m4.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(aeName));
		m4.setRateMeasurement(rm1);
		org.drugis.addis.entities.data.Measurement m5 = new org.drugis.addis.entities.data.Measurement();
		m5.setArm(JAXBConvertor.idReference(5));
		m5.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(pcName));
		m5.setContinuousMeasurement(cm1);
		org.drugis.addis.entities.data.Measurement m6 = new org.drugis.addis.entities.data.Measurement();
		m6.setArm(JAXBConvertor.idReference(8));
		m6.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(pcName));
		m6.setContinuousMeasurement(cm1);
		org.drugis.addis.entities.data.Measurement m7 = new org.drugis.addis.entities.data.Measurement();
		m7.setArm(null);
		m7.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference(pcName));
		m7.setContinuousMeasurement(cm1);
		list.add(m1);		
		list.add(m2);
		list.add(m3);
		list.add(m4);
		list.add(m5);
		list.add(m6);
		list.add(m7);
		
		Map<MeasurementKey, Measurement> expected = new HashMap<MeasurementKey, Measurement>();
		expected.put(new MeasurementKey(ep, arm5), crm1);
		expected.put(new MeasurementKey(ep, arm8), crm2);
		expected.put(new MeasurementKey(ae, arm5), crm2);
		expected.put(new MeasurementKey(ae, arm8), crm1);
		expected.put(new MeasurementKey(pc, arm5), ccm1);
		expected.put(new MeasurementKey(pc, arm8), ccm1);
		expected.put(new MeasurementKey(pc, null), ccm1);
		
		assertEquals(expected, JAXBConvertor.convertMeasurements(measurements, arms, oms));
		assertAllAndOnly(measurements.getMeasurement(), JAXBConvertor.convertMeasurements(expected, arms, oms).getMeasurement());
	}

	public org.drugis.addis.entities.data.Study buildStudy(String name) {
		String indicationName = ExampleData.buildIndicationDepression().getName();
		String[] endpointName = new String[] { 
				ExampleData.buildEndpointHamd().getName(), 
				ExampleData.buildEndpointCgi().getName()
			};
		String[] adverseEventName = new String[] {
				ExampleData.buildAdverseEventConvulsion().getName()
			};
		String[] popCharName = new String[] {
				ExampleData.buildAgeVariable().getName()
			};
		String title = "WHOO";
		
		// Arms
		Arms arms = new Arms();
		arms.getArm().add(buildFixedDoseArmData(1, 100, ExampleData.buildDrugFluoxetine().getName(), 12.5));
		arms.getArm().add(buildFlexibleDoseArmData(2, 102, ExampleData.buildDrugParoxetine().getName(), 12.5, 15.3));
		
		org.drugis.addis.entities.data.Study study = buildStudySkeleton(name,
				title, indicationName, endpointName, adverseEventName,
				popCharName, arms);
		
		
		study.getCharacteristics().setCenters(JAXBConvertor.intWithNotes(3));
		study.getCharacteristics().setAllocation(JAXBConvertor.allocationWithNotes(Allocation.RANDOMIZED));
		
		// Measurements
		List<org.drugis.addis.entities.data.Measurement> list = study.getMeasurements().getMeasurement();
		org.drugis.addis.entities.data.Measurement m1 = new org.drugis.addis.entities.data.Measurement();
		m1.setArm(JAXBConvertor.idReference(2));
		m1.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference("endpoint-" + endpointName[0]));
		RateMeasurement rm1 = new RateMeasurement();
		rm1.setRate(10);
		rm1.setSampleSize(110);
		m1.setRateMeasurement(rm1);
		list.add(m1);
		org.drugis.addis.entities.data.Measurement m2 = new org.drugis.addis.entities.data.Measurement();
		m2.setArm(null);
		m2.setStudyOutcomeMeasure(JAXBConvertor.stringIdReference("popChar-" + popCharName[0]));
		ContinuousMeasurement cm1 = new ContinuousMeasurement();
		cm1.setMean(0.2);
		cm1.setStdDev(0.01);
		cm1.setSampleSize(110);
		m2.setContinuousMeasurement(cm1);
		list.add(m2);
		
		return study;
	}

	private org.drugis.addis.entities.data.Study buildStudySkeleton(
			String name, String title, String indicationName,
			String[] endpointName, String[] adverseEventName,
			String[] popCharName, Arms arms) {
		org.drugis.addis.entities.data.Study study = new org.drugis.addis.entities.data.Study();
		study.setName(name);
		NameReferenceWithNotes indicationRef = JAXBConvertor.nameReferenceWithNotes(indicationName);
		study.setIndication(indicationRef);
		
		// Outcome measures
		StudyOutcomeMeasures studyOutcomeMeasures = new StudyOutcomeMeasures();
		study.setStudyOutcomeMeasures(studyOutcomeMeasures);
		
		// Outcome measures: Endpoints
		for (String epName : endpointName) {
			StudyOutcomeMeasure ep = new StudyOutcomeMeasure();
			ep.setNotes(new Notes());
			ep.setId("endpoint-" + epName);
			ep.setEndpoint(nameReference(epName));
			studyOutcomeMeasures.getStudyOutcomeMeasure().add(ep);
		}
		
		// Outcome measures: Adverse events
		for (String aeName : adverseEventName) {
			StudyOutcomeMeasure ae = new StudyOutcomeMeasure();
			ae.setNotes(new Notes());
			ae.setId("adverseEvent-" + aeName);
			ae.setAdverseEvent(nameReference(aeName));
			studyOutcomeMeasures.getStudyOutcomeMeasure().add(ae);
		}
		
		// Outcome measures: Population chars
		for (String pcName : popCharName) {
			StudyOutcomeMeasure pc = new StudyOutcomeMeasure();
			pc.setNotes(new Notes());
			pc.setId("popChar-" + pcName);
			pc.setPopulationCharacteristic(nameReference(pcName));
			studyOutcomeMeasures.getStudyOutcomeMeasure().add(pc);
		}
		
		// Arms
		study.setArms(arms);
		
		// Study characteristics
		Characteristics chars = new Characteristics();
		study.setCharacteristics(chars);
		chars.setTitle(JAXBConvertor.stringWithNotes(title));
		chars.setReferences(new References());
		
		// Measurements (empty)
		Measurements measurements = new Measurements();
		study.setMeasurements(measurements);
		
		return study;
	}

	@Test
	public void testConvertStudy() throws ConversionException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		domain.addEndpoint(ExampleData.buildEndpointCgi());
		domain.addAdverseEvent(ExampleData.buildAdverseEventConvulsion());
		
		String name = "My fancy study";
		org.drugis.addis.entities.data.Study study = buildStudy(name);
		
		//----------------------------------------
		Study study2 = new Study();
		study2.setStudyId(name);
		study2.setIndication(ExampleData.buildIndicationDepression());
		study2.addEndpoint(ExampleData.buildEndpointHamd());
		study2.addEndpoint(ExampleData.buildEndpointCgi());
		study2.addAdverseEvent(ExampleData.buildAdverseEventConvulsion());
		study2.addOutcomeMeasure(ExampleData.buildAgeVariable());
		Arm arm1 = buildFixedDoseArm(100, ExampleData.buildDrugFluoxetine(), 12.5);
		study2.addArm(arm1);
		Arm arm2 = buildFlexibleDoseArm(102, ExampleData.buildDrugParoxetine(), 12.5, 15.3);
		study2.addArm(arm2);
		study2.setArmIds(Arrays.asList(new Integer[] {1, 2}));
		study2.setCharacteristic(BasicStudyCharacteristic.TITLE, "WHOO");
		study2.setCharacteristic(BasicStudyCharacteristic.CENTERS, 3);
		study2.setCharacteristic(BasicStudyCharacteristic.ALLOCATION, Allocation.RANDOMIZED);
		study2.setCharacteristic(BasicStudyCharacteristic.PUBMED, new PubMedIdList());
		study2.setMeasurement(ExampleData.buildEndpointHamd(), arm2, new BasicRateMeasurement(10, 110));
		study2.setMeasurement(ExampleData.buildAgeVariable(), new BasicContinuousMeasurement(0.2, 0.01, 110));
		
		assertEntityEquals(study2, JAXBConvertor.convertStudy(study, domain));
		assertEquals(study, JAXBConvertor.convertStudy(study2));
	}
	
	@Test
	public void testConvertNote() {
		org.drugis.addis.entities.data.Note note = new org.drugis.addis.entities.data.Note();
		note.setSource(Source.CLINICALTRIALS);
		String text = "Some text here";
		note.setValue(text);
		
		Note expected = new Note(Source.CLINICALTRIALS, text);
		
		assertEquals(expected, JAXBConvertor.convertNote(note));
		assertEquals(note, JAXBConvertor.convertNote(expected));
	}
	
	@Test
	public void testConvertStudyWithNotes() throws ConversionException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		domain.addEndpoint(ExampleData.buildEndpointCgi());
		domain.addAdverseEvent(ExampleData.buildAdverseEventConvulsion());
		
		String name = "My fancy study";
		org.drugis.addis.entities.data.Study studyData = buildStudy(name);
		Study studyEntity = JAXBConvertor.convertStudy(studyData, domain);
		
		Note armNote = new Note(Source.CLINICALTRIALS, "Some text here");
		studyData.getArms().getArm().get(0).getNotes().getNote().add(JAXBConvertor.convertNote(armNote));
		studyData.getArms().getArm().get(1).getNotes().getNote().add(JAXBConvertor.convertNote(armNote));
		studyEntity.getArms().get(0).getNotes().add(armNote);
		studyEntity.getArms().get(1).getNotes().add(armNote);
		
		Note adeNote = new Note(Source.MANUAL, "I would not like to suffer from this!");
		studyData.getStudyOutcomeMeasures().getStudyOutcomeMeasure().get(2).getNotes().getNote().add(JAXBConvertor.convertNote(adeNote));
		studyEntity.getStudyAdverseEvents().get(0).getNotes().add(adeNote);
		Note hamdNote = new Note(Source.MANUAL, "Mmm... HAM!");
		studyData.getStudyOutcomeMeasures().getStudyOutcomeMeasure().get(0).getNotes().getNote().add(JAXBConvertor.convertNote(hamdNote));
		studyEntity.getStudyEndpoints().get(0).getNotes().add(hamdNote);
		Note charNote = new Note(Source.CLINICALTRIALS, "A randomized double blind trial of something");
		studyData.getCharacteristics().getAllocation().getNotes().getNote().add(JAXBConvertor.convertNote(charNote));
		studyEntity.getCharacteristics().get(BasicStudyCharacteristic.ALLOCATION).getNotes().add(charNote);
		
		assertEntityEquals(studyEntity, JAXBConvertor.convertStudy(studyData, domain));
	}
	
	private class MetaAnalysisWithStudies {
		public org.drugis.addis.entities.data.PairwiseMetaAnalysis d_pwma;
		public org.drugis.addis.entities.data.NetworkMetaAnalysis d_nwma;
		public List<org.drugis.addis.entities.data.Study> d_studies;
		
		public MetaAnalysisWithStudies(org.drugis.addis.entities.data.PairwiseMetaAnalysis ma, List<org.drugis.addis.entities.data.Study> s) {
			d_pwma = ma;
			d_studies = s;
		}
		
		public MetaAnalysisWithStudies(org.drugis.addis.entities.data.NetworkMetaAnalysis ma, List<org.drugis.addis.entities.data.Study> s) {
			d_nwma = ma;
			d_studies = s;
		}
	}
	
	@Test
	public void testConvertPairWiseMetaAnalysis() throws ConversionException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		
		String name = "Fluox-Venla Diarrhea for PMA";
		MetaAnalysisWithStudies ma = buildPairWiseMetaAnalysis(name);
		
		//-----------------------------------
		Study study2 = JAXBConvertor.convertStudy(ma.d_studies.get(0), domain);
		List<StudyArmsEntry> armsList = new ArrayList<StudyArmsEntry>();
		armsList.add(new StudyArmsEntry(study2, study2.getArms().get(0), study2.getArms().get(1)));
		domain.addStudy(study2);
		
		RandomEffectsMetaAnalysis pwma2 = new RandomEffectsMetaAnalysis(name, ExampleData.buildEndpointHamd(), armsList);
		
		assertEntityEquals(pwma2, JAXBConvertor.convertPairWiseMetaAnalysis(ma.d_pwma, domain));
		assertEquals(ma.d_pwma, JAXBConvertor.convertPairWiseMetaAnalysis(pwma2));
	}

	private MetaAnalysisWithStudies buildPairWiseMetaAnalysis(String name) {
		String study_name = "My fancy pair-wise study";
		org.drugis.addis.entities.data.Study study = buildStudy(study_name);

		org.drugis.addis.entities.data.PairwiseMetaAnalysis pwma = new org.drugis.addis.entities.data.PairwiseMetaAnalysis();
		pwma.setName(name);		
		pwma.setIndication(nameReference(ExampleData.buildIndicationDepression().getName()));
		pwma.setEndpoint(nameReference(ExampleData.buildEndpointHamd().getName()));
		// Base
		Alternative fluox = new Alternative();
		fluox.setDrug(nameReference(ExampleData.buildDrugFluoxetine().getName()));
		AnalysisArms fluoxArms = new AnalysisArms();
		fluoxArms.getArm().add(JAXBConvertor.armReference(study_name, study.getArms().getArm().get(0)));
		fluox.setArms(fluoxArms);
		pwma.getAlternative().add(fluox);
		// Subject
		Alternative parox = new Alternative();
		parox.setDrug(nameReference(ExampleData.buildDrugParoxetine().getName()));
		AnalysisArms paroxArms = new AnalysisArms();
		paroxArms.getArm().add(JAXBConvertor.armReference(study_name, study.getArms().getArm().get(1)));
		parox.setArms(paroxArms);
		pwma.getAlternative().add(parox);
		
		return new MetaAnalysisWithStudies(pwma, Collections.singletonList(study));
	}

	@Test
	public void testConvertNetworkMetaAnalysis() throws Exception, InstantiationException, InvocationTargetException, NoSuchMethodException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		String name = "CGI network meta-analysis";
		
		MetaAnalysisWithStudies ma = buildNetworkMetaAnalysis(name);
		
		List<Study> studies = new ArrayList<Study>();
		for (org.drugis.addis.entities.data.Study study : ma.d_studies) {
			Study studyEnt = JAXBConvertor.convertStudy(study, domain);
			domain.addStudy(studyEnt);
			studies.add(studyEnt);
		}

		List<Drug> drugs = new ArrayList<Drug>();
		drugs.add(ExampleData.buildDrugFluoxetine());
		drugs.add(ExampleData.buildDrugParoxetine());
		drugs.add(ExampleData.buildDrugSertraline());
		Map<Study, Map<Drug, Arm>> armMap = new HashMap<Study, Map<Drug,Arm>>();
		Map<Drug, Arm> study1map = new HashMap<Drug, Arm>();
		study1map.put(ExampleData.buildDrugFluoxetine(), studies.get(0).getArms().get(0));
		study1map.put(ExampleData.buildDrugSertraline(), studies.get(0).getArms().get(1));
		armMap.put(studies.get(0), study1map);
		Map<Drug, Arm> study2map = new HashMap<Drug, Arm>();
		study2map.put(ExampleData.buildDrugParoxetine(), studies.get(1).getArms().get(0));
		study2map.put(ExampleData.buildDrugSertraline(), studies.get(1).getArms().get(1));
		armMap.put(studies.get(1), study2map);
		Map<Drug, Arm> study3map = new HashMap<Drug, Arm>();
		study3map.put(ExampleData.buildDrugSertraline(), studies.get(2).getArms().get(0));
		study3map.put(ExampleData.buildDrugParoxetine(), studies.get(2).getArms().get(1));
		study3map.put(ExampleData.buildDrugFluoxetine(), studies.get(2).getArms().get(2));
		armMap.put(studies.get(2), study3map);
		
		Collections.sort(studies); // So the reading *by definition* puts the studies in their natural order
		NetworkMetaAnalysis expected = new NetworkMetaAnalysis(name, ExampleData.buildIndicationDepression(),
				ExampleData.buildEndpointCgi(), studies, drugs, armMap);
		
		assertEntityEquals(expected, JAXBConvertor.convertNetworkMetaAnalysis(ma.d_nwma, domain));
		assertEquals(ma.d_nwma, JAXBConvertor.convertNetworkMetaAnalysis(expected));
	}

	private MetaAnalysisWithStudies buildNetworkMetaAnalysis(String name) {
		String study_one = "A Network Meta analysis study 1";
		String study_two = "A Network Meta analysis study 2";
		String study_three = "A Network Meta analysis study 3";
		
		String[] endpoints = new String[] { ExampleData.buildEndpointHamd().getName(), ExampleData.buildEndpointCgi().getName() };
		
		Arms arms1 = new Arms();
		arms1.getArm().add(buildFixedDoseArmData(1, 20, ExampleData.buildDrugFluoxetine().getName(), 12.5));
		arms1.getArm().add(buildFixedDoseArmData(2, 20, ExampleData.buildDrugSertraline().getName(), 12.5));
		String indicationName = ExampleData.buildIndicationDepression().getName();
		org.drugis.addis.entities.data.Study study1 = buildStudySkeleton(study_one, study_one, 
				indicationName, endpoints, new String[] {}, new String[] {}, arms1);
		
		Arms arms2 = new Arms();
		arms2.getArm().add(buildFixedDoseArmData(1, 20, ExampleData.buildDrugParoxetine().getName(), 12.5));
		arms2.getArm().add(buildFixedDoseArmData(2, 20, ExampleData.buildDrugSertraline().getName(), 12.5));
		org.drugis.addis.entities.data.Study study2 = buildStudySkeleton(study_two, study_two, 
				indicationName, endpoints, new String[] {}, new String[] {}, arms2);
		
		Arms arms3 = new Arms();
		arms3.getArm().add(buildFixedDoseArmData(1, 20, ExampleData.buildDrugSertraline().getName(), 12.5));
		arms3.getArm().add(buildFixedDoseArmData(2, 20, ExampleData.buildDrugParoxetine().getName(), 12.5));
		arms3.getArm().add(buildFixedDoseArmData(3, 20, ExampleData.buildDrugFluoxetine().getName(), 12.5));
		org.drugis.addis.entities.data.Study study3 = buildStudySkeleton(study_three, study_three, 
				indicationName, endpoints, new String[] {}, new String[] {}, arms3);

		org.drugis.addis.entities.data.NetworkMetaAnalysis nma = new org.drugis.addis.entities.data.NetworkMetaAnalysis();
		nma.setName(name);		
		nma.setIndication(nameReference(indicationName));
		nma.setEndpoint(nameReference(ExampleData.buildEndpointCgi().getName()));
		
		List<org.drugis.addis.entities.data.Study> studiesl = new ArrayList<org.drugis.addis.entities.data.Study>();
		studiesl.add(study1);
		studiesl.add(study2);
		studiesl.add(study3);

		
		// Fluoxetine
		Alternative fluox = new Alternative();
		fluox.setDrug(nameReference(ExampleData.buildDrugFluoxetine().getName()));
		AnalysisArms fluoxArms = new AnalysisArms();
		fluoxArms.getArm().add(JAXBConvertor.armReference(study_one, arms1.getArm().get(0))); // study 1
		fluoxArms.getArm().add(JAXBConvertor.armReference(study_three, arms3.getArm().get(2))); // study 3
		fluox.setArms(fluoxArms);
		nma.getAlternative().add(fluox);
		// Paroxetine		
		Alternative parox = new Alternative();
		parox.setDrug(nameReference(ExampleData.buildDrugParoxetine().getName()));
		AnalysisArms paroxArms = new AnalysisArms();
		paroxArms.getArm().add(JAXBConvertor.armReference(study_two, arms2.getArm().get(0))); // study 2
		paroxArms.getArm().add(JAXBConvertor.armReference(study_three, arms3.getArm().get(1))); // study 3
		parox.setArms(paroxArms);
		nma.getAlternative().add(parox);
		// Setraline
		Alternative setr = new Alternative();
		setr.setDrug(nameReference(ExampleData.buildDrugSertraline().getName()));
		AnalysisArms sertrArms  = new AnalysisArms();
		sertrArms.getArm().add(JAXBConvertor.armReference(study_one, arms1.getArm().get(1))); // study 1
		sertrArms.getArm().add(JAXBConvertor.armReference(study_two, arms2.getArm().get(1))); // study 2
		sertrArms.getArm().add(JAXBConvertor.armReference(study_three, arms3.getArm().get(0))); // study 2
		setr.setArms(sertrArms);
		nma.getAlternative().add(setr);
		
		return new MetaAnalysisWithStudies(nma, studiesl);
	}
	
	@Test
	public void testConvertMetaAnalyses() throws NullPointerException, ConversionException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		
		MetaAnalyses analyses = new MetaAnalyses();
		MetaAnalysisWithStudies ma1 = buildPairWiseMetaAnalysis("XXX");
		analyses.getPairwiseMetaAnalysisOrNetworkMetaAnalysis().add(ma1.d_pwma);
		MetaAnalysisWithStudies ma2 = buildNetworkMetaAnalysis("XXX 2");
		analyses.getPairwiseMetaAnalysisOrNetworkMetaAnalysis().add(ma2.d_nwma);
		
		addStudies(domain, ma1);
		addStudies(domain, ma2);
		
		List<MetaAnalysis> expected = new ArrayList<MetaAnalysis>();
		expected.add(JAXBConvertor.convertPairWiseMetaAnalysis(ma1.d_pwma, domain));
		expected.add(JAXBConvertor.convertNetworkMetaAnalysis(ma2.d_nwma, domain));
		
		assertEntityEquals(expected, JAXBConvertor.convertMetaAnalyses(analyses, domain));
		assertEquals(analyses, JAXBConvertor.convertMetaAnalyses(expected));
	}

	private void addStudies(DomainImpl domain, MetaAnalysisWithStudies ma1)
			throws ConversionException {
		for (org.drugis.addis.entities.data.Study study : ma1.d_studies) {
			domain.addStudy(JAXBConvertor.convertStudy(study, domain));
		}
	}
		
	@Test
	public void testConvertStudyBenefitRiskAnalysis() throws ConversionException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		domain.addAdverseEvent(ExampleData.buildAdverseEventConvulsion());
		domain.addAdverseEvent(ExampleData.buildAdverseEventDiarrhea());
		
		String name = "BR Analysis";
		String[] adverseEvents = {ExampleData.buildAdverseEventDiarrhea().getName(), ExampleData.buildAdverseEventConvulsion().getName() };
		String[] endpoints = { ExampleData.buildEndpointCgi().getName(), ExampleData.buildEndpointHamd().getName() };
		Arms arms = new Arms();
		arms.getArm().add(buildFixedDoseArmData(1, 12, ExampleData.buildDrugFluoxetine().getName(), 13));
		arms.getArm().add(buildFlexibleDoseArmData(2, 23, ExampleData.buildDrugParoxetine().getName(), 2, 45));
		arms.getArm().add(buildFlexibleDoseArmData(3, 11, ExampleData.buildDrugParoxetine().getName(), 5, 12));
		org.drugis.addis.entities.data.Study study = buildStudySkeleton("Study for Benefit-Risk", "HI", 
				ExampleData.buildIndicationDepression().getName(),
				endpoints, adverseEvents, new String[]{}, arms);
		
		Integer[] armIds = { 1, 2 };
		org.drugis.addis.entities.data.StudyBenefitRiskAnalysis br = buildStudyBR(
				name, study, endpoints, adverseEvents, armIds);
		
		Study convertStudy = JAXBConvertor.convertStudy(study, domain);
		domain.addStudy(convertStudy);
		List<org.drugis.addis.entities.OutcomeMeasure> criteria = new ArrayList<org.drugis.addis.entities.OutcomeMeasure>();
		criteria.add(ExampleData.buildEndpointCgi());
		criteria.add(ExampleData.buildAdverseEventConvulsion());
		
		List<Arm> alternatives = new ArrayList<Arm>();
		alternatives.add(convertStudy.getArms().get(1));
		alternatives.add(convertStudy.getArms().get(2));
		
		StudyBenefitRiskAnalysis expected = new StudyBenefitRiskAnalysis(name, 
				ExampleData.buildIndicationDepression(), convertStudy,
				criteria , alternatives, AnalysisType.LyndOBrien);
		
		assertEntityEquals(expected, JAXBConvertor.convertStudyBenefitRiskAnalysis(br, domain));
		assertEquals(br, JAXBConvertor.convertStudyBenefitRiskAnalysis(expected));
	}

	private org.drugis.addis.entities.data.StudyBenefitRiskAnalysis buildStudyBR(
			String name, org.drugis.addis.entities.data.Study study,
			String[] endpoints, String[] adverseEvents, Integer[] armIds) {
		org.drugis.addis.entities.data.StudyBenefitRiskAnalysis br = new org.drugis.addis.entities.data.StudyBenefitRiskAnalysis();
		br.setName(name);
		br.setIndication(nameReference(study.getIndication().getName()));
		br.setStudy(nameReference(study.getName()));
		br.setAnalysisType(AnalysisType.LyndOBrien);
		br.setOutcomeMeasures(new OutcomeMeasuresReferences());
		br.getOutcomeMeasures().getEndpoint().add(nameReference(endpoints[0]));
		br.getOutcomeMeasures().getAdverseEvent().add(nameReference(adverseEvents[1]));
		ArmReferences armRefs = new ArmReferences();
		for (Integer id : armIds) {
			armRefs.getArm().add(JAXBConvertor.armReference(study.getName(), study.getArms().getArm().get(id)));
		}
		br.setArms(armRefs);
		return br;
	}	
	
	@Test
	public void testConvertMetaBenefitRiskAnalysis() throws ConversionException, NullPointerException, IllegalArgumentException, EntityIdExistsException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		domain.addAdverseEvent(ExampleData.buildAdverseEventConvulsion());
		domain.addAdverseEvent(ExampleData.buildAdverseEventDiarrhea());
		
		String name = "Meta Benefit-Risk Analysis Test";
		// create entities
		String pairWiseName = "First Pair-Wise";
		String networkMetaName = "Second Network Meta Analysis";
		MetaAnalysisWithStudies ma1 = buildPairWiseMetaAnalysis(pairWiseName);
		MetaAnalysisWithStudies ma2 = buildNetworkMetaAnalysis(networkMetaName);
		
		// add to the domain
		addStudies(domain, ma1);
		addStudies(domain, ma2);
		RandomEffectsMetaAnalysis ma1ent = JAXBConvertor.convertPairWiseMetaAnalysis(ma1.d_pwma, domain);
		domain.addMetaAnalysis(ma1ent);
		MetaAnalysis ma2ent = JAXBConvertor.convertNetworkMetaAnalysis(ma2.d_nwma, domain);
		domain.addMetaAnalysis(ma2ent);
		
		// create BR analysis
		String[] drugs = { ma1.d_pwma.getAlternative().get(0).getDrug().getName(), ma1.d_pwma.getAlternative().get(1).getDrug().getName() };
		String indication = ma1.d_pwma.getIndication().getName();
		String[] meta = { pairWiseName, networkMetaName };
		org.drugis.addis.entities.data.MetaBenefitRiskAnalysis br = buildMetaBR(
				name, drugs, indication, meta);

		List<MetaAnalysis> metaList = new ArrayList<MetaAnalysis>();
		metaList.add(ma1ent);
		metaList.add(ma2ent);
		
		List<Drug> drugsEnt = new ArrayList<Drug>(ma1ent.getIncludedDrugs());
		Drug baseline = drugsEnt.get(0);
		drugsEnt.remove(baseline);
		MetaBenefitRiskAnalysis expected = new MetaBenefitRiskAnalysis(name, ma1ent.getIndication(), metaList , baseline, 
				drugsEnt, AnalysisType.SMAA);
		assertEntityEquals(expected, JAXBConvertor.convertMetaBenefitRiskAnalysis(br, domain));
		assertEquals(br, JAXBConvertor.convertMetaBenefitRiskAnalysis(expected));
	}

	private org.drugis.addis.entities.data.MetaBenefitRiskAnalysis buildMetaBR(
			String name, String[] drugs, String indication, String[] meta) {
		org.drugis.addis.entities.data.MetaBenefitRiskAnalysis br = new org.drugis.addis.entities.data.MetaBenefitRiskAnalysis();
		br.setName(name);
		br.setAnalysisType(AnalysisType.SMAA);
		br.setIndication(nameReference(indication));
		br.setBaseline(nameReference(drugs[0]));
		DrugReferences dRef = new DrugReferences();
		dRef.getDrug().add(nameReference(drugs[0]));
		dRef.getDrug().add(nameReference(drugs[1]));
		br.setDrugs(dRef);
		MetaAnalysisReferences mRefs = new MetaAnalysisReferences();
		for (String mName : meta) {
			mRefs.getMetaAnalysis().add(nameReference(mName));
		}
		br.setMetaAnalyses(mRefs);
		return br;
	}
	
	@Test
	public void testConvertBenefitRiskAnalyses() throws ConversionException, EntityIdExistsException {
		DomainImpl domain = new DomainImpl();
		ExampleData.initDefaultData(domain);
		domain.addAdverseEvent(ExampleData.buildAdverseEventConvulsion());
		domain.addAdverseEvent(ExampleData.buildAdverseEventDiarrhea());
		
		String name = "BR Analysis";
		String[] adverseEvents = {ExampleData.buildAdverseEventDiarrhea().getName(), ExampleData.buildAdverseEventConvulsion().getName() };
		String[] endpoints = { ExampleData.buildEndpointCgi().getName(), ExampleData.buildEndpointHamd().getName() };
		Arms arms = new Arms();
		arms.getArm().add(buildFixedDoseArmData(1, 12, ExampleData.buildDrugFluoxetine().getName(), 13));
		arms.getArm().add(buildFlexibleDoseArmData(2, 23, ExampleData.buildDrugParoxetine().getName(), 2, 45));
		arms.getArm().add(buildFlexibleDoseArmData(3, 11, ExampleData.buildDrugParoxetine().getName(), 5, 12));
		org.drugis.addis.entities.data.Study study = buildStudySkeleton("Study for Benefit-Risk", "HI", 
				ExampleData.buildIndicationDepression().getName(),
				endpoints, adverseEvents, new String[]{}, arms);
		
		Integer[] armIds = { 1, 2 };
		org.drugis.addis.entities.data.StudyBenefitRiskAnalysis studyBR = buildStudyBR(
				name, study, endpoints, adverseEvents, armIds);
		
		Study convertStudy = JAXBConvertor.convertStudy(study, domain);
		domain.addStudy(convertStudy);
		
		String pwName = "pairwise MA";
		String nwName = "network MA";
		MetaAnalysisWithStudies pairWiseMetaAnalysis = buildPairWiseMetaAnalysis(pwName);
		MetaAnalysisWithStudies networkMetaAnalysis = buildNetworkMetaAnalysis(nwName);
		addStudies(domain, pairWiseMetaAnalysis);
		domain.addMetaAnalysis(JAXBConvertor.convertPairWiseMetaAnalysis(pairWiseMetaAnalysis.d_pwma, domain));
		addStudies(domain, networkMetaAnalysis);
		domain.addMetaAnalysis(JAXBConvertor.convertNetworkMetaAnalysis(networkMetaAnalysis.d_nwma, domain));
		
		org.drugis.addis.entities.data.MetaBenefitRiskAnalysis metaBR = buildMetaBR("Meta BR", new String[] { ExampleData.buildDrugFluoxetine().getName(), ExampleData.buildDrugParoxetine().getName() }, 
				ExampleData.buildIndicationDepression().getName(),
				new String[] { nwName, pwName });
		
		BenefitRiskAnalyses analyses = new BenefitRiskAnalyses();
		analyses.getStudyBenefitRiskAnalysisOrMetaBenefitRiskAnalysis().add(metaBR);
		analyses.getStudyBenefitRiskAnalysisOrMetaBenefitRiskAnalysis().add(studyBR);
		
		List<BenefitRiskAnalysis<?>> expected = new ArrayList<BenefitRiskAnalysis<?>>();
		expected.add(JAXBConvertor.convertMetaBenefitRiskAnalysis(metaBR, domain));
		expected.add(JAXBConvertor.convertStudyBenefitRiskAnalysis(studyBR, domain));

		assertEntityEquals(expected, JAXBConvertor.convertBenefitRiskAnalyses(analyses, domain));
		assertEquals(analyses, JAXBConvertor.convertBenefitRiskAnalyses(expected));
	}
	
	@Test
	// ACCEPTANCE TEST -- should be replaced by something nicer so we can remove the Javalution support.
	public void testAddisDataToDomainData() throws Exception {
		InputStream xmlStream = getClass().getResourceAsStream("../defaultData.xml");
		InputStream transformedXmlStream = getTransformed();

		DomainData importedDomainData = (DomainData)XMLHelper.fromXml(xmlStream);
		Domain importedDomain = new DomainImpl(importedDomainData);
		
		AddisData data = (AddisData) d_unmarshaller.unmarshal(transformedXmlStream);
		Domain domainData = JAXBConvertor.addisDataToDomain(data);
		assertDomainEquals(importedDomain, domainData);
	}
	
	@Test
	// ACCEPTANCE TEST -- should be replaced by something nicer so we can remove the Javalution support.
	public void testDomainDataToAddisData() throws Exception {
		InputStream xmlStream = getClass().getResourceAsStream("../defaultData.xml");
		InputStream transformedXmlStream = getTransformed();

		Domain domain = new DomainImpl((DomainData)fromXmlPreserveArmIds(xmlStream));

		AddisData data = (AddisData) d_unmarshaller.unmarshal(transformedXmlStream);
		sortMeasurements(data);
		sortAnalysisArms(data);
		sortBenefitRiskOutcomes(data);
		assertEquals(data, JAXBConvertor.domainToAddisData(domain)); 
	}
	
	private void sortBenefitRiskOutcomes(AddisData data) {
		for(Object obj : data.getBenefitRiskAnalyses().getStudyBenefitRiskAnalysisOrMetaBenefitRiskAnalysis()) {
			if (obj instanceof org.drugis.addis.entities.data.StudyBenefitRiskAnalysis) {
				org.drugis.addis.entities.data.StudyBenefitRiskAnalysis sbr = (org.drugis.addis.entities.data.StudyBenefitRiskAnalysis) obj;
				Collections.sort(sbr.getOutcomeMeasures().getAdverseEvent(), new NameReferenceComparator());
				Collections.sort(sbr.getOutcomeMeasures().getEndpoint(), new NameReferenceComparator());
			}
		}
	}

	private void sortAnalysisArms(AddisData data) {
		for(org.drugis.addis.entities.data.MetaAnalysis ma : data.getMetaAnalyses().getPairwiseMetaAnalysisOrNetworkMetaAnalysis()) {
			List<Alternative> alternatives = null;
			if (ma instanceof org.drugis.addis.entities.data.PairwiseMetaAnalysis) {
				org.drugis.addis.entities.data.PairwiseMetaAnalysis pwma = (org.drugis.addis.entities.data.PairwiseMetaAnalysis) ma;
				alternatives = pwma.getAlternative();
			}
			if (ma instanceof org.drugis.addis.entities.data.NetworkMetaAnalysis) {
				org.drugis.addis.entities.data.NetworkMetaAnalysis nwma = (org.drugis.addis.entities.data.NetworkMetaAnalysis) ma;
				alternatives = nwma.getAlternative();
				Collections.sort(alternatives, new AlternativeComparator());
			}
			for (Alternative a : alternatives) {
				Collections.sort(a.getArms().getArm(), new ArmComparator());
			}
		}
	}

	private void sortMeasurements(AddisData data) {
		for(org.drugis.addis.entities.data.Study s : data.getStudies().getStudy()) {
			Collections.sort(s.getMeasurements().getMeasurement(), new MeasurementComparator(s));
		}
	}
	
	public static class NameReferenceComparator implements Comparator<org.drugis.addis.entities.data.NameReference> {
		public int compare(org.drugis.addis.entities.data.NameReference o1, org.drugis.addis.entities.data.NameReference o2) {
			return o1.getName().compareTo(o2.getName());
		}		
	}
	
	public static class AlternativeComparator implements Comparator<org.drugis.addis.entities.data.Alternative> {
		public int compare(org.drugis.addis.entities.data.Alternative o1, org.drugis.addis.entities.data.Alternative o2) {
			return o1.getDrug().getName().compareTo(o2.getDrug().getName());
		}
	}
	
	public static class ArmComparator implements Comparator<org.drugis.addis.entities.data.ArmReference> {
		public int compare(ArmReference o1, ArmReference o2) {
			return o1.getStudy().compareTo(o2.getStudy());
		}
	}
	
	public static class MeasurementComparator implements Comparator<org.drugis.addis.entities.data.Measurement> {
		private org.drugis.addis.entities.data.Study d_study;

		public MeasurementComparator(org.drugis.addis.entities.data.Study s) {
			d_study = s;
		}

		public int compare(org.drugis.addis.entities.data.Measurement o1, org.drugis.addis.entities.data.Measurement o2) {
			int omId1 = findOmIndex(o1.getStudyOutcomeMeasure().getId());
			int omId2 = findOmIndex(o2.getStudyOutcomeMeasure().getId());
			if (omId1 != omId2) {
				return omId1 - omId2;
			}
			return o1.getArm().getId().compareTo(o2.getArm().getId());
		}

		private int findOmIndex(String id) {
			List<StudyOutcomeMeasure> oms = d_study.getStudyOutcomeMeasures().getStudyOutcomeMeasure();
			for (int i = 0; i < oms.size(); ++i) {
				if (oms.get(i).getId().equals(id)) {
					return i;
				}
			}
			return -1;
		}
	}
	
	@Test
	public void testDateWithNotes() {
		String date = "2010-11-12";
		Date oldXmlDate = new GregorianCalendar(2010, 11 -1, 12).getTime();
		DateWithNotes dwn = JAXBConvertor.dateWithNotes(oldXmlDate);
		
		XMLGregorianCalendar cal = XMLGregorianCalendarImpl.parse(date);
		DateWithNotes dwn2 = new DateWithNotes();
		dwn2.setNotes(new Notes());
		dwn2.setValue(cal);
		
		assertEquals(dwn, dwn2);
	}

	public static void writeTransformedXML() throws TransformerException, IOException {
		InputStream transformedXmlStream = getTransformed();
		FileOutputStream output = new FileOutputStream("transformedDefaultData.xml");
		PubMedDataBankRetriever.copyStream(transformedXmlStream, output);
		output.close();
	}
	
	private static InputStream getTransformed() throws TransformerException, IOException {
		System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
		TransformerFactory tFactory = TransformerFactory.newInstance(); 
		InputStream xmlFile = JAXBConvertorTest.class.getResourceAsStream("../defaultData.xml");
		InputStream xsltFile = JAXBConvertorTest.class.getResourceAsStream("../entities/transform-0-1.xslt");
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
	    javax.xml.transform.Source xmlSource = new javax.xml.transform.stream.StreamSource(xmlFile);
	    javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltFile);
	    javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(os);
	    
	    javax.xml.transform.Transformer trans = tFactory.newTransformer(xsltSource);
	    trans.transform(xmlSource, result);
	    os.close();

	    return new ByteArrayInputStream(os.toByteArray());
	}

	private static final class IdResolver extends XMLReferenceResolver {
		private Map<Integer, Object> d_idMap = new HashMap<Integer, Object>();

		public void createReference(Object obj, XMLFormat.InputElement xml)
		throws XMLStreamException {
			CharArray value = xml.getAttribute("id");
			if (value == null)
				return;
			d_idMap.put(value.toInt(), obj);
			super.createReference(obj, xml);
		}

		public Integer getId(Object obj) {
			for (Entry<Integer, Object> entry : d_idMap.entrySet()) {
				if (entry.getValue() == obj) {
					return entry.getKey();
				}
			}
			return null;
		}
	}
	
	public static <T> T fromXmlPreserveArmIds(InputStream xmlStream) throws XMLStreamException {
		XMLObjectReader reader = XMLObjectReader.newInstance(xmlStream, "UTF-8");
		reader.setBinding(new AddisBinding());
		IdResolver resolver = new IdResolver();
		reader.setReferenceResolver(resolver);
		T read = reader.<T>read();
		if (read instanceof DomainData) {
			fixArmIds((DomainData)read, resolver);
		}
		return read;
	}

	private static void fixArmIds(DomainData data, IdResolver resolver) {
		for (Study s : data.getStudies()) {
			List<Integer> ids = new ArrayList<Integer>();
			for (Arm a : s.getArms()) {
				ids.add(resolver.getId(a));
			}
			s.setArmIds(ids);
		}
	}
}