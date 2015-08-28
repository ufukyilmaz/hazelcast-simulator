package com.hazelcast.simulator.protocol.exception;

import com.hazelcast.simulator.protocol.core.SimulatorAddress;
import com.hazelcast.simulator.protocol.operation.ExceptionOperation;
import org.apache.log4j.Logger;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.String.format;

abstract class AbstractExceptionLogger implements ExceptionLogger {

    static final Logger LOGGER = Logger.getLogger(AbstractExceptionLogger.class);

    private final AtomicLong exceptionCount = new AtomicLong();

    private final SimulatorAddress localAddress;
    private final ExceptionType exceptionType;

    AbstractExceptionLogger(SimulatorAddress localAddress, ExceptionType exceptionType) {
        this.localAddress = localAddress;
        this.exceptionType = exceptionType;
    }

    @Override
    public long getLogInvocationCount() {
        return exceptionCount.get();
    }

    @Override
    public void log(Throwable cause) {
        log(cause, null);
    }

    @Override
    public void log(Throwable cause, String testId) {
        if (cause == null) {
            throw new IllegalArgumentException("Exception for RemoteExceptionLogger cannot be null");
        }

        long exceptionId = exceptionCount.incrementAndGet();
        if (exceptionId > MAX_EXCEPTION_COUNT) {
            if (exceptionId == MAX_EXCEPTION_COUNT + 1) {
                LOGGER.warn(format("The maximum number of %d exceptions has been exceeded."
                        + " No more exception will be sent to the remote Simulator component.", MAX_EXCEPTION_COUNT), cause);
            }
            return;
        }

        LOGGER.warn(format("Logged exception #%d: %s(%s)", exceptionId, cause.getClass().getSimpleName(), cause.getMessage()));
        ExceptionOperation operation = new ExceptionOperation(exceptionType.name(), localAddress.toString(), testId, cause);

        handleOperation(exceptionId, operation);
    }

    protected abstract void handleOperation(long exceptionId, ExceptionOperation operation);
}
