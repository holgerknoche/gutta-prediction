package gutta.prediction.span;

import gutta.prediction.util.EqualityUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Span} represents an interval of code execution within a trace. Spans can contain {@linkplain SpanEvent events} and have {@linkplain SpanOverlay
 * overlays}, and form a hierarchy reflecting the call hierarchy from the trace.
 */
public final class Span extends Interval implements TraceElement {

    private final String name;

    private final boolean root;

    private final List<SpanOverlay> spanOverlays;

    private final List<SpanEvent> spanEvents;

    private final List<Span> children;

    /**
     * Creates a new span with the given name and start timestamp without a parent (<i>root span</i>).
     * 
     * @param name           The name of the span
     * @param startTimestamp The start timestamp of the span
     */
    public Span(String name, long startTimestamp) {
        this(name, startTimestamp, 0, null, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Creates a new span with the given name, start timestamp, and parent.
     * 
     * @param name           The name of the span
     * @param startTimestamp The start timestamp of the span
     * @param parent         The parent span of the new span
     */
    public Span(String name, long startTimestamp, Span parent) {
        this(name, startTimestamp, 0, parent, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Creates a new span with from the given data.
     * 
     * @param name           The name of the span
     * @param startTimestamp The start timestamp of the span
     * @param endTimestamp   The end timestamp of the span
     * @param parent         The parent span of the new span
     * @param spanEvents     The span events of the new span
     * @param spanOverlays   The span overlays of the new span
     */
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

    /**
     * Returns the name of the span.
     * 
     * @return see above
     */
    public String name() {
        return this.name;
    }

    /**
     * Denotes whether this span is a root span.
     * 
     * @return see above
     */
    public boolean isRoot() {
        return this.root;
    }

    /**
     * Adds the given event to this span.
     * 
     * @param event The event to add
     */
    public void addEvent(SpanEvent event) {
        this.spanEvents.add(event);
    }

    /**
     * Adds the given overlay to this span.
     * 
     * @param overlay The overlay to add
     */
    public void addOverlay(SpanOverlay overlay) {
        this.spanOverlays.add(overlay);
    }

    private void addChild(Span child) {
        this.children.add(child);
    }

    /**
     * Returns the events contained in this span.
     * 
     * @return see above
     */
    public List<SpanEvent> events() {
        return Collections.unmodifiableList(this.spanEvents);
    }

    /**
     * Returns the overlays associated with this span.
     * 
     * @return see above
     */
    public List<SpanOverlay> overlays() {
        return Collections.unmodifiableList(this.spanOverlays);
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

    private boolean equalsInternal(Span that) {
        if (!super.equalsInternal(that)) {
            return false;
        }

        return Objects.equals(this.name, that.name) && //
                Objects.equals(this.spanEvents, that.spanEvents) && //
                Objects.equals(this.spanOverlays, that.spanOverlays) && //
                Objects.equals(this.children, that.children);
    }

    @Override
    public String toString() {
        return "Span '" + this.name + "', [" + this.startTimestamp() + " -- " + this.endTimestamp() + "]\n events: " + this.events() + "\n overlays: " +
                this.spanOverlays + "\n children: " + this.children;
    }

}
