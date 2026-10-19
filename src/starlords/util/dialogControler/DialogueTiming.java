package starlords.util.dialogControler;

import com.fs.starfarer.api.Global;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.List;

/** One warning for a slow interaction; normal clicks produce no timing output. */
public final class DialogueTiming implements AutoCloseable {
    private static final long WARN_NANOS = 250_000_000L;
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();
    private final String option;
    private final long start = System.nanoTime();
    private final long cpuStart = cpuTime();
    private long stageStart = start;
    private String stage = "dispatch";
    private final List<String> stages = new ArrayList<>();

    public DialogueTiming(String option) {
        this.option = option;
    }

    public void stage(String next) {
        long now = System.nanoTime();
        stages.add(stage + "=" + millis(now - stageStart) + "ms");
        stage = next;
        stageStart = now;
    }

    @Override
    public void close() {
        long now = System.nanoTime();
        if (now - start < WARN_NANOS) return;
        long cpuEnd = cpuTime();
        stages.add(stage + "=" + millis(now - stageStart) + "ms");
        String cpu = cpuStart >= 0 && cpuEnd >= cpuStart ? millis(cpuEnd - cpuStart) + "ms" : "unavailable";
        Global.getLogger(DialogueTiming.class).warn("Slow dialogue option '" + option
                + "': total=" + millis(now - start) + "ms, threadCPU=" + cpu
                + "; " + String.join(", ", stages));
    }

    private static long cpuTime() {
        return THREADS.isCurrentThreadCpuTimeSupported() && THREADS.isThreadCpuTimeEnabled()
                ? THREADS.getCurrentThreadCpuTime() : -1L;
    }

    private static long millis(long nanos) { return nanos / 1_000_000L; }
}
