package aredee.mesos.frameworks.accumulo.framework.api;

public class NotFoundException extends aredee.mesos.frameworks.accumulo.framework.api.ApiException {
	private int code;
	public NotFoundException (int code, String msg) {
		super(code, msg);
		this.code = code;
	}
}
