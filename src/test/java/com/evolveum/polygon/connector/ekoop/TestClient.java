/*
 * Copyright (c) 2010-2015 Evolveum
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

package com.evolveum.polygon.connector.ekoop;

import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.objects.*;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

/**
 * Basic CRUD Tests for E-Koop connector over {@link @DummyServiceImpl}.
 */
public class TestClient {

    static EKoopConfiguration configuration;
    static EKoopConnector connector;

    private static final Log LOG = Log.getLog(TestClient.class);

    static final ObjectClass ACCOUNT_OBJECT_CLASS = new ObjectClass(ObjectClass.ACCOUNT_NAME);

    /**
     * test user name
     */
    static final String USER_NAME = "Evolveum";

    /**
     * test user UID - TCKN (Turkish Citizenship Number)
     */
    static final String USER_UID = "123";

    @BeforeClass
    public static void setUp() throws Exception {
        configuration = new EKoopConfiguration();
        configuration.setUseMockup(true); // target system is not accessible

        connector = new EKoopConnector();
        connector.init(configuration);

        try {
            // delete old test account
            connector.delete(ACCOUNT_OBJECT_CLASS, new Uid(USER_UID), null);
        } catch (Exception e) {
            LOG.warn("Only cleanup test user..." + e, e.toString());
        }
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (connector != null) {
            connector.dispose();
        }
    }

    @Test
    public void testConn() throws RemoteException {
        connector.test();
    }

    @Test
    public void testSchema() throws Exception {
        Schema schema = connector.schema();
        LOG.info("generated schema need to compare manually with specification:\n{0}", schema);
    }

    @Test
    public void testCreate() throws RemoteException {
        Set<Attribute> attributes = new HashSet<Attribute>();

        String givenName = "Test";
        String familyName = "Evolveum";
        String email = "test@evolveum.com";
        Boolean enable = false; // disable user
        String cityName = "Bratislava";
        String telephoneNumber = "421903123456";
        String secondTelephoneNumber = "421903987654";

        attributes.add(AttributeBuilder.build(Name.NAME, USER_NAME));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_UNIQUE_ID, USER_UID));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_GIVEN_NAME, givenName));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_FAMILY_NAME, familyName));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_EMAIL_ADDRESS, email));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_CITY_NAME, cityName));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_TELEPHONE_NUMBER, telephoneNumber));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_SECOND_TELEPHONE_NUMBER, secondTelephoneNumber));
        attributes.add(AttributeBuilder.build(OperationalAttributes.ENABLE_NAME, enable));
        // create it
        OperationOptions operationOptions = null;
        connector.create(ACCOUNT_OBJECT_CLASS, attributes, operationOptions);

        // read it
        ConnectorObject user = getUserByUid(USER_UID);
        Assert.assertTrue(user != null, "Created user " + USER_UID + " not found");

        Assert.assertEquals(user.getAttributeByName(Name.NAME).getValue().get(0), USER_NAME);
        Assert.assertEquals(user.getAttributeByName(Uid.NAME).getValue().get(0), USER_UID);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_GIVEN_NAME).getValue().get(0), givenName);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_FAMILY_NAME).getValue().get(0), familyName);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_EMAIL_ADDRESS).getValue().get(0), email);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_CITY_NAME).getValue().get(0), cityName);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_TELEPHONE_NUMBER).getValue().get(0), telephoneNumber);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_SECOND_TELEPHONE_NUMBER).getValue().get(0), secondTelephoneNumber);
        Assert.assertEquals(user.getAttributeByName(OperationalAttributes.ENABLE_NAME).getValue().get(0), enable);
    }

    @Test(dependsOnMethods = {"testCreate"})
    public void testListAllUsers() throws RemoteException {
        EkoopIKFilter query = null;
        final int[] count = {0};
        ResultsHandler handler = new ResultsHandler() {
            @Override
            public boolean handle(ConnectorObject connectorObject) {
                count[0]++;
                return true; // continue
            }
        };
        OperationOptions options = null;
        connector.executeQuery(ACCOUNT_OBJECT_CLASS, query, handler, options);
        System.out.println("count[0] = " + count[0]);
        Assert.assertTrue(count[0] > 0, "Find all users return zero users");
    }

    @Test(dependsOnMethods = {"testCreate"})
    public void testGetUser() throws RemoteException {
        ConnectorObject user = getUserByUid(USER_UID);
        Assert.assertTrue(user != null, "Find created user returns null");
        Assert.assertEquals(user.getAttributeByName(Name.NAME).getValue().get(0), USER_NAME);
        Assert.assertEquals(user.getAttributeByName(Uid.NAME).getValue().get(0), USER_UID);
    }

    private ConnectorObject getUserByUid(String uid) {
        EkoopIKFilter query = new EkoopIKFilter();
        query.byUid = uid;
        final ConnectorObject[] found = {null};
        ResultsHandler handler = new ResultsHandler() {
            @Override
            public boolean handle(ConnectorObject connectorObject) {
                found[0] = connectorObject;
                return true; // continue
            }
        };
        OperationOptions options = null;
        connector.executeQuery(ACCOUNT_OBJECT_CLASS, query, handler, options);

        // check attribute values
        ConnectorObject user = found[0];

        return user;
    }

    @Test(dependsOnMethods = {"testGetUser"})
    public void testUpdateUser() throws RemoteException {
        Set<Attribute> attributes = new HashSet<Attribute>();

        String givenName = "Test v.2";
        String renamedUserName = USER_NAME + "V2"; // rename user
        String familyName = "Evolveum v.2";
        String email = "testV2@evolveum.com";
        Boolean enable = true; // enable user
        String cityName = "Bratislava II";
        String telephoneNumber = "421903456789";
        String secondTelephoneNumber = "421903654321";

        attributes.add(AttributeBuilder.build(Name.NAME, renamedUserName));
        attributes.add(AttributeBuilder.build(Uid.NAME, USER_UID));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_GIVEN_NAME, givenName));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_FAMILY_NAME, familyName));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_EMAIL_ADDRESS, email));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_CITY_NAME, cityName));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_TELEPHONE_NUMBER, telephoneNumber));
        attributes.add(AttributeBuilder.build(EKoopConnector.ATTR_SECOND_TELEPHONE_NUMBER, secondTelephoneNumber));
        attributes.add(AttributeBuilder.build(OperationalAttributes.ENABLE_NAME, enable));
        // create it
        OperationOptions operationOptions = null;
        connector.update(ACCOUNT_OBJECT_CLASS, new Uid(USER_UID), attributes, operationOptions);

        // read it
        ConnectorObject user = getUserByUid(USER_UID);
        Assert.assertTrue(user != null, "updated user " + USER_UID + " not found");

        Assert.assertEquals(user.getAttributeByName(Name.NAME).getValue().get(0), renamedUserName);
        Assert.assertEquals(user.getAttributeByName(Uid.NAME).getValue().get(0), USER_UID);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_GIVEN_NAME).getValue().get(0), givenName);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_FAMILY_NAME).getValue().get(0), familyName);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_EMAIL_ADDRESS).getValue().get(0), email);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_CITY_NAME).getValue().get(0), cityName);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_TELEPHONE_NUMBER).getValue().get(0), telephoneNumber);
        Assert.assertEquals(user.getAttributeByName(EKoopConnector.ATTR_SECOND_TELEPHONE_NUMBER).getValue().get(0), secondTelephoneNumber);
        Assert.assertEquals(user.getAttributeByName(OperationalAttributes.ENABLE_NAME).getValue().get(0), enable);
    }

    @Test(dependsOnMethods = {"testUpdateUser"})
    public void testDelete() throws RemoteException {
        // delete it
        connector.delete(ACCOUNT_OBJECT_CLASS, new Uid(USER_UID), null);

        ConnectorObject disabledUser = getUserByUid(USER_UID);

        Boolean disabled = false;
        Assert.assertEquals(disabledUser.getAttributeByName(OperationalAttributes.ENABLE_NAME).getValue().get(0), disabled, "deleted user must not to be deleted, only disabled");
    }

}