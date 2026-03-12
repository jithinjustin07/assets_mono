package com.aumReport.aum.repo;

import com.aumReport.aum.entity.BlackDiamondAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackDiamondAccountRepository extends JpaRepository<BlackDiamondAccount, Long> {
}
