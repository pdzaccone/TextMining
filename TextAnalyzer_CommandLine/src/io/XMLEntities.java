package io;

import categories.ICategory;
import dataUnits.IDataUnit;
import utils.ConfigurationData;

public enum XMLEntities {
	category(ICategory.XMLTags.category.getTagText()),
	corpus(IDataUnit.XMLTags.corpus.getTagText()),
	config(ConfigurationData.configTag);
	
	private final String text;
	
	private XMLEntities(String text) {
		this.text = text;
	}
	
	public String getTagText() {
		return this.text;
	}
}