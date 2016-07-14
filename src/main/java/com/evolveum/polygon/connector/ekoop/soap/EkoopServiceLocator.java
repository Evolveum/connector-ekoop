/**
 * EkoopServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.evolveum.polygon.connector.ekoop.soap;

public class EkoopServiceLocator extends org.apache.axis.client.Service implements com.evolveum.polygon.connector.ekoop.soap.EkoopService {

    public EkoopServiceLocator() {
    }


    public EkoopServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public EkoopServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BasicHttpBinding_IEkoopService
    private java.lang.String BasicHttpBinding_IEkoopService_address = "http://ekoopapp06s01.csb.local/EkoopService.svc";

    public java.lang.String getBasicHttpBinding_IEkoopServiceAddress() {
        return BasicHttpBinding_IEkoopService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BasicHttpBinding_IEkoopServiceWSDDServiceName = "BasicHttpBinding_IEkoopService";

    public java.lang.String getBasicHttpBinding_IEkoopServiceWSDDServiceName() {
        return BasicHttpBinding_IEkoopServiceWSDDServiceName;
    }

    public void setBasicHttpBinding_IEkoopServiceWSDDServiceName(java.lang.String name) {
        BasicHttpBinding_IEkoopServiceWSDDServiceName = name;
    }

    public com.evolveum.polygon.connector.ekoop.soap.IEkoopService getBasicHttpBinding_IEkoopService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BasicHttpBinding_IEkoopService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBasicHttpBinding_IEkoopService(endpoint);
    }

    public com.evolveum.polygon.connector.ekoop.soap.IEkoopService getBasicHttpBinding_IEkoopService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            com.evolveum.polygon.connector.ekoop.soap.BasicHttpBinding_IEkoopServiceStub _stub = new com.evolveum.polygon.connector.ekoop.soap.BasicHttpBinding_IEkoopServiceStub(portAddress, this);
            _stub.setPortName(getBasicHttpBinding_IEkoopServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBasicHttpBinding_IEkoopServiceEndpointAddress(java.lang.String address) {
        BasicHttpBinding_IEkoopService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (com.evolveum.polygon.connector.ekoop.soap.IEkoopService.class.isAssignableFrom(serviceEndpointInterface)) {
                com.evolveum.polygon.connector.ekoop.soap.BasicHttpBinding_IEkoopServiceStub _stub = new com.evolveum.polygon.connector.ekoop.soap.BasicHttpBinding_IEkoopServiceStub(new java.net.URL(BasicHttpBinding_IEkoopService_address), this);
                _stub.setPortName(getBasicHttpBinding_IEkoopServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("BasicHttpBinding_IEkoopService".equals(inputPortName)) {
            return getBasicHttpBinding_IEkoopService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "EkoopService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "BasicHttpBinding_IEkoopService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BasicHttpBinding_IEkoopService".equals(portName)) {
            setBasicHttpBinding_IEkoopServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
