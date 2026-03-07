package com.aumReport.aum.entity;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA entity class for "UserProfile"
 *
 * @author Telosys
 *
 */
@Entity
@Table(name="user_profile", schema="public" )
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;

    //--- PRIMARY KEY
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id", nullable=false)
    private int        id ;

    //--- OTHER DATA FIELDS
    @Column(name="created_timestamp", nullable=false)
    private LocalDateTime   createdTimestamp ;

    @Column(name="updated_timestamp", nullable=false)
    private LocalDateTime   updatedTimestamp ;

    @Column(name="created_by")
    private Integer         createdBy ;

    @Column(name="updated_by")
    private Integer         updatedBy ;

    @Column(name="name", nullable=false, length=2147483647)
    private String          name ;

    @Column(name="password", nullable=false, length=2147483647)
    private String          password ;

    @Column(name="mfa", nullable=false)
    private boolean         mfa ;

    @Column(name="password_change", nullable=false)
    private boolean         passwordChange ;

    @Column(name="active", nullable=false)
    private boolean         active ;

    @Column(name="email", nullable=false, length=2147483647)
    private String          email ;

    @Column(name="mobile", length=2147483647)
    private String          mobile ;

    @Column(name="secret", length=2147483647)
    private String          secret ;

    /**
     * Constructor
     */
    public UserProfile() {
        super();
    }

    public void setId( int id ) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public void setCreatedTimestamp( LocalDateTime createdTimestamp ) {
        this.createdTimestamp = createdTimestamp;
    }

    public LocalDateTime getCreatedTimestamp() {
        return this.createdTimestamp;
    }

    public void setUpdatedTimestamp( LocalDateTime updatedTimestamp ) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public LocalDateTime getUpdatedTimestamp() {
        return this.updatedTimestamp;
    }

    public void setCreatedBy( Integer createdBy ) {
        this.createdBy = createdBy;
    }

    public Integer getCreatedBy() {
        return this.createdBy;
    }

    public void setUpdatedBy( Integer updatedBy ) {
        this.updatedBy = updatedBy;
    }

    public Integer getUpdatedBy() {
        return this.updatedBy;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setPassword( String password ) {
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    public void setMfa( boolean mfa ) {
        this.mfa = mfa;
    }

    public boolean isMfa() {
        return this.mfa;
    }

    public void setPasswordChange( boolean passwordChange ) {
        this.passwordChange = passwordChange;
    }

    public boolean isPasswordChange() {
        return this.passwordChange;
    }

    public void setActive( boolean active ) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setEmail( String email ) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setMobile( String mobile ) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return this.mobile;
    }

    public void setSecret( String secret ) {
        this.secret = secret;
    }

    public String getSecret() {
        return this.secret;
    }


    public UserProfile clone(boolean includeKeys) {
        UserProfile copy = new UserProfile();
        if (includeKeys) {
            copy.id = this.id;
        }
        copy.createdTimestamp = this.createdTimestamp;
        copy.updatedTimestamp = this.updatedTimestamp;
        copy.createdBy = this.createdBy;
        copy.updatedBy = this.updatedBy;
        copy.name = this.name;
        copy.password = this.password;
        copy.mfa = this.mfa;
        copy.passwordChange = this.passwordChange;
        copy.active = this.active;
        copy.email = this.email;
        copy.mobile = this.mobile;
        copy.secret = this.secret;
        return copy;
    }

    @Override
    public String toString() {
        String separator = "|";
        StringBuilder sb = new StringBuilder();
        sb.append("UserProfile[");
        sb.append("id=").append(id);
        sb.append(separator).append("createdTimestamp=").append(createdTimestamp);
        sb.append(separator).append("updatedTimestamp=").append(updatedTimestamp);
        sb.append(separator).append("createdBy=").append(createdBy);
        sb.append(separator).append("updatedBy=").append(updatedBy);
        sb.append(separator).append("name=").append(name);
        sb.append(separator).append("password=").append(password);
        sb.append(separator).append("mfa=").append(mfa);
        sb.append(separator).append("passwordChange=").append(passwordChange);
        sb.append(separator).append("active=").append(active);
        sb.append(separator).append("email=").append(email);
        sb.append(separator).append("mobile=").append(mobile);
        sb.append(separator).append("secret=").append(secret);
        sb.append("]");
        return sb.toString();
    }
}
