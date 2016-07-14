/**
 * IEkoopService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.evolveum.polygon.connector.ekoop.soap;

public interface IEkoopService extends java.rmi.Remote {
    public com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS getUser(java.lang.String TCKN) throws java.rmi.RemoteException;
    public java.lang.Integer enableUser(java.lang.String TCKN) throws java.rmi.RemoteException;
    public java.lang.Integer disableUser(java.lang.String TCKN) throws java.rmi.RemoteException;
    public com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS[] listAllUsers() throws java.rmi.RemoteException;
    public java.lang.Integer createUser(com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS obje) throws java.rmi.RemoteException;
    public java.lang.Integer updateUser(com.evolveum.polygon.connector.ekoop.soap.PRL_SYS_USERS obje) throws java.rmi.RemoteException;
}
