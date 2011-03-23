/*
 * This file is part of ADDIS (Aggregate Data Drug Information System).
 * ADDIS is distributed from http://drugis.org/.
 * Copyright (C) 2009 Gert van Valkenhoef, Tommi Tervonen.
 * Copyright (C) 2010 Gert van Valkenhoef, Tommi Tervonen, 
 * Tijs Zwinkels, Maarten Jacobs, Hanno Koeslag, Florin Schimbinschi, 
 * Ahmad Kamal, Daniel Reid.
 * Copyright (C) 2011 Gert van Valkenhoef, Ahmad Kamal, 
 * Daniel Reid, Florin Schimbinschi.
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

package org.drugis.addis.entities.relativeeffect;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class LogitGaussianTest {

	private LogitGaussian d_gauss1;
	private LogitGaussian d_gauss2;

	@Before public void setUp() {
		d_gauss1 = new LogitGaussian(0.0, 1.0);
		d_gauss2 = new LogitGaussian(-5.0, 2.0);
	}

	@Test public void testGetAxisType() {
		assertEquals(AxisType.LINEAR, d_gauss1.getAxisType());
	}

	@Test public void testGetParameters() {
		assertEquals(0.0, d_gauss1.getMu(), 0.00000001);
		assertEquals(1.0, d_gauss1.getSigma(), 0.00000001);
		assertEquals(-5.0, d_gauss2.getMu(), 0.00000001);
		assertEquals(2.0, d_gauss2.getSigma(), 0.00000001);
	}

	@Test public void testGetQuantile() {
		assertEquals(0.8765291, d_gauss1.getQuantile(0.975), 0.00001);
		assertEquals(0.2534923857, d_gauss2.getQuantile(0.975), 0.00001);
		assertEquals(0.1234709, d_gauss1.getQuantile(0.025), 0.00001);
		assertEquals(0.0001336800, d_gauss2.getQuantile(0.025), 0.00001);
		assertEquals(0.8381943, d_gauss1.getQuantile(0.95), 0.00001);
		assertEquals(0.1531257491, d_gauss2.getQuantile(0.95), 0.00001);
		assertEquals(0.5, d_gauss1.getQuantile(0.5), 0.00001);
		assertEquals(0.006692851, d_gauss2.getQuantile(0.5), 0.00001);
	}

	@Test(expected=IllegalArgumentException.class) public void testPreconditionSigmaNonNegative() {
		new LogGaussian(0.0, -.01);
	}

	@Test(expected=IllegalArgumentException.class) public void testPreconditionSigmaNotNaN() {
		new LogGaussian(0.0, Double.NaN);
	}

	@Test(expected=IllegalArgumentException.class) public void testPreconditionMuNotNaN() {
		new LogGaussian(Double.NaN, 1.0);
	}

	@Test
	public void testPlus() {
		GaussianBase x = new LogitGaussian(-1.25, 0.23);
		GaussianBase y = new LogitGaussian(3.8, 1.2);
		double expectedMu = x.getMu() + y.getMu();
		double expectedSigma = Math.sqrt(Math.pow(x.getSigma(), 2) + Math.pow(y.getSigma(), 2));

		GaussianBase z = (GaussianBase)x.plus(y);
		assertTrue(z instanceof LogitGaussian);
		assertEquals(expectedMu, z.getMu(), 0.0000001);
		assertEquals(expectedSigma, z.getSigma(), 0.0000001);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testPlusShouldFailOnIncompatibleType() {
		(new LogitGaussian(0.0, 0.1)).plus(new Gaussian(1.0, 2.0));
	}
}
