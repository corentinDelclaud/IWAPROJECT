package iwaproject.keycloak_service.controller;

import iwaproject.keycloak_service.dto.RealmDto;
import iwaproject.keycloak_service.service.RealmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/realms")
public class RealmController {

    private final RealmService realmService;

    @Autowired
    public RealmController(RealmService realmService) {
        this.realmService = realmService;
    }

    @GetMapping
    public ResponseEntity<List<RealmDto>> getAllRealms() {
        List<RealmDto> realms = realmService.getAllRealms();
        return ResponseEntity.ok(realms);
    }

    @PostMapping
    public ResponseEntity<RealmDto> createRealm(@RequestBody RealmDto realmDto) {
        RealmDto createdRealm = realmService.createRealm(realmDto);
        return ResponseEntity.status(201).body(createdRealm);
    }

    @PutMapping("/{realmId}")
    public ResponseEntity<RealmDto> updateRealm(@PathVariable String realmId, @RequestBody RealmDto realmDto) {
        RealmDto updatedRealm = realmService.updateRealm(realmId, realmDto);
        return ResponseEntity.ok(updatedRealm);
    }

    @DeleteMapping("/{realmId}")
    public ResponseEntity<Void> deleteRealm(@PathVariable String realmId) {
        realmService.deleteRealm(realmId);
        return ResponseEntity.noContent().build();
    }
}