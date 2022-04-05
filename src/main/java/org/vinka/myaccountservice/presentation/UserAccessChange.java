package org.vinka.myaccountservice.presentation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.vinka.myaccountservice.security.log.LogEvent;

@Data
public class UserAccessChange {

    public enum OP {
        LOCK("User %s locked!", "Lock user %s"),
        UNLOCK("User %s unlocked!", "Unlock user %s");


        private final String statusFormat;
        private final String logFormat;

        OP(String statusFormat, String logFormat) {
            this.statusFormat = statusFormat;
            this.logFormat = logFormat;
        }

        public String getStatusFormat() {
            return statusFormat;
        }

        public LogEvent.Action toLogAction() {
            return switch (this) {
                case LOCK -> LogEvent.Action.LOCK_USER;
                case UNLOCK -> LogEvent.Action.UNLOCK_USER;
            };
        }

        public String getLogFormat() {
            return logFormat;
        }
    }

    @JsonProperty("user")
    private String userEmail;
    private OP operation;
}
