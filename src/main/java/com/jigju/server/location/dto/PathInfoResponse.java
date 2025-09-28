package com.jigju.server.location.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class PathInfoResponse {
    private MsgBody msgBody;

    @Getter
    @Setter
    public static class MsgBody {
        private List<Item> itemList;
    }
    @Getter
    @Setter
    public static class Item {
        private int time;
        private int distance;
        private List<Path> pathList;
    }
    @Getter
    @Setter
    public static class Path {
        private String routeNm;
        private Integer routeId;
        private String fname;
        private int fid;
        private double fx;
        private double fy;
        private String tname;
        private int tid;
        private double tx;
        private double ty;
        private List<RailLink> railLinkList;
    }

    @Getter
    @Setter
    public static class RailLink {
        private int railLinkId;
    }
}
