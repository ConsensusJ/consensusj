package com.msgilligan.bitcoinj.peerserver.controller;

/**
 * REST and WebSocket (STOMP) Spring MVC Peer Controller
 */
import com.msgilligan.bitcoinj.spring.service.PeerService;
import org.bitcoinj.core.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.List;


@RestController
public class PeerController {
    private final PeerService peerService;

    @Autowired
    public PeerController(PeerService peerService) {
        this.peerService = peerService;
    }


    @RequestMapping("/hello")
    public String index() {
        return "Greetings from PeerList! We're running on: " + peerService.getNetworkParameters().getId() + ".";
    }

    @MessageMapping("/listPeers")
    public void listPeers(Principal principal) {
        peerService.listPeers(principal);
    }

    @RequestMapping("/listPeers")
    @ResponseBody
    public List<Peer> listPeersREST() {
        return peerService.getPeers();
    }
}
