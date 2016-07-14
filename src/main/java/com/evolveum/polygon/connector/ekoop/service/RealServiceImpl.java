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

import com.evolveum.polygon.connector.ekoop.soap.IEkoopService;
import com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS;

import java.rmi.RemoteException;

/**
 * Implementation of {@link Service} interface for E-Koop system.
 * <p/>
 * Created by gpalos on 12. 7. 2016.
 */
public class RealServiceImpl implements Service {
    private IEkoopService soap = null;

    public RealServiceImpl(IEkoopService soap) {
        this.soap = soap;
    }

    @Override
    public PRL_SYS_USERS getUser(String tckn) throws RemoteException {
        return soap.getUser(tckn);
    }

    @Override
    public Integer createUser(PRL_SYS_USERS user) throws RemoteException {
        return soap.createUser(user);
    }

    @Override
    public Integer updateUser(PRL_SYS_USERS user) throws RemoteException {
        return soap.updateUser(user);
    }

    @Override
    public Integer disableUser(String tckn) throws RemoteException {
        return soap.disableUser(tckn);
    }

    @Override
    public Integer enableUser(String tckn) throws RemoteException {
        return soap.enableUser(tckn);
    }

    @Override
    public PRL_SYS_USERS[] listAllUser() throws RemoteException {
        return soap.listAllUsers();
    }
}
