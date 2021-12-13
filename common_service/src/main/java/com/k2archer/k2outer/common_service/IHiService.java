package com.k2archer.k2outer.common_service;

import com.k2archer.lib.k2router.api.K2IService;

public interface IHiService  extends K2IService {
    String ROUTE_PATH = "IHiService";
    public void hello();
}
