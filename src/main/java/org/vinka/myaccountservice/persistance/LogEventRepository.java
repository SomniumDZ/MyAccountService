package org.vinka.myaccountservice.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.vinka.myaccountservice.security.log.LogEvent;

import java.util.List;

@Repository
public interface LogEventRepository extends CrudRepository<LogEvent, Long> {
    List<LogEvent> findAllByOrderByIdAsc();
}
