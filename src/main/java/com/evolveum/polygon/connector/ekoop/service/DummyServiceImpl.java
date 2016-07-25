/*
 * Copyright (c) 2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.polygon.connector.ekoop.service;

import com.evolveum.polygon.connector.ekoop.EKoopConnector;
import com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

/**
 * Dummy implementation of {@link Service} interface.
 * User's are stored only in memory, after restarted midpoint is cleaned and re-initialized.
 * <p/>
 * Created by gpalos on 13. 7. 2016.
 */
public class DummyServiceImpl implements Service {

    private static final Map<String, PRL_SYS_USERS> users = new HashMap<String, PRL_SYS_USERS>();

    public static final String SAMPLE_TCKN = "11111";

    static {
        // one sample user in dummy resource.
        PRL_SYS_USERS su = new PRL_SYS_USERS();
        su.setAKTIFMI(EKoopConnector.DISABLED);
        su.setCALISTIGIIL("locality");
        su.setCEPTEL2("secondPhoneNumber");
        su.setEMAILADDRESS("email@evolveum.com");
        su.setFAMILYNAME("familyName");
        su.setGIVENNAME("givenName");
        su.setNAME("userName");
        su.setTCKN(SAMPLE_TCKN);
        su.setTELEPHONENUMBER("telephoneNumber");

        users.put(SAMPLE_TCKN, su);

        // one sample user in dummy resource without tckn (will be filtered in connector).
        String tckn2 = "";
        PRL_SYS_USERS su2 = new PRL_SYS_USERS();
        su2.setAKTIFMI(EKoopConnector.DISABLED);
        su2.setCALISTIGIIL("locality 2");
        su2.setCEPTEL2("secondPhoneNumber 2");
        su2.setEMAILADDRESS("email2@evolveum.com");
        su2.setFAMILYNAME("familyName 2");
        su2.setGIVENNAME("givenName 2");
        su2.setNAME("userName 2");
        su2.setTCKN(tckn2);
        su2.setTELEPHONENUMBER("telephoneNumber 2");

        users.put(tckn2, su2);
    }

    @Override
    public PRL_SYS_USERS getUser(String tckn) throws RemoteException {
        if (users.containsKey(tckn)) {
            return users.get(tckn);
        }

        throw new RemoteException(EKoopConnector.NOT_EXISTS_MESSAGE);
    }

    @Override
    public Integer createUser(PRL_SYS_USERS user) throws RemoteException {
        if (users.containsKey(user.getTCKN())) {
            return EKoopConnector.CODE_USER_ALREADY_EXISTS;
        }

        users.put(user.getTCKN(), user);

        return EKoopConnector.CODE_SUCCESS;
    }

    @Override
    public Integer updateUser(PRL_SYS_USERS user) throws RemoteException {
        Integer notExists = userDoesNotExists(user.getTCKN());
        if (notExists != null) {
            return notExists;
        }

        users.put(user.getTCKN(), user);

        return EKoopConnector.CODE_SUCCESS;
    }

    private Integer userDoesNotExists(String tckn) {
        if (!users.containsKey(tckn)) {
            return EKoopConnector.CODE_USER_DOES_NOT_EXIST;
        }

        return null;
    }

    @Override
    public Integer disableUser(String tckn) throws RemoteException {
        Integer notExists = userDoesNotExists(tckn);
        if (notExists != null) {
            return notExists;
        }

        PRL_SYS_USERS user = users.get(tckn);
        if (EKoopConnector.DISABLED.equals(user.getAKTIFMI())) {
            return EKoopConnector.CODE_USER_ALREADY_DISABLED;
        }

        user.setAKTIFMI(EKoopConnector.DISABLED);

        return EKoopConnector.CODE_SUCCESS;
    }

    @Override
    public Integer enableUser(String tckn) throws RemoteException {
        Integer notExists = userDoesNotExists(tckn);
        if (notExists != null) {
            return notExists;
        }

        PRL_SYS_USERS user = users.get(tckn);
        if (EKoopConnector.ENABLED.equals(user.getAKTIFMI())) {
            return EKoopConnector.CODE_USER_ALREADY_ENABLED;
        }

        user.setAKTIFMI(EKoopConnector.ENABLED);

        return EKoopConnector.CODE_SUCCESS;
    }

    @Override
    public PRL_SYS_USERS[] listAllUser() throws RemoteException {
        return users.values().toArray(new PRL_SYS_USERS[users.size()]);
    }
}
