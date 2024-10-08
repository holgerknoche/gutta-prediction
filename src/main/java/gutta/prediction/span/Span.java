package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Span extends Interval implements TraceElement {

    private final String name;
    
    private final boolean root;
    
    private final List<SpanOverlay> spanOverlays;

    private final List<SpanEvent> spanEvents;
    
    private final List<Span> children;

    public Span(String name, long startTimestamp, Span parent) {
        this(name, startTimestamp, 0, parent, new ArrayList<>(), new ArrayList<>());
    }
    
    protected Span(String name, long startTimestamp, long endTimestamp, Span parent, List<SpanEvent> spanEvents, List<SpanOverlay> spanOverlays) {
        super(startTimestamp, endTimestamp);
        
        this.name = name;
        this.spanEvents = spanEvents;
        this.spanOverlays = spanOverlays;
        this.children = new ArrayList<>();
        
        if (parent != null) {
            this.root = false;
            parent.addChild(this);
        } else {
            this.root = true;
        }
    }

    public String name() {
        return this.name;
    }
    
    public boolean isRoot() {
        return this.root;
    }
    
    public void addEvent(SpanEvent event) {
        this.spanEvents.add(event);
    }
    
    public void addOverlay(SpanOverlay overlay) {
        this.spanOverlays.add(overlay);
    }
    
    private void addChild(Span child) {
        this.children.add(child);
    }
    
    @Override
    public <R> R accept(TraceElementVisitor<R> visitor) {
        return visitor.handleSpan(this);
    }
    
    @Override
    public void traverse(TraceElementVisitor<?> visitor) {
        visitor.handleSpan(this);
        
        this.spanOverlays.forEach(overlay -> overlay.traverse(visitor));
        this.spanEvents.forEach(event -> event.traverse(visitor));
        
        this.children.forEach(child -> child.traverse(visitor));
    }
    
    @Override
    public int hashCode() {
        return (this.name.hashCode() + (int) this.startTimestamp());
    }
    
    @Override
    public boolean equals(Object that) {
        return EqualityUtil.equals(this, that, this::equalsInternal);
    }
    
    protected boolean equalsInternal(Span that) {
        if (!super.equalsInternal(that)) {
            return false;
        }
        
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.spanEvents, that.spanEvents) &&
                Objects.equals(this.spanOverlays, that.spanOverlays) &&
                Objects.equals(this.children, that.children);
    }
    
    @Override
    public String toString() {
        return "Span '" + this.name + "', [" + this.startTimestamp() + " -- " + this.endTimestamp() + "]";
    }

}
