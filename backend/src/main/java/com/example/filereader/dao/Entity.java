package com.example.filereader.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略多余的属性
public class Entity {

    private String category;
    @JsonProperty("file_path")
    private String filePath;
    private String id;
    private String intrusiveModify;
    private String isIntrusive;
    @JsonProperty("not_aosp")
    private String notAosp;
    @JsonProperty("old_aosp")
    private String oldAosp;
    private String qualifiedName;
    private String refactor;


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIntrusiveModify() {
        return intrusiveModify;
    }

    public void setIntrusiveModify(String intrusiveModify) {
        this.intrusiveModify = intrusiveModify;
    }

    public String getIsIntrusive() {
        return isIntrusive;
    }

    public void setIsIntrusive(String isIntrusive) {
        this.isIntrusive = isIntrusive;
    }

    public String getNotAosp() {
        return notAosp;
    }

    public void setNotAosp(String notAosp) {
        this.notAosp = notAosp;
    }

    public String getOldAosp() {
        return oldAosp;
    }

    public void setOldAosp(String oldAosp) {
        this.oldAosp = oldAosp;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public String getRefactor() {
        return refactor;
    }

    public void setRefactor(String refactor) {
        this.refactor = refactor;
    }
}

