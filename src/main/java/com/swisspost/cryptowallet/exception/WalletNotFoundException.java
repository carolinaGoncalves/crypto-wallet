package com.swisspost.cryptowallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String username) {
        super("Wallet not found for user:" + username);
    }

}
