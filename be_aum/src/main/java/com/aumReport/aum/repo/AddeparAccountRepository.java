package com.aumReport.aum.repo;

import com.aumReport.aum.entity.AddeparAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddeparAccountRepository extends JpaRepository<AddeparAccount, Long> {
}
