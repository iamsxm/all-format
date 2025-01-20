package com.github.iamsxm.allformat.util;

import com.github.iamsxm.allformat.sql.BasicFormatterImpl;
import com.github.iamsxm.allformat.sql.Formatter;

public class SqlFormat {

    private static Formatter format = new BasicFormatterImpl();

    public static String format(String sql) {
        try {
            return format.format(sql);
        } catch (Exception e) {
            return null;
        }
    }
}