package org.drugis.addis.entities.relativeeffect;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TransformedLogStudentTTest {

	private TransformedLogStudentT d_student1;
	private TransformedLogStudentT d_student2;

	@Before public void setUp() {
		d_student1 = new TransformedLogStudentT(0.0, .01, 1);
		d_student2 = new TransformedLogStudentT(-5.0, .02, 9);
	}
	
	@Test public void testGetAxisType() {
		assertEquals(AxisType.LOGARITHMIC, d_student1.getAxisType());
	}
	
	@Test public void testGetParameters() {
		assertEquals(0.0, d_student1.getMu(), 0.00000001);
		assertEquals(.01, d_student1.getSigma(), 0.00000001);
		assertEquals(1, d_student1.getDegreesOfFreedom());
		assertEquals(-5.0, d_student2.getMu(), 0.00000001);
		assertEquals(.02, d_student2.getSigma(), 0.00000001);
		assertEquals(9, d_student2.getDegreesOfFreedom());
	}
	
	@Test public void testGetQuantile() {
		double t1_90 = 6.314;
		double t1_95 = 12.706;
		double t9_90 = 1.833;
		double t9_95 = 2.262;
		assertEquals(Math.exp(t1_95 * 0.01 + 0.0), d_student1.getQuantile(0.975), 0.001);
		assertEquals(Math.exp(t9_95 * 0.02 + -5.0), d_student2.getQuantile(0.975), 0.001);
		assertEquals(Math.exp(-t1_95 * 0.01 + 0.0), d_student1.getQuantile(0.025), 0.001);
		assertEquals(Math.exp(-t9_95 * 0.02 + -5.0), d_student2.getQuantile(0.025), 0.001);
		assertEquals(Math.exp(t1_90 * 0.01 + 0.0), d_student1.getQuantile(0.95), 0.001);
		assertEquals(Math.exp(t9_90 * 0.02 + -5.0), d_student2.getQuantile(0.95), 0.001);
		assertEquals(Math.exp(0.0), d_student1.getQuantile(0.5), 0.00001);
		assertEquals(Math.exp(-5.0), d_student2.getQuantile(0.5), 0.00001);
	}
	
	@Test(expected=IllegalArgumentException.class) public void testPreconditionSigmaNonNegative() {
		new TransformedLogStudentT(0.0, -.01, 1);
	}

	@Test(expected=IllegalArgumentException.class) public void testPreconditionSigmaNotNaN() {
		new TransformedLogStudentT(0.0, Double.NaN, 1);
	}

	@Test(expected=IllegalArgumentException.class) public void testPreconditionMuNotNaN() {
		new TransformedLogStudentT(Double.NaN, 1.0, 1);
	}
	
	@Test(expected=IllegalArgumentException.class) public void testPreconditionDegreesOfFreedomPositive() {
		new TransformedLogStudentT(0.0, 1.0, 0);
	}
}