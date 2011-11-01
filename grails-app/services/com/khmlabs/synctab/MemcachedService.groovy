package com.khmlabs.synctab

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import grails.converters.JSON
import java.lang.reflect.Type
import net.spy.memcached.AddrUtil
import net.spy.memcached.BinaryConnectionFactory
import net.spy.memcached.MemcachedClient
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.springframework.beans.factory.InitializingBean

class MemcachedService implements InitializingBean {

    static transactional = false

    // expire in 2 weeks
    private static final int EXPIRATION = 60 * 60 * 24 * 14

    static final double VERSION = 1

    Gson gson
    MemcachedClient memcachedClient
    GrailsApplication grailsApplication

    void afterPropertiesSet() {
        def memcachedServers = grailsApplication.config.synctab.memcachedServers
        memcachedClient = new MemcachedClient(
                new BinaryConnectionFactory(),
                AddrUtil.getAddresses(memcachedServers))

        gson = new GsonBuilder().setVersion(VERSION).create()
    }

    Object getObject(String key, Type collectionType = null) {
        try {
            def value = memcachedClient.get(key)
            if (value) {
                def map = JSON.parse(value.toString())

                if (collectionType) {
                    return gson.fromJson(map.object.toString(), collectionType)
                }

                // first get the value type class, and then parse json for
                def valueType = Class.forName(map.'class'.toString(), true, this.class.classLoader)
                return gson.fromJson(map.object.toString(), valueType)
            }
            return value
        }
        catch (Exception e) {
            log.error("Memcached: error to get object by key $key")
            return null
        }
    }

    void putObject(String key, Object value, int expiration = EXPIRATION, Type collectionType = null) {
        try {
            if (value != null) {
                // store object as json object

                def objectValue = collectionType ? gson.toJson(value, collectionType) : gson.toJson(value)
                def cacheMap = ['class': value.getClass().name, 'object': objectValue]

                def json = gson.toJson(cacheMap)
                memcachedClient.set(key, expiration, json)
            }
            else {
                memcachedClient.set(key, expiration, null)
            }
        }
        catch (Exception e) {
            log.error("Memcached: error to put object by key $key")
        }
    }

    Serializable getValue(String key) {
        try {
            return (Serializable) memcachedClient.get(key)
        }
        catch (Exception e) {
            log.error("Memcached: error to get value by key $key")
            return null
        }
    }

    void putValue(String key, Serializable value, int expiration = EXPIRATION) {
        try {
            memcachedClient.set(key, expiration, value)
        }
        catch (Exception e) {
            log.error("Memcached: error to put value by key $key")
        }
    }

    void invalidate(String key) {
        try {
            memcachedClient.delete(key)
        }
        catch (Exception e) {
            log.error("Memcached: error to invalidate by key $key")
        }
    }

}
