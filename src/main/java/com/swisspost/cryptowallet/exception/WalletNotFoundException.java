package com.swisspost.cryptowallet.exception;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String username) {
        super("Wallet not found for user:" + username);
    }

}
