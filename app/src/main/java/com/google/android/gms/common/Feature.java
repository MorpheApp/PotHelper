/*
 * SPDX-FileCopyrightText: 2020, microG Project Team
 * SPDX-License-Identifier: Apache-2.0
 */

package com.google.android.gms.common;

import org.microg.safeparcel.AutoSafeParcelable;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class Feature extends AutoSafeParcelable {
    @Field(1)
    private String name;
    @Field(2)
    private int oldVersion = 0;
    @Field(3)
    private long version = -1;
    @Field(4)
    private boolean fullyRolledOut;

    private Feature() {
    }

    public Feature(String name) {
        this(name, 1);
    }

    public Feature(String name, long version) {
        this(name, version, false);
    }

    public Feature(String name, long version, boolean fullyRolledOut) {
        this(name, -1, version, fullyRolledOut);
    }

    public Feature(String name, int oldVersion, long version, boolean fullyRolledOut) {
        this.name = name;
        this.oldVersion = oldVersion;
        this.version = version;
        this.fullyRolledOut = fullyRolledOut;
    }

    public String getName() {
        return name;
    }

    public long getVersion() {
        if (version == -1) return oldVersion;
        return version;
    }

    public static final Creator<Feature> CREATOR = new AutoCreator<>(Feature.class);
}
