package analysis;

import java.util.Set;

import utils.Languages;

/**
 * This interface provides support to those classes that keep data for multiple languages at the same time
 * @author Pdz
 *
 */
public interface IMultilingual {
	
	/**
	 * Gets a set of all languages, presented in an object 
	 * @return Resulting set of languages or 
	 */
	public Set<Languages> getLanguages();
}
