package info.bbd.user.weibos.spider.service.domain;

public class SinaUserInfo {

	private String uid="";
	private String introduce="";
	private String place="";
	private String birthday="";
	private String label="";
	
	public String getUid() {
		return uid;
	}
	public String getIntroduce() {
		return introduce;
	}
	public String getPlace() {
		return place;
	}
	public String getBirthday() {
		return birthday;
	}
	public String getLabel() {
		return label;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}
	public void setPlace(String place) {
		this.place = place;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	
	@Override
	public String toString() {
		return "SinaUserInfo [uid=" + uid + ", introduce=" + introduce + ", place=" + place + ", birthday=" + birthday
				+ ", label=" + label + "]";
	}
}
