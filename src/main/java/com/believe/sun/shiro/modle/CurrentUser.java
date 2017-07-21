package com.believe.sun.shiro.modle;

/**
 * Created by sungj on 17-7-21.
 */
public class CurrentUser {

    private Integer id;

    private String account;

    private String cellphone;

    private String email;

    private String nickname;

    private String identity;

    private String roles;

    private String headimage;

    private Integer status;

    private String babyid;

    private Integer sex;

    private Integer age;

    public CurrentUser() {
    }

    public CurrentUser(Integer id, String account, String cellphone, String email, String nickname, String identity, String roles, String headimage, Integer status, String babyid, Integer sex, Integer age) {
        this.id = id;
        this.account = account;
        this.cellphone = cellphone;
        this.email = email;
        this.nickname = nickname;
        this.identity = identity;
        this.roles = roles;
        this.headimage = headimage;
        this.status = status;
        this.babyid = babyid;
        this.sex = sex;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getHeadimage() {
        return headimage;
    }

    public void setHeadimage(String headimage) {
        this.headimage = headimage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getBabyid() {
        return babyid;
    }

    public void setBabyid(String babyid) {
        this.babyid = babyid;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
