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

package org.drugis.addis.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import org.drugis.addis.entities.data.AddisData;

public class JAXBHandler {
	public static enum XmlFormatType {
		LEGACY(0),
		SCHEMA1(1),
		SCHEMA2(2),
		SCHEMA3(3),
		SCHEMA_FUTURE(-1);
		
		private final int d_version;

		XmlFormatType(int version) {
			d_version = version;
		}

		public int getVersion() {
			return d_version;
		}
	}
	
	private static JAXBContext s_jaxb;

	private static void initialize() throws JAXBException {
		if (s_jaxb == null) {
			s_jaxb = JAXBContext.newInstance("org.drugis.addis.entities.data");
		}
	}
	
	public static void marshallAddisData(AddisData data, OutputStream os) throws JAXBException {
		initialize();
		Marshaller marshaller = s_jaxb.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://drugis.org/files/addis-" + JAXBConvertor.LATEST_VERSION + ".xsd");
		marshaller.marshal(data, os);
	}
	
	public static AddisData unmarshallAddisData(InputStream is) throws JAXBException {
		initialize();
		Unmarshaller unmarshaller = s_jaxb.createUnmarshaller();
		unmarshaller.setEventHandler(new AddisDataValidationEventHandler());
		return (AddisData) unmarshaller.unmarshal(is);
	}
	
	// should be moved somewhere else and changed
	public static class AddisDataValidationEventHandler implements ValidationEventHandler  {
		public boolean handleEvent(ValidationEvent ve) {
			ValidationEventLocator  locator = ve.getLocator();
			//Print message from valdation event
			System.err.println("Invalid AddisData document: " + locator.getURL());
			System.err.println("Error: " + ve.getMessage());
			//Output line and column number
			System.err.println("Error at column " + locator.getColumnNumber() + 
								", line " + locator.getLineNumber());
			if (ve.getSeverity() == ValidationEvent.ERROR) {
				return true; // keeps unmarshalling
			} else if (ve.getSeverity() == ValidationEvent.FATAL_ERROR) {
				System.err.println("Corrupt AddisData document ... stopped unmarshalling.");
			}
			return false;
		}
	}

	public static XmlFormatType determineXmlType(InputStream is) throws IOException {
		is.mark(1024);
		byte[] buffer = new byte[1024];
		int bytesRead = is.read(buffer);
		String str = new String(buffer, 0, bytesRead);
		Pattern pattern = Pattern.compile("http://drugis.org/files/addis-([0-9]*).xsd");
		Matcher matcher = pattern.matcher(str);
		XmlFormatType type = null;
		if (matcher.find()) {
			if (matcher.group(1).equals("1")) {
				type = XmlFormatType.SCHEMA1;
			} else if (matcher.group(1).equals("2")) {
				type = XmlFormatType.SCHEMA2;
			} else if (matcher.group(1).equals("3")) {
				type = XmlFormatType.SCHEMA3;
			} else {
				type = XmlFormatType.SCHEMA_FUTURE;
			}
		} else {
			type = XmlFormatType.LEGACY;
		}
		is.reset();
		return type;
	}
}
