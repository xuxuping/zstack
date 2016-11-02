package org.zstack.header.vm;

import org.zstack.header.configuration.PythonClass;
import org.zstack.header.exception.CloudRuntimeException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@PythonClass
public enum VmInstanceState {
    Created(null),
    Starting(VmInstanceStateEvent.starting),
    Running(VmInstanceStateEvent.running),
    Stopping(VmInstanceStateEvent.stopping),
    Stopped(VmInstanceStateEvent.stopped),
    Rebooting(VmInstanceStateEvent.rebooting),
    Destroying(VmInstanceStateEvent.destroying),
    Destroyed(VmInstanceStateEvent.destroyed),
    Migrating(VmInstanceStateEvent.migrating),
    Expunging(VmInstanceStateEvent.expunging),
    Suspending(VmInstanceStateEvent.suspending),
    Suspended(VmInstanceStateEvent.suspended),
    Resuming(VmInstanceStateEvent.resuming),
    Resumed(VmInstanceStateEvent.resumed),
    Error(null),
    Unknown(VmInstanceStateEvent.unknown);

    public static List<VmInstanceState> intermediateStates = new ArrayList<VmInstanceState>();

    static {
        intermediateStates.add(Starting);
        intermediateStates.add(Stopping);
        intermediateStates.add(Rebooting);
        intermediateStates.add(Destroying);
        intermediateStates.add(Migrating);
        intermediateStates.add(Suspending);

        Created.transactions(
                new Transaction(VmInstanceStateEvent.starting, VmInstanceState.Starting),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.destroyed, VmInstanceState.Destroyed)
        );
        Starting.transactions(
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown)
        );
        Running.transactions(
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.stopping, VmInstanceState.Stopping),
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped),
                new Transaction(VmInstanceStateEvent.rebooting, VmInstanceState.Rebooting),
                new Transaction(VmInstanceStateEvent.migrating, VmInstanceState.Migrating),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown)
        );
        Stopping.transactions(
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown)
        );
        Stopped.transactions(
                new Transaction(VmInstanceStateEvent.starting, VmInstanceState.Starting),
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped),
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown)
        );
        Migrating.transactions(
                new Transaction(VmInstanceStateEvent.migrated, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying)
        );
        Rebooting.transactions(
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying)
        );
        Unknown.transactions(
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.stopping, VmInstanceState.Stopping),
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown),
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped)
        );
        Destroying.transactions(
                new Transaction(VmInstanceStateEvent.unknown, VmInstanceState.Unknown),
                new Transaction(VmInstanceStateEvent.destroyed, VmInstanceState.Destroyed),
                new Transaction(VmInstanceStateEvent.destroying, VmInstanceState.Destroying),
                new Transaction(VmInstanceStateEvent.running, VmInstanceState.Running),
                new Transaction(VmInstanceStateEvent.expunging, VmInstanceState.Expunging)
        );
        Destroyed.transactions(
                new Transaction(VmInstanceStateEvent.stopped, VmInstanceState.Stopped)
        );
    }

    private VmInstanceState(VmInstanceStateEvent drivenEvent) {
        this.drivenEvent = drivenEvent;
    }

    private static class Transaction {
        VmInstanceStateEvent event;
        VmInstanceState nextState;

        private Transaction(VmInstanceStateEvent event, VmInstanceState nextState) {
            this.event = event;
            this.nextState = nextState;
        }
    }

    private Map<VmInstanceStateEvent, Transaction> transactionMap = new HashMap<VmInstanceStateEvent, Transaction>();

    private VmInstanceStateEvent drivenEvent;

    public VmInstanceStateEvent getDrivenEvent() {
        return drivenEvent;
    }

    private void transactions(Transaction...transactions) {
        for (Transaction tran : transactions) {
            transactionMap.put(tran.event, tran);
        }
    }

    public VmInstanceState nextState(VmInstanceStateEvent event) {
        Transaction tran = transactionMap.get(event);
        if (tran == null) {
            throw new CloudRuntimeException(String.format("cannot find next state for current state[%s] on transaction event[%s]",
                    this, event));
        }

        return tran.nextState;
    }
}
