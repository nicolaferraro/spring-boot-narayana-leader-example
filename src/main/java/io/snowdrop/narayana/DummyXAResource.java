/*
 * Copyright 2016-2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.snowdrop.narayana;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.jta.xa.XidImple;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class DummyXAResource implements XAResource {

    private static final String RECORD_TYPE = String.format("/%s", DummyXAResource.class.getSimpleName());

    private static final Xid[] EMPTY_XID_ARRAY = new Xid[0];

    private final boolean kill;

    public DummyXAResource(boolean kill) {
        this.kill = kill;
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        persistXid(xid);
        return 0;
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        if (kill) {
            System.out.println("Crashing the system");
            Runtime.getRuntime().halt(1);
        }
        removeXid(xid);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        removeXid(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        Xid[] xids = getXid()
                .map(xid -> new Xid[] { xid })
                .orElse(EMPTY_XID_ARRAY);
        System.out.printf("Returning xids '%s' to recover", Arrays.toString(xids));
        return xids;
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return xaResource instanceof DummyXAResource;
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
    }

    @Override
    public void forget(Xid xid) throws XAException {
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return false;
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
    }

    private void persistXid(Xid xid) throws XAException {
        System.out.printf("Persisting xid '%s'\n", xid);
        OutputObjectState state = new OutputObjectState();
        try {
            XidImple.pack(state, xid);
            StoreManager.getParticipantStore().write_uncommitted(Uid.minUid(), RECORD_TYPE, state);
        } catch (IOException | ObjectStoreException e) {
            e.printStackTrace();
            throw new XAException(XAException.XAER_RMFAIL);
        }
    }

    private void removeXid(Xid xid) throws XAException {
        System.out.printf("Removing xid '%s'\n", xid);
        try {
            StoreManager.getParticipantStore().remove_uncommitted(Uid.minUid(), RECORD_TYPE);
        } catch (ObjectStoreException e) {
            e.printStackTrace();
            throw new XAException(XAException.XAER_RMFAIL);
        }
    }

    private Optional<Xid> getXid() throws XAException {
        try {
            InputObjectState state = StoreManager.getParticipantStore().read_uncommitted(Uid.minUid(), RECORD_TYPE);
            if (state != null && state.notempty()) {
                return Optional.of(XidImple.unpack(state));
            }
        } catch (IOException | ObjectStoreException e) {
            e.printStackTrace();
            throw new XAException(XAException.XAER_RMFAIL);
        }

        return Optional.empty();
    }
}
