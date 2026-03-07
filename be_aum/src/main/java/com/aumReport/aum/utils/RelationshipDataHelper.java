package com.aumReport.aum.utils;

import com.aumReport.aum.entity.Relationship;
import com.aumReport.aum.repo.RelationshipRepository;
import com.aumReport.aum.repo.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class RelationshipDataHelper {
    
    private final RelationshipRepository relationshipRepository;
    private final UserProfileRepository userProfileRepository;
    
    @Autowired
    public RelationshipDataHelper(RelationshipRepository relationshipRepository, 
                                 UserProfileRepository userProfileRepository) {
        this.relationshipRepository = relationshipRepository;
        this.userProfileRepository = userProfileRepository;
    }
    
    public Map<Integer, String> getRelationshipNamesMap(List<Integer> relationshipIds) {
        return relationshipRepository.findRelationshipsByIds(relationshipIds)
                .stream()
                .collect(Collectors.toMap(Relationship::getId, Relationship::getName));
    }
    
    public Map<Integer, String> getRelationshipManagerNamesMap(List<Integer> relationshipIds) {
        List<Integer> managerIds = relationshipRepository.findRelationshipsByIds(relationshipIds)
                .stream()
                .map(Relationship::getRelationshipManagerId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        
        if (managerIds.isEmpty()) {
            return Map.of();
        }
        
        List<String> managerNames = userProfileRepository.findActiveUserNamesByIds(managerIds);
        Map<Integer, String> managerNameMap = IntStream.range(0, managerIds.size())
                .boxed()
                .collect(Collectors.toMap(managerIds::get, managerNames::get));
        
        return relationshipRepository.findRelationshipsByIds(relationshipIds)
                .stream()
                .filter(relationship -> relationship.getRelationshipManagerId() != null)
                .collect(Collectors.toMap(
                    Relationship::getId,
                    relationship -> managerNameMap.getOrDefault(relationship.getRelationshipManagerId(), null)
                ));
    }
    
    public String getRelationshipName(int relationshipId) {
        return relationshipRepository.findActiveRelationshipNameById(relationshipId).orElse(null);
    }
    
    public String getRelationshipManagerName(int relationshipId) {
        return relationshipRepository.findRelationshipManagerIdById(relationshipId)
                .flatMap(userProfileRepository::findActiveUserNameById)
                .orElse(null);
    }
}
