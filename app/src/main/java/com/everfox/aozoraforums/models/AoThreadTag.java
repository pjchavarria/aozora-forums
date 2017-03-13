package com.everfox.aozoraforums.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by daniel.soto on 2/1/2017.
 */

@ParseClassName("ThreadTag")
public class AoThreadTag extends ParseObject {
    public static String NAME = "name";

    public static String ADMIN_VISIBLE = "adminVisible";
    public static String ORDER = "order";
    public static String DETAIL = "detail";
    public static String VISIBLE = "visible";
    public static String PRIVATE_TAG = "privateTag";
    public static String SUB_TYPE = "subType";
    public static String TYPE = "type";
}
