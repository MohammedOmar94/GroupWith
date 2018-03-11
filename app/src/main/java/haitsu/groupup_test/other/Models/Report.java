package haitsu.groupup_test.other.Models;

/**
 * Created by moham on 24/10/2017.
 */

public class Report {

    private String groupID;
    private String reason;
    private String reportingMember;
    private String reportedMember;
    private String comments;

    public Report() {

    }

    public Report(String groupID, String reason, String reportedMember, String reportingMember, String comments) {
        this.groupID = groupID;
        this.reason = reason;
        this.reportingMember = reportingMember;
        this.reportedMember = reportedMember;
        this.comments = comments;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReportingMember() {
        return reportingMember;
    }

    public void setReportingMember(String reportingMember) {
        this.reportingMember = reportingMember;
    }

    public String getReportedMember() {
        return reportedMember;
    }

    public void setReportedMember(String reportedMember) {
        this.reportedMember = reportedMember;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
