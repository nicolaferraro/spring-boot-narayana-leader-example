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

import com.arjuna.ats.jbossatx.jta.RecoveryManagerService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.jta.narayana.NarayanaRecoveryManagerBean;
import org.springframework.context.event.EventListener;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This bean makes sure that recover manager service is only stared on a host which name ends with '-0' i.e. on a first
 * pod container in a stateful set.
 *
 * This extension requires a change in a current NarayanaRecoveryManagerBean.
 *
 * In real life this class should live in a separate spring boot starter.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class CustomNarayanaRecoveryManagerBean extends NarayanaRecoveryManagerBean {

    private final RecoveryManagerService recoveryManagerService;

    public CustomNarayanaRecoveryManagerBean(RecoveryManagerService recoveryManagerService) {
        super(recoveryManagerService);
        this.recoveryManagerService = recoveryManagerService;
    }

    @EventListener
    public void create(ApplicationReadyEvent ignored) {
        if (shouldStartRecoveryService()) {
            System.out.println("Starting recovery service");
            super.create(ignored);
            recoveryManagerService.addXAResourceRecovery(new DummyXAResourceRecovery());
        }
    }

    @Override
    public void destroy() throws Exception {
        if (shouldStartRecoveryService()) {
            super.destroy();
        }
    }

    private boolean shouldStartRecoveryService() {
        try {
            return InetAddress.getLocalHost().getHostName().endsWith("-0");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

}
