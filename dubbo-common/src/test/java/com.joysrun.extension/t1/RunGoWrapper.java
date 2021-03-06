package com.joysrun.extension.t1;

import java.util.List;

/**
 * run wrapper
 *
 * @author Sin
 * @time 2019/6/14 5:52 PM
 */
public class RunGoWrapper implements Run {

    private Run run;

    public RunGoWrapper(Run run) {
        this.run = run;
    }

    @Override
    public void run(List<String> users) {
        System.err.println(this.getClass().getSimpleName());
        run.run(users);
    }
}
