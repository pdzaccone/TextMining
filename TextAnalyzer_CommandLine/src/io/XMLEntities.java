package io;

import analysis.ICategory;
import dataUnits.IDataUnit;
import utils.ConfigurationData;

/**
 * This enumeration helps identify various entities that can be encountered in XML files
 * @author Pdz
 *
 */
public enum XMLEntities {
	/**
	 * Single category
	 */
	category(ICategory.XMLTags.category.getTagText()),
	
	/**
	 * Block with multiple categories
	 */
	categories(ICategory.XMLTags.categories.getTagText()),
	
	/**
	 * Document corpus
	 */
	corpus(IDataUnit.XMLTags.corpus.getTagText()),
	
	/**
	 * Configuration block
	 */
	config(ConfigurationData.configTag);
	
	private final String text;
	
	private XMLEntities(String text) {
		this.text = text;
	}
	
	public String getTagText() {
		return this.text;
	}
}