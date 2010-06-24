package org.drugis.addis.gui;


public interface CategoryKnowledge {
	
	/**
	 * Get the type name (singular) of entities in this category.
	 */
	public String getSingular();
	
	/**
	 * Get the type name (plural) of entities in this category.
	 */
	public String getPlural();

	/**
	 * Get the filename for the entity icon.
	 */
	public String getIconName();
}
