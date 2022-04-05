package org.vinka.myaccountservice.presentation;

import lombok.Data;

import static org.vinka.myaccountservice.security.log.LogEvent.*;

@Data
public class UserRoleChange {
    public enum OP {
        GRANT("Grant role %s to %s"),
        REMOVE("Remove role %s from %s");

        private final String formatMessage;

        OP(String message) {
            this.formatMessage = message;
        }

        public String getFormatMessage() {
            return formatMessage;
        }

        public Action toLogAction() {
            return switch (this) {
                case GRANT -> Action.GRANT_ROLE;
                case REMOVE -> Action.REMOVE_ROLE;
            };
        }
    }

    private String user;
    private String role;
    private OP operation;
}
