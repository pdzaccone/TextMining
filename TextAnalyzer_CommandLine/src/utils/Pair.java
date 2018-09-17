package utils;

/**
 * This class holds 2 values
 * @author Pdz
 *
 * @param <T>
 * @param <V>
 */
public class Pair<T, V> {

	/**
	 * 1st value
	 */
	private T dataT;
	
	/**
	 * 2nd value
	 */
	private V dataV;
	
	/**
	 * Constructor without parameters
	 */
	public Pair() {
		this.dataT = null;
		this.dataV = null;
	}

	/**
	 * Constructor with parameters
	 * @param valT 1st value
	 * @param valV 2nd value
	 */
	public Pair(T valT, V valV) {
		this.dataT = valT;
		this.dataV = valV;
	}
	
	public T getFirst() {
		return dataT;
	}

	public V getSecond() {
		return dataV;
	}

	/**
	 * Updates existing values
	 * @param newFirst New first value
	 * @param newSecond New second value
	 */
	public void update(T newFirst, V newSecond) {
		this.dataT = newFirst;
		this.dataV = newSecond;
	}
}