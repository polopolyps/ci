package com.polopoly.ps.ci.tool;

import static com.polopoly.ps.ci.tool.ProcessOperation.HARDRESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.JSTACK;
import static com.polopoly.ps.ci.tool.ProcessOperation.KILL;
import static com.polopoly.ps.ci.tool.ProcessOperation.LOG;
import static com.polopoly.ps.ci.tool.ProcessOperation.RESTART;
import static com.polopoly.ps.ci.tool.ProcessOperation.RUNNING;
import static com.polopoly.ps.ci.tool.ProcessOperation.START;
import static com.polopoly.ps.ci.tool.ProcessOperation.STOP;
import static com.polopoly.ps.ci.tool.ProcessOperation.TAIL;

public abstract class SimpleProcessTool extends ProcessTool<ProcessParameters> {

    @Override
    public ProcessParameters createParameters() {
        return new ProcessParameters(START, STOP, RESTART, HARDRESTART, TAIL, LOG, JSTACK, KILL, RUNNING);
    }

}
