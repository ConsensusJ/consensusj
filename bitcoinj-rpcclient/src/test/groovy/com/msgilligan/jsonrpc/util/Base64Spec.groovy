package com.msgilligan.jsonrpc.util

import com.msgilligan.jsonrpc.util.Base64
import spock.lang.Specification

/**
 * Created by greg on 10/18/16.
 */
class Base64Spec extends Specification{

    def "Smoke Test" (myInt) {
        given:
        def auth = "myuser" + ":" + "mypass" + myInt;

        when:
        def basicAuth1 = "Basic " + Base64.encodeToString(auth.getBytes(),Base64.DEFAULT).trim();
        def basicAuth2 = "Basic " + javax.xml.bind.DatatypeConverter.printBase64Binary(auth.getBytes());

        then:
        basicAuth1 == basicAuth2

        where:
        myInt << [1,2,3,4,5]

    }
}
