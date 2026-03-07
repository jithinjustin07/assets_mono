package com.aumReport.aum.specification;
import com.aumReport.aum.entity.Account;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.*;

import java.util.List;

public class AccountSpecification {

    public static Specification<Account> distinctByVendor(Long vendorId) {

        return (Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            query.distinct(true);

            if (vendorId == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("vendorId"), vendorId);
        };
    }
    
    public static Specification<Account> distinctByVendorAndCustodianIds(Long vendorId, List<Integer> custodianIds) {

        return (Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) -> {

            query.distinct(true);

            if (vendorId == null && (custodianIds == null || custodianIds.isEmpty())) {
                return cb.conjunction();
            }

            Predicate vendorPredicate = vendorId != null ? 
                cb.equal(root.get("vendorId"), vendorId) : cb.conjunction();
            
            Predicate custodianPredicate = (custodianIds != null && !custodianIds.isEmpty()) ? 
                root.get("custodianId").in(custodianIds) : cb.conjunction();

            return cb.and(vendorPredicate, custodianPredicate);
        };
    }
}
