package com.aumReport.aum.repo;

import com.aumReport.aum.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    
    @Query("SELECT up.name FROM UserProfile up WHERE up.id = :userId AND up.active = true")
    Optional<String> findActiveUserNameById(@Param("userId") Integer userId);
    
    @Query("SELECT up.name FROM UserProfile up WHERE up.id IN :userIds AND up.active = true")
    List<String> findActiveUserNamesByIds(@Param("userIds") List<Integer> userIds);
    
    boolean existsByEmailAndActive(String email, boolean active);
    
    Optional<UserProfile> findByEmailAndActive(String email, boolean active);
    
    List<UserProfile> findByActiveTrue();
}
