package org.hcmut.bkelbot.autoupdate;

import lombok.Data;

@Data
public class Scheduler {
    private final int userId;
    private final String key;
    private final int delay;
    private final long lastchecked;
}
