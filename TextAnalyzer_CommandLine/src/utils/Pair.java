package utils;

public class Pair<T, V> {

	private T dataT;
	private V dataV;
	
	public Pair() {
		this.dataT = null;
		this.dataV = null;
	}

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

	public void update(T newFirst, V newSecond) {
		this.dataT = newFirst;
		this.dataV = newSecond;
	}
}