package gutta.prediction.rewriting;

import gutta.prediction.domain.ComponentConnections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test cases for the class {@link TransactionContextRewriter}.
 */
class TransactionContextRewriterTest extends TraceRewriterTestTemplate {
    
    /**
     * Test case: Rewrite a trace containing all event types with a configuration that does not introduce any changes.
     */
    @Test
    void identityRewrite() {
        var fixture = this.createIdentityTraceFixture();
        
        var inputTrace = fixture.trace();
        var rewrittenTrace = new TransactionContextRewriter(fixture.useCaseAllocation(), fixture.methodAllocation(), new ComponentConnections()).rewriteTrace(inputTrace);

        assertEquals(inputTrace, rewrittenTrace);
    }
    
    

}
