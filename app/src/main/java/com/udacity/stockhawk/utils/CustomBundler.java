package com.udacity.stockhawk.utils;

import android.os.Bundle;

import java.util.HashMap;

import icepick.Bundler;

/**
 * Created by Gurupad Mamadapur on 12/27/2016.
 */
public class CustomBundler implements Bundler<HashMap> {
    @Override
    public void put(String s, HashMap hashMap, Bundle bundle) {
        bundle.putSerializable(s,hashMap);
    }

    @Override
    public HashMap get(String s, Bundle bundle) {
        return (HashMap) bundle.getSerializable(s);
    }
}
