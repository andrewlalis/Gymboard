package nl.andrewlalis.gymboard_api.dao.auth;

import nl.andrewlalis.gymboard_api.model.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {
}
