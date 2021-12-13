package com.k2archer.k2outer.service1;

import android.util.Log;

import com.k2archer.k2outer.common_service.IHiService;
import com.k2archer.lib.k2router.api.K2Service;

@K2Service(IHiService.ROUTE_PATH)
public class HiServiceImpl implements IHiService {
    private String TAG = "HiServiceImpl";

    @Override
    public void hello() {
        Log.e(TAG, "hello(): hi");
    }
}
