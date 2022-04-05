package org.vinka.myaccountservice.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.vinka.myaccountservice.business.RoleEntity;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, String> {
}
