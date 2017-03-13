package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by daniel.soto on 2/3/2017.
 */

@ParseClassName("Anime")
public class Anime extends ParseObject {

    public static final String TITLE = "title";
    public static final String TYPE = "type";
    public static final String EPISODES = "episodes";
    public static final String DURATION = "duration";
}
