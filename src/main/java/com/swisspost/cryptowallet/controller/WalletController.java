package com.swisspost.cryptowallet.controller;

import com.swisspost.cryptowallet.dto.*;
import com.swisspost.cryptowallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService){
        this.walletService = walletService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<WalletResponse> getByUsername(@PathVariable String username){
        WalletResponse walletResponse = walletService.getByUsername(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponse);
    }


    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody WalletRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.create(body));
    }

    @PostMapping("/{username}/assets")
    public ResponseEntity<WalletResponse> createWalletAsset(
            @PathVariable String username,
            @Valid @RequestBody WalletAssetRequest body) {

        WalletResponse response = walletService.createAsset(username, body);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
