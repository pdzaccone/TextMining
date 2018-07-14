package utils;

public class Tuple<T, V, K> {

	private T dataT;
	private V dataV;
	private K dataK;
	
	public Tuple() {
		this.dataT = null;
		this.dataV = null;
		this.dataK = null;
	}

	public Tuple(T valT, V valV, K valK) {
		this.dataT = valT;
		this.dataV = valV;
		this.dataK = valK;
	}
	
	public T getFirst() {
		return dataT;
	}

	public V getSecond() {
		return dataV;
	}

	public K getThird() {
		return dataK;
	}
}