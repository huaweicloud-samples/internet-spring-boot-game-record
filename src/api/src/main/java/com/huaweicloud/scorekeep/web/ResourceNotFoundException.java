package com.huaweicloud.scorekeep.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 请求的路径参数无法对应到任何资源。 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource does not exist.")
public class ResourceNotFoundException extends RuntimeException {

    private final String resourceType;

    private final String resourceId;

    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(resourceType + " 不存在：" + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceId() {
        return resourceId;
    }
}
