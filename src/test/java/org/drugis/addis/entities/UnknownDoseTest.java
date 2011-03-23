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

package org.drugis.addis.entities;

import static org.junit.Assert.*;
import javolution.xml.stream.XMLStreamException;

import org.drugis.addis.util.XMLHelper;
import org.junit.Test;

public class UnknownDoseTest {
	@Test
	public void testEqualsOtherUnknown() {
		assertEquals(new UnknownDose(), new UnknownDose());
	}
	
	@Test
	public void testNotEqualsKnown() {
		assertNotSame(new FixedDose(10.0, SIUnit.MILLIGRAMS_A_DAY), new UnknownDose());
	}
	
	@Test
	public void testGetUnit() {
		assertEquals(SIUnit.MILLIGRAMS_A_DAY, new UnknownDose().getUnit());
	}
	
	@Test
	public void testToString() {
		assertEquals("Unknown Dose", new UnknownDose().toString());
	}
	
	@Test
	public void testHashCode() {
		assertEquals(1, new UnknownDose().hashCode());
	}
	
	@Test
	public void testXML() throws XMLStreamException {
		UnknownDose d = new UnknownDose();
		String xml = XMLHelper.toXml(d, UnknownDose.class);
		UnknownDose objFromXml = XMLHelper.fromXml(xml);
		assertEquals(d, objFromXml);
	}
	
}
