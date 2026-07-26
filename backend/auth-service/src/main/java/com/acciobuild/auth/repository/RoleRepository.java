package com.acciobuild.auth.repository;

import com.acciobuild.auth.entity.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository managing transaction operations against Roles table.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {

    @EntityGraph(attributePaths = {"permissions"})
    Optional<Role> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT r FROM Role r WHERE r.status = true AND r.deleted = false")
    List<Role> findActiveRoles();

    @Query("SELECT r FROM Role r WHERE r.name IN ('DEVELOPER', 'VIEWER') AND r.deleted = false")
    List<Role> findDefaultRoles();
}
