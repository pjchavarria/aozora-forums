package com.everfox.aozoraforums.utils;

import java.util.Date;

/**
 * Created by daniel.soto on 1/13/2017.
 */

public class DateUtils {

    public static long getSecondsDiff(Date first, Date last) {
        return (last.getTime() - first.getTime())/1000;
    }
}
