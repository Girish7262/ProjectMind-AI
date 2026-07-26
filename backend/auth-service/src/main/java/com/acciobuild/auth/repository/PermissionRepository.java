package com.acciobuild.auth.repository;

import com.acciobuild.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against Permissions table.
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    Optional<Permission> findByCode(String code);

    List<Permission> findByModule(String module);

    @Query("SELECT p FROM Role r JOIN r.permissions p WHERE r.name = :roleName AND r.deleted = false AND p.deleted = false")
    List<Permission> findByRoleName(@Param("roleName") String roleName);
}
