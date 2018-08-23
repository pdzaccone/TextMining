package io;

import analysis.ICategory;
import dataUnits.IDataUnit;
import utils.ConfigurationData;

public enum XMLEntities {
	category(ICategory.XMLTags.category.getTagText()),
	categories(ICategory.XMLTags.categories.getTagText()),
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