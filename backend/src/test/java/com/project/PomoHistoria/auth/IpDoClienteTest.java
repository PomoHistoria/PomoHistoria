package com.project.PomoHistoria.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class IpDoClienteTest {

    private static MockHttpServletRequest requisicao(String cfConnectingIp) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("104.22.72.4");
        if (cfConnectingIp != null) {
            request.addHeader("CF-Connecting-IP", cfConnectingIp);
        }
        return request;
    }

    @Test
    void semHeaderConfiguradoUsaRemoteAddrMesmoComHeaderPresente() {
        IpDoCliente ip = new IpDoCliente("");
        assertEquals("104.22.72.4", ip.de(requisicao("191.0.2.10")));
    }

    @Test
    void comHeaderConfiguradoUsaOValorDele() {
        IpDoCliente ip = new IpDoCliente("CF-Connecting-IP");
        assertEquals("191.0.2.10", ip.de(requisicao(" 191.0.2.10 ")));
        assertEquals("2001:db8::1", ip.de(requisicao("2001:db8::1")));
    }

    @Test
    void headerAusenteOuInvalidoCaiNoRemoteAddr() {
        IpDoCliente ip = new IpDoCliente("CF-Connecting-IP");
        assertEquals("104.22.72.4", ip.de(requisicao(null)));
        assertEquals("104.22.72.4", ip.de(requisicao("")));
        assertEquals("104.22.72.4", ip.de(requisicao("203.0.113.9, 191.0.2.10")));
        assertEquals("104.22.72.4", ip.de(requisicao("<script>")));
    }
}
