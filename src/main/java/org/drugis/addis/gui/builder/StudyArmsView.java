package org.drugis.addis.gui.builder;

import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParsePosition;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.drugis.addis.entities.Arm;
import org.drugis.addis.entities.BasicArm;
import org.drugis.addis.entities.Endpoint;
import org.drugis.addis.entities.Measurement;
import org.drugis.addis.entities.OutcomeMeasure;
import org.drugis.addis.entities.Study;
import org.drugis.addis.gui.GUIFactory;
import org.drugis.addis.presentation.BasicArmPresentation;
import org.drugis.addis.presentation.PresentationModelFactory;
import org.drugis.common.gui.LayoutUtil;
import org.drugis.common.gui.ViewBuilder;

import com.jgoodies.binding.PresentationModel;
import com.jgoodies.binding.adapter.BasicComponentFactory;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

public class StudyArmsView implements ViewBuilder {
	
	private PresentationModel<? extends Study> d_model;
	private PresentationModelFactory d_pm;

	public StudyArmsView(PresentationModel<? extends Study> model, PresentationModelFactory pm) {
		this.d_model = model;
		this.d_pm = pm;
	}

	public JPanel buildPanel() {
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout( 
				"left:pref, 5dlu, left:pref, 5dlu, center:pref, 5dlu, center:pref",
				"p"
				);
		
		int fullWidth = 5;
		for (int i = 1; i < d_model.getBean().getOutcomeMeasures().size(); ++i) {			
			layout.appendColumn(ColumnSpec.decode("3dlu"));
			layout.appendColumn(ColumnSpec.decode("center:pref"));			
			fullWidth += 2;
		}
		PanelBuilder builder = new PanelBuilder(layout);
		
		int row = 1;

		builder.addLabel("Size", cc.xy(5, row, "center, center"));		
		int col = 7;
		//FIXME
		for (OutcomeMeasure om : d_model.getBean().getOutcomeMeasures()) {
			if (!(om instanceof Endpoint)) {
				continue;
			}
			builder.add(
					GUIFactory.createOutcomeMeasureLabelWithIcon((Endpoint) om),
							cc.xy(col, row));
			col += 2;
		}
		row += 2;
	
		for (Arm g : d_model.getBean().getArms()) {
			row = buildArm(layout, builder, cc, row, g);
		}
		return builder.getPanel();
	}

	@SuppressWarnings("serial")
	private int buildArm(FormLayout layout, PanelBuilder builder, CellConstraints cc, int row, Arm g) {
		int col;
		BasicArmPresentation armModel = (BasicArmPresentation)d_pm.getModel(g);
		LayoutUtil.addRow(layout);
		builder.add(
				BasicComponentFactory.createLabel(d_pm.getLabeledModel(g).getLabelModel()),
				cc.xy(1, row));
		
		builder.add(
				BasicComponentFactory.createLabel(
						armModel.getModel(BasicArm.PROPERTY_DOSE),
						new Format() {
							
							@Override
							public Object parseObject(String source, ParsePosition pos) {
								return null;
							}
							
							@Override
							public StringBuffer format(Object obj, StringBuffer toAppendTo,
									FieldPosition pos) {
								return toAppendTo.append(obj.toString());
							}
						}),
						cc.xy(3, row, "right, center"));
		
		final JLabel armSizeLabel = BasicComponentFactory.createLabel(
				armModel.getModel(BasicArm.PROPERTY_SIZE),
				NumberFormat.getInstance());
		final String pgCharacteristicTooltip = armModel.getCharacteristicTooltip();
		if (!pgCharacteristicTooltip.equals(""))
			armSizeLabel.setToolTipText(pgCharacteristicTooltip);
		builder.add(
				armSizeLabel,
						cc.xy(5, row, "center, center"));
		
		col = 7;
		for (OutcomeMeasure e : d_model.getBean().getOutcomeMeasures()) {
			Measurement m = d_model.getBean().getMeasurement(e, g);
			if (m != null) {
				builder.add(
						BasicComponentFactory.createLabel(d_pm.getLabeledModel(m).getLabelModel()),
						cc.xy(col, row));
			}
			col += 2;
		}
		
		row += 2;
		return row;
	}
}
