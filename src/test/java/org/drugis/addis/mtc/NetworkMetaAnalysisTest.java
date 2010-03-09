package org.drugis.addis.mtc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.drugis.addis.ExampleData;
import org.drugis.addis.entities.Arm;
import org.drugis.addis.entities.BasicRateMeasurement;
import org.drugis.addis.entities.Endpoint;
import org.drugis.addis.entities.Study;
import org.drugis.addis.entities.Variable;
import org.drugis.addis.entities.metaanalysis.NetworkMetaAnalysis;
import org.drugis.mtc.InconsistencyModel;
import org.drugis.mtc.Network;
import org.drugis.mtc.NetworkBuilder;
import org.drugis.mtc.Treatment;
import org.drugis.mtc.jags.JagsModelFactory;
import org.junit.Before;
import org.junit.Test;

public class NetworkMetaAnalysisTest {
    private NetworkBuilder d_builder;
	private Network d_network;
	private InconsistencyModel d_model;

    @Before
    public void setUp() {
        d_builder = new NetworkBuilder();
        
        System.out.println();
        
        NetworkMetaAnalysis netwAnalysis = ExampleData.buildNetworkMetaAnalysis();
        
        for(Study s : netwAnalysis.getIncludedStudies()){
			for (Arm a : s.getArms()) {
				for (Variable v : s.getVariables(Endpoint.class)) {
					if(! (s.getMeasurement(v, a) instanceof BasicRateMeasurement))
						break;
					BasicRateMeasurement m = (BasicRateMeasurement)s.getMeasurement(v, a);		
					d_builder.add(s.getId(), a.getDrug().getName(), m.getRate(), m.getSampleSize());
				}
        	}
        }
        d_network = d_builder.buildNetwork();
        
		d_model = (new JagsModelFactory()).getInconsistencyModel(d_network);
    }

    @Test
    public void getResults() {
    	d_model.run();
    	Treatment fluox = d_builder.getTreatment("Fluoxetine");
    	Treatment sertr = d_builder.getTreatment("Sertraline");
    	Treatment parox = d_builder.getTreatment("Paroxetine");
    	assertNotNull(d_model.getRelativeEffect(fluox, sertr));
    	assertNotNull(d_model.getRelativeEffect(sertr, fluox));
    	assertNotNull(d_model.getRelativeEffect(fluox, parox));
    	assertNotNull(d_model.getRelativeEffect(parox, fluox));
    	assertNotNull(d_model.getRelativeEffect(sertr, parox));
    	assertNotNull(d_model.getRelativeEffect(parox, sertr));

    	assertEquals(0, d_model.getInconsistencyFactors().size());
    }
}
