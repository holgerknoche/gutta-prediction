package gutta.prediction.domain;

public class RootSpan extends Span {
	
	private static final String ROOT_SPAN_NAME = "<root>";
	
	public RootSpan(long beginNs) {
		super(ROOT_SPAN_NAME, beginNs, null);
	}

}
