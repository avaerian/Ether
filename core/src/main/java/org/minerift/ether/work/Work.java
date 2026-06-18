package org.minerift.ether.work;

import org.minerift.ether.util.Either;

public abstract class Work<T> {

    protected volatile ValueStatus status;
    protected volatile Either<T, String> result;

    public Work() {
        this.status = ValueStatus.UNSTARTED;
        this.result = null;
    }

    /**
     * Attempts to complete this task, returning true if the task has completed.
     *
     * <p>
     * <b>NOTE:</b> the return value is <b><u>NOT</u></b> based on the status of the work queue
     * and whether it's open or not.
     *
     * @return whether the task has completed all work
     */
    public abstract boolean complete();

    /**
     * Returns the result if present, else blocks the current thread until either the result
     * is provided, or the task fails for some reason (e.g. queue shutdown, invalid state).
     *
     * @return result, if present
     */
    public T result() throws Exception { // TODO: review Exception
        while (result == null)
            Thread.onSpinWait();

        if(result.isRight())
            throw new Exception(result.getRight());
        return result.getLeft();
    }

    public void block() {

    }

    public void fail(String reason) {

    }

}
