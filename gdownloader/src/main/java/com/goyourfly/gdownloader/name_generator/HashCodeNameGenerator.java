package com.goyourfly.gdownloader.name_generator;

/**
 * Created by gaoyufei on 15/11/28.
 */
public class HashCodeNameGenerator implements NameGenerator {

    @Override
    public String getName(String url) {
        return String.valueOf(url.hashCode());
    }
}
