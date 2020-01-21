package com.guangl.gateway.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

@Table(name = "rebuild_users")
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RebuildUsers implements Serializable {
    /**
     * 编号
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    /**
     * 用户名
     */
    @Column(name = "username")
    private String username;

    /**
     * 密码
     */
    @Column(name = "password")
    private String password;

    /**
     * 手机号码
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 身份证号码(大写)
     */
    @Column(name = "id_card_num")
    private String idCardNum;

    /**
     * 盐值
     */
    @Column(name = "salt")
    private String salt;

    /**
     * 是否有效;1.有效;2.无效
     */
    @Column(name = "is_enabled")
    private Short isEnabled;

    /**
     * 是否删除;1.未删除;2.已删除
     */
    @Column(name = "is_deleted")
    private Short isDeleted;

    /**
     * 创建时间
     */
    @Column(name = "created_tm")
    private Date createdTm;

    /**
     * 更新时间
     */
    @Column(name = "updated_tm")
    private Date updatedTm;

    private static final long serialVersionUID = 1L;

    /**
     * 获取编号
     *
     * @return user_id - 编号
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * 设置编号
     *
     * @param userId 编号
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * 获取用户名
     *
     * @return username - 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * 设置用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     *
     * @return password - 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * 设置密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取手机号码
     *
     * @return phone - 手机号码
     */
    public String getPhone() {
        return phone;
    }

    /**
     * 设置手机号码
     *
     * @param phone 手机号码
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取身份证号码(大写)
     *
     * @return id_card_num - 身份证号码(大写)
     */
    public String getIdCardNum() {
        return idCardNum;
    }

    /**
     * 设置身份证号码(大写)
     *
     * @param idCardNum 身份证号码(大写)
     */
    public void setIdCardNum(String idCardNum) {
        this.idCardNum = idCardNum;
    }

    /**
     * 获取盐值
     *
     * @return salt - 盐值
     */
    public String getSalt() {
        return salt;
    }

    /**
     * 设置盐值
     *
     * @param salt 盐值
     */
    public void setSalt(String salt) {
        this.salt = salt;
    }

    /**
     * 获取是否有效;1.有效;2.无效
     *
     * @return is_enabled - 是否有效;1.有效;2.无效
     */
    public Short getIsEnabled() {
        return isEnabled;
    }

    /**
     * 设置是否有效;1.有效;2.无效
     *
     * @param isEnabled 是否有效;1.有效;2.无效
     */
    public void setIsEnabled(Short isEnabled) {
        this.isEnabled = isEnabled;
    }

    /**
     * 获取是否删除;1.未删除;2.已删除
     *
     * @return is_deleted - 是否删除;1.未删除;2.已删除
     */
    public Short getIsDeleted() {
        return isDeleted;
    }

    /**
     * 设置是否删除;1.未删除;2.已删除
     *
     * @param isDeleted 是否删除;1.未删除;2.已删除
     */
    public void setIsDeleted(Short isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * 获取创建时间
     *
     * @return created_tm - 创建时间
     */
    public Date getCreatedTm() {
        return createdTm;
    }

    /**
     * 设置创建时间
     *
     * @param createdTm 创建时间
     */
    public void setCreatedTm(Date createdTm) {
        this.createdTm = createdTm;
    }

    /**
     * 获取更新时间
     *
     * @return updated_tm - 更新时间
     */
    public Date getUpdatedTm() {
        return updatedTm;
    }

    /**
     * 设置更新时间
     *
     * @param updatedTm 更新时间
     */
    public void setUpdatedTm(Date updatedTm) {
        this.updatedTm = updatedTm;
    }
}