package gutta.prediction.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Future.State;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * Simplistic implementation of the {@link java.util.concurrent.StructuredTaskScope} to avoid the usage of preview features 
 * (in this case, Structured Concurrency).
 * This class only provides the necessary operations that are used in this project.
 * As soon as this feature is no longer a preview, this class can be removed, and usages should be replaced by 
 * {@link java.util.concurrent.StructuredTaskScope.ShutdownOnFailure}. 
 */
public class SimpleTaskScope<T> implements AutoCloseable {
    
    private final ExecutorService executorService;
    
    private final List<Subtask<T>> subtasks = new ArrayList<>();
    
    private boolean closed = false;
    
    private List<Throwable> errors = List.of();
    
    /**
     * Creates a new task scope that executes its subtasks as virtual threads.
     */
    public SimpleTaskScope() {
        this(Executors.newVirtualThreadPerTaskExecutor());
    }
    
    /**
     * Creates a new task scope that uses the given executor service to execute its subtasks.
     * 
     * @param executorService The executor service to use
     */
    public SimpleTaskScope(ExecutorService executorService) {
        this.executorService = requireNonNull(executorService);
    }
    
    /**
     * Forks a subtask to execute the given action.
     * 
     * @param action The action to execute
     * @return The subtask to execute the action
     */
    public Subtask<T> fork(Callable<T> action) {
        this.assertNotClosed();
        
        var subtask = new Subtask<T>(action);
        this.subtasks.add(subtask);
        
        return subtask;
    }
    
    /**
     * Executes the subtasks forked by this scope and waits until they have completed.
     * 
     * @return The scope to define the behavior in case of errors
     * @throws InterruptedException If an interrupt occurs while waiting for the subtasks to complete
     */
    public SimpleTaskScope<T> join() throws InterruptedException {
        this.assertNotClosed();
        
        this.closed = true;
        
        var numberOfSubtasks = this.subtasks.size();
        var latch = new CountDownLatch(numberOfSubtasks);
        var futures = new ArrayList<Future<Void>>(numberOfSubtasks);
        
        // Submit the subtasks to the executor service, which trigger the latch on completion
        for (var subtask : this.subtasks) {
            var future = this.executorService.submit(() -> this.runSubtask(subtask, latch));
            futures.add(future);
        }
        
        // Await for all subtasks to complete 
        latch.await();                
        
        // Collect the errors (if any) from the futures
        var thrownErrors = futures.stream()
            .filter(future -> future.state() == State.FAILED)
            .map(Future::exceptionNow)
            .collect(Collectors.toList());
        
        this.errors = thrownErrors;
        
        return this;
    }
    
    private Void runSubtask(Subtask<T> subtask, CountDownLatch latch) throws Exception {
        try {
            subtask.runActionAndStoreResult();
            return null;
        } finally {
            latch.countDown();            
        }
    }
    
    private void assertNotClosed() {
        if (this.closed) {
            throw new IllegalStateException("Task scope is already closed.");
        }
    }
    
    /**
     * Causes an exception to be thrown after the subtasks in this scope have been executed.
     * 
     * @throws ExecutionException If one or more errors occurred during the execution of the subtasks
     */
    public void throwIfFailed() throws ExecutionException {
        var errorsIterator = this.errors.iterator();
        
        if (errorsIterator.hasNext()) {
            // If there is at least one error, build an execution exception with the first error as its cause, and add
            // the remaining errors as suppressed
            var executionException = new ExecutionException("Error while executing subtasks.", errorsIterator.next());           
            errorsIterator.forEachRemaining(executionException::addSuppressed);
        }
    }
    
    @Override
    public void close() {
        this.executorService.shutdown();        
    }
    
    /**
     * Represents a subtask forked from a {@link SimpleTaskScope} for some action. 
     * 
     * @param <T> The type of the action's result
     */
    public static class Subtask<T> {
        
        private final Callable<T> action;
        
        private volatile boolean completed = false;
        
        private T result;
                
        Subtask(Callable<T> action) {
            this.action = action;
        }
        
        void runActionAndStoreResult() throws Exception {
            try {
                this.result = this.action.call();                
            } finally {
                this.completed = true;
            }
        }
        
        /**
         * Retrieves the result from the underlying action, if any.
         * 
         * @return The result or {@code null} if there was none or an exception occurred
         */
        public T get() {
            if (this.completed) {
                return this.result;
            } else {
                throw new IllegalStateException("Subtask was not yet completed.");
            }
        }
        
    }

}
