package wiki.chiu.micro.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

class RedisTaskLockTest {

    private final RedissonClient redisson = mock(RedissonClient.class);
    private final RLock lock = mock(RLock.class);
    private RedisTaskLock taskLock;

    @BeforeEach
    void setUp() {
        TaskLockProperties properties = new TaskLockProperties();
        properties.setEnvironment("test");
        when(redisson.getLock("megalith:test:task:sample")).thenReturn(lock);
        taskLock = new RedisTaskLock(redisson, properties);
    }

    @Test
    void runsAndUnlocksWhenAcquired() {
        Runnable task = mock(Runnable.class);
        when(lock.tryLock()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        assertTrue(taskLock.tryRun("sample", task));

        verify(task).run();
        verify(lock).unlock();
    }

    @Test
    void skipsWhenAnotherNodeOwnsTheLock() {
        Runnable task = mock(Runnable.class);
        when(lock.tryLock()).thenReturn(false);

        assertFalse(taskLock.tryRun("sample", task));

        verify(task, never()).run();
        verify(lock, never()).unlock();
    }

    @Test
    void unlocksAndPropagatesTaskFailure() {
        when(lock.tryLock()).thenReturn(true);
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        assertThrows(
            IllegalStateException.class,
            () ->
                taskLock.tryRun(
                    "sample",
                    () -> {
                        throw new IllegalStateException("failed");
                    }));

        verify(lock).unlock();
    }

    @Test
    void blockingExecutionReturnsValue() {
        when(lock.isHeldByCurrentThread()).thenReturn(true);

        assertEquals("done", taskLock.run("sample", () -> "done"));

        verify(lock).lock();
        verify(lock).unlock();
    }
}
