package nl.rug.escher.addis.entities.test;

import static org.junit.Assert.*;
import nl.rug.escher.addis.entities.Dose;
import nl.rug.escher.addis.entities.SIUnit;
import nl.rug.escher.common.JUnitUtil;

import org.junit.Test;

public class DoseTest {
	@Test
	public void testSetUnit() {
		JUnitUtil.testSetter(new Dose(0.0, null), Dose.PROPERTY_UNIT, null, SIUnit.MILLIGRAMS_A_DAY);
	}
	
	@Test
	public void testSetQuantity() {
		JUnitUtil.testSetter(new Dose(0.0, null), Dose.PROPERTY_QUANTITY, 0.0, 40.0);
	}
	
	@Test
	public void testToString() {
		Dose d = new Dose(0.0, null);
		assertEquals("INCOMPLETE", d.toString());
		d.setQuantity(25.5);
		d.setUnit(SIUnit.MILLIGRAMS_A_DAY);
		assertEquals("25.5 " + SIUnit.MILLIGRAMS_A_DAY.toString(), d.toString());
	}
	
	@Test
	public void testEquals() {
		double q1 = 13.0;
		double q2 = 8.8;
		
		assertEquals(new Dose(q1, SIUnit.MILLIGRAMS_A_DAY),
				new Dose(q1, SIUnit.MILLIGRAMS_A_DAY));
		
		JUnitUtil.assertNotEquals(new Dose(q1, SIUnit.MILLIGRAMS_A_DAY),
				new Dose(q2, SIUnit.MILLIGRAMS_A_DAY));
		
		assertEquals(new Dose(q1, SIUnit.MILLIGRAMS_A_DAY).hashCode(),
				new Dose(q1, SIUnit.MILLIGRAMS_A_DAY).hashCode());
	}
}
