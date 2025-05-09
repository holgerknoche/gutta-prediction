package gutta.prediction.util;

import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases for the class {@link SimpleTaskScope}.
 */
class SimpleTaskScopeTest {
    
    /**
     * Test case: A single successful task is executed and the result is returned. 
     * 
     * @throws InterruptedException Not expected
     * @throws ExecutionException Not expected
     */
    @Test
    void singleSuccessfulTask() throws InterruptedException, ExecutionException {
        try (var scope = new SimpleTaskScope<String>()) {
            var subtask = scope.fork(this::successfulAction);
            
            scope.join().throwIfFailed();
            
            assertEquals("OK", subtask.get());
        }
    }
    
    /**
     * Test case: A single failing task is executed and the error is reported in the appropriate way. 
     * 
     * @throws InterruptedException Not expected
     * @throws ExecutionException Not expected
     */
    @Test
    void singleFailedTask() throws InterruptedException, ExecutionException {
        try (var scope = new SimpleTaskScope<String>()) {
            var subtask = scope.fork(this::failingAction);
            
            var thrownException = assertThrows(ExecutionException.class, () -> scope.join().throwIfFailed());
            
            assertNull(subtask.get());
            assertTrue(thrownException.getCause() instanceof NullPointerException);
            assertEquals(0, thrownException.getSuppressed().length);
        }
    }
    
    @Test
    void multipleSuccessfulTasks() throws InterruptedException, ExecutionException {
        try (var scope = new SimpleTaskScope<String>()) {
            var subtask1 = scope.fork(this::successfulAction);
            var subtask2 = scope.fork(this::successfulAction);
            var subtask3 = scope.fork(this::successfulAction);
            
            scope.join().throwIfFailed();
            
            assertEquals("OK", subtask1.get());
            assertEquals("OK", subtask2.get());
            assertEquals("OK", subtask3.get());
        }
    }
    
    /**
     * Test case: Multiple failing tasks are executed and the error is reported in the appropriate way. 
     * 
     * @throws InterruptedException Not expected
     * @throws ExecutionException Not expected
     */
    @Test
    void multipleFailingTasks() {
        try (var scope = new SimpleTaskScope<String>()) {
            var subtask1 = scope.fork(this::failingAction);
            // A successful action in between
            var subtask2 = scope.fork(this::successfulAction);
            var subtask3 = scope.fork(this::failingAction);
            var subtask4 = scope.fork(this::failingAction);
            
            var thrownException = assertThrows(ExecutionException.class, () -> scope.join().throwIfFailed());
            
            assertNull(subtask1.get());
            assertEquals("OK", subtask2.get());
            assertNull(subtask3.get());
            assertNull(subtask4.get());
            
            assertTrue(thrownException.getCause() instanceof NullPointerException);
            
            var suppressedErrors = thrownException.getSuppressed();            
            assertEquals(2, suppressedErrors.length);
            assertTrue(suppressedErrors[0] instanceof NullPointerException);
            assertTrue(suppressedErrors[1] instanceof NullPointerException);
        }
    }
    
    private String successfulAction() {
        return "OK";
    }
    
    private String failingAction() {
        throw new NullPointerException();
    }

}
