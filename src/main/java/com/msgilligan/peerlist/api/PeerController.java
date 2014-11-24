package com.msgilligan.peerlist.api;

/**
 * User: sean
 * Date: 2/22/14
 * Time: 6:40 PM
 */
import com.msgilligan.peerlist.service.PeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;


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


}
