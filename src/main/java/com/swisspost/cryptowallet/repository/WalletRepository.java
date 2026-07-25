package com.swisspost.cryptowallet.repository;

import com.swisspost.cryptowallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    @Query("SELECT w FROM Wallet w JOIN w.user u WHERE u.username = :username")
    Optional<Wallet> findByUserUsername(String username);

}
