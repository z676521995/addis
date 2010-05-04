package org.drugis.common.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.NumberFormat;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.BorderFactory;
import javax.swing.text.NumberFormatter;

import org.drugis.addis.gui.components.MeasurementTable;
import org.drugis.addis.presentation.StudyCharacteristicHolder;

import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.binding.adapter.Bindings;
import com.jgoodies.binding.formatter.EmptyNumberFormatter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.binding.value.ValueModel;
import com.jgoodies.forms.util.DefaultUnitConverter;

public class AuxComponentFactory {

	public static <T> JComboBox createBoundComboBox(T[] values, ValueModel model) {
		SelectionInList<T> typeSelectionInList =
			new SelectionInList<T>(values, model);
		JComboBox type = BasicComponentFactory.createComboBox(typeSelectionInList);
		return type;
	}
	
	public static <T> JComboBox createBoundComboBox(ValueModel listHolder, ValueModel model) {
		SelectionInList<T> typeSelectionInList =
			new SelectionInList<T>(listHolder, model);
		JComboBox type = BasicComponentFactory.createComboBox(typeSelectionInList);
		return type;
	}

	public static JScrollPane createTextArea(ValueModel model, boolean editable) {
		JTextArea area = BasicComponentFactory.createTextArea(model);
		area.setEditable(editable);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		if (!editable)
			area.setUI(new javax.swing.plaf.basic.BasicTextAreaUI());
		JScrollPane pane = new JScrollPane(area);
		pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		pane.setPreferredSize(new Dimension(
				DefaultUnitConverter.getInstance().dialogUnitXAsPixel(200, area), 
				DefaultUnitConverter.getInstance().dialogUnitYAsPixel(50, area)));
		return pane;
	}

	public static JComponent createCharacteristicView(StudyCharacteristicHolder model) {
		JComponent component = null;
		Class<?> valueType = model.getCharacteristic().getValueType();
		if (model.getValue() == null) {
			return new JLabel("UNKNOWN");
		}
		if (valueType.equals(String.class)) {
			component = createTextArea(model,false);
		} else if (valueType.equals(Date.class)) {
			component = BasicComponentFactory.createLabel(model, new DayDateFormat());
		} else {
			component = BasicComponentFactory.createLabel(model, new OneWayObjectFormat());
		}
		return component;
	}
	
	public static JTextField createNonNegativeIntegerTextField(ValueModel model) {
	    NumberFormatter numberFormatter = new EmptyNumberFormatter(NumberFormat.getIntegerInstance(),0);
        numberFormatter.setValueClass(Integer.class);
        numberFormatter.setMinimum(0);
		
		JFormattedTextField field = new JFormattedTextField(numberFormatter);
		field.setColumns(3);
		Bindings.bind(field, model);
		return field;
	}

	public static JComponent createUnscrollableTablePanel(
			MeasurementTable measurementTable) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(measurementTable, BorderLayout.CENTER);
		panel.add(measurementTable.getTableHeader(), BorderLayout.PAGE_START);
		
		measurementTable.setBackground(Color.WHITE);
		measurementTable.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		return panel;
	}
	
	public static JRadioButton createDynamicEnabledRadioButton(String text, Object choice, ValueModel selectedValueModel, ValueModel enabledModel) {
		JRadioButton button = BasicComponentFactory.createRadioButton(selectedValueModel, choice, text);
		Bindings.bind(button,"enabled", enabledModel);
		
		enabledModel.addValueChangeListener(new EnabledListener(selectedValueModel));
		
		return button;
	}

	private static class EnabledListener implements PropertyChangeListener {
		ValueModel d_selectedValueModel;
		
		public EnabledListener(ValueModel selectedValueModel) {
			d_selectedValueModel = selectedValueModel;
		}
		
		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getNewValue().equals(false))
				d_selectedValueModel.setValue(null);
		}
	}
}
