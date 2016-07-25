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

package com.evolveum.polygon.connector.ekoop;

import com.evolveum.polygon.connector.ekoop.service.DummyServiceImpl;
import com.evolveum.polygon.connector.ekoop.service.RealServiceImpl;
import com.evolveum.polygon.connector.ekoop.service.Service;
import com.evolveum.polygon.connector.ekoop.soap.EkoopServiceLocator;
import com.evolveum.polygon.connector.ekoop.soap.IEkoopService;
import com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS;
import org.apache.axis.AxisProperties;
import org.identityconnectors.common.StringUtil;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.common.exceptions.AlreadyExistsException;
import org.identityconnectors.framework.common.exceptions.ConnectorIOException;
import org.identityconnectors.framework.common.exceptions.InvalidAttributeValueException;
import org.identityconnectors.framework.common.exceptions.UnknownUidException;
import org.identityconnectors.framework.common.objects.*;
import org.identityconnectors.framework.common.objects.filter.FilterTranslator;
import org.identityconnectors.framework.spi.Configuration;
import org.identityconnectors.framework.spi.ConnectorClass;
import org.identityconnectors.framework.spi.PoolableConnector;
import org.identityconnectors.framework.spi.operations.*;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * E-koop Connector.
 *
 * @author gpalos
 */
@ConnectorClass(displayNameKey = "ekoop.connector.display", configurationClass = EKoopConfiguration.class)
public class EKoopConnector implements PoolableConnector, TestOp, SchemaOp, CreateOp, DeleteOp, UpdateOp, SearchOp<EkoopIKFilter> {

    private static final Log LOG = Log.getLog(EKoopConnector.class);

    private EKoopConfiguration configuration;

    private Service service;

    // attributes
//    public static final String ATTR_UNIQUE_ID = "tckn"; // Turkish Citizenship Number
    public static final String ATTR_USER_NAME = "name";
    public static final String ATTR_GIVEN_NAME = "givenName";
    public static final String ATTR_FAMILY_NAME = "familyName";
    public static final String ATTR_EMAIL_ADDRESS = "emailAddress";
    public static final String ATTR_CITY_NAME = "calistigiil";
    public static final String ATTR_TELEPHONE_NUMBER = "telephoneNumber";
    public static final String ATTR_SECOND_TELEPHONE_NUMBER = "cepTel2";

    /*
    aktifmi – administrativeStatus:
        H”for disabling. E=evet(yes) H=hayır(no) in Turkish.
     */
    public static final String ENABLED = "E";
    public static final String DISABLED = "H";

    // returned message when user with given TCKN doesn't exists
    public static final String NOT_EXISTS_MESSAGE = "Sequence contains no elements";

    /*
    TODO: excetion handling
        0 : SUCCESS (BAŞARILI)
        1 : Cannot login to System (Sisteme girilemiyor)
        2 : User does not exist (Kullanıcı bulunmuyor)
        3 : User is already disabled (Kullanıcı zaten pasif durumda)
        4 : User is already enabled (Kullanıcı zaten aktif durumda)
        5 : User already exists (Varolan kullanıcı)
        6 : User already linked (Kullanıcı hesabı zaten bağlı)
        7 : User already unlinked (Kullanıcı hesabı bağı zaten çözülmüş)
     */
    public static final int CODE_SUCCESS = 0;
    // designed for general connection error, it may be useless.
    public static final int CODE_CANNOT_LOGIN_TO_SYSTEM = 1;
    public static final int CODE_USER_DOES_NOT_EXIST = 2;
    public static final int CODE_USER_ALREADY_DISABLED = 3;
    public static final int CODE_USER_ALREADY_ENABLED = 4;
    public static final int CODE_USER_ALREADY_EXISTS = 5;
//    public static final int CODE_USER_ALREADY_LINKED = 6;
//    public static final int CODE_USER_ALREADY_UNLINKED = 7;

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void init(Configuration configuration) {
        LOG.ok("connector init");
        this.configuration = (EKoopConfiguration) configuration;

        if (this.configuration.getTrustingAllCertificates() != null && this.configuration.getTrustingAllCertificates()) {
            AxisProperties.setProperty("axis.socketSecureFactory", "org.apache.axis.components.net.SunFakeTrustSocketFactory");
        }

        if (this.configuration != null && this.configuration.getUseMockup()) {
            service = new DummyServiceImpl();
        } else try {
            EkoopServiceLocator locator = new EkoopServiceLocator();
            IEkoopService soap = locator.getBasicHttpBinding_IEkoopService(new URL(this.configuration.getEndpoint()));
            service = new RealServiceImpl(soap);
        } catch (Exception e) {
            LOG.error(e, "Connection failed to: " + this.configuration.getEndpoint());
            throw new ConnectorIOException(e.getMessage(), e);
        }
    }

    @Override
    public void dispose() {
        configuration = null;
        service = null;
    }

    @Override
    public void test() {
        try {
            String ssf = "axis.socketSecureFactory";
            String ssfVal = "org.apache.axis.components.net.SunFakeTrustSocketFactory";
            LOG.info(ssf + " value is: " + AxisProperties.getProperty(ssf));
            if (this.configuration.getTrustingAllCertificates() != null && this.configuration.getTrustingAllCertificates()) {
                // new setup to ignore
                if (!ssfVal.equalsIgnoreCase(AxisProperties.getProperty(ssf))) {
                    synchronized (this) {
                        Configuration configuration = this.configuration;
                        dispose();
                        init(configuration);
                    }
                }
            } else if (AxisProperties.getProperty(ssf) != null) {
                AxisProperties.setProperty("axis.socketSecureFactory", null);
            }
            service.listAllUser();
        } catch (RemoteException e) {
            throw new ConnectorIOException(e.getMessage(), e);
        }
    }

    @Override
    public Schema schema() {
        SchemaBuilder builder = new SchemaBuilder(EKoopConnector.class);

        builder.defineObjectClass(schemaAccount());
        // TODO: create other object classes if needed

        return builder.build();
    }

    private ObjectClassInfo schemaAccount() {
        ObjectClassInfoBuilder objClassBuilder = new ObjectClassInfoBuilder();

        // __UID__ && __NAME__ is the same because we need to find by __NAME__ when AlreadyExistsException occurred
        // and are defaults, we don't need to set explicitly

        AttributeInfoBuilder attrUserNameBuilder = new AttributeInfoBuilder(ATTR_USER_NAME);
        attrUserNameBuilder.setRequired(true); // mandatory
        objClassBuilder.addAttributeInfo(attrUserNameBuilder.build());

        AttributeInfoBuilder attrFirstNameBuilder = new AttributeInfoBuilder(ATTR_GIVEN_NAME);
        objClassBuilder.addAttributeInfo(attrFirstNameBuilder.build());

        AttributeInfoBuilder attrLastNameBuilder = new AttributeInfoBuilder(ATTR_FAMILY_NAME);
        objClassBuilder.addAttributeInfo(attrLastNameBuilder.build());

        objClassBuilder.addAttributeInfo(OperationalAttributeInfos.ENABLE); //kullaniciAdi - Administrative Status

        AttributeInfoBuilder attrEmailAddressBuilder = new AttributeInfoBuilder(ATTR_EMAIL_ADDRESS);
        objClassBuilder.addAttributeInfo(attrEmailAddressBuilder.build());

        AttributeInfoBuilder attrCityNameBuilder = new AttributeInfoBuilder(ATTR_CITY_NAME);
        objClassBuilder.addAttributeInfo(attrCityNameBuilder.build());

        AttributeInfoBuilder attrTelephoneNumberBuilder = new AttributeInfoBuilder(ATTR_TELEPHONE_NUMBER);
        objClassBuilder.addAttributeInfo(attrTelephoneNumberBuilder.build());

        AttributeInfoBuilder attrSecondTelephoneNumberBuilder = new AttributeInfoBuilder(ATTR_SECOND_TELEPHONE_NUMBER);
        objClassBuilder.addAttributeInfo(attrSecondTelephoneNumberBuilder.build());

        return objClassBuilder.build();
    }

    @Override
    public Uid create(ObjectClass objectClass, Set<Attribute> attributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {    // __ACCOUNT__
            return createUser(attributes);
        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }
    }

    private Uid createUser(Set<Attribute> attributes) {
        LOG.ok("createUser attributes: {0}", attributes);
        String uid = getStringAttr(attributes, Name.NAME); // NAME & UID is the same and can't be generated in system
        String userName = getStringAttr(attributes, ATTR_USER_NAME);
        if (StringUtil.isBlank(uid)) {
            throw new InvalidAttributeValueException("Missing mandatory attribute " + Name.NAME + " (TCKN)");
        }

        String givenName = getStringAttr(attributes, ATTR_GIVEN_NAME);
        String familyName = getStringAttr(attributes, ATTR_FAMILY_NAME);
        String emailAddress = getStringAttr(attributes, ATTR_EMAIL_ADDRESS);
        Boolean enable = getAttr(attributes, OperationalAttributes.ENABLE_NAME, Boolean.class);
        String cityName = getStringAttr(attributes, ATTR_CITY_NAME);
        String telephoneNumber = getStringAttr(attributes, ATTR_TELEPHONE_NUMBER);
        String secondTelephoneNumber = getStringAttr(attributes, ATTR_SECOND_TELEPHONE_NUMBER);


        // check if user already exists
        try {
            PRL_SYS_USERS existUser = service.getUser(uid);
            if (existUser != null)
                throw new AlreadyExistsException("User " + uid + " already exists: "+existUser);
        } catch (RemoteException e) {
            if (e.getMessage().contains(NOT_EXISTS_MESSAGE)) {
                LOG.info("User {0} not exists (it is OK now): {1}", uid, e.getMessage());
            }
            else {
                LOG.error(e, "Error occurred when testing existence of user {0}:{1} ", uid, e.getMessage());
                throw new ConnectorIOException("Error occurred when testing existence of user "+uid+", "+e, e);
            }
        }

        try {

            PRL_SYS_USERS sysUser = new PRL_SYS_USERS();
            sysUser.setTCKN(uid);
            sysUser.setGIVENNAME(givenName);
            sysUser.setFAMILYNAME(familyName);
            sysUser.setNAME(userName);
            sysUser.setEMAILADDRESS(emailAddress);
            sysUser.setCALISTIGIIL(cityName);
            sysUser.setTELEPHONENUMBER(telephoneNumber);
            sysUser.setCEPTEL2(secondTelephoneNumber);

            LOG.ok("createUser: tckn:{0}, givenName:{1}, familyName:{2}, name:{3}, emailAddress:{4}, cityName:{5}, " +
                            "telephoneNumber:{6}, secondTelephoneNumber:{7}",
                    uid, givenName, familyName, userName, emailAddress, cityName, telephoneNumber, secondTelephoneNumber);

            Integer result = service.createUser(sysUser);
            if (CODE_SUCCESS != result) {
                throw new ConnectorIOException("create user returned: " + result);
            }

            LOG.ok("user created, result: {0}", result);

            if (enable != null) {
                handleAdministrativeStatus(uid, enable);
            }

            return new Uid(uid);
        } catch (java.rmi.RemoteException e) {
            throw new ConnectorIOException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(ObjectClass objectClass, Uid uid, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            try {
                LOG.ok("delete user is transformed to disable user, Uid: {0}", uid);
                Integer result = service.disableUser(uid.getUidValue());
                if (CODE_SUCCESS != result && CODE_USER_ALREADY_DISABLED != result) {
                    throw new ConnectorIOException("delete (disable) user returned: " + result);
                }
                LOG.ok("User {0} disabled.", uid);
            } catch (java.rmi.RemoteException e) {
                throw new ConnectorIOException(e.getMessage(), e);
            }
        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }
    }

    @Override
    public Uid update(ObjectClass objectClass, Uid uid, Set<Attribute> attributes, OperationOptions options) {
        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            return updateUser(uid, attributes);
        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }
    }

    private Uid updateUser(Uid uid, Set<Attribute> attributes) {
        LOG.ok("updateUser, Uid: {0}, attributes: {1}", uid, attributes);
        if (attributes == null || attributes.isEmpty()) {
            LOG.ok("update ignored, nothing changed");
            return uid;
        }

        String targetUserId = uid.getUidValue();

        PRL_SYS_USERS origUser;
        try {
            // need to read existing user from resource, becouse midPoint send only changed attributes,
            // but when update operation is called on resource, we need all attributes
            origUser = service.getUser(targetUserId);

            if (origUser == null) {
                throw new UnknownUidException("User " + targetUserId + " does not exists");
            }
        } catch (java.rmi.RemoteException e) {
            LOG.error(e, "Error occurred when getting user for update {0}:{1}", uid, e.getMessage());
            if (e.getMessage().contains(NOT_EXISTS_MESSAGE)) {
                throw new UnknownUidException("User " + targetUserId + " does not exists: "+e.getMessage());
            }
            else {
                throw new ConnectorIOException("Error occurred when get user "+targetUserId+": "+e.getMessage(), e);
            }
        }

        String userName = getStringAttr(attributes, ATTR_USER_NAME, origUser.getNAME());
        String givenName = getStringAttr(attributes, ATTR_GIVEN_NAME, origUser.getGIVENNAME());
        String familyName = getStringAttr(attributes, ATTR_FAMILY_NAME, origUser.getFAMILYNAME());
        String emailAddress = getStringAttr(attributes, ATTR_EMAIL_ADDRESS, origUser.getEMAILADDRESS());
        Boolean aktifmiAsBoolean = ENABLED.equals(origUser.getAKTIFMI()) ? true : false;
        Boolean enable = getAttr(attributes, OperationalAttributes.ENABLE_NAME, Boolean.class, aktifmiAsBoolean);
        String cityName = getStringAttr(attributes, ATTR_CITY_NAME, origUser.getCALISTIGIIL());
        String telephoneNumber = getStringAttr(attributes, ATTR_TELEPHONE_NUMBER, origUser.getTELEPHONENUMBER());
        String secondTelephoneNumber = getStringAttr(attributes, ATTR_SECOND_TELEPHONE_NUMBER, origUser.getCEPTEL2());

        try {
            // implicitly copy all old values
            // and set new values (if changed)
            origUser.setTCKN(targetUserId);
            origUser.setGIVENNAME(givenName);
            origUser.setFAMILYNAME(familyName);
            origUser.setNAME(userName);
            origUser.setEMAILADDRESS(emailAddress);
            origUser.setCALISTIGIIL(cityName);
            origUser.setTELEPHONENUMBER(telephoneNumber);
            origUser.setCEPTEL2(secondTelephoneNumber);


            LOG.ok("updateUser: tckn:{0}, givenName:{1}, familyName:{2}, userName:{3}, emailAddress:{4}, cityName:{5}, " +
                            "telephoneNumber:{6}, secondTelephoneNumber:{7}",
                    uid, givenName, familyName, userName, emailAddress, cityName, telephoneNumber, secondTelephoneNumber);

            Integer result = service.updateUser(origUser);
            if (CODE_SUCCESS != result) {
                throw new ConnectorIOException("update user returned: " + result);
            }

            LOG.ok("user updated, result: {0}", result);

            if (enable != null) {
                handleAdministrativeStatus(targetUserId, enable);
            }

            return new Uid(targetUserId);
        } catch (java.rmi.RemoteException e) {
            throw new ConnectorIOException(e.getMessage(), e);
        }
    }

    private void handleAdministrativeStatus(String uid, Boolean enable) throws RemoteException {
        if (enable != null) {
            if (enable) {
                Integer result = service.enableUser(uid);
                if (result != CODE_SUCCESS && result != CODE_USER_ALREADY_ENABLED) {
                    throw new ConnectorIOException("enable user " + uid + " returned: " + result);
                }
                LOG.ok("user enabled, result: {0}", result);
            } else {
                Integer result = service.disableUser(uid);
                if (result != CODE_SUCCESS && result != CODE_USER_ALREADY_DISABLED) {
                    throw new ConnectorIOException("disable user " + uid + " returned: " + result);
                }
                LOG.ok("user disabled, result: {0}", result);
            }
        }
    }

    @Override
    public FilterTranslator<EkoopIKFilter> createFilterTranslator(ObjectClass objectClass, OperationOptions options) {
        return new EKoopFilterTranslator();
    }

    @Override
    public void executeQuery(ObjectClass objectClass, EkoopIKFilter query, ResultsHandler handler, OperationOptions options) {

        if (objectClass.is(ObjectClass.ACCOUNT_NAME)) {
            try {
                LOG.ok("executeQuery: {0}, options: {1}", query, options);
                //find by Uid (user Primary Key)
                if (query != null && query.byUid != null) {
                    PRL_SYS_USERS user = null;
                    try {
                        user = service.getUser(query.byUid);
                    }
                    catch (RemoteException re) {
                        LOG.warn(re, "Error occurred when getting user {0}: {1}", query.byUid, re.getMessage());
                        if (re.getMessage().contains(NOT_EXISTS_MESSAGE)) {
                            throw new UnknownUidException("PRL_SYS_USERS with uid: " + query.byUid + " not found: " + re, re);
                        }
                        else {
                            throw new ConnectorIOException("Error occurred when getting user "+query.byUid+":"+re.getMessage(), re);
                        }
                    }
                    if (user == null) {
                        throw new UnknownUidException("PRL_SYS_USERS with uid: " + query.byUid + " not found");
                    }
                    ConnectorObject connectorObject = convertUserToConnectorObject(user);
                    if (connectorObject !=null) {
                        handler.handle(connectorObject);
                    }
                    else {
                        throw new UnknownUidException("PRL_SYS_USERS with uid: " + query.byUid + " not found after converting");
                    }

                    // find all
                } else {
                    PRL_SYS_USERS[] result = service.listAllUser();

                    int count = 0;
                    for (PRL_SYS_USERS user : result) {
                        if (++count % 10 == 0) {
                            LOG.ok("executeQuery: processing {0}. of {1} users", count, result.length);
                        }

                        ConnectorObject connectorObject = convertUserToConnectorObject(user);
                        if (connectorObject!=null) {
                            boolean finish = !handler.handle(connectorObject);
                            if (finish)
                                break;
                        }
                    }
                }

            } catch (java.rmi.RemoteException e) {
                throw new ConnectorIOException(e.getMessage(), e);
            }

        } else {
            throw new UnsupportedOperationException("Unsupported object class " + objectClass);
        }
    }


    private ConnectorObject convertUserToConnectorObject(PRL_SYS_USERS user) throws RemoteException {
        if (StringUtil.isBlank(user.getTCKN())) {
            // users on target system without TCKN we don't support (connector framework need UID and is mandatory)
            LOG.warn("Users TCKN is blank, ignoring, UID: {0}, userName {1}",
                    user.getTCKN(), user.getNAME());
            return null;
        }
        ConnectorObjectBuilder builder = new ConnectorObjectBuilder();
        builder.setUid(user.getTCKN());
        builder.setName(user.getTCKN());
        addAttr(builder, ATTR_USER_NAME, user.getNAME());
        addAttr(builder, ATTR_GIVEN_NAME, user.getGIVENNAME());
        addAttr(builder, ATTR_FAMILY_NAME, user.getFAMILYNAME());
        addAttr(builder, ATTR_EMAIL_ADDRESS, user.getEMAILADDRESS());
        addAttr(builder, ATTR_CITY_NAME, user.getCALISTIGIIL());
        addAttr(builder, ATTR_TELEPHONE_NUMBER, user.getTELEPHONENUMBER());
        addAttr(builder, ATTR_SECOND_TELEPHONE_NUMBER, user.getCEPTEL2());

        boolean enable = ENABLED == user.getAKTIFMI() ? true : false;
        addAttr(builder, OperationalAttributes.ENABLE_NAME, enable);

        ConnectorObject connectorObject = builder.build();
        LOG.ok("convertUserToConnectorObject, user: {0}:{1}, enable: {2}, " +
                        "\n\tconnectorObject: {3}, ",
                user.getTCKN(), user.getNAME(), enable, connectorObject);
        return connectorObject;
    }

    private String getStringAttr(Set<Attribute> attributes, String attrName) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class);
    }

    private String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, String.class, defaultVal);
    }

    private String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, String defaultVal2, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null) {
            if (notNull && defaultVal == null)
                return defaultVal2;
            return defaultVal;
        }
        return ret;
    }

    private String getStringAttr(Set<Attribute> attributes, String attrName, String defaultVal, boolean notNull) throws InvalidAttributeValueException {
        String ret = getAttr(attributes, attrName, String.class, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type) throws InvalidAttributeValueException {
        return getAttr(attributes, attrName, type, null);
    }

    private <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal, boolean notNull) throws InvalidAttributeValueException {
        T ret = getAttr(attributes, attrName, type, defaultVal);
        if (notNull && ret == null)
            return defaultVal;
        return ret;
    }

    @SuppressWarnings("unchecked")
    public <T> T getAttr(Set<Attribute> attributes, String attrName, Class<T> type, T defaultVal) throws InvalidAttributeValueException {
        for (Attribute attr : attributes) {
            if (attrName.equals(attr.getName())) {
                List<Object> vals = attr.getValue();
                if (vals == null || vals.isEmpty()) {
                    // set empty value
                    return null;
                }
                if (vals.size() == 1) {
                    Object val = vals.get(0);
                    if (val == null) {
                        // set empty value
                        return null;
                    }
                    if (type.isAssignableFrom(val.getClass())) {
                        return (T) val;
                    }
                    throw new InvalidAttributeValueException("Unsupported type " + val.getClass() + " for attribute " + attrName);
                }
                throw new InvalidAttributeValueException("More than one value for attribute " + attrName);
            }
        }
        // set default value when attrName not in changed attributes
        return defaultVal;
    }

    private long[] getMultiValAttr(Set<Attribute> attributes, String attrName, long[] defaultVal) {
        for (Attribute attr : attributes) {
            if (attrName.equals(attr.getName())) {
                List<Object> vals = attr.getValue();
                if (vals == null || vals.isEmpty()) {
                    // set empty value
                    return new long[0];
                }
                long[] ret = new long[vals.size()];
                for (int i = 0; i < vals.size(); i++) {
                    Object valAsObject = vals.get(i);
                    if (valAsObject == null)
                        throw new InvalidAttributeValueException("Value " + null + " must be not null for attribute " + attrName);

                    Long val;
                    if (valAsObject instanceof String) {
                        val = Long.parseLong((String) valAsObject);
                    } else {
                        val = (Long) valAsObject;
                    }
                    ret[i] = val;
                }
                return ret;
            }
        }
        // set default value when attrName not in changed attributes
        return defaultVal;
    }


    private <T> void addAttr(ConnectorObjectBuilder builder, String attrName, T attrVal) {
        if (attrVal != null) {
            builder.addAttribute(attrName, attrVal);
        }
    }


    @Override
    public void checkAlive() {
        test();
        // TODO quicker test?
    }
}
