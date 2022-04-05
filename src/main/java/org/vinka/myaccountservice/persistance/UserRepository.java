package org.vinka.myaccountservice.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.vinka.myaccountservice.business.RoleEntity;
import org.vinka.myaccountservice.business.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByRolesContaining(RoleEntity role);
    List<User> findAllByOrderByIdAsc();
}
