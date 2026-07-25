package com.swisspost.cryptowallet.controller;

import com.swisspost.cryptowallet.dto.request.WalletAssetRequest;
import com.swisspost.cryptowallet.dto.request.WalletRequest;
import com.swisspost.cryptowallet.dto.response.WalletPerformanceResponse;
import com.swisspost.cryptowallet.dto.response.WalletResponse;
import com.swisspost.cryptowallet.dto.response.WalletValueResponse;
import com.swisspost.cryptowallet.service.WalletPerformanceService;
import com.swisspost.cryptowallet.service.WalletService;
import com.swisspost.cryptowallet.service.WalletValuationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;


@RestController
@RequestMapping("/wallets")
public class WalletController {

    private final WalletService walletService;
    private final WalletValuationService walletValuationService;
    private final WalletPerformanceService walletPerformanceService;

    public WalletController(WalletService walletService, WalletValuationService walletValuationService, WalletPerformanceService walletPerformanceService){
        this.walletService = walletService;
        this.walletValuationService = walletValuationService;
        this.walletPerformanceService = walletPerformanceService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<WalletResponse> getByUsername(@PathVariable String username){
        WalletResponse walletResponse = walletService.getByUsername(username);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponse);
    }

    @GetMapping("/{username}/value")
    public ResponseEntity<WalletValueResponse> getWalletValueByUsername(@PathVariable String username, @RequestParam(required = false) LocalDate filterDate){
        LocalDate date=filterDate==null? LocalDate.now():filterDate;
        WalletValueResponse walletResponse = walletValuationService.getWalletValueByUsername(username, date);
        return ResponseEntity.status(HttpStatus.CREATED).body(walletResponse);
    }

    @GetMapping("/{username}/performances")
    public ResponseEntity<WalletPerformanceResponse> getPerformancesByUsername(@PathVariable String username) {
        WalletPerformanceResponse response = walletPerformanceService.getWalletPerformancesByUsername(username);
        return ResponseEntity.ok(response);
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
