package nl.rug.escher.addis.entities;

public interface Measurement {

	public static final String PROPERTY_LABEL = "label";
	public static final String PROPERTY_ENDPOINT = "endpoint";
	public static final String PROPERTY_SAMPLESIZE = "sampleSize";

	public String getLabel();

	public Endpoint getEndpoint();

	public Integer getSampleSize();
}