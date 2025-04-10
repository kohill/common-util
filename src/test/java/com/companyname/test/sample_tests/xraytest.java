package com.companyname.test.sample_tests;

import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import xray.XrayListener;
import xray.model.Xray;

@Listeners({ XrayListener.class})
public class xraytest {

    @Test
    @Xray(id = "OQ-3309")
    public void samplexray() {
        System.out.println("SampleTest");
    }

    @Test
    @Xray(id = "OQ-5176")
    public void samplexray1() {
        System.out.println("SampleTest");

    }
}
