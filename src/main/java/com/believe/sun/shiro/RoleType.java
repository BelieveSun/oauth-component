package com.believe.sun.shiro;

/**
 * Created by sungj on 17-7-24.
 */
public enum RoleType {
    ROLE_ADMIN(1),
    ROLE_SERVICE(2),
    ROLE_USER(3);

    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


    RoleType(int id) {
        this.id = id;
    }
}
