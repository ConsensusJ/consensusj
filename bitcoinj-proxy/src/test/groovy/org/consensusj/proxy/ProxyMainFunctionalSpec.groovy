package org.consensusj.proxy

import ratpack.test.MainClassApplicationUnderTest
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import ratpack.groovy.test.GroovyRatpackMainApplicationUnderTest

/**
 *
 */
class ProxyMainFunctionalSpec extends Specification {
    @Shared
    @AutoCleanup
    def aut

    @Shared
    def client

    def setupSpec() {
        aut = new MainClassApplicationUnderTest(ProxyMain.class)
        client = aut.httpClient
    }

    def "home page is correct text string"() {
        when:
        def response = client.requestSpec { req -> }.get("/")

        then:
        response.statusCode == 200
        response.body.text == "Hello world! (Not RPC)"
    }

    // We can't run functional tests on the Proxy endpoints without a running bitcoind

}
