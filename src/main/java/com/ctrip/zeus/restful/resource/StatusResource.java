package com.ctrip.zeus.restful.resource;

import com.ctrip.zeus.auth.Authorize;
import com.ctrip.zeus.exceptions.ValidationException;
import com.ctrip.zeus.model.entity.*;
import com.ctrip.zeus.nginx.entity.TrafficStatus;
import com.ctrip.zeus.nginx.entity.TrafficStatusList;
import com.ctrip.zeus.restful.message.ResponseHandler;
import com.ctrip.zeus.service.model.GroupRepository;
import com.ctrip.zeus.service.model.SlbRepository;
import com.ctrip.zeus.service.nginx.NginxService;
import com.ctrip.zeus.service.status.GroupStatusService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * @author:xingchaowang
 * @date: 3/4/2015.
 */
@Component
@Path("/status")
public class StatusResource {

    @Resource
    private GroupStatusService groupStatusService;
    @Resource
    private SlbRepository slbRepository;
    @Resource
    private GroupRepository groupRepository;
    @Resource
    private NginxService nginxService;
    @Resource
    private ResponseHandler responseHandler;


    @GET
    @Path("/groups")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response allGroupStatusInSlb(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("slbId") Long slbId, @QueryParam("slbName") String slbName) throws Exception {
        Long _slbId = null; //slbRepository.get(slbName).getId();
        List<GroupStatus> statusList = null;
        if (slbId != null) {
            _slbId = slbId;
        } else if (slbName != null) {
            _slbId = slbRepository.get(slbName).getId();
        }
        if (null == _slbId) {
            statusList = groupStatusService.getAllGroupStatus();
        } else {
            statusList = groupStatusService.getAllGroupStatus(_slbId);
        }

        GroupStatusList result = new GroupStatusList();
        for (GroupStatus groupStatus : statusList) {
            result.addGroupStatus(groupStatus);
        }
        return responseHandler.handle(result, hh.getMediaType());
    }

    @GET
    @Path("/group")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @QueryParam("groupId") Long groupId, @QueryParam("groupName") String groupName, @QueryParam("slbId") Long slbId, @QueryParam("slbName") String slbName) throws Exception {
        Long _groupId = null;
        Long _slbId = null;
        GroupStatus statusResult = null;

        if (groupId != null) {
            _groupId = groupId;
        } else if (groupName != null) {
            _groupId = groupRepository.get(groupName).getId();
        }
        if (null == _groupId) {
            throw new Exception("Group Id or Name not found!");
        }
        if (slbId != null) {
            _slbId = slbId;
        } else if (slbName != null) {
            _slbId = slbRepository.get(slbName).getId();
        }
        if (null == _slbId) {
            List<GroupStatus> statusList = groupStatusService.getGroupStatus(_groupId);
            if (statusList != null && statusList.size() > 0)
            {
                statusResult = statusList.get(0);
            }else{
                throw new Exception("SlbId param is needed!");
            }
        } else {
            statusResult = groupStatusService.getGroupStatus(_groupId, _slbId);
        }
        
        return responseHandler.handle(statusResult, hh.getMediaType());
    }

    @GET
    @Path("/group/{groupId:[0-9]+}/slb/{slbId:[0-9]+}/server/{sip}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupServerStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("groupId") Long groupId, @PathParam("slbId") Long slbId, @PathParam("sip") String sip) throws Exception {
        String[] ipPort = sip.split(":");
        if (ipPort.length != 2) {
            throw new IllegalArgumentException("server should be ip:port format");
        }
        GroupServerStatus groupServerStatus = groupStatusService.getGroupServerStatus(groupId, slbId, ipPort[0], Integer.valueOf(ipPort[1]));

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupServerStatus.XML, groupServerStatus)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupServerStatus.JSON, groupServerStatus)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/groupName/{groupName:[a-zA-Z0-9_-]+}/slbName/{slbName:[a-zA-Z0-9_-]+}/server/{sip}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Authorize(name = "getGroupStatus", uriGroupHint = -1)
    public Response groupServerStatus(@Context HttpServletRequest request, @Context HttpHeaders hh, @PathParam("groupName") String groupName, @PathParam("slbName") String slbName, @PathParam("sip") String sip) throws Exception {
        Long groupId = groupRepository.get(groupName).getId();
        Long slbId = slbRepository.get(slbName).getId();
        String[] ipPort = sip.split(":");
        if (ipPort.length != 2) {
            throw new IllegalArgumentException("server should be ip:port format");
        }
        GroupServerStatus groupServerStatus = groupStatusService.getGroupServerStatus(groupId, slbId, ipPort[0], Integer.valueOf(ipPort[1]));

        if (MediaType.APPLICATION_XML_TYPE.equals(hh.getMediaType())) {
            return Response.status(200).entity(String.format(GroupServerStatus.XML, groupServerStatus)).type(MediaType.APPLICATION_XML).build();
        } else {
            return Response.status(200).entity(String.format(GroupServerStatus.JSON, groupServerStatus)).type(MediaType.APPLICATION_JSON).build();
        }
    }

    @GET
    @Path("/traffic")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getTrafficStatusBySlb(@Context HttpHeaders hh,
                                          @QueryParam("slbId") Long slbId) throws Exception {
        if (slbId == null) {
            throw new ValidationException("Missing parameters.");
        }
        TrafficStatusList list = new TrafficStatusList();
        for (TrafficStatus ts : nginxService.getTrafficStatusBySlb(slbId)) {
            list.addTrafficStatus(ts);
        }
        return responseHandler.handle(list, hh.getMediaType());
    }
}
