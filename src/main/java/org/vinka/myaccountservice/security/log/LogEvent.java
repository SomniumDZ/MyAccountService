package org.vinka.myaccountservice.security.log;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class LogEvent {
    public enum Action {
        CREATE_USER, CHANGE_PASSWORD, ACCESS_DENIED, LOGIN_FAILED, GRANT_ROLE, REMOVE_ROLE,
        LOCK_USER, UNLOCK_USER, DELETE_USER, BRUTE_FORCE, UNKNOWN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime date;
    private Action action;
    private String subject;
    private String object;
    private String path;

    public LogEvent(Action action, String subject, String object, String path) {
        this.date = LocalDateTime.now();
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
}
