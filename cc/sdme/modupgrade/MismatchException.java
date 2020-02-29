package cc.sdme.modupgrade;

public class MismatchException extends Exception {

	private static final long serialVersionUID = -4049773065966714542L;

	public MismatchException(String errorMessage) {
        super(errorMessage);
    }
}
