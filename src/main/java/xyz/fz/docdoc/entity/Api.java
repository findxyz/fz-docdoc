package xyz.fz.docdoc.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_doc_api")
public class Api {

    public static final String STATUS_DEVELOP = "develop";

    public static final String STATUS_DEVELOP_DONE = "developDone";

    public static final String STATUS_TEST_DONE = "testDone";

    public static final String STATUS_DEPRECATED = "deprecated";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "projectId")
    private Long projectId;

    @Column(name = "name")
    private String name;

    @Column(name = "requestUrl")
    private String requestUrl;

    @Column(name = "regexUrl")
    private String regexUrl;

    @Column(name = "authType")
    private String authType;

    @Column(name = "contentType")
    private String contentType;

    @Column(name = "requestMethod")
    private String requestMethod;

    @Column(name = "dataType")
    private String dataType;

    @Column(name = "author")
    private String author;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "createTime")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "updateTime")
    private Date updateTime;

    @Column(name = "requestExample")
    private String requestExample;

    @Column(name = "responseExample")
    private String responseExample;

    @Column(name = "status")
    private String status;

    @Version
    @Column(name = "version", columnDefinition = "BIGINT DEFAULT 0")
    private Long version = 0L;

    @Column(name = "isActivity")
    private Integer isActivity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRegexUrl() {
        return regexUrl;
    }

    public void setRegexUrl(String regexUrl) {
        this.regexUrl = regexUrl;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRequestExample() {
        return requestExample;
    }

    public void setRequestExample(String requestExample) {
        this.requestExample = requestExample;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Integer getIsActivity() {
        return isActivity;
    }

    public void setIsActivity(Integer isActivity) {
        this.isActivity = isActivity;
    }
}
