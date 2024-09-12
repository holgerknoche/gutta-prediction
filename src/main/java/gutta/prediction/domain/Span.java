package gutta.prediction.domain;

import java.util.ArrayList;
import java.util.List;

public class Span extends Interval {
	
	private final String name;
	
	private final Span parent;
	
	private final List<SpanOverlay> spanOverlays;
	
	private final List<SpanEvent> spanEvents;
	
	public Span(String name, long beginNs, Span parent) {
		super(beginNs);
		
		this.name = name;
		this.parent = parent;
		
		this.spanEvents = new ArrayList<>();
		this.spanOverlays = new ArrayList<>();
	}
	
	public String name() {
		return this.name;
	}
	
}
