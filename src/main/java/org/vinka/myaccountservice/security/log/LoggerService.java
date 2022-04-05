package org.vinka.myaccountservice.security.log;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vinka.myaccountservice.persistance.LogEventRepository;

import java.util.List;

@Service
public class LoggerService {
    private final LogEventRepository repository;

    @Autowired
    public LoggerService(LogEventRepository repository) {
        this.repository = repository;
    }

    public void log(LogEvent event) {
        repository.save(event);
    }

    public List<LogEvent> findLog() {
        return repository.findAllByOrderByIdAsc();
    }

    public void log(
            LogEvent.Action action,
            String subject,
            String object,
            String path
    ) {
        repository.save(new LogEvent(
                action,
                subject,
                object,
                path
        ));
    }
}
