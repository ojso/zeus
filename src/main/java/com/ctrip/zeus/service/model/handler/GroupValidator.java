package com.ctrip.zeus.service.model.handler;

import com.ctrip.zeus.model.entity.Group;
import com.ctrip.zeus.model.entity.GroupVirtualServer;

import java.util.List;

/**
 * Created by zhoumy on 2015/6/29.
 */
public interface GroupValidator extends ModelValidator<Group> {

    void validateGroupVirtualServers(Long groupId, List<GroupVirtualServer> groupVirtualServers) throws Exception;
}
