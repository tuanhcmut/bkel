package org.hcmut.bkelbot;

import lombok.Data;
import org.hcmut.bkelapi.objects.UserInfo;
import org.hcmut.bkelbot.autoupdate.Scheduler;

import java.util.LinkedList;
import java.util.List;

@Data
public class UserData {
    private String token = "";
    private UserInfo userInfo;
    private List<Scheduler> schedulers = new LinkedList<>();

    private int newinboxcount;
    private int newnotificationcount;
    public boolean hasSchedulerwithKey (String key){
        for (Scheduler scheduler : schedulers)
            if (scheduler.getKey().equals(key))
                return true;
        return false;
    }
    public boolean checkContainsKey (String tocheck){
        for (Scheduler scheduler : schedulers)
            if (tocheck.contains(scheduler.getKey()))
                return true;
        return false;
    }
}
